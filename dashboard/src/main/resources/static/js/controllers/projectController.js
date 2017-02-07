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

var app = angular.module('app');
app.controller('ProjectCtrl', function ($scope, serviceAPI, $routeParams, http, $cookieStore, AuthService, $window, $interval, $rootScope) {

    var url = $cookieStore.get('URL') + "/api/v1/projects/";
    $scope.adminRole = "ADMIN";
    $scope.superProject = "*";
    $scope.alerts = [];
    $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
    };

    loadTable();

    $scope.projectObj = {
        'name': '',
        'description': ''
    };
    console.log("")
    /* -- multiple delete functions Start -- */
    $scope.admin = function () {
        //console.log($scope.userLogged);
        if (typeof $scope.userLogged != 'undefined') {
            if ($scope.userLogged.roles[0].project === $scope.superProject && $scope.userLogged.roles[0].role === $scope.adminRole) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    };

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
                showOk('Project: ' + ids.toString() + ' deleted.');
                loadTable();
            })
            .error(function (response, status) {
                showError(response, status);
            });
        $scope.multipleDelete = false;
        $scope.selection = {};
        $scope.selection.ids = {};

    };

    $scope.main = { checkbox: false };
    $scope.$watch('main', function (newValue, oldValue) {
        ////console.log(newValue.checkbox);
        ////console.log($scope.selection.ids);
        angular.forEach($scope.selection.ids, function (value, k) {
            if (k === $scope.defaultID) {
                return;
            }
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
    $scope.deleteEvent = function (data) {
        http.delete(url + data.id)
            .success(function (response) {
                showOk('Project: ' + data.name + ' is being deleted.');
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
        //console.log($scope.projectObj);
        http.post(url, $scope.projectObj)
            .success(function (response) {
                showOk('Project: ' + $scope.projectObj.name + ' saved.');
                loadTable();
                $scope.projectObj = {
                    'name': '',
                    'description': ''
                };
                //location.reload();
            })
            .error(function (response, status) {
                showError(response, status);
            });
    };
    $scope.defaultID = "";
    function loadTable() {
        http.get(url)
            .success(function (response) {
                $scope.projects = response;
                //console.log(response);
                for (i = 0; i < $scope.projects.length; i++) {
                    if ($scope.projects[i].name === 'default') {
                        $scope.defaultID = $scope.projects[i].id;
                    }
                }
                changeProject();
                //console.log($scope.defaultID);
            })
            .error(function (data, status) {
                showError(data, status);
            });
    }

    function changeProject() {
        if (arguments.length === 0) {
            http.syncGet(url)
                .then(function (response) {
                    if (response === 401) {
                        console.log(status + ' Status unauthorized')
                        AuthService.logout();
                    }
                    if (angular.isUndefined($cookieStore.get('project')) || $cookieStore.get('project').id == '') {
                        $rootScope.projectSelected = response[0];
                        $cookieStore.put('project', response[0])
                    } else {
                        $rootScope.projectSelected = $cookieStore.get('project');
                    }
                    $rootScope.projects = response;
                })
               
        }
        else {
            $rootScope.projectSelected = project;
            console.log(project);
            $cookieStore.put('project', project);
            $window.location.reload();
        }


    };

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
        //location.reload();
    }

    $scope.admin = function () {
        //console.log($cookieStore.get('userName'));
        if ($cookieStore.get('userName') === 'admin') {
            return true;
        } else {
            return false;
        }
    };

});
