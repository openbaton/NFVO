angular.module('app')
    .factory('http', function ($http, $q, $cookieStore) {

        var customHeaders = {};
        if ($cookieStore.get('token') === '' || angular.isUndefined($cookieStore.get('token')))
            customHeaders = {
                'Accept': 'application/json',
                'Content-type': 'application/json'
            };
        else {
            var project = $cookieStore.get('project');
            customHeaders = {
                'Accept': 'application/json',
                'Content-type': 'application/json',
                'Authorization': 'Bearer ' + $cookieStore.get('token'),
                'project-id': project.id
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

            console.log(customHeaders);
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
        http.postLog = function (url) {
            $('#modalSend').modal('show');

            console.log(url);
            return $.ajax({
                url: url,
                type: 'post',
                headers: customHeaders,
                dataType: 'json'
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
            console.log(customHeaders);
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
