angular.module('app').
        controller('ChainsCtrl', function($scope, http, topologiesAPI, $q, $routeParams) {

            loadTable();
            loadChain();
            $scope.chainForm = {
                "src": "192.168.41.186",
                "dst": "192.168.41.197",
                "srcPort": "1",
                "dstPort": "2",
                "protocol": "tcp",
                "qosLevel": ''
            };

            $scope.qosLevel = ['1', '2', '3', '4'];

            $scope.sendChain = function(chainForm) {
                var obj = {flows: [chainForm]};
                console.log(obj);
                http.post('/api/rest/crosslayer/v1/chains', obj)
                        .success(function(response) {
                            showOk('Chain created!');
                            loadTable();

                        })
                        .error(function(data, status) {
                            showError(data, status);
                        });
            };


            if (!angular.isUndefined($routeParams.chainid)) {
                $('#graphicInfrastructure').html('');
                topologiesAPI.getTopologyD3(1, $routeParams.chainid);
            }



            $scope.deleteAllChains = function() {

                http.delete('/api/rest/crosslayer/v1/chains')
                        .success(function(response) {
                            showOk('Deleted all Chains!');

                        })
                        .error(function(data, status) {
                            showError(data, status);
                        });
            };
            $scope.deleteChain = function(id) {

                http.delete('/api/rest/crosslayer/v1/chains/' + id)
                        .success(function(response) {
                            showOk('Deleted Chain with id: ' + id);
                            loadTable();

                        })
                        .error(function(data, status) {
                            showError(data, status);
                        });
            };

            $scope.updateChain;
            $scope.update = function(chain) {
                $scope.updateChain = chain;
                $('#modalUpdateC').modal('show');
            };

            $scope.updateC = function(updateChain) {
                console.log('put on: ' + '/api/rest/crosslayer/v1/chains/' + updateChain.id + '/' + updateChain.qosLevel);
                http.put('/api/rest/crosslayer/v1/chains/' + updateChain.id + '/' + updateChain.qosLevel)
                        .success(function(response) {
                            showOk('Updated Chain with id: ' + updateChain.id);
                            loadTable();
                        })
                        .error(function(data, status) {
                            showError(data, status);
                        });

                ;
            };

            function loadChain() {
                if (!angular.isUndefined($routeParams.chainid))
                    http.get('/api/rest/crosslayer/v1/chains')
                            .success(function(response) {
                                _.each(response, function(chains) {
                                    if ($routeParams.chainid == chains.id)
                                        $scope.chain = chains;
                                });
                            });
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
                http.get('/api/rest/crosslayer/v1/chains').success(function(response) {
                    $scope.chains = {};
                    $scope.chains = response;
                });
            }



        });

