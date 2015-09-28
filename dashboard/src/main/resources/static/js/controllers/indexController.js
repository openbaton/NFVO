var app = angular.module('app');
app.controller('IndexCtrl', function ($scope, $cookieStore, $location, AuthService) {
    $('#side-menu').metisMenu();

    $scope.logged = $cookieStore.get('logged');
    console.log($scope.logged);
    $location.replace();

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
        $window.location.reload();
    };

//    http.get('/api/rest/orchestrator/v2/services/').success(function(data) {
//        $scope.numberServices = data.length;
//
//    });

//    http.get('/api/rest/orchestrator/v2/nsrecords/').success(function(data) {
//        $scope.numberTopologies = data.length;
//
//        var numUinit = 0;
//        $.each(data, function(i, obj) {
//            $.each(obj.serviceContainers, function(ind, v) {
//                numUinit += v.relationElements.length;
//            });
//        });
//        $scope.numberUnits = numUinit;
//    });


//    http.get('/api/rest/admin/v2/templates/').success(function(data) {
//        $scope.numberTemplates = data.length;
//
//    });


});

/**
 *
 * Manages the login page
 *
 */

app.controller('LoginController', function ($scope, AuthService, Session, $rootScope, $location, $cookieStore, $http) {
    $scope.currentUser = null;
    $scope.URL = 'http://localhost:8080';
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
        console.log($scope.URL + "/oauth/token");
        $http.get($scope.URL + "/oauth/token")
            .success(function (data) {
                console.log(data);
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
        $scope.loginError = angular.isUndefined($cookieStore.get('logged'));
        console.log($scope.loginError)
    };


});
