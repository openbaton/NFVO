var app = angular.module('app').controller('marketCtrl', function ($scope, serviceAPI, $routeParams, $http, $cookieStore, AuthService, $window, $interval, http) {

var url =  $cookieStore.get('URL');
var defaultUrl = "marketplace.openbaton.org:80";
$scope.alerts = [];

$scope.marketUrl = null;
$scope.privatepackages = [];
$scope.publicpackages = [];

getMarketURL();


function getMarketURL() {
  $http.get(url + "/configprops")
      .success(function (response) {
          if (response.restVNFPackage.properties.ip) {
            $scope.marketUrl = response.restVNFPackage.properties.ip;
            loadTable();
          }
          else {
            return;
          }


          console.log($scope.marketUrl);
      })
      .error(function (data, status) {
          showError(data, status);
      });


}


function loadTable() {
    //console.log($routeParams.userId);
    $http.get("http://"+ $scope.marketUrl + "/api/v1/vnf-packages")
        .success(function (response) {
            $scope.privatepackages = response;

            //console.log($scope.packages);
        })
        .error(function (data, status) {
            showError(data, status);
        });


}

function loadTablePublic() {
    //console.log($routeParams.userId);
    $http.get("http://"+ defaultUrl + "/api/v1/vnf-packages")
        .success(function (response) {
            $scope.packages = $scope.packages + response;

            //console.log($scope.packages);
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

$scope.download = function(data) {
  $scope.requestlink = {};
  $scope.requestlink['link'] = "http://" + $scope.marketUrl + "/api/v1/vnf-packages/" + data.id + "/tar/";
    console.log($scope.requestlink);
     http.post(url + "/api/v1/vnf-packages/marketdownload", JSON.stringify($scope.requestlink)).success(function (response) {
      showOk("The package is being downloaded");
      })
     .error(function (data, status) {
         console.error('STATUS: ' + status + ' DATA: ' + JSON.stringify(data));

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

});
