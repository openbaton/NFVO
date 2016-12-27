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

angular.module('app').controller('vimInstanceCtrl', function ($scope, $routeParams, http, $location, AuthService, $cookieStore, $interval) {

    var url = $cookieStore.get('URL') + "/api/v1/datacenters/";
    var bareUrl = $cookieStore.get('URL');
    $scope.alerts = [];
    $scope.datacenter = {};
    $scope.file = '';
    $scope.showPass = false;
    $scope.newvim = { type: "openstack", securityGroups: [] };
    $scope.driversinstalled = [];
    $scope.installed = [];
    var formInput = true;
    var fileInput = false;
    loadVIM();
    loadInstalled();


    $scope.textTopologyJson = '';
    $scope.setFormInput = function () {
        formInput = true;
        fileInput = false;
        
    };
    $scope.setFileInput = function () {
        formInput = false;
        fileInput = true;    
    };

    $scope.changeText = function (text) {
        $scope.textTopologyJson = text;
    };

    $scope.edit = function (obj) {
        $scope.editObj = obj;
    };
    $scope.isArray = function (obj) {
        if (angular.isArray(obj) || angular.isObject(obj))
            return false;
        else
            return true;
    };
    $scope.updateObj = function () {
        if (!angular.isUndefined($routeParams.vimInstanceId))
            updateObject($routeParams.vimInstanceId);
        else
            updateObject($scope.editObj.id);
    };

    function updateObject(id) {
        console.log($scope.editObj);
        http.put(url + '/' + id, $scope.editObj)
            .success(function (response) {
                showOk('VIM Instance updated!');
                loadVIM();
            })
            .error(function (data, status) {
                console.error('STATUS: ' + status + ' DATA: ' + JSON.stringify(data));
                showError(status, JSON.stringify(data));
            });
    }

    $scope.editField = function () {
        this.editValue = true;
    };
    $scope.hoverIn = function () {
        this.hoverEdit = true;
    };

    $scope.hoverOut = function () {
        this.hoverEdit = false;
    };

    $scope.setFile = function (element) {
        $scope.$apply(function ($scope) {

            var f = element.files[0];
            if (f) {
                var r = new FileReader();
                r.onload = function (element) {
                    var contents = element.target.result;
                    $scope.file = contents;
                };
                r.readAsText(f);
            } else {
                alert("Failed to load file");
            }
        });
    };
    $scope.sendInfrastructure = function () {
        if (formInput) {
            console.log("Using formInput")
            //console.log($scope.newvim);
            http.post(url, $scope.newvim)
                .success(function (response) {
                    showOk('VIM Instance created.');
                    $scope.file = '';
                    loadVIM();
                })
                .error(function (data, status) {
                    if (status === 400)
                        showError(status, {message:"Something went wrong"});
                    else
                        showError(status, data);

                });
        } else if (fileInput) {
            if ($scope.file !== '' && !angular.isUndefined($scope.file)) {
                console.log("Using fileInput")
                //console.log($scope.file);
                http.post(url, $scope.file)
                    .success(function (response) {
                        showOk('Vim Instance created.');
                        loadVIM();
                    })
                    .error(function (data, status) {
                        if (status === 400)
                            showError(status, {message:"Bad request: your json is not well formatted"});
                        else
                            showError(status, data);

                    });
            } else if ($scope.textTopologyJson !== '') {
                console.log($scope.textTopologyJson);
                http.post(url, $scope.textTopologyJson)
                    .success(function (response) {
                        showOk('VIM Instance created.');
                        $scope.file = '';
                        loadVIM();
                    })
                    .error(function (data, status) {
                        if (status === 400)
                            showError(status, {message:"Bad request: your json is not well formatted"});

                    });
            }
        }
        else {
            showError('None of the inputs were correct');

        }
        $scope.textTopologyJson = '';
        $scope.file = '';
    };


    $scope.nameFilter = null;


    $scope.changeSelection = function (selection) {
        $scope.vimInstanceJson = {};
        $scope.vimInstanceJson = dataCenterJ[selection];
    };
    $scope.changeLocation = function (location) {
        $scope.locationRadio = location;
    };

    $scope.addSecurityGroup = function () {
        $scope.newvim.securityGroups.push("");
    };
    $scope.removeSecurityGroup = function (index) {
        $scope.newvim.securityGroups.splice(index, 1);
    };
    $scope.saveDataCenter = function (vimInstanceJson) {
        if ($scope.file !== '') {
            vimInstanceJson = $scope.file;

        }

        $('.modal').modal('hide');
        console.log(vimInstanceJson);
        http.post(url, vimInstanceJson)
            .success(function (response) {
                showOk('Data Center created!');
                console.log(response);
                $scope.selection = $scope.dataSelect[0];
                $scope.vimInstanceJson = {};
            })
            .error(function (data, status) {
                showError(status, data);
            });

    };

    $scope.setFile = function (element) {
        $scope.$apply(function ($scope) {

            var f = element.files[0];
            if (f) {
                var r = new FileReader();
                r.onload = function (element) {
                    var contents = element.target.result;
                    $scope.file = contents;
                };
                r.readAsText(f);
            } else {
                alert("Failed to load file");
            }
        });
    };

    $scope.refreshDc = function () {

        $('#refreshIco').addClass('fa-spin');
        http.get(url + $routeParams.vimInstanceId + '/refresh')
            .success(function (data) {
                $('#refreshIco').removeClass('fa-spin');
                $scope.datacenter = data;
                $scope.datacenterJSON = JSON.stringify(data, undefined, 4);
                $scope.upDatacenter = data;
            })
            .error(function (data, status) {
                showError(status, data);
            });
    };
    $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
    };


    $scope.deleteData = function (id) {
        http.delete(url + id)
            .success(function (response) {
                showOk('Vim Instance deleted with id ' + id + '.');
                loadVIM();

            })
            .error(function (data, status) {
                showError(status, data);
            });
    };

    function loadVIM() {
        if (!angular.isUndefined($routeParams.vimInstanceId))
            http.get(url + $routeParams.vimInstanceId)
                .success(function (response, status) {
                    console.log(response);
                    $scope.vimInstance = response;
                    $scope.vimInstanceJSON = JSON.stringify(response, undefined, 4);

                }).error(function (data, status) {
                    showError(status, data);
                });
        else {
            http.get(url)
                .success(function (response) {
                    $scope.vimInstances = response;
                })
                .error(function (data, status) {
                    showError(status, data);
                });
        }
    }


    function loadInstalled() {
        http.get(bareUrl + "/api/v1/plugins").success(function (response) {
            $scope.driversinstalled = response;
            response.map(function (pl) {
                l1 = pl.split(".");
                $scope.installed.push({ name: l1[2], type: l1[1] });

            })
            console.log($scope.installed);


        })
            .error(function (data, status) {
                showError(status, data);
            });

    }


    function showError(status, data) {
        if (status === 500) {
            $scope.alerts.push({
            type: 'danger',
            msg: 'An error occured and could not be handled properly, please, report to us and we will fix it as soon as possible'
        });
        } else {
        console.log('Status: ' + status + ' Data: ' + JSON.stringify(data));
        $scope.alerts.push({
            type: 'danger',
            msg:  data.message + " Code: " + status
        });
        }

        $('.modal').modal('hide');
        if (status === 401) {
            console.log(status + ' Status unauthorized')
            AuthService.logout();
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
        $('.modal').modal('hide');
    }

});
