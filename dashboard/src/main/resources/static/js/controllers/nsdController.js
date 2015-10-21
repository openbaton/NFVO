var app = angular.module('app').controller('NsdCtrl', function ($scope, $compile, $cookieStore, $routeParams, http, serviceAPI, $window, $route, $interval, $http, topologiesAPI, AuthService) {

    var url = '/api/v1/ns-descriptors';
    //var url = 'http://localhost:8080/api/v1/ns-descriptors';
    var urlRecord = '/api/v1/ns-records';
    //var urlRecord = 'http://localhost:8080/api/v1/ns-records';
    var urlVim = '/api/v1/datacenters';
    //var urlVim = 'http://localhost:8080/api/v1/datacenters';


    loadTable();

    $.fn.bootstrapSwitch.defaults.size = 'mini';

    $('#set-flavor').bootstrapSwitch();


    $('#set-flavor').on('switchChange.bootstrapSwitch', function (event, state) {
        $scope.showSetting = state;
        console.log($scope.showSetting);
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
            console.log(response);
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

    $scope.toggleSelection = function toggleSelection(image) {
        var idx = $scope.selection.indexOf(image);
        if (idx > -1) {
            $scope.selection.splice(idx, 1);
        }
        else {
            $scope.selection.push(image);
        }
        console.log($scope.selection);
        $scope.vduCreate.vm_image = $scope.selection;
    };
    $scope.addVDU = function () {
        $http.get('descriptors/vnfd/vdu.json')
            .then(function (res) {
                console.log(res.data);
                $scope.vduCreate = angular.copy(res.data);
            });
        $('#addEditVDU').modal('show');
    };

    $scope.editVDU = function (vnfd, index) {
        $scope.vduCreate = vnfd;
        $scope.vduEditIndex = index;
        $('#addEditVDU').modal('show');
    };

    $scope.addVDUtoVND = function () {
        $('#addEditVDU').modal('hide');
        if (!angular.isUndefined($scope.vduEditIndex)) {
            $scope.vnfdCreate.vdu.splice($scope.vduEditIndex, 1);
            delete $scope.vduEditIndex;
        }
        $scope.vnfdCreate.vdu.push(angular.copy($scope.vduCreate));
    };

    $scope.deleteVDU = function (index) {
        $scope.vnfdCreate.vdu.splice(index, 1);
    };

    $scope.addVNFD = function () {
        $http.get('descriptors/vnfd/vnfd.json')
            .then(function (res) {
                console.log(res.data);
                $scope.vnfdCreate = angular.copy(res.data);
            });
        $('#addEditVNDF').modal('show');
    };

    $scope.editVNFD = function (vnfd, index) {
        $scope.vnfdCreate = vnfd;
        $scope.vnfdEditIndex = index;
        $('#addEditVNDF').modal('show');
    };

    $scope.editDF = function (df, index) {
        $scope.depFlavor = df;
        $scope.dfEditIndex = index;
        $('#modaladdDepFlavour').modal('show');
    };

    $scope.editLEfromVNFD = function (le, index) {
        $scope.lifecycle_event = le;
        $scope.leEditIndex = index;
        $('#modaladdLifecycleEvent').modal('show');
    };


    function paintBackdropModal() {
        var height = parseInt($(window).height()) + 150 * $scope.nsdCreate.vnfd.length;
        console.log('heigh: ' + height + 'px');
        $(".modal-backdrop").height(height)
    }

    $scope.saveVirtualLink = function (vl) {
//console.log(vl);
        var obj = {};
        obj[vl.key] = vl.value;
        $scope.nsdCreate.vld.push(obj);
    };

    $scope.deleteVirtualLink = function (index) {
        $scope.nsdCreate.vld.splice(index, 1);
    };
    $scope.addVNDtoNSD = function () {
        $('#addEditVNDF').modal('hide');
        if (!angular.isUndefined($scope.vnfdEditIndex)) {
            $scope.nsdCreate.vnfd.splice($scope.vnfdEditIndex, 1);
            delete $scope.vnfdEditIndex;
        }
        $scope.nsdCreate.vnfd.push(angular.copy($scope.vnfdCreate));
        paintBackdropModal();
    };

    $scope.deleteVNFDForm = function (index) {
        $scope.nsdCreate.vnfd.splice(index, 1);
    };

    $scope.deleteVNFDependency = function (vnfd) {
        http.delete(url + '/' + $scope.nsdinfo.id + '/vnfdependencies/' + vnfd.id)
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
        http.delete(url + '/' + $scope.nsdinfo.id + '/vnfdescriptors/' + vnfd.id)
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
        console.log($scope.vduId);
    }


    $scope.storeNSDF = function (nsdCreate) {
        $('#modalForm').modal('hide');
        console.log(nsdCreate);
        http.post(url, nsdCreate)
            .success(function (response) {
                showOk('Network Service Descriptor stored!');
                loadTable();
            })
            .error(function (data, status) {
                console.error('STATUS: ' + status + ' DATA: ' + JSON.stringify(data));
                showError(status, JSON.stringify(data));
            });
    };

    $scope.saveValueVMI = function (newValue) {
        console.log(newValue);
        $scope.vduCreate.vm_image.push(newValue);
    };

    $scope.deleteVMI = function (index) {
        $scope.vduCreate.vm_image.splice(index, 1);
    };

    $scope.saveValueMP = function (newValue) {
        console.log(newValue);
        $scope.vduCreate.monitoring_parameter.push(newValue);
    };

    $scope.deleteMP = function (index) {
        $scope.vduCreate.monitoring_parameter.splice(index, 1);
    };
    $scope.saveValueMPfromVNFD = function (newValue) {
        console.log(newValue);
        $scope.vnfdCreate.monitoring_parameter.push(newValue);
    };

    $scope.deleteMPfromVNFD = function (index) {
        $scope.vnfdCreate.monitoring_parameter.splice(index, 1);
    };

    $scope.saveValueMPfromNSD = function (newValue) {
        console.log(newValue);
        $scope.nsdCreate.monitoring_parameter.push(newValue);
    };

    $scope.deleteMPfromNSD = function (index) {
        $scope.nsdCreate.monitoring_parameter.splice(index, 1);
    };

    $scope.saveValueCVDU = function (newValue) {
        console.log(newValue);
        $scope.depFlavor.costituent_vdu.push(newValue);
    };

    $scope.deleteCVDU = function (index) {
        $scope.depFlavor.costituent_vdu.splice(index, 1);
    };

    $scope.saveValueDFC = function (newValue) {
        console.log(newValue);
        $scope.depFlavor.df_constraint.push(newValue);
    };


    $scope.deleteDFC = function (index) {
        $scope.depFlavor.df_constraint.splice(index, 1);
    };

    $scope.saveValueLifeCE = function (newValue) {
        console.log(newValue);
        $scope.lifecycle_event.lifecycle_events.push(newValue);
    };

    $scope.deleteLEfromVNFD = function (index) {
        $scope.vnfdCreate.lifecycle_event.splice(index, 1);
    };

    $scope.addLifecycle = function () {
        $scope.vnfdCreate.lifecycle_event.push(angular.copy($scope.lifecycle_event));
    };

    $scope.deleteLifeCE = function (index) {
        $scope.lifecycle_event.lifecycle_events.splice(index, 1);
    };

    $scope.deleteVNFDep = function (index) {
        $scope.nsdCreate.vnf_dependency.splice(index, 1);
    };

    $scope.deleteDF = function (index) {
        $scope.vnfdCreate.deployment_flavour.splice(index, 1);
    };

    $scope.addDepFlavour = function () {
        $http.get('descriptors/vnfd/deployment_flavour.json')
            .then(function (res) {
                console.log(res.data);
                $scope.depFlavor = angular.copy(res.data);
            });
        $('#modaladdDepFlavour').modal('show');
    };

    $scope.addLifecycleEvent = function () {
        $http.get('descriptors/vnfd/lifecycle_event.json')
            .then(function (res) {
                console.log(res.data);
                $scope.lifecycle_event = angular.copy(res.data);
            });
        $('#modaladdLifecycleEvent').modal('show');
    };
    $scope.addVNFDependencies = function () {
        $('#modalVNFDependencies').modal('show');
    };

    $scope.source_target = {'source': {'name': ''}, 'target': {'name': ''}};
    $scope.addVNFDep = function () {
        $scope.nsdCreate.vnf_dependency.push(angular.copy($scope.source_target));
        $('#modalVNFDependencies').modal('hide');
    };
    $scope.addLifecycle = function () {
        if (!angular.isUndefined($scope.leEditIndex)) {
            $scope.vnfdCreate.lifecycle_event.splice($scope.leEditIndex, 1);
            delete $scope.leEditIndex;
        }
        $scope.vnfdCreate.lifecycle_event.push(angular.copy($scope.lifecycle_event));
        $('#modaladdLifecycleEvent').modal('hide');
    };

    $scope.storeDepFlavour = function () {
        $('#modaladdDepFlavour').modal('hide');
        if (!angular.isUndefined($scope.dfEditIndex)) {
            $scope.vnfdCreate.deployment_flavour.splice($scope.dfEditIndex, 1);
            delete $scope.dfEditIndex;
        }
        $scope.vnfdCreate.deployment_flavour.push(angular.copy($scope.depFlavor));
    };

    $http.get('descriptors/network_service_descriptors/NetworkServiceDescriptor.json')
        .then(function (res) {
            //console.log(res.data);
            $scope.nsdCreate = angular.copy(res.data);
        });

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

        console.log(postNSD);
        console.log(type);

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
        http.delete(url + '/' + data.id)
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
        $('#madalLaunch').modal('show');
    };

    $scope.launch = function () {
        console.log($scope.nsdToSend);
        http.post(urlRecord + '/' + $scope.nsdToSend.id)
            .success(function (response) {
                showOk("Created Network Service Record from Descriptor with id: \<a href=\'\#nsrecords\'>" + $scope.nsdToSend.id + "<\/a>");
            })
            .error(function (data, status) {
                showError(status, data);
            });
    };

    $scope.Jsplumb = function () {
        http.get(url + '/' + $routeParams.nsdescriptorId)
            .success(function (response, status) {
                topologiesAPI.Jsplumb(response, 'descriptor');
                console.log(response);

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
            console.log(status + ' Status unauthorized')
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
                    console.log(response);
                })
                .error(function (data, status) {
                    showError(status, data);

                });
        else
            http.get(url + '/' + $routeParams.nsdescriptorId)
                .success(function (response, status) {
                    $scope.nsdinfo = response;
                    $scope.nsdJSON = JSON.stringify(response, undefined, 4);
                    console.log(response);
                })
                .error(function (data, status) {
                    showError(status, data);
                });
    }

});

