var app = angular.module('app').controller('NsdCtrl', function ($scope, $compile, $cookieStore, $routeParams, http, serviceAPI, $window, $route, $interval, $http, topologiesAPI) {

    var url = 'http://localhost:8080/api/v1/ns-descriptors';
    var urlRecord = 'http://localhost:8080/api/v1/ns-records';

    loadTable();


    $scope.textTopologyJson = '';
    $scope.file = '';
    $scope.alerts = [];


    $scope.tabs = [
        {active: true},
        {active: false},
        {active: false}
    ];

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
            $scope.vnfdCreate.vnfd.splice($scope.vduEditIndex, 1);
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

    $scope.addVNDtoNSD = function () {
        $('#addEditVNDF').modal('hide');
        if (!angular.isUndefined($scope.vnfdEditIndex)) {
            $scope.nsdCreate.vnfd.splice($scope.vnfdEditIndex, 1);
            delete $scope.vnfdEditIndex;
        }
        $scope.nsdCreate.vnfd.push(angular.copy($scope.vnfdCreate));
        var height = parseInt($(window).height()) + 150 * $scope.nsdCreate.vnfd.length;
        console.log('heigh: ' + height + 'px');
        $(".modal-backdrop").height(height)
    };

    $scope.deleteVNFD = function (index) {
        $scope.nsdCreate.vnfd.splice(index, 1);
    };

    $scope.storeNSDF = function (nsdCreate) {
        $('#modalForm').modal('hide');
        console.log(nsdCreate);
        http.post(url, nsdCreate)
            .success(function (response) {
                showOk('Network Service Descriptors stored!');
                loadTable();
            })
            .error(function (data, status) {
                showError(status, data);
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

    $scope.addVNFDependencies = function () {
        $('#modalVNFDependencies').modal('show');
    };

    $scope.source_target = {'source': {'name': ''}, 'target': {'name': ''}};
    $scope.addVNFDep = function () {
        $scope.nsdCreate.vnf_dependency.push(angular.copy($scope.source_target))
        $('#modalVNFDependencies').modal('hide');
    };
    $scope.storeDepFlavour = function () {
        $('#modaladdDepFlavour').modal('hide');
        if (!angular.isUndefined($scope.dfEditIndex)) {
            $scope.vnfdCreate.deployment_flavour.splice($scope.dfEditIndex, 1);
            delete $scope.dfEditIndex;
        }
        $scope.vnfdCreate.deployment_flavour.push(angular.copy($scope.depFlavor));
    };

    $http.get('descriptors/network_service_descriptors/NetworkServiceDescriptor-with-dependencies.json')
        .then(function (res) {
            console.log(res.data);
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
    }

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
        if ($scope.toggle) {
            var template = {};
            var nameTemplate = $('#nameTemplate').val();
//            template.flavour = $('#inputFlavorTemplate').val();
            template.name = (nameTemplate === '') ? 'Template-' + Math.floor((Math.random() * 100) + 1) : nameTemplate;
            template.topology = postNSD;
            console.log(template);
            http.post('/api/rest/admin/v2/templates', template)
                .success(function (response) {
                    showOk('Template created!');
//                        $scope.cleanModal();
                })
                .error(function (data, status) {
                    showError(status, data);
                });
        }
        $scope.toggle = false;
        $scope.file !== '';
        //        $scope.services = [];
    };


    $scope.Jsplumb = function () {

        http.syncGet(url + $routeParams.topologyid).then(function (response) {
            topologiesAPI.Jsplumb(response);
            console.log(response);

        });
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

    $scope.launch = function (data) {
        console.log(data)
        http.post(urlRecord + '/' + data.id)
            .success(function (response) {
                showOk('Created Network Service Record from Descriptor with id: ' + data.id);
            })
            .error(function (data, status) {
                showError(status, JSON.stringify(data));
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
        $scope.alerts.push({
            type: 'danger',
            msg: 'ERROR: <strong>HTTP status</strong>: ' + status + ' response <strong>data</strong>: ' + data
        });
//        loadTable();
        $('.modal').modal('hide');
    }

    function showOk(msg) {
        $scope.alerts.push({type: 'success', msg: msg});
        $('.modal').modal('hide');
    }


    function loadTable() {
        //if (!$('#jsonInfo').hasClass('in'))
        if (angular.isUndefined($routeParams.nsdescriptorId))
            http.get(url)
                .success(function (response, status) {
                    $scope.nsdescriptors = response;
                    console.log(response);
                })
                .error(function (data, status) {
                    showError(status, data);
                    //var destinationUrl = '#';
                    //$window.location.href = destinationUrl;
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
                    //var destinationUrl = '#';
                    //$window.location.href = destinationUrl;
                });
    }


});

