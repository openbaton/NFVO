/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


angular.module('app')
    .factory('http', function ($http, $q, $cookieStore, $rootScope) {

        var customHeaders = {};
        var http = {};

        if ($cookieStore.get('token') === '' || angular.isUndefined($cookieStore.get('token')))
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


        http.get = function (url) {
            //console.log(customHeaders);

            if (url.indexOf("/scripts/") > -1 || url.indexOf("/version/") > -1) {
                customHeaders['Accept'] = 'text/plain';
                customHeaders['Content-type'] = 'text/plain';

            } else {
                customHeaders['Accept'] = 'application/json';
                customHeaders['Content-type'] = 'application/json';

            }

            customHeaders['project-id'] = $cookieStore.get('project').id;
            //console.log(customHeaders);
            //console.log($cookieStore.get('project'));

            return $http({
                url: url,
                method: 'GET',
                headers: customHeaders
            })
        };

        http.getRC = function (url) {
            //console.log(customHeaders);

        
            customHeaders['Accept'] = 'application/octet-stream';
            customHeaders['Content-type'] = 'application/octet-stream';


            customHeaders['project-id'] = $cookieStore.get('project').id;
            //console.log(customHeaders);
            //console.log($cookieStore.get('project'));

            return $http({
                url: url,
                method: 'GET',
                headers: customHeaders
            })
        };



        http.getPlain = function (url) {
            //console.log(customHeaders);


            customHeaders['Accept'] = 'application/json';
            customHeaders['Content-type'] = 'application/json';



            //customHeaders['project-id'] = $cookieStore.get('project').id;
            //console.log(customHeaders);
            //console.log($cookieStore.get('project'));

            return $http({
                url: url,
                method: 'GET',
                //headers: customHeaders
            })
        };



        http.post = function (url, data) {
            customHeaders['Accept'] = 'application/json';
            customHeaders['Content-type'] = 'application/json';
            customHeaders['project-id'] = $cookieStore.get('project').id;
            //console.log(data);
            $('#modalSend').modal('show');
            return $http({
                url: url,
                method: 'POST',
                data: data,
                headers: customHeaders
            });

        };
        http.postPlain = function (url, data) {
            customHeaders['project-id'] = $cookieStore.get('project').id;
            customHeaders['Accept'] = 'text/plain';
            customHeaders['Content-type'] = 'text/plain';
            //console.log(data);
            $('#modalSend').modal('show');
            return $http({
                url: url,
                method: 'POST',
                data: data,
                headers: customHeaders
            });

        };
        http.postLog = function (url) {
            customHeaders['project-id'] = $cookieStore.get('project').id;
            $('#modalSend').modal('show');
            //console.log(url);
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
            customHeaders['project-id'] = $cookieStore.get('project').id;
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
            customHeaders['project-id'] = $cookieStore.get('project').id;
            customHeaders['Content-type'] = 'application/json';
            customHeaders['Accept'] = 'application/json';
            //console.log(customHeaders);
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
            })
            .error(function (response, status) {
                deferred.resolve(status);
            });
           
            return deferred.promise;
        };


        return http;
    })
;
