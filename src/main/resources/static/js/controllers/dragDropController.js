
var myApp = angular.module('app');
myApp.controller('DragDropCtrl', function($scope, http, serviceAPI, $routeParams) {

    
    var url = '/api/rest/orchestrator/v2/services/';
    loadServicesTable();
    $scope.schema = [];
    $scope.library = [];
    function module(library_id, schema_id, title, description, x, y) {
        this.library_id = library_id;
        this.schema_id = schema_id;
        this.title = title;
        this.description = cleanService(description);
        this.x = x;
        this.y = y;
    }
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
    $scope.redraw = function() {
        $scope.schema_uuid = 0;
        jsPlumb.detachEveryConnection();
        $scope.schema = [];
        $scope.library = [];
        $scope.addModuleToLibrary("Sum", "Aggregates an incoming sequences of values and returns the sum",
                $scope.library_topleft.x + $scope.library_topleft.margin,
                $scope.library_topleft.y + $scope.library_topleft.margin);
        $scope.addModuleToLibrary("Camera", "Hooks up to hardware camera and sends out an image at 20 Hz",
                $scope.library_topleft.x + $scope.library_topleft.margin,
                $scope.library_topleft.y + $scope.library_topleft.margin + $scope.library_topleft.item_height);
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
//        for (var i = 0; i < $scope.library.length; i++) {
//            if ($scope.library[i].library_id == library_id) {
//                title = $scope.library[i].title;
//                description = $scope.library[i].description;
//            }
//        }
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
        console.log($scope.schema);
    };
    $scope.showModal = function(index) {
        console.log($scope.schema[index]);
        $scope.serviceEdit = $scope.schema[index];
        $scope.serviceEdit.description.instanceName = $scope.serviceEdit.description.serviceType + '-' + $scope.getRandom();
        console.log($scope.serviceEdit);
//        $scope.serviceEdit = $scope.schema[index];
        $('#addInstanceModal').modal('show');
    };
    $scope.init = function() {
        jsPlumb.bind("ready", function() {

            loadServicesTable();
            jsPlumb.bind("click", function(conn, originalEvent) {
//                console.log(originalEvent);
                if (confirm("Delete connection from " + conn.sourceId + " to " + conn.targetId + "?"))
                    jsPlumb.detach(conn);
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
                    console.log("Possibility to push connection into array");
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
        var configuration = angular.copy(_.omit(serviceEdit.configuration, 'id', 'version'));
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
                    outlineColor: "yellow",
                    fillStyle: "transparent",
                    radius: 5,
                    lineWidth: 2
                },
            });
            jsPlumb.draggable(element, {
                containment: 'parent'
            });
            // this should actually done by a AngularJS template and subsequently a controller attached to the dbl-click event
            element.bind('dblclick', function(e) {
                console.log($(this));
                console.log(attrs);
                jsPlumb.detachAllConnections($(this));
                $(this).remove();
                // stop event propagation, so it does not directly generate a new state
                e.stopPropagation();
                //we need the scope of the parent, here assuming <plumb-item> is part of the <plumbApp>			
                scope.$parent.removeState(attrs.identifier);
                scope.$parent.$digest();
                console.log(scope.$parent);
            });
        }
    };
});
//
// This directive should allow an element to be dragged onto the main canvas. Then after it is dropped, it should be
// painted again on its original position, and the full module should be displayed on the dragged to location.
//
myApp.directive('plumbMenuItem', function() {
    return {
        replace: true,
        controller: 'DragDropCtrl',
        link: function(scope, element, attrs) {
            console.log("Add plumbing for the 'menu-item' element");
            // jsPlumb uses the containment from the underlying library, in our case that is jQuery.
            jsPlumb.draggable(element, {
                containment: element.parent().parent()
            });
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
                    outlineColor: "yellow",
                    fillStyle: "transparent",
                    radius: 1,
                    lineWidth: 2
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
                $(this).parent().remove();
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
            console.log("Let draggable item snap back to previous position");
            element.draggable({
                // let it go back to its original position
                revert: true,
            });
        }
    };
});