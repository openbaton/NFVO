var app = angular.module('app');
app.controller('EventCtrl', function ($scope, serviceAPI, $routeParams, http, $cookieStore, AuthService) {

    var url = $cookieStore.get('URL') + "/api/v1/events/";

    $scope.alerts = [];
    $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
    };

    loadTable();

    $scope.deleteEvent = function (data) {
        http.delete(url + data.id)
            .success(function (response) {
                showOk('Event: ' + data.event + ' deleted.');
                loadTable();
            })
            .error(function (response, status) {
                showError(response, status);
            });
    };

    $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
    };


    function loadTable() {
        http.get(url)
            .success(function (response) {
                $scope.events = response;
                console.log(response);
            })
            .error(function (data, status) {
                showError(data, status);
            });
    }

    function showError(data, status) {
        $scope.alerts.push({
            type: 'danger',
            msg: 'ERROR: <strong>HTTP status</strong>: ' + status + ' response <strong>data</strong> : ' + JSON.stringify(data)
        });
        $('.modal').modal('hide');
        if (status === 401) {
            console.log(status + ' Status unauthorized')
            AuthService.logout();
        }
    }

    function showOk(msg) {
        $scope.alerts.push({type: 'success', msg: msg});
        loadTable();
        $('.modal').modal('hide');
    }

});



