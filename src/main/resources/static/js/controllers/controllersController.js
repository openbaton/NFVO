
angular.module('app').
        controller('ControllerCtrl', function($scope, http, serviceAPI, $routeParams) {

         
            //loadTable();
            //loadController();
            $scope.controller = {
                "ip": "192.168.2.102",
                "tcpPort": 345,
                "type": "epc",
                "switches": []
            };


            $scope.switch = {
                "location": {
                    "name": "Berlin",
                    "latitude": 40.388692,
                    "longitude": 32.172499
                },
                "ports": [
                ],
                "dpid": "dpid_4",
                "ip": "0.0.0.0",
                "tcpPort": 22
            };
            $scope.start = function(data) {
                http.put('/api/rest/admin/v2/controllers/start')
                        .success(function(response) {
                            showOk('Controller started!');

                        })
                        .error(function(data, status) {
                            showError(data, status);
                        });
            };
            $scope.delete = function(data) {
                http.delete('/api/rest/admin/v2/controllers/' + data.id)
                        .success(function(response) {
                            showOk('Controller deleted!');
                            loadController();
                        })
                        .error(function(data, status) {
                            showError(data, status);
                        });
            };

            $scope.addSwitch = function()
            {
                serviceAPI.setCoordinates($scope.switch.location.name, $scope.switch);
                if (angular.isUndefined($scope.controller.switches))
                    $scope.controller.switches = [];
                else
                    $scope.controller.switches.push(angular.copy($scope.switch));
                $scope.switch.ports = [];
                console.log($scope.controller);
            };
            $scope.addNewPort = function()
            {
                if (angular.isUndefined($scope.switch.ports))
                    $scope.switch.ports = [];
                else
                    $scope.switch.ports.push(angular.copy($scope.portAdd));
            };

            $scope.sendController = function()
            {
                console.log($scope.controller);
                http.post('/api/rest/admin/v2/controllers', $scope.controller)
                        .success(function(response) {
                            showOk('Controller created!');
                            $scope.controller.switches = [];
                            loadController();

                        })
                        .error(function(data, stat) {
                            showError(data, stat);
                        });

            };
            $scope.deleteSwitch = function(index)
            {
                $scope.controller.switches.splice(index, 1);
            };

            $scope.deletePort = function(index)
            {
                $scope.switch.ports.splice(index, 1);
            };

            function loadController() {
                if (!angular.isUndefined($routeParams.controllerid))
                    http.get('/api/rest/admin/v2/controllers/' + $routeParams.controllerid)
                            .success(function(response) {
                                $scope.controllerT = response;
                            });
                else
                {
                    http.get('/api/rest/admin/v2/controllers')
                            .success(function(response) {
                                $scope.controllers = response;
                            });

                }
            }

            $scope.alerts = [];

            $scope.closeAlert = function(index) {
                $scope.alerts.splice(index, 1);
            };

            function showError(data, status) {
                $scope.alerts.push({type: 'danger', msg: 'ERROR: <strong>HTTP status</strong>: ' + status + ' response <strong>data</strong> : ' + data});
                $('.modal').modal('hide');
            }

            function showOk(msg) {
                $scope.alerts.push({type: 'success', msg: msg});
                $('.modal').modal('hide');
            }

            function loadTable() {
                http.get('/api/rest/admin/v2/controllers').success(function(response) {
                    $scope.controllers = response;
                });
            }

        });

