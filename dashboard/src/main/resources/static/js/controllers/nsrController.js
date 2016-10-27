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

var app = angular.module('app').controller('NsrCtrl', function ($scope, $http, $compile, $cookieStore, $routeParams, http, serviceAPI, topologiesAPI, AuthService, $location, $interval) {

    var baseUrl = $cookieStore.get('URL') + "/api/v1/";
    var url = baseUrl + 'ns-records/';
    var urlVNFD = baseUrl + 'vnf-descriptors/';
    var urlLog = baseUrl + 'logs/';


    loadTable();


    $scope.textTopologyJson = '';
    $scope.file = '';
    $scope.alerts = [];
    $scope.lastActions = {};

    $scope.getLastHistoryLifecycleEvent = function (vnfs) {

        angular.forEach(vnfs, function (vnf, i) {
            console.log(vnf.lifecycle_event_history);
            var result;
            var timestamp = "0";
            // console.log("calculating...");
            angular.forEach(vnf.lifecycle_event_history, function (event_history, i) {
                console.log(event_history);
                console.log(event_history.executedAt + " >= " + timestamp);
                if (event_history.executedAt.localeCompare(timestamp) >= 0) {
                    timestamp = event_history.executedAt;
                    result = event_history;
                    // console.log("Found! " + event_history)
                }
            });
            $scope.lastActions[vnf.id] = "" + result.event + ":" + result.description;
        });
        console.log("lastActions");
        console.log($scope.lastActions);
    };

    $scope.tabs = [
        {active: true},
        {active: false},
        {active: false}
    ];

    $scope.addVNFCIModal = function (data) {
        $scope.vnfrSelected = angular.copy(data);
        $('#addVNFCItoVDU').modal('show');
    };
    $scope.addVNFCI = function () {
        //console.log($scope.connection_points)
        http.post(url + $routeParams.nsrecordId + '/vnfrecords/' + $scope.vnfrSelected.id + '/vdunits/vnfcinstances', {"connection_point": $scope.connection_points})
            .success(function (response) {
                showOk('Added a Virtual Network Function Component Instance.');
                loadTable();
            })
            .error(function (data, status) {
                showError(status, data);
            });
    };
    $scope.removeVNFCI = function (data) {
        http.delete(url + $routeParams.nsrecordId + '/vnfrecords/' + data.id + '/vdunits/vnfcinstances')
            .success(function (response) {
                showOk('Removed a Virtual Network Function Component Instance.');
                loadTable();
            })
            .error(function (data, status) {
                showError(status, data);
            });
    };

    $scope.removeVNFCItoVDU = function (vdu) {
        http.delete(url + $routeParams.nsrecordId + '/vnfrecords/' + $routeParams.vnfrecordId + '/vdunits/' + vdu.id + '/vnfcinstances')
            .success(function (response) {
                showOk('Removed the Virtual Network Function Component Instance to Vdu with id: ' + vdu.id + '.');
                loadTable();
            })
            .error(function (data, status) {
                showError(status, data);
            });
    };

    $scope.addCPtoVNFCI = function () {
        $scope.connection_points.push(angular.copy($scope.connection_point));
    };
    $scope.removeCPtoVNFCI = function (index) {
        $scope.connection_points.splice(index, 1);
    };

    $scope.connection_points = [];
    $scope.connection_point = {
        "floatingIp": "",
        "virtual_link_reference": "private"
    };
    $scope.addVNFCItoVDU = function (vnfr, vdu) {

        $scope.vduSelected = angular.copy(vdu);
        $scope.vnfrSelected = angular.copy(vnfr);
        $('#addVNFCItoVDU').modal('show');
        /*$scope.connectionPoints = {"connection_point": angular.copy(vdu.vnfc[0].connection_point)};
         angular.forEach($scope.connectionPoints.connection_point, function (cp, index) {
         if (!angular.isUndefined(cp.id)) {
         delete cp.id;
         delete cp.version;
         }
         });
         //console.log($scope.connectionPoints);*/

    };

    $scope.addCPtoVDU = function () {
        //console.log($scope.connection_points);
        http.post(url + $routeParams.nsrecordId + '/vnfrecords/' + $routeParams.vnfrecordId + '/vdunits/' + $scope.vduSelected.id + '/vnfcinstances', {"connection_point": $scope.connection_points})
            .success(function (response) {
                showOk('Added a Virtual Network Function Component Instance to Vdu with id: ' + $scope.vduSelected.id + '.');
                loadTable();
            })
            .error(function (data, status) {
                showError(status, data);
            });
    };
    $scope.setFile = function (element) {
        $scope.$apply(function ($scope) {

            var f = element.files[0];
            if (f) {
                var r = new FileReader();
                r.onload = function (element) {
                    var contents = element.target.result;
                    $scope.file = contents;
                };
                r.readAsText(f);
            } else {
                alert("Failed to load file");
            }
        });
    };


    $scope.checkIP = function (str) {
        if (str === '')
            return true;
        else
            return /^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$|^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\-]*[A-Za-z0-9])$|^\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:)))(%.+)?\s*$/.test(str);

    };


    $scope.sendFile = function () {


        $('.modal').modal('hide');
        var postNSD;
        var sendOk = true;
        var type = 'topology';
        if ($scope.file !== '') {
            postNSD = $scope.file;
            if (postNSD.charAt(0) === '<')
                type = 'definitions';
        }

        else if ($scope.textTopologyJson !== '')
            postNSD = $scope.textTopologyJson;
        else if ($scope.topology.serviceContainers.length !== 0)
            postNSD = $scope.topology;

        else {
            alert('Problem with NSD');
            sendOk = false;

        }

        //console.log(postNSD);
        //console.log(type);

        if (sendOk) {
            if (type === 'topology') {
                http.post(url, postNSD)
                    .success(function (response) {
                        showOk('Network Service Descriptors stored!');
                        loadTable();

                        //                        window.setTimeout($scope.cleanModal(), 3000);
                    })
                    .error(function (data, status) {
                        showError(status, data);
                    });
            }

            else {
                http.postXML('/api/rest/tosca/v2/definitions/', postNSD)
                    .success(function (response) {
                        showOk('Definition created!');
                        loadTable();
                        //                        window.setTimeout($scope.cleanModal(), 3000);
                    })
                    .error(function (data, status) {
                        showError(status, data);
                    });
            }
        }

    };


    $scope.isEmpty = function (obj) {
        return angular.equals({}, obj);
    };

    $scope.deleteNSD = function (data) {
        http.delete(url + data.id)
            .success(function (response) {
                showOk('Deleted Network Service Descriptor with id: ' + data.id);
                setTimeout(loadTable, 500);
            })
            .error(function (data, status) {
                showError(status, data);
            });
    };

    if (!angular.isUndefined($routeParams.vnfrecordId))
        $scope.vnfrecordId = $routeParams.vnfrecordId;

    if (!angular.isUndefined($routeParams.vnfdependencyId))
        $scope.vnfdependencyId = $routeParams.vnfdependencyId;

    if (!angular.isUndefined($routeParams.vduId)) {
        $scope.vduId = $routeParams.vduId;
        //console.log($scope.vduId);
    }
    if (!angular.isUndefined($routeParams.vnfciId)) {
        $scope.vnfciId = $routeParams.vnfciId;
        //console.log($scope.vnfciId);
    }


    $scope.$watch('logReq', function (newValue, oldValue) {
        console.log(newValue);
        console.log(oldValue);
    });

    $scope.vnfrName = '';
    $scope.setVNFRName = function (vduId, hostanme) {
        console.log(vduId);
        console.log(hostanme);
        $cookieStore.put('vnfrName', hostanme);
        $scope.vnfrName = hostanme;
        $location.path('nsrecords/' + $routeParams.nsrecordId + '/vnfrecords/' + $routeParams.vnfrecordId + '/vdus/' + vduId);
        $location.replace();
    };


    $scope.loadFullLog = function (hostname) {
        console.log($scope.logReq);
        console.log(hostname);
        http.postLog(urlLog + $routeParams.nsrecordId + '/vnfrecord/' + $cookieStore.get('vnfrName') + '/hostname/' + hostname)
            .success(function (response, status) {
                $('.modal').modal('hide');
                var html = "";
                angular.forEach(response, function (val, i) {
                    html = html + val + '<br/>';
                });
                $scope.log = html;
                $scope.$apply();
            }).error(function (data, status) {
            showError(data, status);
        });
    };

    $scope.logReq = {};
    $scope.loadLog = function (hostname) {
        console.log($scope.logReq);
        console.log(hostname);
        //"{nsrId}/vnfrecord/{vnfrName}/hostname/{hostname}"
        var lines;
        if (!angular.isUndefined($scope.logReq.lines)) {
            http.post(urlLog + $routeParams.nsrecordId + '/vnfrecord/' + $cookieStore.get('vnfrName') + '/hostname/' + hostname, {'lines': $scope.logReq.lines})
                .success(function (response, status) {
                    $('.modal').modal('hide');
                    var html = "";
                    angular.forEach(response, function (val, i) {
                        html = html + val + '<br/>';
                    });
                    $scope.log = html;

                }).error(function (data, status) {
                showError(data, status);
            });
        }
    };
    $scope.returnUptime = function (longUptime) {
        var string = serviceAPI.returnStringUptime(longUptime);
        return string;
    };

    $scope.stringContains = function (k1, k2) {
        return k2.indexOf(k1) > -1;
    };


    $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
    };

    $scope.deleteVNFDependency = function (vnfd) {
        http.delete(url + $scope.nsrinfo.id + '/vnfdependencies/' + vnfd.id)
            .success(function (response) {
                showOk('Deleted VNF Dependecy with id: ' + vnfd.id);
                loadTable();
            })
            .error(function (data, status) {
                console.error('STATUS: ' + status + ' DATA: ' + data);
                showError(status, data);
            });
    };

    $scope.deleteVNFR = function (vnfr) {
        http.delete(url + $scope.nsrinfo.id + '/vnfrecords/' + vnfr.id)
            .success(function (response) {
                showOk('Deleted VNF Record with id: ' + vnfr.id);
                loadTable();
            })
            .error(function (data, status) {
                console.error('STATUS: ' + status + ' DATA: ' + data);
                showError(status, data);
            });
    };

    $scope.deleteNSR = function (data) {
        http.delete(url + data.id)
            .success(function (response) {
                showOk('The removal of the NSR will be done shortly!');
                window.setTimeout(loadTable, 500);
            })
            .error(function (data, status) {
                showError(status, data);
            });
    };

    $scope.resumeNSR = function (data) {
        http.post(url + data.id)
            .success(function (response) {
                showOk('The resume of the NSR will be done shortly!');
                window.setTimeout(loadTable, 500);
            })
            .error(function (data, status) {
                showError(status, data);
            });
    };

    /* -- multiple delete functions Start -- */

    $scope.multipleDeleteReq = function () {
        var ids = [];
        angular.forEach($scope.selection.ids, function (value, k) {
            if (value) {
                ids.push(k);
            }
        });
        //console.log(ids);
        http.post(url + 'multipledelete', ids)
            .success(function (response) {
                showOk('NSR with id: ' + ids.toString() + ' deleted.');
                loadTable();
            })
            .error(function (response, status) {
                showError(response, status);
            });

    };
    $scope.main = {checkbox: false};
    $scope.$watch('main', function (newValue, oldValue) {
        ////console.log(newValue.checkbox);
        ////console.log($scope.selection.ids);
        angular.forEach($scope.selection.ids, function (value, k) {
            $scope.selection.ids[k] = newValue.checkbox;
        });
        //console.log($scope.selection.ids);
    }, true);

    $scope.$watch('selection', function (newValue, oldValue) {
        //console.log(newValue);
        var keepGoing = true;
        angular.forEach($scope.selection.ids, function (value, k) {
            if (keepGoing) {
                if ($scope.selection.ids[k]) {
                    $scope.multipleDelete = false;
                    keepGoing = false;
                }
                else {
                    $scope.multipleDelete = true;
                }
            }

        });
        if (keepGoing)
            $scope.mainCheckbox = false;
    }, true);

    $scope.multipleDelete = true;

    $scope.selection = {};
    $scope.selection.ids = {};
    /* -- multiple delete functions END -- */

    function showError(status, data) {
        $scope.alerts.push({
            type: 'danger',
            msg: 'ERROR: <strong>HTTP status</strong>: ' + status + ' response <strong>data</strong>: ' + JSON.stringify(data)
        });

        $('.modal').modal('hide');
        if (status === 401) {
            //console.log(status + ' Status unauthorized')
            AuthService.logout();
        }
    }


    function showOk(msg) {
        $scope.alerts.push({type: 'success', msg: msg});
        $('.modal').modal('hide');
    }


    $scope.Jsplumb = function () {
        http.get(url + $routeParams.nsrecordId)
            .success(function (response, status) {
                topologiesAPI.Jsplumb(response, 'record');
                //console.log(response);

            }).error(function (data, status) {
            showError(data, status);
        });

    };

    function loadTable() {
        if (angular.isUndefined($routeParams.nsrecordId))
            http.get(url)
                .success(function (response, status) {
                    $scope.nsrecords = response;
                    console.log(response);
                })
                .error(function (data, status) {
                    showError(status, data);

                });
        else
            http.get(url + $routeParams.nsrecordId)
                .success(function (response, status) {
                    $scope.nsrinfo = response;
                    $scope.getLastHistoryLifecycleEvent($scope.nsrinfo.vnfr);
                    $scope.nsrJSON = JSON.stringify(response, undefined, 4);
                    //console.log(response);
                    //topologiesAPI.Jsplumb(response);
                })
                .error(function (data, status) {
                    showError(status, data);
                    //var destinationUrl = '#';
                    //$window.location.href = destinationUrl;
                });


    }

    $scope.startVNFCI = function(vdu, vnfci, vnfr) {
      startObj = {};
      vnfciurl = url + $scope.nsrinfo.id + '/vnfrecords/' + vnfr.id + '/vdunits/' + vdu.id + '/vnfcinstances/' + vnfci.id + '/start';
      //console.log(vnfciurl);
      http.post(vnfciurl, startObj)
          .success(function (response) {
              showOk("Started VNFCI with id" + vnfci.id);
          })
          .error(function (data, status) {
              showError(status, data);
          });
    };

    $scope.stopVNFCI = function(vdu, vnfci, vnfr) {
      startObj = {};
      vnfciurl = url + $scope.nsrinfo.id + '/vnfrecords/' + vnfr.id + '/vdunits/' + vdu.id + '/vnfcinstances/' + vnfci.id + '/stop';
      //console.log(vnfciurl);
      http.post(vnfciurl, startObj)
          .success(function (response) {
              showOk("Stopped VNFCI with id" + vnfci.id);
          })
          .error(function (data, status) {
              showError(status, data);
          });
    };


});
