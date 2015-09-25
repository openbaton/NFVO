angular.module('app').
    controller('vimInstanceCtrl', function ($scope, $routeParams, http) {

        var url = '/api/v1/datacenters/';
        //var url = 'http://localhost:8080/api/v1/datacenters/';

        $scope.alerts = [];
        $scope.datacenter = {};
        $scope.file = '';


        loadVIM();


        $scope.textTopologyJson = '';
        $scope.changeText = function (text) {
            $scope.textTopologyJson = text;
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
        $scope.sendInfrastructure = function () {
            if ($scope.file !== '' && !angular.isUndefined($scope.file)) {
                console.log($scope.file);
                http.post(url, $scope.file)
                    .success(function (response) {
                        showOk('Vim Instance created.');
                        loadTable();
                    })
                    .error(function (data, status) {
                        showError(data, status);
                    });
            } else if ($scope.textTopologyJson !== '') {
                console.log($scope.textTopologyJson);
                http.post(url, $scope.textTopologyJson)
                    .success(function (response) {
                        showOk('VIM Instance created.');
                        resizeMap();
                        $scope.file = '';
                    })
                    .error(function (data, status) {
                        showError(data, status);

                        resizeMap();
                    });
            }
            else {
                showError('Problem with the VIM Instance');

            }
        };


        $scope.nameFilter = null;
        $scope.datacenters = [];
        loadTable();


        $scope.changeSelection = function (selection) {
            $scope.vimInstanceJson = {};
            $scope.vimInstanceJson = dataCenterJ[selection];
        };
        $scope.changeLocation = function (location) {
            $scope.locationRadio = location;
        };


        $scope.saveDataCenter = function (vimInstanceJson) {
            if ($scope.file !== '') {
                vimInstanceJson = $scope.file;

            }

            $('.modal').modal('hide');
            console.log(vimInstanceJson);
            http.post(url, vimInstanceJson)
                .success(function (response) {
                    showOk('Data Center created!');
                    console.log(response);
                    $scope.selection = $scope.dataSelect[0];
                    $scope.vimInstanceJson = {};
                })
                .error(function (data, status) {
                    showError(data, status);
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

        $scope.refreshDc = function () {
            loadDatacenter();
            $('#refreshIco').addClass('fa-spin');
            http.get(url + $routeParams.vimInstanceId + '/refresh')
                .success(function (data) {
                    $('#refreshIco').removeClass('fa-spin');
                    $scope.datacenter = data;
                    $scope.datacenterJSON = JSON.stringify(data, undefined, 4);
                    $scope.upDatacenter = data;
                })
                .error(function (data, status) {
                    showError(data, status);
                });
        };
        $scope.closeAlert = function (index) {
            $scope.alerts.splice(index, 1);
        };


        $scope.loadFormUpdate = function (data) {
            $scope.upDatacenter = cleanDatacenter(data);
            $('#modalUpdate').modal('show');
        };
        $scope.deleteData = function (id) {
            http.delete(url + id)
                .success(function (response) {
                    showOk('Data Center deleted with id ' + id + '.');
                    delete $scope.dataCenterSelected;
                })
                .error(function (data, status) {
                    showError(data, status);
                });
        };

        function loadVIM() {
            if (!angular.isUndefined($routeParams.vimInstanceId))
                http.syncGet(url + $routeParams.vimInstanceId).then(function (data) {
                    console.log(data);
                    $scope.vimInstance = data;
                    $scope.vimInstanceJSON = JSON.stringify(data, undefined, 4);

                });
        }

        function loadTable() {
            http.get(url)
                .success(function (response) {
                    $scope.vimInstances = response;
                })
                .error(function (data, status) {
                    showError(data, status);
                });
        }

        function showError(data, status) {
            console.log('DATA: ' + data + ' STATUS: ' + status)
            $scope.alerts.push({
                type: 'danger',
                msg: 'ERROR: <strong>HTTP status</strong>: ' + status + ' response <strong>data</strong> : ' + JSON.stringify(data)
            });

            $('.modal').modal('hide');
        }

        function showOk(msg) {
            $scope.alerts.push({type: 'success', msg: msg});

            $('.modal').modal('hide');
        }

    });

