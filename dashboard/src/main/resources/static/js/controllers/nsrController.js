var app = angular.module('app').controller('NsrCtrl', function ($scope, $http, $compile, $cookieStore, $routeParams, http, serviceAPI, topologiesAPI, AuthService) {

    var url = $cookieStore.get('URL') + "/api/v1/ns-records/";

    loadTable();


    $scope.textTopologyJson = '';
    $scope.file = '';
    $scope.alerts = [];


    $scope.tabs = [
        {active: true},
        {active: false},
        {active: false}
    ];

    $scope.selectedVDU=function(vdu){
        $scope.vduSelected=vdu;
    };
    $scope.addVNFCI = function () {
        console.log($scope.vnfci);
        http.post(url + $routeParams.nsrecordId + '/vnfrecords/' + $scope.vnfrSelected.id + '/vdunits/vnfcinstances',$scope.vnfci)
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
    $scope.addVNFCItoVDU = function () {
        http.post(url + $routeParams.nsrecordId + '/vnfrecords/' + $routeParams.vnfrecordId + '/vdunits/' + $scope.vduSelected.id + '/vnfcinstances',$scope.vnfci)
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

    $scope.editCP = [];
    $scope.vnfci = {connection_point: []};
    $scope.cp = {
        "floatingIp": "random",
        "virtual_link_reference": "private"
    };
    $scope.addNewCP = function () {
        $scope.vnfci.connection_point.push(angular.copy($scope.cp))
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

        console.log(postNSD);
        console.log(type);

        if (sendOk) {
            if (type === 'topology') {
                http.post(url, postNSD)
                    .success(function (response) {
                        showOk('Network Service Descriptors stored!');
                        loadTable();
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
                loadTable();
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
        console.log($scope.vduId);
    }


    $scope.returnUptime = function (longUptime) {
        var string = serviceAPI.returnStringUptime(longUptime);
        return string;
    };

    $scope.stringContains = function (k1, k2) {
        return k2.indexOf(k1) > -1;
    };

    $scope.selectedVNFR = function (vnfr) {
        $scope.vnfrSelected=vnfr;
        $scope.virtual_links = [];
        $scope.floatingIps = [];
        $scope.floatingIps.push("random");

        $.each(vnfr.vdu, function (ind, vdu) {
            $.each(vdu.vnfc_instance, function (ind, vnfci) {
                $.each(vnfci.floatingIps, function (ind, floatingIp) {
                    $scope.floatingIps.push(floatingIp.ip)
                })
            })
        });
        $.each(vnfr.virtual_link, function (ind, virtual_link) {
            $scope.virtual_links.push(virtual_link.name)
        });
        console.log($scope.floatingIps);
        console.log($scope.virtual_links);
    };
    $scope.deleteCPfromVNFCI = function (index) {
        $scope.vnfci.connection_point.splice(index, 1);
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
                showOk('Network Service Record deleted!');
                loadTable();
            })
            .error(function (data, status) {
                showError(status, data);
            });
    };


    function showError(status, data) {
        $scope.alerts.push({
            type: 'danger',
            msg: 'ERROR: <strong>HTTP status</strong>: ' + status + ' response <strong>data</strong>: ' + JSON.stringify(data)
        });

        $('.modal').modal('hide');
        if (status === 401) {
            console.log(status + ' Status unauthorized')
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
                console.log(response);

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
                    //var destinationUrl = '#';
                    //$window.location.href = destinationUrl;
                });
        else
            http.get(url + $routeParams.nsrecordId)
                .success(function (response, status) {
                    console.log(response);
                    $scope.nsrinfo = response;
                    $scope.nsrJSON = JSON.stringify(response, undefined, 4);

                    //topologiesAPI.Jsplumb(response);
                })
                .error(function (data, status) {
                    showError(status, data);
                    //var destinationUrl = '#';
                    //$window.location.href = destinationUrl;
                });


    }


});

