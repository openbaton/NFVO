var app = angular.module('app');
app.controller('keyPairsCtrl', function ($scope, serviceAPI, $routeParams, http, $cookieStore, AuthService, $window) {

    var url = $cookieStore.get('URL') + "/api/v1/keys/";
    var urlprojects = $cookieStore.get('URL') + "/api/v1/projects/";
    $scope.keyName = "";
    $scope.pubKey = "";
    $scope.keypairs = "";
    $scope.newKey = {name:"", publicKey:""};
    $scope.alerts = [];
    loadTable();
    function loadTable() {

        //console.log($routeParams.userId);
            http.get(url)
                .success(function (response) {
                    $scope.keypairs = response;
                    //console.log($scope.users.length);

                    console.log($scope.keypairs);
                })
                .error(function (data, status) {
                    showError(data, status);
                });


        }
        $scope.closeAlert = function (index) {
            $scope.alerts.splice(index, 1);
        };

        $scope.save = function () {
            //console.log($scope.projectObj);

            $scope.newKey.name = $scope.keyName;
            $scope.newKey.publicKey = $scope.pubKey;
            console.log($scope.newKey);
            http.post(url, $scope.newKey)
                .success(function (response) {
                    showOk('Key pair: ' + $scope.keyName + ' saved.');
                    setTimeout(loadTable(),250);
                    $scope.keyName = "";
                    $scope.pubKey = "";
                    //location.reload();
                })
                .error(function (response, status) {
                    showError(response, status);
                });
        };
        $scope.delete = function (data) {
            http.delete(url + data.id)
                .success(function (response) {
                    showOk('Key with name: ' + data.name + ' deleted.');
                    setTimeout(loadTable(), 250);

                })
                .error(function (response, status) {
                    showError(response, status);
                });
        };
        $scope.createKeyName = "";
        $scope.createKey = function () {
            //console.log($scope.projectObj);



            http.postPlain(url + 'generate', $scope.createKeyName)
                .success(function (response) {
                    showOk('Key: ' + $scope.createKeyName + ' generated.');
                    setTimeout(loadTable(),250);
                    //console.log(response);
                    var key = document.createElement("a");
                    key.download = $scope.createKeyName + '.pem';
                    key.href = 'data:application/x-pem-file,' + encodeURIComponent(response);
                    document.body.appendChild(key);
                    key.click()
                    document.body.removeChild(key);
                    delete key;
                    //document.location = 'title: key.pem, data:application/x-pem-file,' +
                      //  encodeURIComponent(response);
                    //location.reload();
                })
                .error(function (response, status) {
                    showError(response, status);
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
