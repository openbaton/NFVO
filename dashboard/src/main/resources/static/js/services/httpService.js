angular.module('app')
    .factory('http', function ($http, $q, $cookieStore) {

        var http = {};
        http.get = function (url) {
            return $http({
                url: url,
                method: 'GET',
                headers: {
                    'Authorization': 'Bearer ' + $cookieStore.get('token')
                }
            })
        };


        http.post = function (url, data) {
            console.log(data);
            $('#modalSend').modal('show');
            var headerAutorization = 'Bearer ' + $cookieStore.get('token');
            console.log(headerAutorization);
            return $http({
                url: url,
                method: 'POST',
                data: data,
                headers: {
                    'Authorization': headerAutorization,
                    'Accept': 'application/json',
                    'Content-type': 'application/json'
                }
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
            var headerAutorization = 'Bearer ' + $cookieStore.get('token');
            console.log(headerAutorization);
            return $http({
                url: url,
                method: 'PUT',
                data: data,
                headers: {
                    'Authorization': headerAutorization,
                    'Accept': 'application/json',
                    'Content-type': 'application/json'
                }
            });
        };

        http.delete = function (url) {
            $('#modalSend').modal('show');
            var headerAutorization = 'Bearer ' + $cookieStore.get('token');
            console.log(headerAutorization);
            return $http({
                url: url,
                method: 'DELETE',
                headers: {
                    'Authorization': headerAutorization
                }
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
    });
