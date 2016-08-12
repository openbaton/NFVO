var app = angular.module('app');
app.controller('ProjectCtrl', function ($scope, serviceAPI, $routeParams, http, $cookieStore, AuthService, $window) {

    var url = $cookieStore.get('URL') + "/api/v1/projects/";

    $scope.alerts = [];
    $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
    };

    loadTable();

    $scope.projectObj = {
        'name': '',
        'description': ''
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
          if (k === $scope.defaultID) {
            return;
          }
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


    $scope.types = ['REST', 'RABBIT'];
    $scope.deleteEvent = function (data) {
        http.delete(url + data.id)
            .success(function (response) {
                showOk('Project: ' + data.name + ' deleted.');
                loadTable();
                location.reload();
            })
            .error(function (response, status) {
                showError(response, status);
            });
    };

    $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
    };


    $scope.save = function () {
        //console.log($scope.projectObj);
        http.post(url, $scope.projectObj)
            .success(function (response) {
                showOk('Project: ' + $scope.projectObj.name + ' saved.');
                loadTable();
                $scope.projectObj = {
                    'name': '',
                    'description': ''
                };
                //location.reload();
            })
            .error(function (response, status) {
                showError(response, status);
            });
    };
    $scope.defaultID = "";
    function loadTable() {
            http.get(url)
                .success(function (response) {
                    $scope.projects = response;
                    //console.log(response);
                    for (i = 0; i < $scope.projects.length; i++) {
                      if ($scope.projects[i].name === 'default') {
                        $scope.defaultID = $scope.projects[i].id;
                      }
                    }
                    //console.log($scope.defaultID);
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
            console.error(status + ' Status unauthorized')
            AuthService.logout();
        }
    }

    function showOk(msg) {
        $scope.alerts.push({type: 'success', msg: msg});
        loadTable();
        $('.modal').modal('hide');
    }

    $scope.admin = function() {
      //console.log($cookieStore.get('userName'));
      if($cookieStore.get('userName') === 'admin') {
        return true;
      } else {
        return false;
      }
    };

});
