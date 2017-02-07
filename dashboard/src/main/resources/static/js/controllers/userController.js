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
app.controller('UserCtrl', function ($scope, serviceAPI, $routeParams, http, $cookieStore, AuthService, $window, $interval) {

    var url = $cookieStore.get('URL') + "/api/v1/users/";
    var urlprojects = $cookieStore.get('URL') + "/api/v1/projects/";
    $scope.passwordSame = false;
    $scope.passwordStrong = false;
    $scope.emailValid = false;

    $scope.alerts = [];
    $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
    };

    $scope.roles = [
        'GUEST',
        'USER'
    ];

    loadTable();
    $scope.newpassword = "";

    http.get(urlprojects)
        .success(function (response) {
            //console.log(response);
            $scope.projects = response;
            //$scope.projects.push({name: '*'});
        })
        .error(function (response, status) {
            showError(response, status);
        });

    $scope.addRole = function () {
        console.log($scope.projects[0]);
        var newRole = {
            "role": "USER",
            "project": $scope.projects[0].name
        };
        $scope.userObj.roles.push(newRole);
    };
    $scope.addRoleUpdate = function () {
        var newRole = {
            "role": "USER",
            "project": $scope.projects[0].name
        };
        $scope.userUpdate.roles.push(newRole);
    };
    $scope.currentUser = {};

    loadCurrentUser = function () {
        http.get(url + 'current')
            .success(function (response) {
                //console.log(response);
                $scope.currentUser = response
            })
            .error(function (response, status) {
                showError(status, response);
            });
    };



    $scope.userObj = {
        "username": "",
        "password": "",
        "email": "",
        "enabled": true,
        "roles": [

        ]
    };
    $scope.adminRole = {
        "role": "ADMIN",
        "project": "*"
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
                showOk('User: ' + ids.toString() + ' deleted.');
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
            if (k === $scope.currentUser.id) {
                return;
            }
            $scope.selection.ids[k] = newValue.checkbox;
        });
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
    $scope.userUpdate = "";

    $scope.selection = {};
    $scope.selection.ids = {};

    /* -- multiple delete functions END -- */
    $scope.makeAdmin = false;

    $scope.deleteUser = function (data) {
        http.delete(url + data.id)
            .success(function (response) {
                showOk('User: ' + data.username + ' deleted.');
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
        //console.log("Saving");
        if ($scope.makeAdmin) {
            $scope.saveAsAdmin();
        } else {
            $scope.saveAsUser();
        }
        $scope.makeAdmin = false;
    };

    $scope.updateSave = function () {
        if ($scope.makeAdmin) {
            updateAsAdmin();
        } else {
            updateAsUser();
        }
        $scope.makeAdmin = false;
    };


    $scope.saveAsUser = function () {
        //console.log($scope.userObj);
        if ($scope.userObj.password !== $scope.newpassword) {
            alert("New passwords are not the same");
            return;
        }
        http.post(url, $scope.userObj)
            .success(function (response) {
                showOk('User: ' + $scope.userObj.username + ' saved.');
                loadTable();
            })
            .error(function (response, status) {
                showError(response, status);
            });
    };

    $scope.update = function (data) {
        $scope.userUpdate = JSON.parse(JSON.stringify(data));;
        //console.log(data);
    };

    //Save as admin function
    $scope.adminObj = {};
    $scope.saveAsAdmin = function () {
        //console.log("Adding admin user");
        if ($scope.userObj.password !== $scope.newpassword) {
            alert("New passwords are not the same");
            return;
        }
        $scope.adminObj.username = $scope.userObj.username;
        $scope.adminObj.password = $scope.userObj.password;
        $scope.adminObj.email = $scope.userObj.email;
        $scope.adminObj.enabled = $scope.userObj.enabled;
        $scope.adminObj.roles = [];
        $scope.adminObj.roles.push($scope.adminRole);

        http.post(url, $scope.adminObj)
            .success(function (response) {
                showOk('User: ' + $scope.adminObj.username + ' saved.');
                loadTable();
                $scope.adminObj = {};
            })
            .error(function (response, status) {
                showError(response, status);
            });

    };

    function loadTable() {
        //console.log($routeParams.userId);
        if (!angular.isUndefined($routeParams.userId))
            http.get(url + $routeParams.userId)
                .success(function (response, status) {
                    //console.log(response);
                    $scope.user = response;
                    $scope.userJSON = JSON.stringify(response, undefined, 4);

                }).error(function (data, status) {
                    showError(data, status);
                });
        else {
            http.get(url)
                .success(function (response) {
                    $scope.users = response;
                    for (i = 0; i < $scope.users.length; i++) {
                        if ($scope.users[i].username === 'admin') {
                            $scope.adminID = $scope.users[i].id;
                        }
                    }
                    loadCurrentUser();
                    //console.log($scope.currentUser);
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
        $scope.alerts.push({ type: 'success', msg: msg });
        loadTable();
        window.setTimeout(function () {
            for (i = 0; i < $scope.alerts.length; i++) {
                if ($scope.alerts[i].type == 'success') {
                    $scope.alerts.splice(i, 1);
                }
            }
        }, 5000);
        $('.modal').modal('hide');
    }

    $scope.$watch("userObj.password", function (newValue, oldValue) {
        if ($scope.userObj.password.length < 8 || !(/[a-z]/.test($scope.userObj.password)) || !(/[A-Z]/.test($scope.userObj.password)) || !(/[0-9]/.test($scope.userObj.password))) {
            $scope.passstyle = { 'background-color': 'pink' };
            $scope.passwordStrong = false;
        } else {
            $scope.passstyle = { 'background-color': 'white' };
            $scope.passwordStrong = true;
        }
    }, true);
    $scope.newUserPassword1 = '';
    $scope.newUserPassword2 = '';
    $scope.newPasswordStrong = false;
    $scope.newPasswordSame = false;
    $scope.newPasswordStyle = { 'background-color': 'pink' };
    $scope.newPasswordRepeat = { 'background-color': 'pink' };
    $scope.$watchGroup(["newUserPassword1", "newUserPassword2"], function (newValue, oldValue) {
        if ($scope.newUserPassword1.length < 8 || !(/[a-z]/.test($scope.newUserPassword1)) || !(/[A-Z]/.test($scope.newUserPassword1)) || !(/[0-9]/.test($scope.newUserPassword1))) {
            $scope.newPasswordStyle = { 'background-color': 'pink' };
            $scope.newPasswordStrong = false;
        } else {
            $scope.newPasswordStyle = { 'background-color': 'white' };
            $scope.newPasswordStrong = true;
        }

        if ($scope.newUserPassword1 !== $scope.newUserPassword2) {
            $scope.newPasswordRepeat = { 'background-color': 'pink' };
            $scope.newPasswordSame = false;
        } else {
            $scope.newPasswordRepeat = { 'background-color': 'white' };
            $scope.newPasswordSame = true;
        }
    }, true);
    var changingPasswordData = {};
    $scope.passChangeUsername = '';
    $scope.changeUserPassword = function (data) {
        $scope.newUserPassword1 = '';
        $scope.newUserPassword2 = '';
        //console.log(data.username);
        changingPasswordData = data;
        $scope.passChangeUsername = changingPasswordData.username;

    };

    $scope.postNewPassword = function () {
        var newPass = { "new_pwd": $scope.newUserPassword1 };
        http.put(url + "changepwd/" + changingPasswordData.username, newPass)
            .success(function (response) {
                showOk('User: ' + changingPasswordData.username + ' updated.');
                loadTable();
            })
            .error(function (response, status) {
                showError(response, status);
            });


    };




    $scope.$watch("userObj.email", function (newValue, oldValue) {
        var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

        if (re.test($scope.userObj.email)) {
            $scope.emailstyle = { 'background-color': 'white' };
            $scope.emailValid = true;
        } else {
            $scope.emailstyle = { 'background-color': 'pink' };
            $scope.emailValid = false;
        }
    }, true);


    $scope.$watchGroup(["userObj.password", "newpassword", "userObj.username"], function (newValue, oldValue) {
        if ($scope.userObj.password !== $scope.newpassword) {
            $scope.passwordSame = false;
            $scope.checkstyle = { 'background-color': 'pink' };
        } else {
            $scope.checkstyle = { 'background-color': 'white' };
            $scope.passwordSame = true;
        }
        if ($scope.userObj.username.length > 0) {
            $scope.namestyle = { 'background-color': 'white' };

        } else {
            $scope.namestyle = { 'background-color': 'pink' };
        }
    }, true);

    function updateAsUser() {
        //console.log($scope.userUpdate);
        /*if ($scope.userUpdate.password !== $scope.newpassword) {
          alert("New passwords are not the same");
          return;
        }*/
        updateObj = {};
        updateObj.username = $scope.userUpdate.username;
        //updateObj.password = $scope.userUpdate.password;
        updateObj.email = $scope.userUpdate.email;
        updateObj.enabled = $scope.userUpdate.enabled;
        updateObj.id = $scope.userUpdate.id;
        updateObj.roles = [];
        for (i = 0; i < $scope.userUpdate.roles.length; i++) {
            var newRole = {
                "id": $scope.userUpdate.roles[i].id,
                "role": $scope.userUpdate.roles[i].role,
                "project": $scope.userUpdate.roles[i].project
            };
            updateObj.roles.push(newRole);
        }
        //console.log("Copied");
        //console.log(updateObj);
        http.put(url + updateObj.username, updateObj)
            .success(function (response) {
                showOk('User: ' + $scope.userObj.username + ' updated.');
                loadTable();
            })
            .error(function (response, status) {
                showError(response, status);
            });
        delete updateObj;
        delete $scope.userUpdate;
    };
    function updateAsAdmin() {
        //console.log($scope.userUpdate);
        /*if ($scope.userUpdate.password !== $scope.newpassword) {
          alert("New passwords are not the same");
          return;
        }*/
        updateObj = {};
        updateObj.username = $scope.userUpdate.username;
        updateObj.id = $scope.userUpdate.id;
        //updateObj.password = $scope.userUpdate.password;
        updateObj.email = $scope.userUpdate.email;
        updateObj.enabled = $scope.userUpdate.enabled;
        updateObj.roles = [];
        updateObj.roles.push($scope.adminRole);
        //console.log("Copied");
        //console.log(updateObj);
        http.put(url + updateObj.username, updateObj)
            .success(function (response) {
                showOk('User: ' + $scope.userObj.username + ' updated.');
                loadTable();
            })
            .error(function (response, status) {
                showError(response, status);
            });
        delete updateObj;
        delete $scope.userUpdate;
    };
    $scope.update = function (data) {
        $scope.userUpdate = JSON.parse(JSON.stringify(data));;
        //console.log(data);
    };

});
