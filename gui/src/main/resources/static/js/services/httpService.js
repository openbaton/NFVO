
angular.module('app')
        .factory('http', function($http, $q) {

            var http = {};
            http.get = function(url) {
                return $http.get(url);
            };


            http.post = function(url, data) {
                $('#modalSend').modal('show');
                return $http.post(url, data);
            };
            http.postXML = function(url, data) {
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
            http.put = function(url, data) {
                $('#modalSend').modal('show');
                return $http.put(url, data);
            };

            http.delete = function(url) {
                $('#modalSend').modal('show');
                return $http.delete(url);
            };

            http.syncGet = function(url) {
                var deferred = $q.defer();
                http.get(url).success(function(data, status) {
                    deferred.resolve(data);
                });
                return deferred.promise;
            };

            return http;
        });
