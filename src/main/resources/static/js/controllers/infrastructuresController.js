
angular.module('app')
        .controller('InfrastructureCtrl', function($scope, $q, $compile, http, $routeParams, $route, $window, topologiesAPI, $timeout) {


            //loadTable();
            $scope.isCollapsed = true;
            $scope.alerts = [];
            $scope.textTopologyJson = '';
            $scope.changeText = function(text) {
                $scope.textTopologyJson = text;
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
            $scope.sendInfrastructure = function() {
                if ($scope.file !== '' && !angular.isUndefined($scope.file))
                {
                    console.log($scope.file);
                    http.post('/api/rest/admin/v2/infrastructures/', $scope.file)
                            .success(function(response) {
                                showOk('Infrastructure created.');
                                $route.reload();
                            })
                            .error(function(data, status) {
                                showError(data, status);
                            });
                } else if ($scope.textTopologyJson !== '') {
                    console.log($scope.textTopologyJson);
                    http.post('/api/rest/admin/v2/infrastructures/', $scope.textTopologyJson)
                            .success(function(response) {
                                showOk('Infrastructure created.');
                                resizeMap();
                                $scope.file = '';
                            })
                            .error(function(data, status) {
                                showError(data, status);

                                resizeMap();
                            });
                }
                else {
                    showError('Problem with the Infrastructure');

                }
            };

            

            $scope.$watch('isCollapsed', function() {

                console.log($scope.isCollapsed);
                if ($('#graphicInfrastructure').is(":visible")) {
                    $('#graphicInfrastructure').html('');
                    topologiesAPI.getTopologyD3();
                }


            });
            $scope.delete = function() {
                http.delete('/api/rest/admin/v2/infrastructures/')
                        .success(function(response) {
                            showOk('Infrastructure deleted.');
                        })
                        .error(function(data, status) {
                            showError(data, status);
                        });
            };
            $scope.closeAlert = function(index) {
                $scope.alerts.splice(index, 1);
            };


            function loadTable() {
                http.get('/api/rest/admin/v2/infrastructures').success(function(response) {
                    $scope.infrastructure = response[0];
                    $scope.infrastructureJSON = JSON.stringify($scope.infrastructure, undefined, 2);

                });
            }

            function showError(data, status) {
                $scope.alerts.push({type: 'danger', msg: 'ERROR: <strong>HTTP status</strong>: ' + status + ' response <strong>data</strong> : ' + data});
                loadTable();
                $('.modal').modal('hide');
            }
            function showOk(msg) {
                $scope.alerts.push({type: 'success', msg: msg});
                loadTable();
                $('.modal').modal('hide');
            }


            window.onresize = function() {
                window.location.reload();
            };

            populate();

            function populate() {
                if (!angular.isUndefined($routeParams.topologyid) && $window.location.hash.match("^#/deployed/")) {
                    console.log($routeParams.topologyid);
                    http.syncGet("/api/rest/orchestrator/v2/nsrecords/" + $routeParams.topologyid).then(function(topology) {
                        drawTopologyDeployed(topology);
                    });
                }

            }


            function drawTopologyDeployed(topology) {
                var lines = [];
                var links = http.get("/api/rest/admin/v2/links"),
                        datacenters = http.get("/api/rest/admin/v2/vim-instances"),
                        accesspoints = http.get("/api/rest/admin/v2/accesspoints"),
                        chains = http.get("/api/rest/crosslayer/v1/chains"),
                        switches = http.get("/api/rest/admin/v2/switches");
                $q.all([links, datacenters, accesspoints, switches, chains, topology]).then(function(result) {
                    var links = [];
                    var datacenters = [];
                    var switches = [];
                    var accesspoints = [];
                    var chains = [];
                    var tmp = {};
                    angular.forEach(result, function(response) {
                        _.each(response.data, function(data) {
                            if (_.has(data, 'source')) {
                                data.typeJs = 'links';
                                links.push(data);
                            }
                            if (_.has(data, 'dpid') && !_.has(data, 'mac')) {
                                data.typeJs = 'switches';
                                switches.push(data);
                            }
                            if (_.has(data, 'name')) {
                                data.typeJs = 'datacenters';
                                datacenters.push(data);
                            }
                            if (_.has(data, 'mac')) {
                                data.typeJs = 'accesspoints';
                                accesspoints.push(data);
                            }
                            if (_.has(data, 'route')) {
                                data.typeJs = 'chains';
                                chains.push(data);
                            }


                        });
                    });
                    tmp.chains = chains;
                    tmp.links = links;
                    tmp.switches = switches;
                    tmp.datacenters = datacenters;
                    tmp.accesspoints = accesspoints;
                    _.each(tmp.datacenters, function(datacenter) {
                        if (angular.isUndefined(datacenter.units))
                            datacenter.units = [];
                        _.each(topology.serviceContainers, function(serviceContainer) {
                            _.each(serviceContainer.relationElements, function(relationElement) {
                                if (relationElement.unit.datacenterId === datacenter.id) {
                                    if (!(_.where(datacenter.units, {id: relationElement.unit.id}).length > 0)) {
                                        relationElement.unit.serviceContainerId = serviceContainer.id;
                                        datacenter.units.push(relationElement.unit);

                                    }

                                    _.each(datacenter.units, function(unit) {
                                        if (angular.isUndefined(unit.services))
                                            unit.services = [];
                                        if (unit.id === relationElement.unit.id)
                                            if (!(_.where(unit.services, {id: relationElement.serviceInstance.id}).length > 0)) {
                                                unit.services.push(relationElement.serviceInstance);

                                            }
                                    });
                                }
                            });
                        });
                    });

                    return tmp;
                }).then(function(obj) {

                    lines = [];
                    _.each(obj.links, function(link) {
                        var source, target;
                        var found = {
                            source: false,
                            target: false
                        };
                        _.each(obj.datacenters, function(datacenter) {
                            _.each(datacenter.switch.ports, function(port) {
                                if (link.source.id === port.id) {
                                    source = datacenter;
                                    found.source = true;
                                    found.sourceV = source;
                                }
                                else {
                                    if (!found.source)
                                        _.each(obj.switches, function(switches) {
                                            _.each(switches.ports, function(port) {

                                                if (link.source.id === port.id) {
                                                    source = switches;
                                                    found.source = true;
                                                    found.sourceV = source;
                                                }
                                            });
                                        });
                                    if (!found.source)
                                        _.each(obj.accesspoints, function(accesspoints) {
                                            _.each(accesspoints.ports, function(port) {

                                                if (link.source.id === port.id) {
                                                    source = accesspoints;
                                                    found.source = true;
                                                    found.sourceV = source;
                                                }
                                            });
                                        });
                                }
                                if (link.target.id === port.id) {
                                    target = datacenter;
                                    found.target = true;
                                    found.targetV = target;
                                }
                                else {
                                    if (!found.target)
                                        _.each(obj.switches, function(switches) {
                                            _.each(switches.ports, function(port) {
                                                if (link.target.id === port.id) {
                                                    target = switches;
//                                     
                                                    found.target = true;
                                                    found.targetV = target;

                                                }
                                            });
                                        });
                                    if (!found.target)
                                        _.each(obj.accesspoints, function(accesspoints) {
                                            _.each(accesspoints.ports, function(port) {
                                                if (link.target.id === port.id) {
                                                    target = accesspoints;
//                                     
                                                    found.target = true;
                                                    found.targetV = target;

                                                }
                                            });
                                        });
                                }
                            });
                        });
                        if (found.source && found.target)
                            lines.push(found);
                    });
                    var arrayNode = [];
                    var accespointArray = [];
                    var miserables = {};
                    miserables.nodes = [];
                    miserables.links = [];
                    var index = 0;
                    var dCArray = [];
                    var sWArray = [];

                    for (var i = 0; i < lines.length; i++) {
                        if (lines[i].sourceV.typeJs === 'datacenters' || lines[i].targetV.typeJs === 'datacenters')
                        {
                            dCArray.push(lines[i]);
                        }
                        if (lines[i].sourceV.typeJs === 'switches' && lines[i].targetV.typeJs === 'switches')
                            sWArray.push(lines[i]);
                    }
                    lines = dCArray.concat(sWArray);

                    console.log(lines);

                    _.each(lines, function(line) {
                        if (line.sourceV.typeJs === 'accesspoints')
                            accespointArray.push(line.sourceV);
                        else if (line.targetV.typeJs === 'accesspoints')
                            accespointArray.push(line.targetV);
                        var node = {
                            name: line.sourceV.id,
                            group: line.sourceV.typeJs,
                            obj: line.sourceV
                        };
                        if (!_.contains(arrayNode, line.sourceV.id)) {
                            arrayNode.push(line.sourceV.id);
                            miserables.nodes[index++] = node;
                        }

                        node = {
                            name: line.targetV.id,
                            group: line.targetV.typeJs,
                            obj: line.targetV
                        };
                        if (!_.contains(arrayNode, line.targetV.id)) {
                            arrayNode.push(line.targetV.id);
                            miserables.nodes[index++] = node;

                        }
                        var link = {
                            "source": line.sourceV.id,
                            "target": line.targetV.id
                        };
                        miserables.links.push(link);
                    });

                    _.each(obj.datacenters, function(datacenter) {
                        var node = {
                            name: datacenter.id,
                            group: datacenter.typeJs,
                            obj: datacenter
                        };
                        if (!_.contains(arrayNode, datacenter.id))
                            miserables.nodes.push(node);
                    });

                    $scope.datacenters = [];
                    $scope.switches = [];
                    console.log(miserables);
                    var orderedNodes = [];
                    var arrayNode = [];

                    _.each(miserables.nodes, function(node) {
                        console.log(node);

                        if (node.group === 'datacenters')
                            if (!_.contains(arrayNode, node.name)) {
                                arrayNode.push(node.name);
                                paintDatacenter(node);
                            }
                        if (node.group === 'switches')
                        {
                            if (!_.contains(arrayNode, node.name)) {
                                arrayNode.push(node.name);
                                paintSwitch(node);

                            }

                        }

                    });


                    $scope.linksG = miserables.links;

                    _.each(miserables.links, function(link) {
                        if (!(angular.isUndefined($('#' + link.source).children(0).offset()) || angular.isUndefined($('#' + link.target).children(0).offset()))) {
                            paintPath(link.source, link.target);
                        }
                    });

                });
            }

            $scope.removeId = function(string) {
                var split = string.split('-');
                var name = '';
                for (var i = 0; i < split.length; i++)
                {
                    var isnum = /^\d+$/.test(split[i]);
                    if (!isnum)
                        name += split[i] + '-';
                }
                return name.substring(0, name.length - 1);

            };

            var unitsInDc = 0;
            function paintDatacenter(node) {

                var html = '<div id = "' + node.obj.id + '" class="dataC " ng-show="vim-instances[' + $scope.datacenters.length + ']"><div class="vm" ng-repeat="unit in vim-instances[' + $scope.datacenters.length + '].units"><div class="service" ng-repeat="service in unit.services">{{service.instanceName}}</div><div class="footer"><a href="#/nsrecords/' + $routeParams.topologyid + '/containers/{{unit.serviceContainerId}}">{{removeId(unit.hostname)}}</a></div></div><div class="footer"><a href="#/vim-instances/' + node.obj.id + '">{{vim-instances[' + $scope.datacenters.length + '].name}} : {{vim-instances[' + $scope.datacenters.length + '].location.name}}</a></div></div>';

                var element = angular.element(html);
                $compile(element)($scope);

                $scope.datacenters.push(node.obj);
                if ($('#' + node.obj.id).length === 0)
                    $('#dataC' + $scope.datacenters.length).append(element);
                var localDim = 0;
                angular.forEach($scope.datacenters, function(datacenter) {
                    angular.forEach(datacenter.units, function(unit) {
                        localDim = localDim + unit.services.length;
                    });
                });
                if (localDim > unitsInDc)
                    unitsInDc = localDim;
                console.log(unitsInDc);
                $('#firstRow').css('min-height', 30 * unitsInDc + 'px');
            }

            function paintSwitch(node) {
                var html = '<div  class="col-md-3 col-md-offset-2 switchC" ><div id="' + node.obj.id + '" class="imageSwitch2"><a href="#/switches/' + node.obj.dpid + '"></a></div><div class="footer"><a href="#/switches/' + node.obj.dpid + '">' + node.obj.dpid + '</a></div></div>';
                var element = angular.element(html);

                $compile(element)($scope);
                if ($('#' + node.obj.id).length === 0)
                    $('#switchIDs').append(element);

            }

            $scope.arrayDiv = [];
            function paintPath(source, target) {
                var docWidth = $(document).width();
                var offsetTop = 150;

                if (docWidth > 752)
                    var offsetLeft = 255;
                else
                    var offsetLeft = 0;
                var off1 = getOffset(document.getElementById(source));
                var off2 = getOffset(document.getElementById(target));

                var x1 = off1.left + off1.width / 2 - offsetLeft;
                var y1 = off1.top - offsetTop;

                var x2 = off2.left + off2.width / 2 - offsetLeft;
                var y2 = off2.top - offsetTop;

                // distance
                var length = Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)));
                // center
                var cx = ((x1 + x2) / 2) - (length / 2);
                var cy = ((y1 + y2) / 2) - (5 / 2);
                // angle
                var angle = Math.atan2((y1 - y2), (x1 - x2)) * (180 / Math.PI);

                var id = 'line_' + source + '-' + target;
                var htmlLine = "<div id='" + id + "'  class='line' style='padding:0px; margin:0px;  position:absolute; left:" + cx + "px; top:" + cy + "px; width:" + length + "px; -moz-transform:rotate(" + angle + "deg); -webkit-transform:rotate(" + angle + "deg); -o-transform:rotate(" + angle + "deg); -ms-transform:rotate(" + angle + "deg); transform:rotate(" + angle + "deg);' /></div>";

                $("#staticInfrastructure").append(htmlLine);

            }


            function getOffset(el) {
                var _x = 0;
                var _y = 0;
                var _w = el.offsetWidth | 0;
                var _h = el.offsetHeight | 0;
                while (el && !isNaN(el.offsetLeft) && !isNaN(el.offsetTop)) {
                    _x += el.offsetLeft - el.scrollLeft;
                    _y += el.offsetTop - el.scrollTop;
                    el = el.offsetParent;
                }
                return {top: _y, left: _x, width: _w, height: _h};
            }

        });
