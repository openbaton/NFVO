var app = angular.module('app').controller('NsdCtrl', function ($scope, $compile, $cookieStore, $routeParams, http, serviceAPI, $window, $route, $interval, $http, topologiesAPI, AuthService) {

    var baseURL = $cookieStore.get('URL') + "/api/v1";

    var url = baseURL + '/ns-descriptors/';
    var urlRecord = baseURL + '/ns-records/';
    var urlVim = baseURL + '/datacenters/';
    var urlVNFD = baseURL + '/vnf-descriptors/';

    loadTable();

    $.fn.bootstrapSwitch.defaults.size = 'mini';

    $('#set-flavor').bootstrapSwitch();


    $('#set-flavor').on('switchChange.bootstrapSwitch', function (event, state) {
        $scope.showSetting = state;
        //console.log($scope.showSetting);
        $scope.$apply(function () {
            $scope.showSetting;
        });

    });


    $scope.nsdToSend = {};
    $scope.textTopologyJson = '';
    $scope.file = '';
    $scope.alerts = [];
    $scope.vimInstances = [];
    http.get(urlVim)
        .success(function (response, status) {
            $scope.vimInstances = response;
            //console.log(response);
        })
        .error(function (data, status) {
            showError(status, data);

        });


    $scope.tabs = [
        {active: true},
        {active: false},
        {active: false}
    ];

    $scope.selection = [];

    $scope.addTONSD = function () {
        $scope.nsdCreateTmp.vnfd.push(angular.copy($scope.selectedVNFD));
        delete $scope.selectedVNFD;
    };

    $scope.saveDependency = function () {
        $scope.nsdCreateTmp.vnf_dependency.push(angular.copy($scope.dependency));
        //console.log($scope.nsdCreateTmp.vnf_dependency);
        //console.log($scope.dependency);

        $('#modalDependency').modal('hide');
    };

    $scope.selectedVNFD;
    $scope.vnfdList = [];

    $scope.dependency = {};
    $scope.dependency.parameters = [];

    $scope.addParam = function (par) {
        $scope.dependency.parameters.push(par);
    };

    $scope.removeParam = function (index) {
        $scope.dependency.parameters.splice(index, 1);
    };

    $scope.addVld = function (vld) {
        $scope.nsdCreateTmp.vld.push({'name': vld});
    };

    $scope.removeVld = function (index) {
        $scope.nsdCreateTmp.vld.splice(index, 1);
    };

    $scope.deleteDependency = function (index) {
        $scope.nsdCreateTmp.vnf_dependency.splice(index, 1);
    };

    $scope.isArray = function (obj) {
        if (angular.isArray(obj) || angular.isObject(obj))
            return false;
        else
            return true;
    };
    $scope.edit = function (obj) {
        $scope.editObj = obj;
    };

    $scope.updateObj = function () {
        http.put(url + $scope.editObj.id, $scope.editObj)
            .success(function (response) {
                showOk('Network Service Descriptor updated!');
                loadTable();
            })
            .error(function (data, status) {
                console.error('STATUS: ' + status + ' DATA: ' + JSON.stringify(data));
                showError(status, JSON.stringify(data));
            });
    };

    $scope.updateVNFD = function () {
        http.put(url + $routeParams.nsdescriptorId + '/vnfdescriptors/' + $scope.editObj.id, $scope.editObj)
            .success(function (response) {
                showOk('VNF Descriptor updated!');
                loadTable();
            })
            .error(function (data, status) {
                console.error('STATUS: ' + status + ' DATA: ' + JSON.stringify(data));
                showError(status, JSON.stringify(data));
            });
    };

    $scope.addNewConfig = function () {
        if (angular.isUndefined($scope.editObj.configurations)) {
            $scope.editObj.configurations = {};
            $scope.editObj.configurations.configurationParameters = [];
        }
        $scope.editObj.configurations.configurationParameters.push({'confKey': '', 'value': ''})
    };
    $scope.removeConfig = function (index) {
        $scope.editObj.configurations.configurationParameters.splice(index, 1);
    };

    $scope.addLifecycleEvent = function (vdu) {
        vdu.lifecycle_event.push({'event': "CONFIGURE", 'lifecycle_events': []})
    };


    $scope.loadVNFD = function () {
        $scope.nsdCreateTmp = {};
        $scope.nsdCreateTmp.name = '';
        $scope.nsdCreateTmp.vendor = '';
        $scope.nsdCreateTmp.version = '';
        $scope.nsdCreateTmp.vnfd = [];
        $scope.nsdCreateTmp.vnf_dependency = [];
        $scope.nsdCreateTmp.vld = [];

        http.get(urlVNFD)
            .success(function (response, status) {
                $scope.vnfdList = response;
                //console.log(response);
                $('#modalCreateNSD').modal('show');
            })
            .error(function (data, status) {
                showError(status, data);
            });
    };

    $scope.toggleSelection = function toggleSelection(image) {
        var idx = $scope.selection.indexOf(image);
        if (idx > -1) {
            $scope.selection.splice(idx, 1);
        }
        else {
            $scope.selection.push(image);
        }
        //console.log($scope.selection);
        $scope.vduCreate.vm_image = $scope.selection;
    };


    $scope.deleteVDU = function (index) {
        $scope.vnfdCreate.vdu.splice(index, 1);
    };


    $scope.deleteVNFDependency = function (vnfd) {
        http.delete(url + $scope.nsdinfo.id + '/vnfdependencies/' + vnfd.id)
            .success(function (response) {
                showOk('Deleted VNF Dependecy with id: ' + vnfd.id);
                loadTable();
            })
            .error(function (data, status) {
                console.error('STATUS: ' + status + ' DATA: ' + data);
                showError(status, JSON.stringify(data));
            });

    };


    $scope.deleteVNFD = function (vnfd) {
        http.delete(url + $scope.nsdinfo.id + '/vnfdescriptors/' + vnfd.id)
            .success(function (response) {
                showOk('Deleted VNF Descriptors with id: ' + vnfd.id);
                loadTable();
            })
            .error(function (data, status) {
                console.error('STATUS: ' + status + ' DATA: ' + data);
                showError(status, JSON.stringify(data));
            });
    };


    if (!angular.isUndefined($routeParams.vnfdescriptorId))
        $scope.vnfdescriptorId = $routeParams.vnfdescriptorId;


    if (!angular.isUndefined($routeParams.vnfdependencyId))
        $scope.vnfdependencyId = $routeParams.vnfdependencyId;

    if (!angular.isUndefined($routeParams.vduId)) {
        $scope.vduId = $routeParams.vduId;
        //console.log($scope.vduId);
    }


    $scope.sendNSDCreate = function (nsdCreate) {
        $('.modal').modal('hide');
        //console.log($scope.nsdCreateTmp);
        http.post(url, $scope.nsdCreateTmp)
            .success(function (response) {
                showOk('Network Service Descriptor stored!');
                loadTable();
            })
            .error(function (data, status) {
                console.error('STATUS: ' + status + ' DATA: ' + JSON.stringify(data));
                showError(status, JSON.stringify(data));
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

    $scope.showTab = function (value) {
        return (value > 0);
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

        $scope.toggle = false;
        $scope.file !== '';
        //        $scope.packages = [];
    };


    $scope.isEmpty = function (obj) {
        if (angular.equals({}, obj))
            return true;
        else if (angular.equals([], obj))
            return true;
        else return false;
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

    $scope.launchOption = function (data) {
        $scope.nsdToSend = data;
        //$('#madalLaunch').modal('show');
        $scope.launch();
    };

    $scope.launch = function () {
        //console.log($scope.nsdToSend);
        http.post(urlRecord + $scope.nsdToSend.id)
            .success(function (response) {
                showOk("Created Network Service Record from Descriptor with id: \<a href=\'\#nsrecords\'>" + $scope.nsdToSend.id + "<\/a>");
            })
            .error(function (data, status) {
                showError(status, data);
            });
    };

    $scope.Jsplumb = function () {
        http.get(url + $routeParams.nsdescriptorId)
            .success(function (response, status) {
                topologiesAPI.Jsplumb(response, 'descriptor');
                //console.log(response);

            }).error(function (data, status) {
            showError(status, data);
        });

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
                showOk('Items with id: ' + ids.toString() + ' deleted.');
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
        if (status === 400)
            $scope.alerts.push({
                type: 'danger',
                msg: 'ERROR: <strong>HTTP status</strong>: ' + status + ' response <strong>data</strong>: ' + "Bad request: your json is not well formatted"
            });

        else
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

    function loadTable() {

        if (angular.isUndefined($routeParams.nsdescriptorId))
            http.get(url)
                .success(function (response, status) {
                    $scope.nsdescriptors = response;
                    //console.log(response);
                })
                .error(function (data, status) {
                    showError(status, data);

                });
        else
            http.get(url + $routeParams.nsdescriptorId)
                .success(function (response, status) {
                    $scope.nsdinfo = response;
                    $scope.nsdJSON = JSON.stringify(response, undefined, 4);
                    //console.log(response);
                })
                .error(function (data, status) {
                    showError(status, data);
                });
    }

});

