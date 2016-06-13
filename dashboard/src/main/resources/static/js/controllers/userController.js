var app = angular.module('app');
app.controller('UserCtrl', function ($scope, serviceAPI, $routeParams, http, $cookieStore, AuthService, $window) {

    var url = $cookieStore.get('URL') + "/api/v1/users/";
    var urlprojects = $cookieStore.get('URL') + "/api/v1/projects/";

    $scope.alerts = [];
    $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
    };

    $scope.roles = [
        'GUEST',
        'ADMIN',
        'OB_ADMIN'
    ];

    loadTable();

    $scope.roleAdd = {
        "role": "GUEST",
        "project": "*"
    };

    $scope.loadCurrentUser = function(){
        http.get(url +'current')
            .success(function (response) {
                console.log(response);
                $scope.currentUser= response
            })
            .error(function (response, status) {
                showError(status, response);
            });
    };

    http.get(urlprojects)
        .success(function (response) {
            //console.log(response);
            $scope.projects = response;
            $scope.projects.push({name: '*'});
        })
        .error(function (response, status) {
            showError(response, status);
        });


    $scope.userObj = {
        "username": "guest",
        "password": "",
        "enabled": true,
        "roles": [
            {
                "role": "GUEST",
                "project": "*"
            }
        ]
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
                showOk('user: ' + ids.toString() + ' deleted.');
                loadTable();
            })
            .error(function (response, status) {
                showError(response, status);
            });

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


    $scope.deleteUser = function (data) {
        http.delete(url + data.id)
            .success(function (response) {
                showOk('User: ' + data.name + ' deleted.');
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
        //console.log($scope.userObj);
        http.post(url, $scope.userObj)
            .success(function (response) {
                showOk('Project: ' + $scope.userObj.name + ' saved.');
                loadTable();
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
                    //console.log(response);
                })
                .error(function (data, status) {
                    showError(data, status);
                });


        }

    }

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
        $scope.alerts.push({type: 'success', msg: msg});
        loadTable();
        $('.modal').modal('hide');
    }

});



