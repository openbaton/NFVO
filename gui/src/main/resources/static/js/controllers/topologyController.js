

var app = angular.module('app').controller('TopologyCtrl', function($scope, $compile, $cookieStore, $routeParams, http, serviceAPI, $window, $route, $interval, $http, topologiesAPI) {

    var urlT = '/api/v1/ns-descriptors';

//    $('li').removeClass('active');
//    if ($window.location.hash === '#/templates')
//    {
//        $('#menutemplates').addClass('active');
//        loadTableTemplate();
//
//
//    }
//
//    else
//        $('#menutopologies').addClass('active');


//    loadinfoTopology();
//    loadTableService();
//    loadTableTopology();
//    loadSelect();
//    loadUnits();


    $scope.services = [];
    $scope.serviceEdit = {};
    $scope.locations = serviceAPI.getLocations();
    $scope.locationsS = serviceAPI.getLocations();

    $scope.servicesToPost = [];
    $scope.policies = [];


    $scope.servicesTable = [];
    $scope.textTopologyJson = '';
    $scope.file = '';
    $scope.showContainer = false;
    $scope.lockAddContainer = false;
    $scope.showInstance = false;
    $scope.selectedTopology = {};
    $scope.toggle = false;
    $scope.alerts = [];
    $scope.mgmt = '';

    $scope.serviceContainers = [];
    $scope.netCheck = false;

    $scope.tabs = [
        {active: true},
        {active: false},
        {active: false}
    ];


    inizializeVariables();

    //Reload the topology's table
//    $interval(loadTableTopology, 10000);

    //Reload the Service Containers's table
//    $interval(loadinfoTopology, 20000);

    //Reload the Service Instance's table
//    $interval(loadTableService, 20000);



    $scope.toggleF = function() {
        $scope.toggle = !$scope.toggle;
    };


    loadItems();
    function loadItems() {

        if (angular.isUndefined($cookieStore.get('items')))
            http.get('/api/rest/monitoring/v2/items/')
                    .success(function(response) {
                        console.log(response);
                        if (response.length > 0)
                        {
                            var newArr = _.map(response, function(o) {
                                return _.pick(o, 'itemKey', 'name');
                            });
                            newArr = _.uniq(newArr, function(item, key, a) {
                                return item.itemKey;
                            });

                            console.log(newArr);
                            $cookieStore.put('items', newArr);
                            $scope.items = $cookieStore.get('items');
                        }


                    }).error(function(data, status) {
//                showError(status, 'Problem with the Alarm Items list');

            });
        else
        {
            $scope.items = $cookieStore.get('items');

        }

    }

    $scope.serviceContainer = {
        "flavour": "m1.small",
        "minNumInst": 1,
        "maxNumInst": 3,
        "serviceType": "container",
        "containerName": "container1"
    };

    $scope.signs = [
        "<", "=", ">"
    ];

    $scope.values = [
        "10", "20", "30", "40", "50", "60", "70", "80", "90"
    ];
    $scope.types = [
        "ADD", "REMOVE"
    ];


    $scope.takenIP = {};
    $scope.updateFloatingIPs = function(dc, value) {
        if (angular.isUndefined($scope.takenIP[dc.name]))
            $scope.takenIP[dc.name] = [];
        $scope.takenIP[dc.name] = [];

        angular.forEach($scope.expose[dc.name], function(value, key) {
            if (value.expose)
                $scope.takenIP[dc.name].push(value.floatingIP);

            console.log($scope.floatingIPsNET);

        });


        $scope.selectExpose(dc, true, value);
    };

    $scope.floatingIPsNET = [];
    $scope.noMoreFloatingIp = false;


    $scope.selectMgmt = function(newValue) {
        $scope.mgmt = newValue;
        console.log(newValue);
    };

    $scope.$watchCollection(
            "floatingIPsNET",
            function(newValue, oldValue) {
//                console.log(newValue);
                if (newValue.length === 1 && newValue[0] === 'random')
                    $scope.noMoreFloatingIp = true;
                else
                    $scope.noMoreFloatingIp = false;

            }
    );

    $scope.selectExpose = function(dc, valueExpose, valueIP) {
        if (valueExpose)
        {

            if (angular.isUndefined($scope.floatingIPs))
            {
                http.syncGet('/api/rest/admin/v2/floatingips/' + dc.id).then(function(response) {
                    $scope.floatingIPs = response;
                    $scope.floatingIPsNET = response;
                    $scope.floatingIPsNET.unshift('random');
                    console.log(response);

                });
            }
            else
            {
                $scope.floatingIPsNET = _.difference($scope.floatingIPs, $scope.takenIP[dc.name]);
                if (!_.contains($scope.floatingIPsNET, 'random'))
                    $scope.floatingIPsNET.unshift('random');

            }

        }

        if (!valueExpose)
        {
            $scope.updateFloatingIPs(dc, valueIP);

        }

    };
    $scope.selectSubnets = function(val) {
        var name = serviceAPI.returnKeys(val, true);
        var nameFalse = serviceAPI.returnKeys(val, false);

        if (!angular.isUndefined($scope.expose[nameFalse]) && val[nameFalse] === false)
            $scope.expose[nameFalse][val.name].expose = false;

        if (angular.isUndefined($scope.sub[name]) && name != "") {
            $scope.sub[name] = {'subnets': []};
            if (!_.contains($scope.sub[name].subnets, val.name))
                $scope.sub[name].subnets.push(val.name);

        }
        else if (angular.isUndefined($scope.sub[name])) {
            name = serviceAPI.returnKeys(val, false);
            console.log(name);
            console.log(_.contains($scope.sub[name].subnets, val.name));

            if (_.contains($scope.sub[name].subnets, val.name))
                $scope.sub[name].subnets = _.difference($scope.sub[name].subnets, val.name);
        }

        else if (!angular.isUndefined($scope.sub[name]) && !_.contains($scope.sub[name].subnets, val.name))
            $scope.sub[name].subnets.push(val.name);

        var names = serviceAPI.returnKeys($scope.locationCheckedS, true);
        var flagSub = 0;

        var arrayKV = [];

        arrayKV = _.pairs($scope.sub);
        _.each(arrayKV, function(sub) {

            if (sub[1].subnets.length > 0)
                flagSub++;

        });

        if ($scope.datacenters.length === flagSub)
            $scope.netCheck = true;
        else
            $scope.netCheck = false;


    };

    function inizializeVariables() {
        $scope.topology = {};
        $scope.topology.serviceContainers = [];
        $scope.locationChecked = {};
        $scope.locationCheckedS = {};
        $scope.sub = {};
        $scope.policy = {};
        $scope.item = {};
        $scope.requireChecked = {};
        $scope.requires = [];
        $scope.selectedOneLocation = true;
        $scope.subnets = {};
        $scope.showServices = {};
        $scope.unitsInfo = [];
        $scope.serviceContainers = [];
    }

    function loadUnits() {

        if (!angular.isUndefined($routeParams.topologyid) && !angular.isUndefined($routeParams.containerId)) {
            $scope.tablesUnits = {};
            $scope.instanceId = $routeParams.instanceId;
            $scope.containerId = $routeParams.containerId;

            http.syncGet(urlT + $routeParams.topologyid + '/containers/' + $scope.containerId + '/units').then(function(units) {
                $scope.tablesUnits = units;
                console.log(units);

            });
        }
    }


    $scope.addContainer = function(serviceContainer) {
        $('#addContainerModal').modal('show');
        //        console.log($scope.subnets);
        var serviceC = angular.copy(serviceContainer);
        serviceC.images = $scope.imageId;
        console.log($scope.sub);
        var subnets = $scope.sub;

        _.each(subnets, function(values, key) {
            values.subnets.sort();
            var subnets2 = [];
            _.each(values.subnets, function(value, index) {
                var subnet = {};
                subnet.name = value;
                console.log(value.toString());
                console.log($scope.mgmt);

                console.log(_.isEqual($scope.mgmt.toString(), value.toString()));

                if (value === $scope.mgmt)
                {
                    subnet.mgmt = true;
                    console.log(value);
                }
                if (!angular.isUndefined($scope.fixedip[key][value]) && $scope.fixedip[key][value] !== '')
                    subnet.fixedIp = $scope.fixedip[key][value];
                if (!angular.isUndefined($scope.expose[key][value]) && $scope.expose[key][value].expose)
                    subnet.floatingIp = $scope.expose[key][value].floatingIP;
                subnets2.push(subnet);
            });
            values.subnets = subnets2;
        });
        serviceC.subnets = subnets;
        serviceC.locations = serviceAPI.returnKeys($scope.locationCheckedS, true);
        console.log(serviceC);
        var tmpServiceContainer = angular.copy(serviceC);
        $scope.serviceContainers.push(tmpServiceContainer);
        if (angular.isUndefined($scope.topology.serviceContainers)) {
            $scope.topology.serviceContainers = [];
            $scope.serviceContainers[0].services;
        }
        $scope.sub = {};
        $scope.topology.serviceContainers.push(tmpServiceContainer);
        $scope.locationCheckedS = {};
        $scope.tabs = [{active: true}, {active: false}, {active: false}];
    };

    $scope.addInstance = function(serviceContainer) {
        $('#addInstanceModal').modal('show');
        $scope.containerSelected = serviceContainer;
    };

    $scope.addInstanceInfo = function(serviceContainer) {
        $('#addInstanceModal').modal('show');
        $routeParams.containerId = '' + serviceContainer.id;
        console.log($routeParams);
        http.syncGet(urlT + $routeParams.topologyid).then(function(topology) {
            populateRequires(topology);
        });

    };

    function populateRequires(topologyR) {


        _.each(topologyR.serviceContainers, function(serviceContainer) {
            if (!angular.isUndefined(serviceContainer.relationElements))
                _.each(serviceContainer.relationElements, function(relationElement) {
                    if (!_.contains($scope.requires, relationElement.serviceInstance.instanceName))
                        $scope.requires.push(relationElement.serviceInstance.instanceName);
                });
            else
            {

                _.each(serviceContainer.services, function(service) {
                    if (!_.contains($scope.requires, service.instanceName))
                        $scope.requires.push(service.instanceName);
                });
            }
        });
    }

    $scope.cleanTopology = function(topology) {
        http.put(urlT + topology.id + '/clean')
                .success(function(response) {
                    showOk('Cleaned Topology: <b>' + topology.name + '</b>. ');
                    loadinfoTopology();
                })
                .error(function(data, status) {
                    showError(status, data);
                    loadinfoTopology();
                });
    };
    $scope.startTopology = function(topology) {
        http.post(urlT + topology.id + '/start')
                .success(function(response) {
                    showOk('Started Topology: <b>' + topology.name + '</b> done. ' + response);
                    loadinfoTopology();
                })
                .error(function(data, status) {
                    showError(status, data);
                    loadinfoTopology();
                });
    };

    $scope.deleteContainer = function(index) {
        var sC = $scope.topology.serviceContainers[index];
        _.each(sC.services, function(service) {
            if (_.contains($scope.requires, service.instanceName))
            {
                console.log($scope.requires);
                $scope.requires = _.without($scope.requires, service.instanceName);

            }
        });
        $scope.topology.serviceContainers.splice(index, 1);
        populateRequires($scope.topology);
        $scope.locationCheckedS = {};
        $scope.tabs = [{active: true}, {active: false}, {active: false}];
    };

    $scope.deletePolicy = function(index) {
        $scope.policies.splice(index, 1);
    };


    $scope.addServiceToContainer = function(service) {
        var serviceInstance = cleanService(service);
        $('#addInstanceModal').modal('hide');
        $scope.serviceEdit = {};
        loadSelect();
        $scope.requireChecked = {};
        delete serviceInstance.flavour;
        http.put(urlT + $routeParams.topologyid + '/containers/' + $routeParams.containerId, serviceInstance)
                .success(function(response) {
                    showOk('Service Instance: <b>' + response.instanceName +
                            '</b> added to Service Container with id: ' + $routeParams.containerId);
                    loadinfoTopology();
                    loadTableService();
                })
                .error(function(data, status) {
                    showError(status, data);
                    loadinfoTopology();
                });
        console.log(serviceInstance);
    };

    $scope.addServicesInstance = function() {
        $('#addInstanceModal').modal('hide');

        for (var i = 0; i < $scope.topology.serviceContainers.length; i++)
            if ($scope.topology.serviceContainers[i].containerName === $scope.containerSelected.containerName)
                $scope.topology.serviceContainers[i].services = $scope.servicesForContainer;
        $scope.servicesToPost = [];
        $scope.requires = [];
        $scope.requireChecked = {};
        console.log($scope.containerSelected.services);
        console.log($scope.topology.serviceContainers);
    };

    $scope.new_config = {
        parameter: {"config_key": "config_key", "config_value": "config_value"}

    };

    $scope.addconfigContainer = function() {

        $scope.serviceContainer.configuration.parameters.push($scope.new_config);
        $scope.new_config.parameter = {"config_key": "config_key", "config_value": "config_value"};
    };

    $scope.removeConfigContainer = function(config) {
        var index = $scope.serviceContainer.configuration.parameters.indexOf(config);
        if (index >= 0) {
            $scope.serviceContainer.configuration.parameters.splice(index, 1);
        }
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



    $scope.checkIP = function(str) {
        if (str === '')
            return true;
        else
            return /^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$|^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\-]*[A-Za-z0-9])$|^\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:)))(%.+)?\s*$/.test(str);

    };
    $scope.fixedip = {};

    $scope.imageId = {};
    $scope.$watchCollection(
            "locationChecked",
            function(newValue, oldValue) {
                //                console.log(newValue);
                //                console.log(serviceAPI.returnKeys($scope.locationChecked, true));
                $scope.datacenters = [];
                var arrayLocations = serviceAPI.returnKeys($scope.locationChecked, true);
                http.syncGet('/api/rest/admin/v2/datacenters').then(function(response) {
                    _.each(response, function(datacenter) {
                        _.each(arrayLocations, function(location) {
                            if (datacenter.location.name === location) {
                                if (_.contains($scope.datacenters, datacenter.id))
                                    $scope.datacenters.push(datacenter);
                            }

                        });
                    });
                });
            }
    );

    $scope.$watchCollection(
            "locationCheckedS",
            function(newValue, oldValue) {
                $scope.datacenters = [];
                var arrayLocations = serviceAPI.returnKeys($scope.locationCheckedS, true);
                http.syncGet('/api/rest/admin/v2/datacenters').then(function(response) {
                    _.each(response, function(datacenter) {
                        _.each(arrayLocations, function(location) {
                            if (datacenter.location.name === location) {
                                $scope.datacenters.push(datacenter);
                            }
                        });
                    });
                });
            }
    );
    $scope.selImage = function(image) {
        console.log(image);
    };

    $scope.selectedLocations2 = function(location) {
    };

    $scope.selectedLocations = function(location) {

        $scope.locationCheckedS[location] = $scope.locationChecked[location];
        console.log($scope.locationChecked);
        console.log($scope.locationCheckedS);

        if (serviceAPI.returnKeys($scope.locationChecked, true).length > 0)
            $scope.selectedOneLocation = false;
        else
            $scope.selectedOneLocation = true;

    };

    $scope.sendTopology = function() {

        $scope.topology.locations = serviceAPI.returnKeys($scope.locationChecked, true);
        $('.modal').modal('hide');
        var postTopology;
        var sendOk = true;
        var type = 'topology';
        if ($scope.file !== '')
        {
            postTopology = $scope.file;
            if (postTopology.charAt(0) === '<')
                type = 'definitions';
        }

        else if ($scope.textTopologyJson !== '')
            postTopology = $scope.textTopologyJson;
        else if ($scope.topology.serviceContainers.length !== 0)
            postTopology = $scope.topology;

        else {
            alert('Problem with Topology');
            sendOk = false;
//            $scope.cleanModal();
        }

        console.log(postTopology);
        console.log(type);

        if (sendOk)
        {
            if (type === 'topology')
            {
                http.post(urlT, postTopology)
                        .success(function(response) {
                            showOk('Topology created!');
                            inizializeVariables();
                            loadTableTopology();

                            //                        window.setTimeout($scope.cleanModal(), 3000);
                        })
                        .error(function(data, status) {
                            showError(status, data);
                        });
            }

            else
            {
                http.postXML('/api/rest/tosca/v2/definitions/', postTopology)
                        .success(function(response) {
                            showOk('Definition created!');
                            loadTableTopology();
                            //                        window.setTimeout($scope.cleanModal(), 3000);
                        })
                        .error(function(data, status) {
                            showError(status, data);
                        });
            }
        }
        if ($scope.toggle) {
            var template = {};
            var nameTemplate = $('#nameTemplate').val();
//            template.flavour = $('#inputFlavorTemplate').val();
            template.name = (nameTemplate === '') ? 'Template-' + Math.floor((Math.random() * 100) + 1) : nameTemplate;
            template.topology = postTopology;
            console.log(template);
            http.post('/api/rest/admin/v2/templates', template)
                    .success(function(response) {
                        showOk('Template created!');
//                        $scope.cleanModal();
                    })
                    .error(function(data, status) {
                        showError(status, data);
                    });
        }
        $scope.toggle = false;
        $scope.file !== '';
        //        $scope.services = [];
        loadSelect();
    };
    $scope.servicesForContainer = [];
    $scope.saveService = function(serviceEdit) {


        var serviceInstance = cleanService(serviceEdit);
        //before of delete add to service
        serviceInstance.policies = $scope.policies;
        $scope.policies = [];
        if (angular.isUndefined($scope.containerSelected.services))
            $scope.containerSelected.services = [];
        $scope.containerSelected.services.push(serviceInstance);
        $scope.requires.push(serviceInstance.instanceName);
        populateRequires($scope.topology);
        $scope.serviceEdit = {};
        loadSelect();
        $('#addInstanceModal').modal('hide');
        $scope.requireChecked = {};
    };
    $scope.changeText = function(text) {
        $scope.textTopologyJson = text;
        console.log($scope.textTopologyJson);
    };
    $scope.changeTextXML = function(text) {
        $scope.xmlInputArea = text;
        console.log($scope.xmlInputArea);
    };
    $scope.formIsEmpity = function() {
        if (Object.getOwnPropertyNames($scope.serviceEdit).length > 0)
            return false;
        else
            return true;
    };
    $scope.cleanModal = function() {
        $('.modal').modal('hide');
        $route.reload();
    };


    $scope.changeSelectionItem = function(selection) {
        $scope.item = selection;
    };
    $scope.changeSelection = function(selection) {

        $scope.serviceEdit = {};
        serviceAPI.cleanService(selection);
        selection.instanceName = selection.serviceType + '-' + serviceAPI.getRandom();
        selection.requires = '';
        $scope.serviceEdit = _.omit(selection, 'maxNumInst', 'minNumInst', 'flavour', 'networkIds');
    };

    $scope.Jsplumb = function() {

        http.syncGet(urlT + $routeParams.topologyid).then(function(response) {
            topologiesAPI.Jsplumb(response);
            console.log(response);

        });
    };
    $scope.showServices = function() {
        $scope.selectedTopology = $scope.topologyShow;
        console.log($scope.topologyShow);
        $window.location.href = '#/topologies/' + $scope.topologyShow.id + '/graph';



    };

    $scope.isEmpty = function(obj) {
        return angular.equals({}, obj);
    };
    $scope.removeUnit = function(id) {
        http.delete(urlT + $routeParams.topologyid + '/containers/' + id + '/units')
                .success(function(response) {
                    showOk('Delete unit done.');
                    loadTableTopology();
                })
                .error(function(data, status) {
                    showError(status, data);
                });
    };
    $scope.cleanQueue = function(data) {
        http.delete('/api/rest/admin/v2/queue/' + data.id)
                .success(function(response) {
                    showOk('Cleaned queue for Topology: ' + data.id);
                })
                .error(function(data, status) {
                    showError(status, data);
                });
    };
    $scope.deleteTopology = function(data) {
        http.delete(urlT + data.id)
                .success(function(response) {
                    showOk('Deleted Topology: ' + data.id);

                    loadTableTopology();

                })
                .error(function(data, status) {
                    showError(status, data);

                });
    };
    $scope.addUnit = function(id) {
        $('#addUnit').modal('show');
        $routeParams.containerId = id;
    };
    $scope.sendAddUnit = function() {
        console.log($scope.chooseLoc);
        console.log($scope.locAddUnit);

        if ($scope.chooseLoc)
        {
            addUnitPUT($scope.locAddUnit);
        }
        else
        {
            addUnitPUT();
        }
    };
    function addUnitPUT(obj) {
        if (!angular.isUndefined(obj))
            http.put(urlT + $routeParams.topologyid + '/containers/' + $routeParams.containerId + '/units', obj)
                    .success(function(response) {
                        showOk('Add unit done.');
                        loadinfoTopology();
                        loadTableService();
                    })
                    .error(function(data, status) {
                        showError(status, data);
                        loadinfoTopology();
                    });

        else
            http.put(urlT + $routeParams.topologyid + '/containers/' + $routeParams.containerId + '/units')
                    .success(function(response) {
                        showOk('Add unit done.');
                        loadinfoTopology();
                        loadTableService();
                    })
                    .error(function(data, status) {
                        showError(status, data);
                        loadinfoTopology();
                    });

    }


    $scope.cleanService = function(id) {
        http.put(urlT + $routeParams.topologyid + '/services/' + id + '/clean')
                .success(function(response) {
                    showOk('Cleaned Service with id: ' + id);
                })
                .error(function(data, status) {
                    showError(status, data);
                });
    };
    $scope.stopUnit = function(idS, id) {
        actionUnit(idS, id, 'stop');
    };
    $scope.startUnit = function(idS, id) {
        actionUnit(idS, id, 'start');
    };
    $scope.cleanUnit = function(idS, id) {
        actionUnit(idS, id, 'clean');
    };
    $scope.testUnit = function(idS, id) {
        actionUnit(idS, id, 'start-test');
    };

    $scope.floatingIP = {};
    $scope.showFloatingIps = function(unit) {
        $scope.modalUnit = unit;
        http.syncGet('/api/rest/admin/v2/floatingips/' + unit.datacenterId).then(function(response) {
            $scope.floatingIPs = response;
            $scope.networks = unit.ips;

            $('#floatingIP').modal('show');


        });
    };
    $scope.expose = {};

    $scope.setFloatingIP = function() {
        http.put('/api/rest/admin/v2/floatingips/units/' + $scope.modalUnit.id + '/subnets/' + $scope.floatingIP.net,
                {'floatingIp': $scope.floatingIP.IP})
                .success(function(response) {
                    showOk('Setted the Floating ip: <b>' + $scope.floatingIP.IP + '</b> to the network: <b>'
                            + $scope.floatingIP.net + '</b> in the unit: <b>' + $scope.modalUnit.hostname + '</b>');
                    loadUnits();
                })
                .error(function(data, status) {
                    showError(status, data);
                    loadUnits();

                });
    };
    $scope.deleteFloatingIp = function(unit) {
        http.delete('/api/rest/admin/v2/floatingips/units/' + unit.id + '/' + unit.floatingIp)
                .success(function(response) {
                    showOk('Deleted Flotaing IP: <b>' + unit.floatingIp + '</b> to the unit: <b>' + unit.hostname + '</b>');
                    loadUnits();

                })
                .error(function(data, status) {
                    showError(status, data);
                    loadUnits();

                });
    };
    $scope.deleteUnitToService = function(idS, idU) {
        http.delete(urlT + $routeParams.topologyid + '/containers/' + idS + '/units/' + idU)
                .success(function(response) {
                    showOk('Deleted Unit with id:' + idU);
                    loadinfoTopology();
                    loadTableService();
                })
                .error(function(data, status) {
                    showError(status, data);
                    loadinfoTopology();
                });
    };
    $scope.deleteUnit = function(id) {
        http.delete(urlT + $routeParams.topologyid + '/containers/' + $scope.serviceSelected.id + '/units/' + id)
                .success(function(response) {
                    showOk('Deleted Unit with id:' + id);
                    loadinfoTopology();
                })
                .error(function(data, status) {
                    showError(status, data);
                    loadinfoTopology();
                });
    };
    $scope.returnUptime = function(longUptime) {
        var string = serviceAPI.returnStringUptime(longUptime);
        return string;
    };

    $scope.stringContains = function(k1, k2) {
        return k2.indexOf(k1) > -1;
    };


    $scope.deletePolicyId = function(id) {
        http.delete(urlT + $routeParams.topologyid + '/services/' + $routeParams.serviceId + '/policies/' + id)
                .success(function(response, status) {
                    showOk('Deleted Policy with id: ' + id);
                    getServiceInstance();

                })
                .error(function(data, status) {
                    showError(status, data);
                    getServiceInstance();

                });

    };
    $scope.addPolicy = function() {
        $('#modalPolicy').modal('hide');
        var policy = {};
        var alarm = {"expression": {"item": $scope.policy.alert.item.itemKey, "operation": $scope.policy.alert.sign, "value": $scope.policy.alert.value.toString()}, "period": $scope.policy.alert.period};
        var action = {"actionType": $scope.policy.action.actionType, "num": $scope.policy.action.unit, "period": $scope.policy.action.period};
        policy.alarm = alarm;
        policy.action = action;
        policy.strategy = "AVERAGE";
        if (angular.isUndefined($routeParams.serviceId))
            $scope.policies.push(policy);
        else
        {
            http.post(urlT + $routeParams.topologyid + '/services/' + $routeParams.serviceId + '/policies', policy)
                    .success(function(response) {
                        showOk('Added a new Policy.');
                        getServiceInstance();
                    })
                    .error(function(data, status) {
                        showError(status, data);
                        getServiceInstance();

                    });
        }
        console.log(policy);
    };

    function loadTableService() {
        if (!angular.isUndefined($routeParams.containerId) && !$('#jsonInfo').hasClass('in')) {
            $scope.relationElements = [];
            var arrayServiceIds = [];
            http.syncGet(urlT + $routeParams.topologyid + '/containers/' + $routeParams.containerId).then(function(response) {
                $scope.serviceSelected = response;
                _.each(response.relationElements, function(relationElement) {

                    if (!_.contains(arrayServiceIds, relationElement.serviceInstance.id)) {
                        arrayServiceIds.push(relationElement.serviceInstance.id);
                        $scope.relationElements.push(relationElement);
                    }
                });

            });
        }
    }

    $scope.deleteServiceInstance = function(service) {
        if (!angular.isUndefined($routeParams.containerId) && !angular.isUndefined($routeParams.topologyid))
            http.delete(urlT + $routeParams.topologyid + '/containers/' + $routeParams.containerId + '/services/' + service.id)
                    .success(function(response) {
                        showOk('Deleted Service Instance with id:' + service.id);
                        loadinfoTopology();
                        loadTableService();
                    })
                    .error(function(data, status) {
                        showError(status, data);
                    });
    };

    $scope.deleteService = function(service) {
        http.delete(urlT + $routeParams.topologyid + '/containers/' + service.id)
                .success(function(response) {
                    showOk('Deleted Service Container with id:' + service.id);
                    loadinfoTopology();
                    loadTableService();
                })
                .error(function(data, status) {
                    showError(status, data);
                });
    };
    $scope.selectedRequires = function(require) {
        //        console.log($scope.requireChecked);
    };


    $scope.getTarget = function(relations) {
        return $scope.relations[relations.id].target;
    };

    $scope.sendServiceToAdd = function(serviceEdit) {
        $scope.topology = {};
        $scope.serviceContainers[0].services = $scope.containerSelected.services;
        console.log($scope.serviceContainers[0]);
        $('.modal').modal('hide');
        http.put(urlT + $scope.selectedTopology.id + '/containers/ ', $scope.serviceContainers[0])
                .success(function(response) {
                    showOk('Service: ' + serviceEdit.containerName + ' added to Topology with id: ' + $scope.selectedTopology.id);
                    inizializeVariables();
                })
                .error(function(data, status) {
                    showError(status, data);
                    inizializeVariables();
                });
    };
    $scope.addServiceContainer = function(topology) {
        $('#modalAdd').modal('show');

        $scope.selectedTopology = topology;
        console.log(topology);
        _.each(topology.serviceContainers, function(serviceContainer) {
            _.each(serviceContainer.relationElements, function(relationElement) {
                if (!_.contains($scope.requires, relationElement.serviceInstance.instanceName))
                    $scope.requires.push(relationElement.serviceInstance.instanceName);
            });
        });
    };

    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };

    $scope.sendTemplate = function() {
        $('.modal').modal('hide');
        $scope.template.topology = $scope.topology;
        $scope.template.topology.locations = serviceAPI.returnKeys($scope.locationChecked, true);
        console.log($scope.template);
        http.post('/api/rest/admin/v2/templates', $scope.template)
                .success(function(response) {
                    showOk('Template created!');
                    inizializeVariables();
                    loadTableTemplate();
                })
                .error(function(data, status) {
                    showError(status, data);
                });
    };

    $scope.deleteTemplate = function(template) {
        http.delete('/api/rest/admin/v2/templates/' + template.id)
                .success(function(response) {
                    showOk('Template deleted!');
                    loadTableTemplate();
                })
                .error(function(data, status) {
                    showError(data, status);
                });
    };

    $scope.launch = function(template) {
        angular.forEach(template.topology.serviceContainers, function(sC, index) {
            angular.forEach(sC.subnets, function(subnet, key) {
                angular.forEach(subnet.subnets, function(sub, ind) {
                    delete sub.id;
                    delete sub.version;
                });
            });
        });
        console.log(template.topology);
        http.post(urlT, template.topology)
                .success(function(response) {
                    showOk('Topology <strong>' + template.topology.name + '</strong> created!');
                    loadTableTemplate();
                })
                .error(function(data, status) {
                    showError(data, status);
                });
    };


    if (!angular.isUndefined($routeParams.templateid)) {
        http.get('/api/rest/admin/v2/templates/' + $routeParams.templateid)
                .success(function(response) {
                    $scope.templateInfo = response;
                })
                .error(function(data, status) {
                    showError(data, status);
                });
    }

    function showError(status, data) {
        $scope.alerts.push({type: 'danger', msg: 'ERROR: <strong>HTTP status</strong>: ' + status + ' response <strong>data</strong>: ' + data});
//        loadTableTopology();
        $('.modal').modal('hide');
    }
    function showOk(msg) {
        $scope.alerts.push({type: 'success', msg: msg});
        $('.modal').modal('hide');
    }

    function loadTableTemplate() {
        http.get('/api/rest/admin/v2/templates')
                .success(function(response, status) {
                    $scope.templates = response;
                })
                .error(function(data, status) {
                    showError(data, status);
                });
    }
    getServiceInstance();
    function getServiceInstance() {
        if (!angular.isUndefined($routeParams.topologyid) && !angular.isUndefined($routeParams.serviceId))
            http.get(urlT + $routeParams.topologyid + '/services/' + $routeParams.serviceId)
                    .success(function(response, status) {
                        $scope.serviceInstance = response;
                    })
                    .error(function(data, status) {
                        showError(data, status);
                    });
    }
    function loadTableTopology() {
        if (angular.isUndefined($routeParams.topologyid) && !$('#jsonInfo').hasClass('in'))
            http.get(urlT)
                    .success(function(response, status) {
                        $scope.topologies = response;
                    })
                    .error(function(data, status) {
                        var destinationUrl = '#';
                        $window.location.href = destinationUrl;
                    });
        else
            http.get(urlT + $routeParams.topologyid)
                    .success(function(response, status) {
                        $scope.topologyShow = response;
                    })
                    .error(function(data, status) {
                        var destinationUrl = '#';
                        $window.location.href = destinationUrl;
                    });
    }



    function actionUnit(idS, idU, action) {
        http.put(urlT + 'services/' + idS + '/units/' + idU + '/' + action)
                .success(function(response) {
                    showOk('Action: ' + action + ' done.');
                })
                .error(function(data, status) {
                    showError(status, data);
                });
    }

    function loadSelect() {
        serviceAPI.getServices().then(function(data)
        {
            $scope.services = data;
        });
    }

    function loadinfoTopology() {
        if (!angular.isUndefined($routeParams.topologyid) && !$('#jsonInfo').hasClass('in')) {
            http.get(urlT + $routeParams.topologyid)
                    .success(function(response, status) {
                        $scope.topologyShow = response;
                        $scope.topologyShowJSON = JSON.stringify(response, undefined, 4);
                        $scope.topologyId = $routeParams.topologyid;

                        $scope.topologies = response;
                    })
                    .error(function(data, status) {
                        $window.location.href = '#';
                    });

        }
    }

    function cleanService(serviceEdit) {
        var parameters = angular.copy(serviceEdit.configuration.parameters);
        _.each(parameters, function(parameter) {
            delete parameter.version;
        });
        var configuration = angular.copy(_.omit(serviceEdit.configuration, 'id', 'version'));
        configuration.parameters = parameters;

        var serviceInstance = angular.copy(_.omit(serviceEdit, 'locations', 'flavour', 'maxNumInst', 'minNumInst', 'networkIds'));
        serviceInstance.configuration = configuration;

        serviceInstance.requires = serviceAPI.returnKeys($scope.requireChecked, true);
        return serviceInstance;
    }

});

