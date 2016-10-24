var app = angular.module('app').controller('driverCtrl', function ($scope, serviceAPI, $routeParams, $http, $cookieStore, AuthService, $window, $interval, http) {
  var url =  $cookieStore.get('URL');
  //var defaultUrl = "lore:8082"
  var defaultUrl = "marketplace.openbaton.org:8082";
  $scope.drivers = [];
  $scope.alerts = [];
  loadTable();


  function loadTable() {
      //console.log($routeParams.userId);
      $http.get("http://"+ defaultUrl + "/api/v1/vim-drivers")
          .success(function (response) {
              $scope.drivers = response;

          })
          .error(function (data, status) {
              showError(data, status);
          });


  }

  $scope.install = function(data) {
    http.get(url + "/api/v1/plugins/" + data.id).success(function (response) {
     showOk("Plugin " + data.name + " will be installed");
     })
    .error(function (data, status) {
        showError(data, status);

    });

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
$scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
    };



});
