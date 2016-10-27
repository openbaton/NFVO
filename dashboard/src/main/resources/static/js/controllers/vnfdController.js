/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

var app = angular.module('app').controller('VnfdCtrl', function ($scope, $compile, $cookieStore, $routeParams, http, $http, $window, AuthService, clipboard, $interval) {

    var baseUrl = $cookieStore.get('URL') + "/api/v1/";
    var url = baseUrl + '/vnf-descriptors/';
    var urlVim = baseUrl + '/datacenters';

    //$interval(loadTable, 2000);
    loadTable();

    $scope.alerts = [];
    $scope.vimInstances = [];
    http.get(urlVim)
        .success(function (response, status) {
            $scope.vimInstances = response;
            console.log(response);
        })
        .error(function (data, status) {
            showError(status, data);

        });


    $scope.copyToClipboard = function () {
        var ids = [];
        angular.forEach($scope.selection.ids, function (value, k) {
            if (value) {
                ids.push({ 'id': k });
            }
        });
        //console.log(ids);
        clipboard.copyText(JSON.stringify(ids));
    };


    if (!angular.isUndefined($routeParams.vduId)) {
        $scope.vduId = $routeParams.vduId;
        console.log($scope.vduId);
    }

    $scope.deleteMPfromVNFD = function (index) {
        $scope.vnfdCreate.monitoring_parameter.splice(index, 1);
    };
    $scope.addVDUtoVND = function () {
        $('#addEditVDU').modal('hide');
        if (!angular.isUndefined($scope.vduEditIndex)) {
            $scope.vnfdCreate.vdu.splice($scope.vduEditIndex, 1);
            delete $scope.vduEditIndex;
        }
        $scope.vnfdCreate.vdu.push(angular.copy($scope.vduCreate));
    };
    $scope.storeDepFlavour = function () {
        $('#modaladdDepFlavour').modal('hide');
        if (!angular.isUndefined($scope.dfEditIndex)) {
            $scope.vnfdCreate.deployment_flavour.splice($scope.dfEditIndex, 1);
            delete $scope.dfEditIndex;
        }
        $scope.vnfdCreate.deployment_flavour.push(angular.copy($scope.depFlavor));
    };

    $scope.selection = [];

    $scope.toggleSelection = function toggleSelection(image) {
        var idx = $scope.selection.indexOf(image);
        if (idx > -1) {
            $scope.selection.splice(idx, 1);
        }
        else {
            $scope.selection.push(image);
        }
        console.log($scope.selection);
        $scope.vduCreate.vm_image = $scope.selection;
    };

    $scope.sendVNFD = function () {
        $('#addEditVNDF').modal('hide');

        http.post(url, $scope.vnfdCreate)
            .success(function (response, status) {
                showOk('Virtual Network Function saved.');
                console.log(response);
                loadTable();
            })
            .error(function (data, status) {
                showError(status, data);

            });
        console.log($scope.vnfdCreate);

    };

    $scope.deleteMP = function (index) {
        $scope.vduCreate.monitoring_parameter.splice(index, 1);
    };

    $scope.saveValueMP = function (newValue) {
        console.log(newValue);
        $scope.vduCreate.monitoring_parameter.push(newValue);
    };
    $scope.saveValueMPfromVNFD = function (newValue) {
        console.log(newValue);
        $scope.vnfdCreate.monitoring_parameter.push(newValue);
    };
    $scope.editVDU = function (vnfd, index) {
        $scope.vduCreate = vnfd;
        $scope.vduEditIndex = index;
        $('#addEditVDU').modal('show');
    };

    $scope.saveValueCVDU = function (newValue) {
        console.log(newValue);
        $scope.depFlavor.costituent_vdu.push(newValue);
    };
    $scope.saveValueDFC = function (newValue) {
        console.log(newValue);
        $scope.depFlavor.df_constraint.push(newValue);
    };

    $scope.deleteVDU = function (index) {
        $scope.vnfdCreate.vdu.splice(index, 1);
    };
    $scope.editDF = function (df, index) {
        $scope.depFlavor = df;
        $scope.dfEditIndex = index;
        $('#modaladdDepFlavour').modal('show');
    };

    $scope.deleteDF = function (index) {
        $scope.vnfdCreate.deployment_flavour.splice(index, 1);
    };
    $scope.saveValueVMI = function (newValue) {
        console.log(newValue);
        $scope.vduCreate.vm_image.push(newValue);
    };

    $scope.addDepFlavour = function () {
        $http.get('descriptors/vnfd/deployment_flavour.json')
            .then(function (res) {
                console.log(res.data);
                $scope.depFlavor = angular.copy(res.data);
            });
        $('#modaladdDepFlavour').modal('show');
    };

    $scope.addVDU = function () {
        $http.get('descriptors/vnfd/vdu.json')
            .then(function (res) {
                console.log(res.data);
                $scope.vduCreate = angular.copy(res.data);
            });
        $('#addEditVDU').modal('show');
    };

    $scope.showTab = function (value) {
        return (value > 0);
    };

    $scope.addVNFD = function () {
        $http.get('descriptors/vnfd/vnfd.json')
            .then(function (res) {
                console.log(res.data);
                $scope.vnfdCreate = angular.copy(res.data);
            });
        $('#addEditVNDF').modal('show');
    };

    $scope.deleteVNFD = function (data) {
        http.delete(url + data.id)
            .success(function (response, status) {
                showOk('Virtual Network Function deleted.');
                loadTable();
            })
            .error(function (data, status) {
                showError(status, data);

            });
    };

    /* -- multiple delete functions Start -- */

    $scope.multipleDeleteReq = function () {
        var ids = [];
        angular.forEach($scope.selection.ids, function (value, k) {
            if (value) {
                ids.push(k);
            }
        });
        console.log(ids);
        http.post(url + 'multipledelete', ids)
            .success(function (response) {
                showOk('Virtual Network Function Descriptor with id: ' + ids.toString() + ' deleted.');
                loadTable();
            })
            .error(function (response, status) {
                showError(response, status);
            });

    };

    $scope.main = { checkbox: false };
    $scope.$watch('main', function (newValue, oldValue) {
        //console.log(newValue.checkbox);
        //console.log($scope.selection.ids);
        angular.forEach($scope.selection.ids, function (value, k) {
            $scope.selection.ids[k] = newValue.checkbox;
        });
        console.log($scope.selection.ids);
    }, true);

    $scope.$watch('selection', function (newValue, oldValue) {
        console.log(newValue);
        var keepGoing = true;
        angular.forEach($scope.selection.ids, function (value, k) {
            if (keepGoing) {
                if ($scope.selection.ids[k]) {
                    $scope.multipleDelete = false;
                    keepGoing = false;
                }
                else {
                    $scope.multipleDelete = true;
                }
            }

        });
        if (keepGoing)
            $scope.mainCheckbox = false;
    }, true);

    $scope.multipleDelete = true;

    $scope.selection = {};
    $scope.selection.ids = {};
    /* -- multiple delete functions END -- */

    function loadTable() {
        if (angular.isUndefined($routeParams.vnfdescriptorId))
            http.get(url)
                .success(function (response, status) {
                    $scope.vnfdescriptors = response;
                    //console.log(response);
                })
                .error(function (data, status) {
                    showError(status, data);

                });
        else {
            http.get(url + $routeParams.vnfdescriptorId)
                .success(function (response, status) {
                    $scope.vnfdinfo = response;
                    $scope.vnfdJson = JSON.stringify(response, undefined, 4);
                    //console.log($scope.vnfdJson);
                })
                .error(function (data, status) {
                    showError(status, data);

                });
            $scope.vnfdescriptorId = $routeParams.vnfdescriptorId;
        }
    }

    function showError(status, data) {
        $scope.alerts.push({
            type: 'danger',
            msg: 'ERROR: <strong>HTTP status</strong>: ' + status + ' response <strong>data</strong> : ' + JSON.stringify(data)
        });
        $('.modal').modal('hide');
        if (status === 401) {
            console.log(status + ' Status unauthorized')
            AuthService.logout();
            $window.location.reload();
        }
    }

    function showOk(msg) {
        $scope.alerts.push({ type: 'success', msg: msg });
        window.setTimeout(function () {
            for (i = 0; i < $scope.alerts.length; i++) {
                if ($scope.alerts[i].type == 'success') {
                    $scope.alerts.splice(i, 1);
                }
            }
        }, 5000);
        loadTable();
        $('.modal').modal('hide');
    }


});
