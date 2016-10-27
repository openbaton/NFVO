/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


angular.module('app')
    .factory('topologiesAPI', function (http, $q, serviceAPI) {

        var topologies = {};


        /* Function for painting the Topology graph
         *
         * @param {type} topology
         * @returns {undefined} paints the topology with packages and relations
         */
        topologies.Jsplumb = function (topology, type) {
            console.log(topology);
            $('#graphContainer').html('');
//            jsPlumb.Defaults.Container = $("#graphContainer");

            if (type === 'record')
                var unit = topology.vnfr;
            else
                var unit = topology.vnfd;

            _.each(unit, function (vnfr) {

                var top = Math.floor((Math.random() * 400) + 1);
                var left = Math.floor((Math.random() * 600) + 1);
                var u = $('<div title = "' + vnfr.name + '"><b>' + vnfr.name + '</b><br/></div>')
                    .attr('id', vnfr.name)
                    .attr('style', 'top:' + top + 'px;left:' + left + 'px')
                    .addClass('unit');
                //u.append('<div class="ep"></div>');
                //u.tooltip({
                //    'placement': 'auto',
                //    'title': vnfr.name
                //});
                $('#graphContainer').append(u).fadeIn();

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
                onMaxConnections: function (info, e) {
                    alert("Maximum connections (" + info.maxConnections + ") reached");
                }
            });

            //var connectionCatalogue = catalogueRelForJsPlumb(topology);

//TODO To fix the problem when the name starts with '<'
            if (type === 'record') {

                _.each(topology.vnf_dependency, function (r) {
                    console.log(_.keys(r.idType));
                    _.each(_.keys(r.idType), function (source) {
                        console.log(_.isString(source));
                        jsPlumb.connect({
                            source: $('#' + source),
                            target: $('#' + r.target),
                            anchor: "Continuous"
                        });

                    });
                });
                jsPlumb.draggable($(".unit"), {
                    containment: "#graphContainer"
                });
            } else {
                _.each(topology.vnf_dependency, function (r) {
                    jsPlumb.connect({
                        source: $('#' + r.source.name),
                        target: $('#' + r.target.name),
                        anchor: "Continuous"
                    });


                });
                jsPlumb.draggable($(".unit"), {
                    containment: "#graphContainer"
                });
            }

            // set jsplumb properties

//        $('#graphContainer').data('topologyId', id);


            /*
             * Function for finding the Relation between all packages in a Topology deleting
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