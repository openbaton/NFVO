/**
 * Angular Service for managing the login of the user
 */

angular.module('app').factory('AuthService', function($http, Session, $location, http, $cookieStore, $window, $q) {
    var authService = {};

    var clientId = "openbatonOSClient";
    var clientPass = "secret";

    function loginPost(credentials, URL) {
        console.log(credentials);
        var basic = "Basic " + btoa(clientId + ":" + clientPass);
        return $http({
            method: 'POST',
            url: URL + '/oauth/token',
            headers: {
                "Authorization": basic,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            data: "username=" + credentials.username + "&password=" + credentials.password + "&grant_type=" + credentials.grant_type
        })
            .then(function (res) {
                console.log(res);
                Session.create(URL, res.data.access_token, credentials.username, true);
                $location.path("/");
//                    $window.location.href = '#/dashboard';

                $window.location.reload();
                return;
            });
    }

    authService.login = function(credentials, URL) {
        $http({
            method: 'POST',
            url: URL + '/oauth/token',
        }).success(
console.log("SUCCESS")
        ).error(
            console.log("ERROR")

        );
        return loginPost(credentials, URL);
    };


    authService.setServcies = function(servicesFromScope) {
//        console.log(servicesFromScope);

        services = servicesFromScope;
        return services;
    };
    authService.getServcies = function() {
        if (angular.isUndefined(services))
            authService.getListOfServices();
        return services;
    };

    authService.getListOfServices = function() {
        var url = $cookieStore.get('URL');
        var deferred = $q.defer();
        http.get(url + ':35357/v2.0/OS-KSADM/services').
            then(function(data, status) {
                var status = status;
//                    services = data;
                services = data.data;
//                    console.log(services);

                http.get(url + ':35357/v2.0/endpoints').
                    then(function(data, status) {
                        var status = status;
                        var endpoints = data.data;
//                                console.log(endpoints);

                        angular.forEach(services, function(value, key) {
                            services = value;
                            delete services[key];
//                                    console.log(services);
                            angular.forEach(services, function(service, index) {
                                angular.forEach(endpoints.endpoints, function(endpoint) {
                                    if (service.id === endpoint.service_id) {
                                        console.log(service.id === endpoint.service_id);
                                        service.endpoint = endpoint;
                                    }
                                });

                            });
                        });
                        deferred.resolve(services);
                        deferred.promise.then(function(data, status) {
//                                    console.log(data);
                            authService.setServcies(data);

                        });

//                                deferred.resolve(data);
                    });
            });

        return;

    };

    authService.isAuthenticated = function() {
        return !!Session.userName;
    };

    authService.logout = function() {
        Session.destroy();
//        console.log(Session);
        $window.location.reload();
    };

    authService.isAuthorized = function(authorizedRoles) {
        if (!angular.isArray(authorizedRoles)) {
            authorizedRoles = [authorizedRoles];
        }
        return (authService.isAuthenticated() &&
        authorizedRoles.indexOf(Session.userName) !== -1);
    };

    return authService;


    /**
     * Angular Service for managing the session and cookies of the user
     */

}).service('Session', function($cookieStore) {


    this.create = function(URL, token, userName, logged) {
        this.URL = URL;
        this.token = token;
        this.userName = userName;
        this.logged = logged;
        $cookieStore.put('logged', logged);
        $cookieStore.put('userName', userName);
        $cookieStore.put('token', token);
        $cookieStore.put('URL', URL);
//        console.log($cookieStore.get('token'));

    };
    this.destroy = function() {
        this.URL = null;
        this.token = null;
        this.userName = null;
        this.logged = false;
        $cookieStore.remove('logged');
        $cookieStore.remove('userName');
        $cookieStore.remove('token');
        $cookieStore.remove('URL');

    };
    return this;
});