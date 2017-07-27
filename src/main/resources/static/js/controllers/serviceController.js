/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var app = angular.module('app');
app.controller('ServiceCtrl', function ($scope, $interval, serviceAPI, $routeParams, http, $cookieStore, AuthService, $http) {

    var url = $cookieStore.get('URL') + "/api/v1/components/services/";

    $scope.alerts = [];
    $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
    };

    loadTable();

    $scope.serviceObj = {
        'name': '',
        'roles':[]
    };

    /* -- multiple delete functions Start -- */

    $scope.multipleDeleteReq = function () {
        var ids = [];
        angular.forEach($scope.selection.ids, function (value, k) {
            if (value) {
                ids.push(k);
            }
        });
        //console.log(ids);
        http.post(url + 'multipledelete', ids)
            .success(function (response) {
                showOk('Service: ' + ids.toString() + ' deleted.');
                loadTable();
            })
            .error(function (response, status) {
                showError(response, status);
            });
        $scope.multipleDelete = false;
        $scope.selection = {};
        $scope.selection.ids = {};

    };

    $scope.main = {checkbox: false};

    $scope.$watch('main', function (newValue, oldValue) {
        ////console.log(newValue.checkbox);
        ////console.log($scope.selection.ids);
        angular.forEach($scope.selection.ids, function (value, k) {
            $scope.selection.ids[k] = newValue.checkbox;
        });
        //console.log($scope.selection.ids);
    }, true);

    $scope.$watch('selection', function (newValue, oldValue) {
        //console.log(newValue);
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

    $scope.types = ['REST', 'RABBIT'];

    $scope.deleteService = function (data) {
        http.delete(url + data.id)
            .success(function (response) {
                showOk('Service: ' + data.name + ' deleted.');
                loadTable();
            })
            .error(function (response, status) {
                showError(response, status);
            });
    };

    $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
    };


    $scope.save = function () {
        console.log($scope.serviceObj);
        var customHeader = {};
        customHeader['Content-type'] = 'application/json';
        customHeader['Accept'] = 'application/octet-stream';
        http.post_with_header(url + "/create", $scope.serviceObj, customHeader)
            .success(function (response) {
                var rc = document.createElement("a");
                rc.download = $scope.serviceObj.name + '.txt';
                rc.href = 'data:application/x-shellscript,' + encodeURIComponent(response);
                document.body.appendChild(rc);
                rc.click();
                document.body.removeChild(rc);
                showOk('Service: ' + $scope.serviceObj.name + ' saved.');
                loadTable();
            })
            .error(function (response, status) {
                showError(response, status);
            });

        // var url2 = "" + url + "/create";
        // console.log(url2)
        //
        // $('#modalSend').modal('show');
        // $http({
        //     url: url2,
        //     method: 'POST',
        //     data: $scope.serviceObj,
        //     headers: {
        //         'Content-type': 'application/json',
        //         'Accept': "application/octet-stream"
        //     }
        // }).success(function (data, status, headers, config) {
        //     console.log(data);
        //     console.log(status);
        //     console.log(headers);
        //     console.log(config);
        //     var rc = document.createElement("a");
        //     rc.download = "openbaton" + '.rc';
        //     rc.href = 'data:application/x-shellscript,' + encodeURIComponent(response);
        //     document.body.appendChild(rc);
        //     rc.click();
        //     document.body.removeChild(rc);
        // })
    };

    function loadTable() {
        if (!angular.isUndefined($routeParams.serviceId))
            http.get(url + $routeParams.serviceId)
                .success(function (response, status) {
                    //console.log(response);
                    $scope.service = response;
                    $scope.serviceJSON = JSON.stringify(response, undefined, 4);

                })
                .error(function (data, status) {
                    showError(data, status);
                });
        else {
            http.get(url)
                .success(function (response) {
                    $scope.services = response;
                    //console.log(response);
                })
                .error(function (data, status) {
                    showError(data, status);
                });
        }

    }

    function showError(data, status) {
        if (status === 500) {
            $scope.alerts.push({
                type: 'danger',
                msg: 'An error occured and could not be handled properly, please, report to us and we will fix it as soon as possible'
            });
        } else {
            console.log('Status: ' + status + ' Data: ' + JSON.stringify(data));
            $scope.alerts.push({
                type: 'danger',
                msg: data.message + " Code: " + status
            });
        }

        $('.modal').modal('hide');

        if (status === 401) {
            console.log(status + ' Status unauthorized')
            AuthService.logout();
        }
    }

    function showOk(msg) {
        $scope.alerts.push({type: 'success', msg: msg});
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

    $scope.addRole = function () {
        console.log($scope.projects[0]);
        var newRole = {
            "role": "USER",
            "project": $scope.projects[0].name
        };
        $scope.serviceObj.roles.push(newRole);
    };
});
