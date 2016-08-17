var app = angular.module('app');
app.controller('EventCtrl', function ($scope, $interval, serviceAPI, $routeParams, http, $cookieStore, AuthService) {

    var url = $cookieStore.get('URL') + "/api/v1/events/";

    $scope.alerts = [];
    $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
    };
  
    loadTable();

    $scope.eventObj = {
        'name': 'event_name',
        'networkServiceId': '',
        'virtualNetworkFunctionId': '',
        'type': 'REST',
        'endpoint': 'localhost:8081/events',
        'event': 'INSTANTIATE_FINISH'
    };

    /* -- multiple delete functions Start -- */

    $scope.multipleDeleteReq = function(){
        var ids = [];
        angular.forEach($scope.selection.ids, function (value, k) {
                if (value) {
                    ids.push(k);
                }
        });
        //console.log(ids);
        http.post(url + 'multipledelete', ids)
            .success(function (response) {
                showOk('Event: ' + ids.toString() + ' deleted.');
                loadTable();
            })
            .error(function (response, status) {
                showError(response, status);
            });

    };

    $scope.main = {checkbox: false};
    $scope.$watch('main', function (newValue, oldValue) {
        ////console.log(newValue.checkbox);
        ////console.log($scope.selection.ids);
        angular.forEach($scope.selection.ids, function (value, k) {
            $scope.selection.ids[k] = newValue.checkbox;
        });
        //console.log($scope.selection.ids);
    }, true);

    $scope.$watch('selection', function (newValue, oldValue) {
        //console.log(newValue);
        var keepGoing = true;
        angular.forEach($scope.selection.ids, function (value, k) {
            if (keepGoing) {
                if ($scope.selection.ids[k]) {
                    $scope.multipleDelete = false;
                    keepGoing = false;
                }
                else {
                    $scope.multipleDelete = true;
                }
            }

        });
        if (keepGoing)
            $scope.mainCheckbox = false;
    }, true);

    $scope.multipleDelete = true;

    $scope.selection = {};
    $scope.selection.ids = {};
    /* -- multiple delete functions END -- */


    $scope.types = ['REST', 'RABBIT', 'JMS'];
    $scope.deleteEvent = function (data) {
        http.delete(url + data.id)
            .success(function (response) {
                showOk('Event: ' + data.name + ' deleted.');
                loadTable();
            })
            .error(function (response, status) {
                showError(response, status);
            });
    };

    $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
    };


    $scope.save = function () {
        //console.log($scope.eventObj);
        http.post(url, $scope.eventObj)
            .success(function (response) {
                showOk('Event: ' + $scope.eventObj.name + ' saved.');
                loadTable();
            })
            .error(function (response, status) {
                showError(response, status);
            });
    };
    function loadTable() {
        if (!angular.isUndefined($routeParams.eventId))
            http.get(url + $routeParams.eventId)
                .success(function (response, status) {
                    //console.log(response);
                    $scope.event = response;
                    $scope.eventJSON = JSON.stringify(response, undefined, 4);

                }).error(function (data, status) {
                showError(data, status);
            });
        else {
            http.get(url)
                .success(function (response) {
                    $scope.events = response;
                    //console.log(response);
                })
                .error(function (data, status) {
                    showError(data, status);
                });

            http.get(url + '/actions')
                .success(function (response) {
                    $scope.actions = response;
                })
                .error(function (data, status) {
                    showError(data, status);
                });
        }

    }

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
