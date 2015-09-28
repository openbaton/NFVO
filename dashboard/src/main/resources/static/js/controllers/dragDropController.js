var myApp = angular.module('app');
myApp.controller('DragDropCtrl', function($scope, http, serviceAPI, $routeParams, $cookieStore, $window, $timeout) {

    var urlT = '/api/rest/orchestrator/v2/nsrecords/';
    var url = '/api/rest/orchestrator/v2/services/';




    $scope.connectionArray = [];
    loadServicesTable();
    $scope.schema = [];
    $scope.library = [];
    function module(library_id, schema_id, title, description, x, y) {
        this.library_id = library_id;
        this.schema_id = schema_id;
        this.title = title;
        description.instanceName = title + '-' + $scope.getRandom();
        this.description = cleanService(description);
        this.x = x;
        this.y = y;
    }

    $scope.mgmt = '';

    $scope.serviceContainer = {
        "flavour": "m1.small",
        "minNumInst": 1,
        "maxNumInst": 3,
        "serviceType": "container",
        "containerName": "container1"
    };
    $scope.locations = serviceAPI.getLocations();

    $scope.selectMgmt = function(newValue) {
        $scope.mgmt = newValue;
        console.log(newValue);
    };

    $scope.locationChecked = {};

    $scope.datacentersArray = [];

    loadDC();
    function loadDC() {
        http.get('/api/rest/admin/v2/datacenters').then(function(response) {
            $scope.datacentersArray = response;
        });
    }


    $scope.expose = {};
    $scope.fixedip = {};
    $scope.imageId = {};
    $scope.sub = {};
    $scope.containers = [];

    $scope.addContainer = function(serviceContainer) {
        var container = angular.copy(serviceContainer);
        container.images = $scope.imageId;
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
        container.subnets = subnets;
        container.locations = serviceAPI.returnKeys($scope.locationChecked, true);
        container.services = [];
        $scope.containers.push(container);
    };


    $scope.$watchCollection(
        "containers",
        function(newValue, oldValue) {
//                console.log(newValue);
        }
    );
    $scope.$watchCollection(
        "schema",
        function(newValue, oldValue) {
            updateSchemaMachines(newValue);
        }
    );


    function updateSchemaMachines(newValue) {
        angular.forEach($scope.containers, function(container) {
            container.services = [];
        });

        $scope.schemaM = angular.copy(newValue);
//        console.log(newValue);
    }

    $scope.netCheck = false;
    $scope.checkIP = function(str) {
        if (str === '')
            return true;
        else
            return /^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$|^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\-]*[A-Za-z0-9])$|^\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:)))(%.+)?\s*$/.test(str);

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
    $scope.selectedLocations = function(location) {

        var arrayLocations = serviceAPI.returnKeys($scope.locationChecked, true);

        $scope.datacenters = [];
        _.each($scope.datacentersArray, function(datacenter) {
            _.each(arrayLocations, function(location) {
                if (datacenter.location.name === location) {
                    $scope.datacenters.push(datacenter);
                }

            });
        });

        if (serviceAPI.returnKeys($scope.locationChecked, true).length > 0)
            $scope.selectedOneLocation = false;
        else
            $scope.selectedOneLocation = true;

    };


    $scope.hideMe = function() {

        return $scope.containers.length > 0;
    };
// module should be visualized by title, icon
    $scope.library = [];
    // library_uuid is a unique identifier per module type in the library
    $scope.library_uuid = 0;
    // state is [identifier, x position, y position, title, description]
    $scope.schema = [];
    // schema_uuid should always yield a unique identifier, can never be decreased
    $scope.schema_uuid = 0;
    // todo: find out how to go back and forth between css and angular
    $scope.library_topleft = {
        x: 15,
        y: 145,
        item_height: 50,
        margin: 5,
    };
    $scope.module_css = {
        width: 150,
        height: 100, // actually variable
    };
    $scope.tabs = [
        {
            "heading": "Topology",
            "active": true,
            "template": "pages/dragdropT.html"
        },
        {
            "heading": "Machines",
            "active": false,
            "template": "pages/dragdropM.html"
        }];


    $scope.signs = [
        "<", "=", ">"
    ];

    $scope.values = [
        "10", "20", "30", "40", "50", "60", "70", "80", "90"
    ];
    $scope.types = [
        "ADD", "REMOVE"
    ];

    $scope.sendTopology = function() {
        var connections = jsPlumb.getConnections();
        var tmpArray = [];

        console.log($scope.schema);
        angular.forEach(connections, function(con) {
//            console.log($scope.schema[con.sourceId].description.instanceName);
//            console.log($scope.schema[con.targetId].description.instanceName);

            var connec = {source: $scope.schema[con.sourceId].description.instanceName, target: $scope.schema[con.targetId].description.instanceName};
            tmpArray.push(connec);
        });
        console.log(tmpArray);

        angular.forEach($scope.containers, function(container) {
            angular.forEach(container.services, function(service) {
                angular.forEach(tmpArray, function(conn) {
                    //Already assigned the description to the service
                    if (!angular.isUndefined(service.description)) {
                        service = service.description;
                    }
                    console.log(service);
                    console.log(service.instanceName === conn.source);

                    if (service.instanceName === conn.source) {
                        if (angular.isUndefined(service.requires))
                            service.requires = [];
                        if (!_.contains(service.requires, conn.target))
                            service.requires.push(conn.target);
                        console.log(service);

                    }

                });

            });

        });

        console.log($scope.containers);

        $('#modalTopology').modal('show');

    };

    $scope.topology = {};
    $scope.topology.name = '';
    $scope.sendTopologyModal = function() {
        angular.forEach($scope.containers, function(container) {
            var services = [];
            angular.forEach(container.services, function(service) {

                services.push(service.description);

            });
            container.services = services;
        });

        $scope.topology.locations = serviceAPI.returnKeys($scope.locationChecked, true);
        $scope.topology.serviceContainers = $scope.containers;
        console.log($scope.topology);
        http.post(urlT, $scope.topology)
            .success(function(response) {
//                    $window.location.href = '#/nsrecords';
                showOk('Created Topology: <b>' + $scope.topology.name + '</b>. ' + JSON.stringify(response));
                $timeout($window.location.href = '#/nsrecords', 10000);

            })
            .error(function(data, status) {
                showError(status, data);

            });
    };

    $scope.deleteContainer = function(index) {
        var container = $scope.containers[index];
        console.log(container.services);

        angular.forEach(container.services, function(service) {
            console.log(service);
            $scope.schemaM.push(service);
        });

        $scope.containers.splice(index, 1);
    };
//    $scope.items = [{itemKey: 'fake.item'}];
    loadItems();
    function loadItems() {

        if (angular.isUndefined($cookieStore.get('items')))
            http.get('/api/rest/monitoring/v2/items/')
                .success(function(response) {
//                        console.log(response);
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

    $scope.policies = [];
    $scope.policy = {};
    $scope.addPolicy = function() {
        var policy = {};
        var alarm = {"expression": {"item": $scope.policy.alert.item.itemKey, "operation": $scope.policy.alert.sign, "value": $scope.policy.alert.value.toString()}, "period": $scope.policy.alert.period};
        var action = {"actionType": $scope.policy.action.actionType, "num": $scope.policy.action.unit, "period": $scope.policy.action.period};
        policy.alarm = alarm;
        policy.action = action;
        policy.strategy = "AVERAGE";
        if (angular.isUndefined($scope.serviceEdit.description.policies))
            $scope.serviceEdit.description.policies = [];
        $scope.serviceEdit.description.policies.push(policy);
        console.log(policy);
    };

    $scope.deletePolicy = function(index) {
        $scope.serviceEdit.description.policies.splice(index, 1);
    };


    $scope.addModuleToLibrary = function(title, description, posX, posY) {
        console.log("Add module " + title + " to library, at position " + posX + "," + posY);
        var library_id = $scope.library_uuid++;
        var schema_id = -1;
        var m = new module(library_id, schema_id, title, description, posX, posY);
        $scope.library.push(m);
    };
    // add a module to the schema
    $scope.addModuleToSchema = function(library_id, json, posX, posY) {
        console.log("Add module " + title + " to schema, at position " + posX + "," + posY);
        var schema_id = $scope.schema_uuid++;
        var title = json.serviceType;
        var description = json;
        var m = new module(library_id, schema_id, title, description, posX, posY);
        $scope.schema.push(m);
        console.log($scope.schema);
    };
    $scope.removeState = function(schema_id) {
        console.log("Remove state " + schema_id + " in array of length " + $scope.schema.length);
        console.log($scope.schema);
        for (var i = 0; i < $scope.schema.length; i++) {
            // compare in non-strict manner
            if ($scope.schema[i].schema_id == schema_id) {
                console.log("Remove state at position " + i);
                $scope.schema.splice(i, 1);
            }
        }
        console.log($scope.schema);
    };
    $scope.deleteItem = function(id, e) {
        var node = $('#' + id);
        console.log(node);
        console.log($(node).data('identifier'));
        e.stopPropagation();
        jsPlumb.detachAllConnections(node);
//        $(node).remove();

        // stop event propagation, so it does not directly generate a new state
//        event.stopPropagation();
//        //we need the scope of the parent, here assuming <plumb-item> is part of the <plumbApp>			
        $scope.removeState(id);
//        $scope.$parent.$digest();

    };
    $scope.getRandom = function() {
        return Math.floor((Math.random() * 100) + 1);
    };
    $scope.saveService = function() {
        updateSchemaMachines($scope.schema);
    };
    $scope.showModal = function(index) {
        console.log($scope.schema[index]);
        $scope.serviceEdit = $scope.schema[index];
        console.log($scope.serviceEdit);
//        $scope.serviceEdit = $scope.schema[index];
        $('#addInstanceModal').modal('show');
    };
    $scope.init = function() {





        jsPlumb.bind("ready", function() {

            loadServicesTable();



            jsPlumb.bind("click", function(conn, originalEvent) {
//                console.log(originalEvent);
                if (confirm("Delete connection from " + conn.sourceId + " to " + conn.targetId + "?")) {
                    jsPlumb.detach(conn);
                }
            });
            console.log("Set up jsPlumb listeners (should be only done once)");
            jsPlumb.bind("connection", function(info) {

                console.log(jsPlumb.getConnections());
                var con = info.connection;
                var arr = jsPlumb.select({source: con.sourceId, target: con.targetId});
                var arrRev = jsPlumb.select({source: con.targetId, target: con.sourceId});
                if (arr.length > 1) {
                    jsPlumb.detach(con);
                }
                if (arrRev.length >= 1) {
                    angular.forEach(jsPlumb.getConnections(), function(connection, index) {
                        if (connection.sourceId === con.targetId && connection.targetId === con.sourceId)
                            jsPlumb.detach(connection);
                    });
                }



                $scope.$apply(function() {
//                    console.log("Possibility to push connection into array");


                    console.log(info);
                });
            });
        });


    };
    function loadServicesTable() {
        http.get(url).success(function(response) {

            $scope.services = response;
        });

    }


    function cleanService(serviceEdit) {

        var parameters = angular.copy(serviceEdit.configuration.parameters);


        _.each(parameters, function(parameter) {
            delete parameter.version;
        });
        var configuration = angular.copy(_.omit(serviceEdit.configuration, 'id', 'version', 'configurationName'));
        configuration.parameters = parameters;
        var serviceInstance = angular.copy(_.omit(serviceEdit, 'id', 'flavour', 'maxNumInst', 'minNumInst', 'version'));
        serviceInstance.configuration = configuration;
//        serviceInstance.requires = serviceAPI.returnKeys($scope.requireChecked, true);
        return serviceInstance;
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



});
myApp.directive('postRender', ['$timeout', function($timeout) {
    var def = {
        restrict: 'A',
        terminal: true,
        transclude: true,
        link: function(scope, element, attrs) {
            $timeout(scope.redraw, 0); //Calling a scoped method
        }
    };
    return def;
}]);
//directives link user interactions with $scope behaviours
//now we extend html with <div plumb-item>, we can define a template <> to replace it with "proper" html, or we can 
//replace it with something more sophisticated, e.g. setting jsPlumb arguments and attach it to a double-click 
//event
myApp.directive('plumbItem', function() {
    return {
        replace: true,
        controller: 'DragDropCtrl',
        link: function(scope, element, attrs) {
            console.log("Add plumbing for the 'item' element");
            jsPlumb.makeTarget(element, {
                anchor: 'Continuous',
                maxConnections: 2,
                paintStyle: {
                    outlineColor: "",
                    fillStyle: "transparent",
                    radius: 0,
                    lineWidth: 1
                },
            });
            jsPlumb.draggable(element, {
                containment: 'parent'
            });
            // this should actually done by a AngularJS template and subsequently a controller attached to the dbl-click event
//            element.bind('dblclick', function(e) {
//                console.log($(this));
//                console.log(attrs);
//                jsPlumb.detachAllConnections($(this));
//                $(this).remove();
//                // stop event propagation, so it does not directly generate a new state
//                e.stopPropagation();
//                //we need the scope of the parent, here assuming <plumb-item> is part of the <plumbApp>			
//                scope.$parent.removeState(attrs.identifier);
//                scope.$parent.$digest();
//                console.log(scope.$parent);
//            });
        }
    };
});
//
// This directive should allow an element to be dragged onto the main canvas. Then after it is dropped, it should be
// painted again on its original position, and the full module should be displayed on the dragged to location.
//
myApp.directive('plumbMenuItem', function($timeout) {
    return {
        replace: true,
        controller: 'DragDropCtrl',
        link: function(scope, element, attrs) {
//            console.log("Add plumbing for the 'menu-item' element");
            // jsPlumb uses the containment from the underlying library, in our case that is jQuery.
            jsPlumb.draggable(element, {
                containment: element.parent().parent()
            });

            //add the JscrollPane after ng-repeat wehen the function has finished
//            if (scope.$last)
//                $timeout(function() {
//                    $('.scroll-pane').jScrollPane();
//                }, 200);



        }
    };
});
myApp.directive('plumbConnect', function() {
    return {
        replace: true,
        link: function(scope, element, attrs) {
            console.log("Add plumbing for the 'connect' element");
            jsPlumb.makeSource(element, {
                parent: $(element).parent(),
                anchor: 'Continuous',
                connectionType: "test",
                paintStyle: {
                    outlineColor: "",
                    fillStyle: "transparent",
                    radius: 0,
                    lineWidth: 1
                },
            });
        }
    };
});
myApp.directive('droppable', function($compile) {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            console.log("Make this element droppable");
            element.droppable({
                drop: function(event, ui) {
                    // angular uses angular.element to get jQuery element, subsequently data() of jQuery is used to get
                    // the data-identifier attribute
                    var dragIndex = angular.element(ui.draggable).data('identifier'),
                        elemJson = angular.element(ui.draggable).data('json'),
                        dragEl = angular.element(ui.draggable),
                        dropEl = angular.element(this);
                    // if dragged item has class menu-item and dropped div has class drop-container, add module 
                    if (dragEl.hasClass('menu-item') && dropEl.hasClass('drop-container')) {
                        console.log("Drag event on " + dragIndex);
                        console.log(elemJson);
                        var x = event.pageX - scope.module_css.width / 2;
                        var y = event.pageY - scope.module_css.height / 2;
                        scope.addModuleToSchema(dragIndex, elemJson, x, y);
                    }

                    scope.$apply();
                }
            });
        }
    };
});
myApp.directive('plumbRemove', function() {
    return {
        replace: true,
        controller: 'DragDropCtrl',
        link: function(scope, element, attrs) {

            // this should actually done by a AngularJS template and subsequently a controller attached to the dbl-click event
            element.bind('click', function(e) {
                console.log($(this).parent().attr('id'));
                jsPlumb.detachAllConnections($(this).parent());

//                $(this).parent().remove();


                // stop event propagation, so it does not directly generate a new state
                e.stopPropagation();
                //we need the scope of the parent, here assuming <plumb-item> is part of the <plumbApp>			
                scope.$parent.removeState($(this).parent().attr('id'));

                scope.$parent.$digest();
            });
        }
    };
});
myApp.directive('showModal', function() {
    return {
        replace: true,
        controller: 'DragDropCtrl',
        scope: {
            myindex: '='
        },
        link: function(scope, element, attrs) {

            // this should actually done by a AngularJS template and subsequently a controller attached to the dbl-click event
            element.bind('click', function(e) {
                console.log(scope.myindex);
                scope.$parent.$parent.showModal(scope.myindex);
                scope.$parent.$parent.$apply();
            });
        }
    };
});
myApp.directive('draggable', function() {
    return {
        // A = attribute, E = Element, C = Class and M = HTML Comment
        restrict: 'A',
        //The link function is responsible for registering DOM listeners as well as updating the DOM.
        link: function(scope, element, attrs) {
//            console.log("Let draggable item snap back to previous position");
            element.draggable({
                // let it go back to its original position
                revert: true,
            });
        }
    };
});