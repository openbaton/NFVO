var app = angular.module('app').controller('marketCtrl', function ($scope, serviceAPI, $routeParams, http, $cookieStore, AuthService, $window, $interval) {

$scope.marketUrl = "http://marketplace.openbaton.org:8082"
$scope.packages = null;
loadTable();
function loadTable() {

    //console.log($routeParams.userId);
    http.getPlain($scope.marketUrl + "/api/v1/vnf-packages")
        .success(function (response) {
            $scope.packages = response;

            console.log($scope.packages);
        })
        .error(function (data, status) {
            showError(data, status);
        });


}


$scope.loadTable = function() {
  loadTable();
};

$scope.closeAlert = function (index) {
    $scope.alerts.splice(index, 1);
};



function showError(data, status) {
    $scope.alerts.push({
        type: 'danger',
        msg: 'ERROR: <strong>HTTP status</strong>: ' + status + ' response <strong>data</strong> : ' + JSON.stringify(data)
    });
    $('.modal').modal('hide');
    if (status === 401) {
        //console.log(status + ' Status unauthorized')
        AuthService.logout();
    }
}

function showOk(msg) {
    $scope.alerts.push({type: 'success', msg: msg});
    loadTable();
    $('.modal').modal('hide');
}

});
