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

var app = angular.module('app').controller('keyPairsCtrl', function ($scope, serviceAPI, $routeParams, http, $cookieStore, AuthService, $window, $interval) {

    var url = $cookieStore.get('URL') + "/api/v1/keys/";
    var urlprojects = $cookieStore.get('URL') + "/api/v1/projects/";
    $scope.keypairs = "";
    $scope.alerts = [];

    loadTable();
    function loadTable() {

        //console.log($routeParams.userId);
        http.get(url)
            .success(function (response) {
                $scope.keypairs = response;
                //console.log($scope.users.length);

                //console.log($scope.keypairs);
            })
            .error(function (data, status) {
                showError(status, data);
            });


    }

    $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
    };

    $scope.importKey = function (keyName, pubKey) {
        //console.log($scope.projectObj);
        newKey = {name: "", publicKey: ""};
        newKey.name = keyName;
        newKey.publicKey = pubKey;
        console.log(newKey);
        http.post(url, newKey)
            .success(function (response) {
                showOk('Key pair: ' + keyName + ' saved.');
                setTimeout(loadTable(), 250);
                keyName = "";
                pubKey = "";
                //location.reload();
            })
            .error(function (response, status) {
                showError(status, response);
            });
    };
    $scope.delete = function (data) {
        http.delete(url + data.id)
            .success(function (response) {
                showOk('Key with name: ' + data.name + ' deleted.');
                setTimeout(loadTable(), 250);

            })
            .error(function (response, status) {
                showError(status, response);
            });
    };
    $scope.generateKey = function (generateKeyName) {
        //console.log($scope.projectObj);
        http.postPlain(url + 'generate', generateKeyName)
            .success(function (response) {
                showOk('Key: ' + generateKeyName + ' generated.');
                setTimeout(loadTable(), 250);
                //console.log(response);
                var key = document.createElement("a");
                key.download = generateKeyName + '.pem';
                key.href = 'data:application/x-pem-file,' + encodeURIComponent(response);
                document.body.appendChild(key);
                key.click()
                document.body.removeChild(key);
                delete key;
                //document.location = 'title: key.pem, data:application/x-pem-file,' +
                //  encodeURIComponent(response);
                //location.reload();
            })
            .error(function (response, status) {
                showError(status, response);
            });
    };

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
        loadTable();
        $('.modal').modal('hide');
    }
});
