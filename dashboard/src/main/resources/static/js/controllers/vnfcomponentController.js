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

angular.module('app').
    controller('VnfcCtrl', function ($scope, $routeParams, http, $location, AuthService,$cookieStore, $interval) {

        var url = $cookieStore.get('URL')+"/api/v1/vnfcomponents/";


        $scope.alerts = [];

        //$interval(loadTable, 2000);
        loadTable();




        $scope.closeAlert = function (index) {
            $scope.alerts.splice(index, 1);
        };



        function loadTable() {
            if (!angular.isUndefined($routeParams.vimInstanceId))
                http.get(url + $routeParams.vimInstanceId)
                    .success(function (response, status) {
                        //console.log(response);
                        $scope.vnfcomponent = response;
                        $scope.vnfcomponentJSON = JSON.stringify(response, undefined, 4);

                    }).error(function (data, status) {
                        showError(status, data);
                    });
            else {
                http.get(url)
                    .success(function (response) {
                        $scope.vnfcomponents = response;
                    })
                    .error(function (data, status) {
                        showError(status, data);
                    });
            }
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
            $scope.alerts.push({type: 'success', msg: msg});

            $('.modal').modal('hide');
        }

    });
