angular.module('app')
    .factory('http', function ($http, $q, $cookieStore) {

        var customHeaders = {};
        if ($cookieStore.get('token') === '')
            customHeaders = {
                'Accept': 'application/json',
                'Content-type': 'application/json'
            };
        else {

            customHeaders = {
                'Accept': 'application/json',
                'Content-type': 'application/json',
                'Authorization': 'Bearer ' + $cookieStore.get('token')
            };
        }

        var http = {};
        http.get = function (url) {
            if (url.indexOf("/scripts/") > -1) {
                customHeaders['Accept'] = 'text/plain';
                customHeaders['Content-type'] = 'text/plain';

            } else {
                customHeaders['Accept'] = 'application/json';
                customHeaders['Content-type'] = 'application/json';

            }

            return $http({
                url: url,
                method: 'GET',
                headers: customHeaders
            })
        };


        http.post = function (url, data) {
            console.log(data);
            $('#modalSend').modal('show');
            return $http({
                url: url,
                method: 'POST',
                data: data,
                headers: customHeaders
            });

        };
        http.postXML = function (url, data) {
            $('#modalSend').modal('show');
            return $http({
                url: url,
                dataType: 'xml',
                method: 'POST',
                data: data,
                headers: {
                    "Content-Type": "application/xml"
                }

            });
        };
        http.put = function (url, data) {
            $('#modalSend').modal('show');
            if (url.indexOf("/scripts/") > -1) {
                customHeaders['Content-type'] = 'text/plain';
                customHeaders['Accept'] = 'text/plain';
            } else {
                customHeaders['Accept'] = 'application/json';
                customHeaders['Content-type'] = 'application/json';
            }

            return $http({
                url: url,
                method: 'PUT',
                data: data,
                headers: customHeaders
            });
        };

        http.delete = function (url) {
            $('#modalSend').modal('show');
            return $http({
                url: url,
                method: 'DELETE',
                headers: customHeaders
            });
        };

        http.syncGet = function (url) {
            var deferred = $q.defer();
            http.get(url).success(function (data, status) {
                deferred.resolve(data);
            });
            return deferred.promise;
        };

        return http;
    })
;
