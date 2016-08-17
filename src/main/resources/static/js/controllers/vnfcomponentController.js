angular.module('app').
    controller('VnfcCtrl', function ($scope, $routeParams, http, $location, AuthService,$cookieStore, $interval) {

        var url = $cookieStore.get('URL')+"/api/v1/vnfcomponents/";


        $scope.alerts = [];

        //$interval(loadTable, 2000);
        loadTable();




        $scope.closeAlert = function (index) {
            $scope.alerts.splice(index, 1);
        };



        function loadTable() {
            if (!angular.isUndefined($routeParams.vimInstanceId))
                http.get(url + $routeParams.vimInstanceId)
                    .success(function (response, status) {
                        //console.log(response);
                        $scope.vnfcomponent = response;
                        $scope.vnfcomponentJSON = JSON.stringify(response, undefined, 4);

                    }).error(function (data, status) {
                        showError(status, data);
                    });
            else {
                http.get(url)
                    .success(function (response) {
                        $scope.vnfcomponents = response;
                    })
                    .error(function (data, status) {
                        showError(status, data);
                    });
            }
        }


        function showError(status,data) {
            console.log('Status: ' + status + ' Data: ' +  JSON.stringify(data));
            $scope.alerts.push({
                type: 'danger',
                msg: 'ERROR: <strong>HTTP status</strong>: ' + status + ' response <strong>data</strong> : ' + JSON.stringify(data)
            });

            $('.modal').modal('hide');
            if (status === 401) {
                console.log(status + ' Status unauthorized')
                AuthService.logout();
            }
        }

        function showOk(msg) {
            $scope.alerts.push({type: 'success', msg: msg});

            $('.modal').modal('hide');
        }

    });
