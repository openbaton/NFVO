

angular.module('app').
        controller('DataCenterCtrl', function($scope, $routeParams, http, serviceAPI) {

            $scope.alerts = [];
            $scope.datacenter = {};
            $scope.file = '';

            loadDatacenter();

            $scope.dataSelect = ['select..', 'TEST', 'AMAZON', 'OPENSTACK'];
            $scope.city = 'Berlin';
            $scope.locations = serviceAPI.getLocations();
            $scope.selection = $scope.dataSelect[0];
            $scope.locationRadio = $scope.locations[0];
            $scope.locationChecked = {};
            var dataCenterJ = {};
            $scope.switch = [];
            dataCenterJ['select..'] = {};
            dataCenterJ['OPENSTACK'] = {
                "name": "datacenter-1",
                "location": {
//                    "name": "Munich",
//                    "latitude": 48.13513,
//                    "longitude": 11.58198
                },
                "type": "OPENSTACK",
                "switch": {
                    "ports": [
                        {
                            "capacity": 30000,
                            "portNumber": 1
                        }
                    ],
                    "dpid": "datacenter-1",
                    "ip": "192.168.45.111",
                    "tcpPort": 22
                },
                "configuration": {
                    "configurationName": "datacenter-1",
                    "parameters": [
                        {
                            "config_key": "AUTH_URL",
                            "config_value": "http://127.0.0.1:35357/v2.0"
                        },
                        {
                            "config_key": "DEFAULT_KEY",
                            "config_value": "stack"
                        },
                        {
                            "config_key": "NETWORK_SERVICE",
                            "config_value": "neutron"
                        },
                        {
                            "config_key": "VERSION",
                            "config_value": "havana"
                        },
                        {
                            "config_key": "USERNAME",
                            "config_value": "admin"
                        },
                        {
                            "config_key": "PASSWORD",
                            "config_value": "admin"
                        },
                        {
                            "config_key": "TENANT",
                            "config_value": "demo"
                        }
                    ]
                }
            };
            dataCenterJ['AMAZON'] = {
                "name": "",
                "location": "",
                "type": "AMAZON",
                "configuration": {
                    "configurationName": "datacenter-1",
                    "parameters": [
                        {
                            "config_key": "NETWORK_SERVICE",
                            "config_value": "neutron"
                        },
                        {
                            "config_key": "VERSION",
                            "config_value": "havana"
                        },
                        {
                            "config_key": "USERNAME",
                            "config_value": "admin"
                        },
                        {
                            "config_key": "PASSWORD",
                            "config_value": "admin"
                        },
                        {
                            "config_key": "TENANT",
                            "config_value": "demo"
                        }
                    ]
                }
            };


            dataCenterJ['TEST'] = {
                "name": "test_datacenter",
                "location": {
                    "name": "Berlin",
                    "latitude": 52.525804,
                    "longitude": 13.314282
                },
                "type": "TESTCLIENT",
                "switch": {
                    "ports": [
                        {
                            "capacity": 30000,
                            "portNumber": 1111
                        },
                        {
                            "capacity": 10000,
                            "portNumber": 2222
                        }
                    ],
                    "dpid": "dpid_1",
                    "ip": "0.0.0.0",
                    "tcpPort": 22
                },
                "configuration": {
                    "configurationName": "test_configuration",
                    "parameters": [
                        {
                            "config_key": "test_key",
                            "config_value": "test_value"
                        }
                    ]
                }
            };
            $scope.nameFilter = null;
            $scope.datacenters = [];
            loadTableDatacenter();


            $scope.changeSelection = function(selection) {
                $scope.datacenterJson = {};
                $scope.datacenterJson = dataCenterJ[selection];
            };
            $scope.changeLocation = function(location) {
                $scope.locationRadio = location;
            };


            $scope.saveDataCenter = function(datacenterJson) {
                if ($scope.file !== '')
                {
                    datacenterJson = $scope.file;

                }

                $('.modal').modal('hide');
                console.log(datacenterJson);
                http.post('/api/v1/datacenters/', datacenterJson)
                        .success(function(response) {
                            showOk('Data Center created!');
                            $scope.selection = $scope.dataSelect[0];
                            $scope.datacenterJson = {};
                        })
                        .error(function(data, status) {
                            showError(data, status);
                        });

            };

            $scope.setFile = function(element) {
                $scope.$apply(function($scope) {

                    var f = element.files[0];
                    if (f) {
                        var r = new FileReader();
                        r.onload = function(element) {
                            var contents = element.target.result;
                            $scope.file = contents;
                        };
                        r.readAsText(f);
                    } else {
                        alert("Failed to load file");
                    }
                });
            };

            $scope.refreshDc = function() {
                loadDatacenter();
                $('#refreshIco').addClass('fa-spin');
                http.get('/api/rest/admin/v2/datacenters/' + $routeParams.dataCenterId + '/refresh')
                        .success(function(data) {
                            $('#refreshIco').removeClass('fa-spin');
                            $scope.datacenter = data;
                            $scope.datacenterJSON = JSON.stringify(data, undefined, 4);
                            $scope.upDatacenter = data;
                        })
                        .error(function(data, status) {
                            showError(data, status);
                        });
            };
            $scope.closeAlert = function(index) {
                $scope.alerts.splice(index, 1);
            };
            $scope.addNewSub = function(subAdd) {
                var obj = angular.copy(subAdd);
                console.log(obj);
                $scope.upDatacenter.subnets.push(obj);
            };
            $scope.deleteSubnet = function(index) {
                console.log(index);
                $scope.upDatacenter.subnets.splice(index, 1);
                console.log($scope.upDatacenter.subnets);
            };
            $scope.deleteImage = function(key) {
                console.log(key);
                delete $scope.upDatacenter.serviceImageId[key];
            };
            $scope.createDataCenter = function() {


            };
            $scope.loadFormImage = function(data) {
                $scope.upDatacenter = data;
                $('#modalUpdateImage').modal('show');
            };
            $scope.loadFormSubnets = function(data) {
                $scope.upDatacenter = data;
                $('#modalUpdateSubnets').modal('show');
            };
            $scope.updateDC = function(datacenter) {
                $scope.upDatacenter.serviceContainers = $scope.upDatacenter.serviceImageId;
                var copyDatacanter = angular.copy(_.omit($scope.upDatacenter, 'serviceImageId'));
                copyDatacanter = serviceAPI.cleanDC(copyDatacanter);
                console.log(copyDatacanter);

                http.put('/api/rest/admin/v2/datacenters/' + datacenter.id, copyDatacanter)
                        .success(function(response) {
                            showOk('Datacenter updated.');
                        })
                        .error(function(data, status) {
                            showError(data, status);
                        });
            };
            $scope.sendNewImage = function(image) {
                var obj = angular.copy(image);
                $scope.upDatacenter.serviceImageId[obj.key] = obj.value;
            };
            $scope.emptyObj = function(obj) {
                return angular.equals({}, obj);
            };
            function cleanDatacenter(data) {
                var tmp = angular.copy(_.omit(data, 'version'));
                tmp.location = angular.copy(_.omit(tmp.location, 'id', 'version'));
                tmp.switch = angular.copy(_.omit(tmp.switch, 'id', 'version'));
                delete tmp.location.id;
                return tmp;
            }

            $scope.loadFormUpdate = function(data) {
                $scope.upDatacenter = cleanDatacenter(data);
                $('#modalUpdate').modal('show');
            };
            $scope.deleteData = function(id) {
                http.delete('/api/rest/admin/v2/datacenters/' + id)
                        .success(function(response) {
                            showOk('Data Center deleted with id ' + id + '.');
                            delete $scope.dataCenterSelected;
                        })
                        .error(function(data, status) {
                            showError(data, status);
                        });
            };

            function loadDatacenter() {
                if (!angular.isUndefined($routeParams.dataCenterId))
                    http.syncGet('/api/rest/admin/v2/datacenters/' + $routeParams.dataCenterId).then(function(data) {
                        $scope.datacenter = data;
                        $scope.datacenterJSON = JSON.stringify(data, undefined, 4);
                        $scope.upDatacenter = data;
                    });
            }

            function loadTableDatacenter() {
                http.get('/api/rest/admin/v2/datacenters').success(function(response) {

                    $scope.datacenters = response;
                });
            }

            function showError(data, status) {
                $scope.alerts.push({type: 'danger', msg: 'ERROR: <strong>HTTP status</strong>: ' + status + ' response <strong>data</strong> : ' + data});
                loadTableDatacenter();
                $('.modal').modal('hide');
            }
            function showOk(msg) {
                $scope.alerts.push({type: 'success', msg: msg});
                loadTableDatacenter();
                $('.modal').modal('hide');
            }

        });

