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

var app = angular.module('app').controller('prController', function ($scope, serviceAPI, $routeParams, $http, $cookieStore, AuthService, $window, $interval, http, $location) {
    function env() {
        http.get(url + '/env')
            .success(function (response) {
                //console.log(response);
                //$scope.env1 = response;
                defaultUrl = response['applicationConfig: [file:/etc/openbaton/openbaton.properties]']['nfvo.package-repository.ip'] + ":" +
                    response['applicationConfig: [file:/etc/openbaton/openbaton.properties]']['nfvo.package-repository.port'];
                $scope.defaultUrl = defaultUrl;
                newloadTable();
                NSDTable();
                getVersion();


            })
            .error(function (response, status) {
                showError(response, status);
            });
    }
    var url = $cookieStore.get('URL');
    var defaultUrl = "";
    $scope.defaultUrl = defaultUrl;
    $scope.alerts = [];
    $scope.otherVersion = [];
    $scope.NSDs = [];
    $scope.VNFPackages = [];
    env();
    $scope.defaultUrl = defaultUrl;

    function newloadTable() {
        if (angular.isUndefined($routeParams.name) && angular.isUndefined($routeParams.vendor)) {
            $http.get("http://" + defaultUrl + "/api/v1/vnf-packages/defaults")
                .success(function (response) {
                    $scope.VNFPackages = response;

                })
                .error(function (data, status) {
                    showError(data, status);
                });
        }
        else {
            http.getMark("http://" + defaultUrl + "/api/v1/vnf-packages/" + $routeParams.name + "/" + $routeParams.vendor)
                .success(function (response, status) {
                    $scope.otherVersion = response;
                    console.log($scope.otherVersion );

                })
                .error(function (data, status) {
                    showError(data, status);

                });
        }

    }

    function getVersion() {
        http.get(url + '/api/v1/main/version/')
            .success(function (response) {
               // console.log("version is " + response);
                $scope.NFVOversion = response
            })
            .error(function (response, status) {
                showError(response, status);
            });
    }
    function NSDTable() {
        $http.get("http://" + defaultUrl + "/api/v1/nsds")
            .success(function (response) {
                $scope.NSDs = response;
            })
            .error(function (data, status) {
                showError(data, status);
            });
    }
    function OtherVersionTable() {
        $http.get("http://" + defaultUrl + "/api/v1/vnf-packages/" + $routeParams.name + "/" + $routeParams.vendor)
            .success(function (response) {
                $scope.otherVersion = response;
            })
            .error(function (data, status) {
                showError(data, status);
            });

        $scope.name = $routeParams.name;
        $scope.vendor = $routeParams.vendor;
    }

    $scope.downloadpacakge = function (data) {
            if (data.type === "tar") {
                $scope.requestlink = {};
                $scope.requestlink['link'] = "http://" + defaultUrl + "/api/v1/vnf-packages/" + data.id + "/tar/";
                console.log($scope.requestlink);
                http.post(url + "/api/v1/vnf-packages/package-repository-download", JSON.stringify($scope.requestlink)).success(function (response) {
                    showOk("The package is being downloaded");
                })
                    .error(function (data, status) {
                        showError(data, status);

                    });
            }
    };
    $scope.downloadpacakgeNSD = function (data) {
        $scope.requestlink = {};
        $scope.requestlink['link'] = "http://" + defaultUrl + "/api/v1/nsds/" + data.id + "/json/";
        console.log($scope.requestlink);
        http.post(url + '/api/v1/ns-descriptors/package-repository-download' +  '', JSON.stringify($scope.requestlink)).success(function (response) {
            showOk("The NSD is being onboarded");
            })
                .error(function (data, status) {
                    showError(data, status);

                });
        }

        $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
    };

    function showOk(msg) {
        $scope.alerts.push({type: 'success', msg: msg});
        window.setTimeout(function() {
            for (i = 0; i < $scope.alerts.length; i++) {
                if ($scope.alerts[i].type == 'success') {
                    $scope.alerts.splice(i, 1);
                }
            }
        }, 5000);
        $('.modal').modal('hide');
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
                msg:  data.message + " Code: " + status
            });
        }

        $('.modal').modal('hide');
        if (status === 401) {
            console.log(status + ' Status unauthorized')
            AuthService.logout();
        }
    }

    $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
    };

});