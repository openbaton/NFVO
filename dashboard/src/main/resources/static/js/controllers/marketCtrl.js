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

var app = angular.module('app').controller('marketCtrl', function ($scope, serviceAPI, $routeParams, $http, $cookieStore, AuthService, $window, $interval, http) {

    var url = $cookieStore.get('URL');
    //var defaultUrl = "lore:8082"
    var defaultUrl = "marketplace.openbaton.org:8082";
    $scope.alerts = [];

    $scope.marketUrl = null;
    $scope.privatepackages = [];
    $scope.publicpackages = [];
    $scope.publicNSDs = [];
    $scope.csarVNFs = [];
    $scope.csarNSs = [];

    loadTablePublic();
    loadTablePublicNSD();

    getMarketURL();



    function loadCSARTableVNF() {
        $http.get("http://" + defaultUrl + "/api/v1/csar-vnf")
            .success(function (response) {
                $scope.csarVNFs = response;

                //console.log($scope.packages);
            })
            .error(function (data, status) {
                showError(data, status);
            });
    }

    function loadCSARTableNSD() {
        $http.get("http://" + defaultUrl + "/api/v1/csar-ns")
            .success(function (response) {
                $scope.publicNSDs = $scope.publicNSDs.concat(response);
                console.log($scope.publicNSDs);
            })
            .error(function (data, status) {
                showError(data, status);
            });
    }


    function getMarketURL() {
        $http.get(url + "/configprops")
            .success(function (response) {
                if (response.restVNFPackage.properties.privateip) {
                    $scope.marketUrl = response.restVNFPackage.properties.privateip;
                    loadTable();
                }
                else {
                    return;
                }


                //console.log($scope.marketUrl);
            })
            .error(function (data, status) {
                showError(data, status);
            });


    }


    function loadTable() {
        //console.log($routeParams.userId);
        $http.get("http://" + $scope.marketUrl + "/api/v1/vnf-packages")
            .success(function (response) {
                $scope.privatepackages = response;

                //console.log($scope.packages);
            })
            .error(function (data, status) {
                showError(data, status);
            });


    }

    function loadTablePublic() {
        //console.log($routeParams.userId);
        $http.get("http://" + defaultUrl + "/api/v1/vnf-packages")
            .success(function (response) {
                $scope.publicpackages = response;

                //console.log($scope.packages);
            })
            .error(function (data, status) {
                showError(data, status);
            });


    }

    function loadTablePublicNSD() {
        //console.log($routeParams.userId);
        $http.get("http://" + defaultUrl + "/api/v1/nsds")
            .success(function (response) {
                console.log(response);
                $scope.publicNSDs = response;
                loadCSARTableNSD();

            })
            .error(function (data, status) {
                showError(data, status);
            });


    }


    $scope.loadTable = function () {
        loadTable();
    };

    $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
    };

    $scope.downloadCSARVNF = function (data) {
        $scope.requestlink = {};
        $scope.requestlink['link'] = "http://" + defaultUrl + "/api/v1/csar-vnf/" + data.id + "/csar/";
        console.log($scope.requestlink);
        http.post(url + "/api/v1/csar-vnf/marketdownload", JSON.stringify($scope.requestlink)).success(function (response) {
            showOk("The package is being downloaded");
        })
            .error(function (data, status) {
                showError(data, status);

            });
    };

    $scope.download = function (data) {

        if (data.type === "csar") {
            $scope.requestlink = {};
            $scope.requestlink['link'] = "http://" + defaultUrl + "/api/v1/csar-vnf/" + data.id + "/csar/";
            console.log($scope.requestlink);
            http.post(url + "/api/v1/csar-vnf/marketdownload", JSON.stringify($scope.requestlink)).success(function (response) {
                showOk("The package is being downloaded");
            })
                .error(function (data, status) {
                    showError(data, status);

                });
        }
        else {
            if (data.type === "tar") {
                $scope.requestlink = {};
                $scope.requestlink['link'] = "http://" + defaultUrl + "/api/v1/vnf-packages/" + data.id + "/tar/";
                console.log($scope.requestlink);
                http.post(url + "/api/v1/vnf-packages/marketdownload", JSON.stringify($scope.requestlink)).success(function (response) {
                    showOk("The package is being downloaded");
                })
                    .error(function (data, status) {
                        showError(data, status);

                    });
            }
        }
    };

    $scope.downloadPrivate = function (data) {
        $scope.requestlink = {};
        $scope.requestlink['link'] = "http://" + $scope.marketUrl + "/api/v1/vnf-packages/" + data.id + "/tar/";
        console.log($scope.requestlink);
        http.post(url + "/api/v1/vnf-packages/marketdownload", JSON.stringify($scope.requestlink)).success(function (response) {
            showOk("The package is being downloaded");
        })
            .error(function (data, status) {
                showError(data, status);

            });
    };

    $scope.downloadNSD = function (data) {
        if (data.type === "csar") {
            $scope.requestlink = {};
            $scope.requestlink['link'] = "http://" + defaultUrl + "/api/v1/csar-ns/" + data.id + "/csar/";
            console.log($scope.requestlink);
            http.post(url + '/api/v1/csar-ns/marketdownload', JSON.stringify($scope.requestlink)).success(function (response) {
                showOk("The NSD is being onboarded");
            })
                .error(function (data, status) {
                    showError(data, status);

                });

        } else {
            $scope.requestlink = {};
            $scope.requestlink['link'] = "http://" + defaultUrl + "/api/v1/nsds/" + data.id + "/json/";
            console.log($scope.requestlink);
            http.post(url + '/api/v1/ns-descriptors/marketdownload', JSON.stringify($scope.requestlink)).success(function (response) {
                showOk("The NSD is being onboarded");
            })
                .error(function (data, status) {
                    showError(data, status);

                });
        }
    };


    function showError(data, status) {
        $scope.alerts.push({
            type: 'danger',
            msg: 'ERROR: <strong>HTTP status</strong>: ' + status + ' response <strong>data</strong> : ' + JSON.stringify(data)
        });
        $('.modal').modal('hide');
        if (status === 401) {
            //console.log(status + ' Status unauthorized')
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

    $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
    };

});
