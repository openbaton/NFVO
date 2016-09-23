/**
 * Angular Service for managing the login of the user
 */

angular.module('app').factory('AuthService', function ($http, Session, $location, http, $cookieStore, $window, $q) {
    var authService = {};

    var clientId = "openbatonOSClient";
    var clientPass = "secret";

    authService.login = function (credentials, URL) {
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
                Session.create(URL, res.data.value, credentials.username, true);
                $location.path("/main");
                $window.location.reload();
                return;
            });

    };

    authService.loginGuest = function (URL) {
        Session.create(URL, '', 'guest', true);
        $location.path("/main");
        $window.location.reload();
        return;
    };


    authService.isAuthenticated = function () {
        return !!Session.userName;
    };

    authService.removeSession = function () {
        Session.destroy();
    };

    authService.logout = function () {
        Session.destroy();
        $window.location.reload();
    };

    authService.isAuthorized = function (authorizedRoles) {
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

}).service('Session', function ($cookieStore) {

    this.create = function (URL, token, userName, logged) {
        this.URL = URL;
        this.token = token;
        this.userName = userName;
        this.logged = logged;
        $cookieStore.put('logged', logged);
        $cookieStore.put('userName', userName);
        $cookieStore.put('token', token);
        $cookieStore.put('URL', URL);
        $cookieStore.put('project', {name: 'default', id: ''});


//        console.log($cookieStore.get('token'));

    };
    this.destroy = function () {
        this.URL = null;
        this.token = null;
        this.userName = null;
        this.logged = false;
        $cookieStore.remove('logged');
        $cookieStore.remove('userName');
        $cookieStore.remove('token');
        $cookieStore.remove('URL');
        $cookieStore.remove('project');
    };
    return this;
});
