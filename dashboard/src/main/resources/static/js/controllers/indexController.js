var app = angular.module('app');

/**
 *
 * Manages the login page
 *
 */

app.controller('LoginController', function ($scope, AuthService, Session, $rootScope, $location, $cookieStore, $http) {
    $scope.currentUser = null;
    //$scope.URL = 'http://localhost:8080';
    $scope.URL = '';
    $scope.credential = {
        "username": '',
        "password": '',
        "grant_type": "password"
    };

    if (angular.isUndefined($cookieStore.get('logged'))) {
        $scope.logged = false;
        $rootScope.logged = false;
    }

    else if ($cookieStore.get('logged')) {
        $scope.logged = $cookieStore.get('logged');
        $rootScope.logged = $cookieStore.get('logged');
    }
    $location.replace();
    //console.log($scope.logged);
    $scope.loggedF = function () {
        return $scope.logged;
    };


    $scope.checkSecurity = function () {
        //console.log($scope.URL + "/api/v1/security");
        AuthService.removeSession();
        $http.get($scope.URL + "/api/v1/security")
            .success(function (data) {
                //console.log(data);
                if (data === "false") {
                    AuthService.loginGuest($scope.URL);
                }
            })
            .error(function (data, status) {
                if (status == 404) {
                    AuthService.loginGuest($scope.URL);
                }
                console.info(('status != 404'));
                console.error('Response error', status, data);
            })

    };

    /**
     * Calls the AuthService Service for retrieving the token access
     *
     * @param {type} credential
     * @returns {undefined}
     */
    $scope.login = function (credential) {
        AuthService.login(credential, $scope.URL);
        setTimeout(showLoginError, 2000);
    };


    $scope.register = function (newUser) {
        delete newUser.password2;
        //console.log(newUser);
        $http.post($scope.URL + '/register', newUser)
            .success(function (data, status) {
                $window.location.reload();
            })
            .error(function (status, data) {
            });
    };
    function showLoginError() {
        $scope.$apply(function () {
            $scope.loginError = angular.isUndefined($cookieStore.get('logged'));
            //console.log($scope.loginError);
        });
    }

});


app.controller('IndexCtrl', function ($scope, $compile, $routeParams, serviceAPI, $interval, $cookieStore, $location, AuthService, http, $rootScope, $window) {
    $('#side-menu').metisMenu();
    $scope.adminRole = "ADMIN";
    $scope.superProject = "*";
    var url = $cookieStore.get('URL') + "/api/v1";
    //$interval(loadNumbers, 2000);
    $scope.config = {};
    $scope.userLogged = {};
    function loadCurrentUser() {
        http.get(url +'/users/current')
            .success(function (response) {
                //console.log(response);
                $scope.userLogged = response
            })
            .error(function (response, status) {
                showError(status, response);
            });
    };
    loadCurrentUser();
    function getConfig() {

        http.get(url + '/configurations/')
            .success(function (data, status) {
                //console.log(data);
                $.each(data, function (i, conf) {
                    if (conf.name === "system") {
                        $scope.config = conf;
                    }
                })
            });
    }

    $scope.loadSettings = function () {
        getConfig();
        $("#modalSetting").modal('show');

    };

    $scope.logged = $cookieStore.get('logged');
    //console.log($scope.logged);
    $location.replace();


    $scope.numberNSR = 0;
    $scope.numberNSD = 0;
    $scope.numberVNF = 0;
    $scope.numberUnits = 0;

    function loadNumbers() {
        http.syncGet(url + '/ns-descriptors/').then(function (data) {
            $scope.numberNSD = data.length;
            var vnf = 0;
            $.each(data, function (i, nsd) {
                //console.log(nsd.vnfd.length);
                if (!angular.isUndefined(nsd.vnfd.length))
                    vnf = vnf + nsd.vnfd.length;
            });
            $scope.numberVNF = vnf;
        });
        http.syncGet(url + '/ns-records/').then(function (data) {
            $scope.numberNSR = data.length;
            var units = 0;
            $.each(data, function (i, nsr) {
                $.each(nsr.vnfr, function (i, vnfr) {
                    $.each(vnfr.vdu, function (i, vdu) {
                        if (!angular.isUndefined(vdu.vnfc_instance.length))
                            units = units + vdu.vnfc_instance.length;
                    });
                });
            });
            $scope.numberUnits = units;
        });

    }


    $scope.$watch('projectSelected', function (newValue, oldValue) {
        console.log(newValue);
        if (!angular.isUndefined(newValue) && !angular.isUndefined(oldValue)) {
            $cookieStore.put('project', newValue);
        }
        if (!angular.isUndefined(newValue) && angular.isUndefined(oldValue)) {
            $cookieStore.put('project', newValue);
            loadNumbers();
            getConfig();
            loadCurrentUser();
        }
    });


    console.log($rootScope.projects);
    console.log($rootScope.projectSelected);

    $scope.changeProject = function (project) {
        if (arguments.length === 0) {
            http.syncGet(url + '/projects/')
                .then(function (response) {
                    if (angular.isUndefined($cookieStore.get('project')) || $cookieStore.get('project').id == '') {
                        $rootScope.projectSelected = response[0];
                        $cookieStore.put('project', response[0])
                    } else {
                        $rootScope.projectSelected = $cookieStore.get('project');
                    }
                    $rootScope.projects = response;
                });
        }
        else {
            $rootScope.projectSelected = project;
            console.log(project);
            $cookieStore.put('project', project);
            $window.location.reload();
        }


    };


    $scope.saveSetting = function (config) {
        //console.log(config);
        $('.modal').modal('hide');
        $('#modalSend').modal('show');

        http.put(url + '/configurations/' + config.id, config)
            .success(function (response) {
                $('.modal').modal('hide');
                alert('Configurations Updated! ');

            })
            .error(function (response, status) {
                $('.modal').modal('hide');
                alert('ERROR: <strong>HTTP</strong> status:' + status + ' response <strong>response:</strong>' + response);
            });
    };

    /**
     * Checks if the user is logged
     * @returns {unresolved}
     */
    $scope.loggedF = function () {
        return $scope.logged;
    };

    if ($scope.logged)
    //console.log('Ok Logged');
        $location.replace();
    $scope.username = $cookieStore.get('userName');

    //console.log($scope.username);


    /**
     * Delete the session of the user
     * @returns {undefined}
     */
    $scope.logout = function () {
        AuthService.logout();
    };

    $scope.changePassword = function () {
        $scope.oldPassword = '';
        $scope.newPassword = '';
        $scope.newPassword1 = '';

        $('#modalChangePassword').modal('show');
    };

    $scope.postNew = function() {
      if ($scope.newPassword.localeCompare($scope.newPassword1) == 0) {
        $scope.passwordData = {};
        $scope.passwordData.old_pwd = $scope.oldPassword;
        $scope.passwordData.new_pwd = $scope.newPassword;
        http.put(url + '/users/changepwd', JSON.stringify($scope.passwordData))
        .success(function (response) {
          alert("The password has been successfully changed")
          AuthService.logout()})
        .error(function (data, status) {
            console.error('STATUS: ' + status + ' DATA: ' + JSON.stringify(data));
            alert('STATUS: ' + status + ' DATA: ' + JSON.stringify(data))
            ? "" : location.reload();
        });
    } else {
      alert("The new passwords are not the same");
    }

    };

    $scope.admin = function() {
      //console.log($scope.userLogged);

      if($scope.userLogged.roles[0].project === $scope.superProject && $scope.userLogged.roles[0].role === $scope.adminRole) {
        return true;
      } else {
        return false;
      }
    };



});
