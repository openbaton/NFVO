
angular.module('app').
        controller('SwitchCtrl', function($scope, $routeParams, http, $interval, $q) {


            //loadTableSwitches();
            //loadTableSwitch();
            //loadTableFlow();
            $scope.alerts = [];
            var portUsed = [];
            //http.get('/api/rest/admin/v2/links').success(function(response) {
            //
            //    $scope.links = response;
            //});
            //http.get('/api/rest/admin/v2/locations').success(function(response) {
            //
            //    $scope.locations = response;
            //});
            //
            //
            //http.get('/api/rest/admin/v2/switches').success(function(response) {
            //    $scope.switchesTarget = response;
            //    http.get('/api/rest/admin/v2/vim-instances').success(function(response) {
            //        _.each(response, function(datacenter) {
            //            $scope.switchesTarget.push(datacenter.switch);
            //        });
            //    });
            //});


            $interval(loadTableFlow, 5000);
            function loadTableFlow() {
                if (!angular.isUndefined($routeParams.flowid)) {
                    http.syncGet('/api/rest/sdn/v1/switches/' + $routeParams.flowid + '/flows').then(function(response) {
//                        console.log(response);
                        var arrayFlows = [];
                        _.each(response, function(flow) {
                            if (flow.dpid === $routeParams.flowid) {
                                delete flow.bandwidth;
                                arrayFlows.push(flow);
                            }
                        });
                        if (arrayFlows.length !== 0)
                            $scope.flows = arrayFlows;

                    });
                }
            }

            $scope.flowForm = {
                "src": "10.10.10.3",
                "dst": "10.10.10.2",
                "srcPort": "121",
                "dstPort": "5001",
                "protocol": "tcp",
                "qosLevel": "2"
            };
            $scope.sendFlow = function(flow) {
                var flows = {};
                flows.flows = [flow];
                console.log(flows);
                http.post('/api/rest/crosslayer/v1/chains', flow)
                        .success(function(response) {
                            showOk('Flow created!');

                        })
                        .error(function(data, status) {
                            showError(data, status);
                        });
            };

            $scope.switchForm = {
//                "ports": [{
//                        "capacity": 10000,
//                        "portId": 0,
//                        "portNumber": 1
//                    }, {
//                        "capacity": 30000,
//                        "portId": 0,
//                        "portNumber": 2
//                    }],
                "dpid": "dpid_3",
                "ip": "0.0.0.0",
                "tcpPort": 22

            };

            $scope.deleteAllFlow = function() {
                http.delete('/api/rest/sdn/v1/flows')
                        .success(function(response) {
                            showOk('All Flows deleted!');

                        })
                        .error(function(data, status) {
                            showError(data, status);
                        });
            };

            $scope.delete = function(id) {
                http.delete('/api/rest/admin/v2/links/' + id)
                        .success(function(response) {
                            showOk('Deleted Link with id: ' + id);
                            loadTableSwitch();
                        })
                        .error(function(data, status) {
                            showError(data, status);
                        });
            };

            $scope.sendLink = function() {
                $scope.portS;

                var reqLinkS = {};
                reqLinkS.dpId = $scope.portS.dpid;
                reqLinkS.portNumber = $scope.portS.portNumber;

                var reqLinkT = {};
                reqLinkT.dpId = $scope.switchT.dpid;
                reqLinkT.portNumber = $scope.portT.portNumber;

                console.log('Addending the id:' + $scope.portT.id);
                portUsed.push($scope.portT.id);

                var LinkEndpointRequest = {};
                LinkEndpointRequest.source = reqLinkS;
                LinkEndpointRequest.target = reqLinkT;

                console.log(JSON.stringify(LinkEndpointRequest));

                http.post('/api/rest/admin/v2/links', LinkEndpointRequest)
                        .success(function(response) {
                            showOk('Link created!');
                            loadTableSwitch();

                        })
                        .error(function(data, status) {
                            showError(data, status);
                        });


            };
            $scope.sendSwitch = function(switchF) {
                var postSwitch = angular.copy(switchF);
                postSwitch.location = $scope.location;
                console.log(postSwitch);

            };
            $scope.changeSelection = function(location) {
                $scope.location = location;
            };
            $scope.changeSelectionTarget = function(selection) {
                delete $scope.portT;
                console.log(selection);
                $.each(selection.ports, function(i) {

                    if (!angular.isUndefined(selection.ports[i]))
                        if (_.contains(portUsed, selection.ports[i].id)) {

                            selection.ports.splice(i, 1);
                        }
                });
            };
            $scope.changeSelectionSource = function(selection) {
                delete $scope.portS;
                console.log(selection);

            };
            $scope.addLink = function(dpid, data) {
                $scope.portS = data;
                $scope.portS.dpid = dpid;
                console.log(data);

                $('#modalLink').modal('show');
            };

            function loadTableSwitches() {
                http.get('/api/rest/admin/v2/switches').success(function(response) {

                    $scope.switches = response;
                });
            }

            function loadTableSwitch() {
                delete $scope.switch;
                if (!angular.isUndefined($routeParams.switchid))
                    http.get('/api/rest/admin/v2/switches/' + $routeParams.switchid).success(function(switchSel) {

                        var links = http.get("/api/rest/admin/v2/links"),
                                switches = http.get("/api/rest/admin/v2/switches");
                        $q.all([links, switches]).then(function(result) {
                            var links = [];
                            var switches = [];
                            angular.forEach(result, function(response) {
                                _.each(response.data, function(data) {
                                    if (_.has(data, 'source')) {
                                        links.push(data);
                                    }
                                    else {
                                        switches.push(data);
                                    }
                                });
                            });

                            var tmp = {};
                            tmp.links = links;
                            tmp.switches = switches;

                            return tmp;
                        }).then(function(obj) {
                            _.each(switchSel.ports, function(port) {

                                _.each(obj.links, function(link) {
                                    if (!_.contains(portUsed, link.source.id))
                                        portUsed.push(link.source.id);
                                    if (!_.contains(portUsed, link.target.id))
                                        portUsed.push(link.target.id);
                                    if (link.source.id === port.id) {
                                        _.each(obj.switches, function(swit) {
                                            _.each(swit.ports, function(portT) {
                                                if (link.target.id === portT.id) {

                                                    if (angular.isUndefined(port.link)) {
                                                        port.link = {};
                                                    }

                                                    portT.switch_dpid = swit.dpid;
                                                    portT.switch_id = swit.id;
                                                    portT.link = link;

                                                    port.link = portT;

                                                }
                                            });

                                        });
                                    }
                                    if (link.target.id === port.id) {
                                        _.each(obj.switches, function(swit) {
                                            _.each(swit.ports, function(portS) {
                                                if (link.source.id === portS.id) {

                                                    if (angular.isUndefined(port.link)) {
                                                        port.link = {};
                                                    }

                                                    portS.switch_dpid = swit.dpid;
                                                    portS.switch_id = swit.id;
                                                    portS.link = link;

                                                    port.link = portS;



                                                }
                                            });

                                        });
                                    }

                                }
                                );
                            });
                            console.log(switchSel);
                            $scope.switch = switchSel;
                            $scope.switchJSON = JSON.stringify(switchSel, undefined, 4);
                            ;

                        });

                    });
            }

            $scope.closeAlert = function(index) {
                $scope.alerts.splice(index, 1);
            }
            ;
            function showError(data, status) {
                $scope.alerts.push({type: 'danger', msg: 'ERROR: <strong>HTTP status</strong>: ' + status + ' response <strong>data</strong> : ' + data});
                $('.modal').modal('hide');
            }

            function showOk(msg) {
                $scope.alerts.push({type: 'success', msg: msg});
                $('.modal').modal('hide');
            }


        });

