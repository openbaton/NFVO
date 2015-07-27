angular.module('app')
        .factory('topologiesAPI', function(http, $q, serviceAPI) {

            var topologies = {};
            var relations = serviceAPI.getRelations();
            /*
             * Function for drawing the infrastructure topology
             * 
             * @param {int|null} showChains = 1 It will be painted some dashed green lines on the chain path  
             * @param {int} chainId the id of chain
             * @returns {D3 infrastracture topology graph}
             */

            topologies.getTopologyD3 = function(showChains, chainId) {
                var lines = [];
                var links = http.get("/api/rest/admin/v2/links"),
                        datacenters = http.get("/api/rest/admin/v2/vim-instances"),
                        accesspoints = http.get("/api/rest/admin/v2/accesspoints"),
                        chains = http.get("/api/rest/crosslayer/v1/chains"),
                        switches = http.get("/api/rest/admin/v2/switches");
                $q.all([links, datacenters, accesspoints, switches, chains]).then(function(result) {
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
//                                                    lines.push(new google.maps.LatLng(target.latitude, target.longitude));
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
//                                                    lines.push(new google.maps.LatLng(target.latitude, target.longitude));
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
                            miserables.nodes.push(node);
                        }

                        node = {
                            name: line.targetV.id,
                            group: line.targetV.typeJs,
                            obj: line.targetV
                        };
                        if (!_.contains(arrayNode, line.targetV.id)) {
                            arrayNode.push(line.targetV.id);
                            miserables.nodes.push(node);
                        }
                        var link = {
                            "source": _.lastIndexOf(arrayNode, line.sourceV.id),
                            "target": _.lastIndexOf(arrayNode, line.targetV.id),
                            "value": 10
                        };
                        miserables.links.push(link);


                    });

                    if (miserables.links.length === 0 && miserables.nodes.length === 0) {
                        _.each(obj.datacenters, function(datacenter) {
                            var node = {
                                name: datacenter.id,
                                group: datacenter.typeJs,
                                obj: datacenter
                            };
                            miserables.nodes.push(node);
                        });
                    }




//draw the chains

                    if (showChains === 1) {
                        var arraySwitch = [];
                        var chainVar;

                        _.each(obj.chains, function(chain) {
                            console.log(chainId);
                            console.log(chain);

                            if (chain.id == chainId) {
                                chainVar = chain;
                                console.log(chainVar);

                                _.each(chain.route.route, function(route) {
                                    if (!_.contains(arraySwitch, route.dpid))
                                        arraySwitch.push(route.dpid);

                                });
                            }

                            console.log(chainVar);

                            _.each(miserables.nodes, function(node) {
                                if (node.group !== 'switches') {
//                                    if (!checkIP(node.obj, chainVar)) {
//                                        console.log(chainVar);
//
//                                    }

                                }
                            });

                        });


                        _.each(miserables.links, function(links) {
                            if (miserables.nodes[links.source].group === 'switches')
                                if (_.contains(arraySwitch, miserables.nodes[links.source].obj.dpid)) {
                                    if (_.contains(arraySwitch, miserables.nodes[links.target].obj.dpid)) {
                                        links.value = 1;
                                    }
                                }

                            if (miserables.nodes[links.source].group === 'accesspoints' || miserables.nodes[links.target].group === 'accesspoints')
                                if (_.contains(arraySwitch, miserables.nodes[links.source].obj.dpid) || _.contains(arraySwitch, miserables.nodes[links.target].obj.dpid))
                                    _.each(obj.chains, function(chain) {
                                        if (miserables.nodes[links.source].group === 'accesspoints' || ((miserables.nodes[links.source].obj.ip === chain.dstIP) || (miserables.nodes[links.source].obj.ip === chain.srcIP))) {
                                            links.value = 1;
                                        }
                                        else
                                        if (miserables.nodes[links.target].group === 'accesspoints' || ((miserables.nodes[links.target].obj.ip === chain.dstIP) || (miserables.nodes[links.target].obj.ip === chain.srcIP))) {

                                            links.value = 1;
                                        }
                                    });

                            if (miserables.nodes[links.source].group === 'datacenters' || miserables.nodes[links.target].group === 'datacenters')
                                if (_.contains(arraySwitch, miserables.nodes[links.source].obj.dpid) || _.contains(arraySwitch, miserables.nodes[links.target].obj.dpid))
                                    _.each(obj.chains, function(chain) {
                                        if (miserables.nodes[links.source].group === 'datacenters' || ((miserables.nodes[links.source].obj.ip === chain.dstIP) || (miserables.nodes[links.source].obj.ip === chain.srcIP))) {

                                            links.value = 1;
                                        }
                                        else
                                        if (miserables.nodes[links.target].group === 'datacenters' || ((miserables.nodes[links.target].obj.ip === chain.dstIP) || (miserables.nodes[links.target].obj.ip === chain.srcIP))) {

                                            links.value = 1;
                                        }
                                    });
                        });

//                    console.log(accespoinObj);

                    }
                    else
                        initializeMap(miserables);

                    console.log(miserables);
                    drawD3(miserables);
                });
            };

            var line;
            var offset = {};
            var center;
            var marker;
            var map;


            function initializeMap(object) {
//                console.log(object);
                var iconType = {};
                iconType['datacenters'] = 'img/server_multiple_48x48.png';
                iconType['switches'] = 'img/switch.png';
//                loadMarkers();
                center = new google.maps.LatLng(52.395715, 4.888916);
                var mapProp = {
                    center: center,
                    zoom: 4,
                    mapTypeId: google.maps.MapTypeId.ROADMAP
                };
                map = new google.maps.Map(document.getElementById("map_canvas"), mapProp);
                _.each(object.nodes, function(node) {
                    if (!angular.isUndefined(node.obj.location)) {
                        var infowindow = new google.maps.InfoWindow();
                        marker = new google.maps.Marker({
                            map: map,
                            icon: iconType[node.group],
                            draggable: false,
//                            animation: google.maps.Animation.DROP,
                            position: new google.maps.LatLng(node.obj.location.latitude, node.obj.location.longitude)
                        });
                        google.maps.event.addListener(marker, 'click', (function(marker, node) {
                            return function() {
//                                infowindow.setContent('<a href="#'+$scope.markers[i].type+'/'+$scope.markers[i].id+'">'+$scope.markers[i].name+'</a>');
                                infowindow.setContent(node.obj.id);
                                infowindow.open(map, marker);
                            };
                        })(marker, node));
                    }
                });

                _.each(object.links, function(link) {
                    if (!angular.isUndefined(object.nodes[link.source].obj.location) && !angular.isUndefined(object.nodes[link.target].obj.location)) {

                        line = new google.maps.Polyline({
                            path: [
                                new google.maps.LatLng(object.nodes[link.source].obj.location.latitude, object.nodes[link.source].obj.location.longitude),
                                new google.maps.LatLng(object.nodes[link.target].obj.location.latitude, object.nodes[link.target].obj.location.longitude)
                            ],
                            strokeColor: "red",
                            strokeOpacity: 1,
                            strokeWeight: 2,
                            icons: [{
                                    icon: {
                                        path: 'M 0,-3 0,3',
//                                        strokeColor: 'red',
                                        strokeOpacity: 10.0
                                    },
                                    offset: '0px',
                                    repeat: '20px'
                                }]
                        });
                        line.setMap(map);
                        animateLine(line);
                    }
                });

                resizeMap();
            }

            function resizeMap() {

                google.maps.event.addListenerOnce(map, 'idle', function() {
                    google.maps.event.trigger(map, 'resize');
                    map.setCenter(center);
                });


            }
            function animateLine(line) {
                offset = {
                    'line': 0
                };
                window.setInterval(function() {
                    if (offset['line'] === 200) {
                        offset['line'] = 0;
                    } else {
                        offset['line']++;
                    }
                    var icons = line.get('icons');
                    icons[0].offset = offset['line'] + 'px';
                    line.set('icons', icons);
                }, 50);
            }

            function checkIP(obj, chain) {
                var ip = false;

                if (!angular.isUndefined(obj.ip)) {
                    if (obj.ip === chain.srcIP || obj.ip === chain.dstIP)
                        ip = true;
                }
                else {
                    if (obj.switch.ip === chain.srcIP || obj.switch.ip === chain.dstIP)
                        ip = true;
                }
                return ip;
            }


            function drawD3(miserables) {

                var width = 1000,
                        height = 500;
                var force = d3.layout.force()
                        .gravity('0.1')
                        .charge(-800)
                        .linkDistance(120)
                        .size([width, height]);
                var svg = d3.select("#graphicInfrastructure").append("svg")
                        .attr("width", width)
                        .attr("height", height);
                var div = d3.select("#graphicInfrastructure").append("div")
                        .attr("class", "tooltip")
                        .style("opacity", 0);

                force
                        .nodes(miserables.nodes)
                        .links(miserables.links)
                        .on("tick", tick)
                        .start();

                //bounding box graph
                function tick() {
                    node.attr("cx", function(d) {
                        return d.x = Math.max(15, Math.min(width - 15, d.x));
                    })
                            .attr("cy", function(d) {
                                return d.y = Math.max(15, Math.min(height - 15, d.y));
                            });
                    link.attr("x1", function(d) {
                        return d.source.x;
                    })
                            .attr("y1", function(d) {
                                return d.source.y;
                            })
                            .attr("x2", function(d) {
                                return d.target.x;
                            })
                            .attr("y2", function(d) {
                                return d.target.y;
                            });
                }

                var link = svg.selectAll(".link")
                        .data(miserables.links)
                        .enter().append("line")
                        .attr("class", function(d) {
                            return "link" + d.value + "";
                        });


                var node = svg.selectAll(".node")
                        .data(miserables.nodes)
                        .enter().append("g")
                        .attr("class", "node")
                        .on("mouseover", function(d) {
                            div.transition()
                                    .duration(200)
                                    .style("opacity", .9);
                            div.html(
                                    returnDiv(d.obj)
                                    )
                                    .style("left", (d3.event.pageX - 200) + "px")
                                    .style("top", (d3.event.pageY - 350) + "px");
                        })
                        .on("mouseout", function(d) {
                            div.transition()
                                    .duration(5000)
                                    .style("opacity", 0);
                        })
                        .call(force.drag);
                node.append("image")
                        .attr("xlink:href",
                                function(d) {
                                    if (d.group === 'datacenters')
                                        return "img/server_multiple_48x48.png";
                                    if (d.group === 'switches')
                                        return "img/switch_22x22.png";
                                    if (d.group === 'accesspoints')
                                        return "img/access_point.png";
                                    if (d.group === 'phone')
                                        return "img/phone.png";

                                })
                        .attr("x", -30)
                        .attr("y", -30)
                        .attr("width", 60)
                        .attr("height", 60);
                node.append("title")
                        .attr("dx", 12)
                        .attr("dy", ".35em")
                        .text(function(d) {
                            return d.name;
                        });
                force.on("tick", function() {
                    link.attr("x1", function(d) {
                        return d.source.x;
                    })
                            .attr("y1", function(d) {
                                return d.source.y;
                            })
                            .attr("x2", function(d) {
                                return d.target.x;
                            })
                            .attr("y2", function(d) {
                                return d.target.y;
                            });
                    node.attr("transform", function(d) {
                        return "translate(" + d.x + "," + d.y + ")";
                    });
                });
            }

            ///////////////////////////////////////////////////


            function returnDiv(obj) {
                if (obj.typeJs === 'datacenters')
                    return '<b><a href="#vim-instances/' + obj.id + '">Datacenter: ' + obj.name + '</a></b>' +
                            '<br/>switch: ' + obj.switch.dpid + '::' + obj.switch.ip +
                            '<br/>ports: ' + obj.switch.ports.length +
                            '<br/>type: ' + obj.type;
                if (obj.typeJs === 'switches')
                    return '<b><a href="#switches/' + obj.dpid + '">Switch: ' + obj.dpid + '</a></b>' +
                            '<br/>id: ' + obj.id +
                            '<br/>ip: ' + obj.ip +
                            '<br/>tpcPort: ' + obj.tcpPort +
                            '<br/>ports: ' + obj.ports.length;
                if (obj.typeJs === 'accesspoints')
                    return '<b>Access point: ' + obj.dpid + '</b>' +
                            '<br/>id: ' + obj.id +
                            '<br/>ip: ' + obj.ip +
                            '<br/>mac: ' + obj.mac +
                            '<br/>ports: ' + obj.ports.length;

            }

            ///////////////////////////////////////////////////





            //spectrum graph
            function drawGraphP() {
                var n = 40,
                        random = d3.random.normal(.2, .1),
                        data = d3.range(n).map(random);


                var margin = {top: 20, right: 20, bottom: 20, left: 40},
                width = 960 - margin.left - margin.right,
                        height = 500 - margin.top - margin.bottom;
                var x = d3.scale.linear()
                        .domain([0, n - 1])
                        .range([0, width]);
                var y = d3.scale.linear()
                        .domain([0, 1])
                        .range([height, 0]);
                var line = d3.svg.line()
                        .x(function(d, i) {
                            return x(i);
                        })
                        .y(function(d, i) {
                            return y(d);
                        });
                var svg = d3.select("#graphicInfrastructure3").append("svg")
                        .attr("width", width + margin.left + margin.right)
                        .attr("height", height + margin.top + margin.bottom)
                        .append("g")
                        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
                svg.append("defs").append("clipPath")
                        .attr("id", "clip")
                        .append("rect")
                        .attr("width", width)
                        .attr("height", height);
                svg.append("g")
                        .attr("class", "x axis")
                        .attr("transform", "translate(0," + y(0) + ")")
                        .call(d3.svg.axis().scale(x).orient("bottom"));
                svg.append("g")
                        .attr("class", "y axis")
                        .call(d3.svg.axis().scale(y).orient("left"));
                var path = svg.append("g")
                        .attr("clip-path", "url(#clip)")
                        .append("path")
                        .datum(data)
                        .attr("class", "line")
                        .attr("d", line);
                tick();
                function tick() {

                    // push a new data point onto the back
                    data.push(random());

                    // redraw the line, and slide it to the left
                    path
                            .attr("d", line)
                            .attr("transform", null)
                            .transition()
                            .duration(500)
                            .ease("linear")
                            .attr("transform", "translate(" + x(-1) + ",0)")
                            .each("end", tick);
                    // pop the old data point off the front
                    data.shift();
                }
            }

            function removeId(string) {
                var split = string.split('-');
                return split[0] + '-' + split[1];
            }


            /* Function for painting the Topology graph 
             * 
             * @param {type} topology
             * @returns {undefined} paints the topology with services and relations
             */
            topologies.Jsplumb = function(topology) {

                $('#graphContainer').html('');
//            jsPlumb.Defaults.Container = $("#graphContainer");
                _.each(topology.serviceContainers, function(container) {
                    _.each(container.relationElements, function(rel) {

                        if ($("#" + rel.id).length === 0) {
                            var top = Math.floor((Math.random() * 400) + 1);
                            var left = Math.floor((Math.random() * 600) + 1);
                            var u = $('<div title = "' + rel.unit.hostname + '"><b>' + rel.serviceInstance.instanceName + '</b><br/>' +
                                    '' + removeId(rel.unit.hostname) + '</div>')
                                    .attr('id', rel.id)
                                    .attr('style', 'top:' + top + 'px;left:' + left + 'px')
                                    .addClass('unit');
//                            u.append('<div class="ep"></div>');
//                            u.tooltip({
//                                'placement': 'auto',
//                                'title': rel.unit.hostname
//                            });
                            $('#graphContainer').append(u).fadeIn();
                        }


                    });
                });
                // // setup some defaults for jsPlumb.
                jsPlumb.importDefaults({
                    Endpoint: ["Dot", {
                            radius: 2
                        }],
                    HoverPaintStyle: {
                        strokeStyle: "#1e8151",
                        lineWidth: 2
                    }

                });
                jsPlumb.makeSource($(".unit"), {
                    filter: ".ep",
                    anchor: "Continuous",
                    connector: ["StateMachine", {
                            curviness: 20
                        }],
                    connectorStyle: {
                        strokeStyle: "#5c96bc",
                        lineWidth: 2,
                        outlineColor: "transparent",
                        outlineWidth: 4
                    },
                    maxConnections: 10,
                    onMaxConnections: function(info, e) {
                        alert("Maximum connections (" + info.maxConnections + ") reached");
                    }
                });
//        jsPlumb.makeTarget($(".unit"), {
//            dropOptions: {
//                hoverClass: "dragHover"
//            },
//            anchor: "Continuous"
//        });
                var connectionCatalogue = catalogueRelForJsPlumb(topology);
//                var connectionCatalogue = serviceAPI.getRelations();

                $.each(connectionCatalogue, function(i, r) {

                    jsPlumb.connect({
                        source: $('#' + r.source),
                        target: $('#' + r.target),
                        anchor: "Continuous"
                    });

                });
                jsPlumb.draggable($(".unit"), {
                    containment: "#graphContainer"
                });


                // set jsplumb properties

//        $('#graphContainer').data('topologyId', id);




                function catalogueRelForJsPlumb(topology) {

                    var catRel = [];
                    http.syncGet('/api/rest/orchestrator/v2/relations').then(function(response) {
                        _.each(response, function(r) {
                            $('.unit').each(function(i) {
                                var id = this.id;
                                var s = r.source;
                                var t = r.target;
                                if (id == r.source || id == r.target)
                                {
                                    if (!checkExist(catRel, r)) {
                                        catRel.push(r);
                                        jsPlumb.connect({
                                            source: $('#' + r.source),
                                            target: $('#' + r.target),
                                            anchor: "Continuous"
                                        });
                                    }
                                }
                            });
//                  
                        });

                    });
                    return catRel;
                }


                function returnServiceId(id, topology) {
                    var serId = 0;
                    _.each(topology.serviceContainers, function(serviceContainer) {
                        _.each(serviceContainer.relationElements, function(relationElement) {
                            if (relationElement.id === id)
                            {
                                serId = relationElement.id;
                            }
                        });
                    });
                    return serId;
                }


                /*
                 * Function for finding the Relation between all services in a Topology deleting
                 * the double lines
                 */
                function checkExist(catRel, rel) {
                    var result = false;
                    for (var i = 0; i < catRel.length; i++) {
                        var r = catRel[i];
                        if (r.source === rel.target && r.target === rel.source
                                ||
                                r.source === rel.source && r.target === rel.target) {
                            result = true;
                        }
                    }
                    return result;
                }

            };




            return topologies;
        });

