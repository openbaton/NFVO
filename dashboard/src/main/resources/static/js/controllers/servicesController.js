var app = angular.module('app');
app.controller('ServiceCtrl', function($scope, serviceAPI, $routeParams, http, $cookieStore, AuthService) {

    var url = '/api/rest/orchestrator/v2/services/';


    $scope.alerts = [];
    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };

    //loadServicesTable();



    $scope.deleteService = function(data) {
        http.delete(url + data.id)
                .success(function(response) {
                    showOk('Service deleted from the Catalog.');
                })
                .error(function(response, status) {
                    showError(status, response);
                });
    };
    $scope.updateServicePut = function(service) {
        var id = service.id;
        serviceAPI.cleanService(service);

        delete service.locations;
        console.log(service);

        $('.modal').modal('hide');

        http.put(url + id, service)
                .success(function(response) {
                    $('.modal').modal('hide');

                    showOk('Service ' + service.serviceType + ' Updated.');
                    loadServicesTable();

                })
                .error(function(data, status) {
                    $('.modal').modal('hide');

                    showError(data, status);
                    loadServicesTable();

                });


    };

    $scope.launchService = function(service) {
        var serv = angular.copy(service);
        serviceAPI.cleanService(serv);
        serv.instanceName = serv.serviceType + '-' + serviceAPI.getRandom();
        $scope.serviceEdit = _.omit(serv, 'maxNumInst', 'minNumInst', 'networkIds', 'flavour');

    };

    $scope.updateService = function(service) {
        $scope.serviceEdit = service;
    };


    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };
    $scope.showData = function(data) {
        $cookieStore.put('service', data);
    };

    function loadServicesTable() {
        http.get(url).success(function(response) {

            $scope.services = response;

        });
    }
    function showError(data, status) {
        $scope.alerts.push({type: 'danger', msg: 'ERROR: <strong>HTTP status</strong>: ' + status + ' response <strong>data</strong> : ' + data});
        loadServicesTable();
        $('.modal').modal('hide');
        if (status === 401) {
            console.log(status + ' Status unauthorized')
            AuthService.logout();
            $window.location.reload();
        }
    }
    function showOk(msg) {
        $scope.alerts.push({type: 'success', msg: msg});
        loadServicesTable();
        $('.modal').modal('hide');
    }
    if (!angular.isUndefined($routeParams.serviceid))
        $scope.serviceSelected = $cookieStore.get('service');


});



