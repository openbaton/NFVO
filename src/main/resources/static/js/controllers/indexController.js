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
    console.log($scope.logged);
    $scope.loggedF = function () {
        return $scope.logged;
    };


    $scope.checkSecurity = function () {
        console.log($scope.URL + "/api/v1/security");
        AuthService.removeSession();
        $http.get($scope.URL + "/api/v1/security")
            .success(function (data) {
                console.log(data);
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

    function showLoginError() {
        $scope.$apply(function () {
            $scope.loginError = angular.isUndefined($cookieStore.get('logged'));
            console.log($scope.loginError);
        });
    }

});

app.controller('IndexCtrl', function ($scope, $cookieStore, $location, AuthService, http) {
    $('#side-menu').metisMenu();

    var url = $cookieStore.get('URL') + "/api/v1";

    $scope.config = {};

    getConfig();

    function getConfig() {

        http.get(url + '/configurations/')
            .success(function (data, status) {
                console.log(data);
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
    console.log($scope.logged);
    $location.replace();


    $scope.numberNSR = 0;
    $scope.numberNSD = 0;
    $scope.numberVNF = 0;
    $scope.numberUnits = 0;
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


    $scope.saveSetting = function (config) {
        console.log(config);
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
        console.log('Ok Logged');
    $location.replace();
    $scope.username = $cookieStore.get('userName');

    console.log($scope.username);


    /**
     * Delete the session of the user
     * @returns {undefined}
     */
    $scope.logout = function () {
        AuthService.logout();
    };


});


