var app = angular.module('app').controller('driverCtrl', function ($scope, serviceAPI, $routeParams, $http, $cookieStore, AuthService, $window, $interval, http) {
  var url =  $cookieStore.get('URL');
  var defaultUrl = "lore:8082";
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
        console.error('STATUS: ' + status + ' DATA: ' + JSON.stringify(data));

    });

  };


});
