/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */



var app = angular.module('app');

/**
 *
 * Manages the login page
 *
 */

app.controller('LoginController', function ($scope, AuthService, Session, $rootScope, $location, $cookieStore, $http) {
    $scope.currentUser = null;
    //$scope.URL = 'http://lore:8080';
    $scope.URL = '';
    $scope.NFVOVersion = "";
    $scope.credential = {
        "username": '',
        "password": '',
        "grant_type": "password"
    };

    if (angular.isUndefined($cookieStore.get('logged'))) {
        $scope.logged = false;
        $rootScope.logged = false;
    }

    else if ($cookieStore.get('logged')) {
        $scope.logged = $cookieStore.get('logged');
        $rootScope.logged = $cookieStore.get('logged');
    }
    $location.replace();
    //console.log($scope.logged);
    $scope.loggedF = function () {

        return $scope.logged;
    };


    $scope.checkSecurity = function () {
        //console.log($scope.URL + "/api/v1/security");
        AuthService.removeSession();
        $http.get($scope.URL + "/api/v1/security")
            .success(function (data) {
                //console.log(data);
                if (data === "false") {
                    window.location.assign('/login');
                    window.location.reload();
                }
            })
            .error(function (data, status) {
                if (status == 404) {
                    return;
                }
                console.info(('status != 404'));
                console.error('Response error', status, data);
            })

    };

    /**
     * Calls the AuthService Service for retrieving the token access
     *
     * @param {type} credential
     * @returns {undefined}
     */
    $scope.login = function (credential) {
        AuthService.login(credential, $scope.URL);
        setTimeout(showLoginError, 2000);
    };


    $scope.register = function (newUser) {
        delete newUser.password2;
        //console.log(newUser);
        $http.post($scope.URL + '/register', newUser)
            .success(function (data, status) {
                $window.location.reload();
            })
            .error(function (status, data) {
            });
    };
    function showLoginError() {
        $scope.$apply(function () {
            $scope.loginError = angular.isUndefined($cookieStore.get('logged'));
            //console.log($scope.loginError);
        });
    }

});


app.controller('IndexCtrl', function ($document, $scope, $compile, $routeParams, serviceAPI, $interval, $cookieStore, $location, AuthService, http, $rootScope, $window, $route) {
    $('#side-menu').metisMenu();


    $scope.adminRole = "ADMIN";
    $scope.superProject = "*";
    $scope.numberNSR = 0;
    $scope.numberNSD = 0;
    $scope.numberVNF = 0;
    $scope.numberUnits = 0;
    $scope.numberKeys = 0;
    $scope.quota = null;
    var chartsHere = false;
    var url = $cookieStore.get('URL') + "/api/v1";

    $interval(waitCharts, 1000);
    $scope.config = {};
    $scope.userLogged;
    $location.replace();

    //this is here for mozilla browser to redirect user to main overview after login, mozilla does not do it automatically
    if ($cookieStore.get('logged') && (window.location.href.substring(window.location.href.length -'login'.length) === 'login')) {
      window.location.href = window.location.href.substring(0,window.location.href.length -'login'.length) + 'main';

    }

    function getVersion() {
      http.get(url +'/main/version/')
          .success(function (response) {
              console.log("version is " + response);
              $scope.NFVOversion = response
          })
          .error(function (response, status) {
              showError(status, response);
          });
    }



    function loadCurrentUser() {
        http.get(url +'/users/current')
            .success(function (response) {
                //console.log(response);
                $scope.userLogged = response
            })
            .error(function (response, status) {
                showError(status, response);
            });
    };

    function getConfig() {

        http.get(url + '/configurations/')
            .success(function (data, status) {
                //console.log(data);
                $.each(data, function (i, conf) {
                    if (conf.name === "system") {
                        $scope.config = conf;
                    }
                })
            });
    }

    $scope.loadSettings = function () {
        getConfig();
        $("#modalSetting").modal('show');

    };

    $scope.logged = $cookieStore.get('logged');
    //console.log($scope.logged);


    function stop() {
      $interval.cancel(promise);
    };

    function loadNumbers() {
        http.syncGet(url + '/ns-descriptors/').then(function (data) {
            $scope.numberNSD = data.length;
            var vnf = 0;
            $.each(data, function (i, nsd) {
                //console.log(nsd.vnfd.length);
                if (!angular.isUndefined(nsd.vnfd.length))
                    vnf = vnf + nsd.vnfd.length;
            });
            $scope.numberVNF = vnf;
        });
        http.syncGet(url + '/ns-records/').then(function (data) {
            $scope.numberNSR = data.length;
            var units = 0;
            $.each(data, function (i, nsr) {
                $.each(nsr.vnfr, function (i, vnfr) {
                    $.each(vnfr.vdu, function (i, vdu) {
                        if (!angular.isUndefined(vdu.vnfc_instance.length))
                            units = units + vdu.vnfc_instance.length;
                    });
                });
            });
            $scope.numberUnits = units;
        });

        http.syncGet(url + '/keys/').then(function (data) {
            $scope.numberKeys = data.length;
        });

    }


    $scope.$watch('projectSelected', function (newValue, oldValue) {
        console.log(newValue);
        if (!angular.isUndefined(newValue) && !angular.isUndefined(oldValue)) {
            $cookieStore.put('project', newValue);

            loadNumbers();
            loadQuota();
            getConfig();
            loadCurrentUser();
            getVersion();

        }
        if (!angular.isUndefined(newValue) && angular.isUndefined(oldValue)) {
            $cookieStore.put('project', newValue);

            loadNumbers();
            loadQuota();
            getConfig();
            loadCurrentUser();
            getVersion();
        }


    });


    console.log($rootScope.projects);
    console.log($rootScope.projectSelected);

    $scope.changeProject = function (project) {
        if (arguments.length === 0) {
            http.syncGet(url + '/projects/')
                .then(function (response) {
                    if (angular.isUndefined($cookieStore.get('project')) || $cookieStore.get('project').id == '') {
                        $rootScope.projectSelected = response[0];
                        $cookieStore.put('project', response[0])
                    } else {
                        $rootScope.projectSelected = $cookieStore.get('project');
                    }
                    $rootScope.projects = response;
                });
        }
        else {
            $rootScope.projectSelected = project;
            console.log(project);
            $cookieStore.put('project', project);
            $window.location.reload();
        }


    };


    $scope.saveSetting = function (config) {
        //console.log(config);
        $('.modal').modal('hide');
        $('#modalSend').modal('show');

        http.put(url + '/configurations/' + config.id, config)
            .success(function (response) {
                $('.modal').modal('hide');
                alert('Configurations Updated! ');

            })
            .error(function (response, status) {
                $('.modal').modal('hide');
                alert('ERROR: <strong>HTTP</strong> status:' + status + ' response <strong>response:</strong>' + response);
            });
    };

    /**
     * Checks if the user is logged
     * @returns {unresolved}
     */
    $scope.loggedF = function () {
        return $scope.logged;
    };

    if ($scope.logged)
    //console.log('Ok Logged');
        $location.replace();
    $scope.username = $cookieStore.get('userName');

    //console.log($scope.username);


    /**
     * Delete the session of the user
     * @returns {undefined}
     */
    $scope.logout = function () {
        AuthService.logout();
    };

    $scope.changePassword = function () {
        $scope.oldPassword = '';
        $scope.newPassword = '';
        $scope.newPassword1 = '';

        $('#modalChangePassword').modal('show');
    };

    $scope.postNew = function() {
      if ($scope.newPassword.localeCompare($scope.newPassword1) == 0) {
        $scope.passwordData = {};
        $scope.passwordData.old_pwd = $scope.oldPassword;
        $scope.passwordData.new_pwd = $scope.newPassword;
        http.put(url + '/users/changepwd', JSON.stringify($scope.passwordData))
        .success(function (response) {
          alert("The password has been successfully changed")
          AuthService.logout()})
        .error(function (data, status) {
            console.error('STATUS: ' + status + ' DATA: ' + JSON.stringify(data));
            alert('STATUS: ' + status + ' DATA: ' + JSON.stringify(data))
            ? "" : location.reload();
        });
    } else {
      alert("The new passwords are not the same");
    }

    };
    $scope.test = 34;
    $scope.admin = function() {
      //console.log($scope.userLogged);
      if (typeof $scope.userLogged != 'undefined') {
        if($scope.userLogged.roles[0].project === $scope.superProject && $scope.userLogged.roles[0].role === $scope.adminRole) {
         return true;
        }  else {
         return false;
        }
      }
      return false;
    };


 $(document).ready(function() {});

 function loadQuota() {
     http.get(url +'/quotas')
         .success(function (response) {
             console.log(response);
             $scope.quota = response;

              //console.log($scope.quota.left.ram)
         })
         .error(function (response, status) {
             showError(status, response);
         });
}

$scope.rcdownload = function() {
    http.getRC(url +'/main/openbaton-rc/')
         .success(function (response) {
             console.log(response);
             var rc = document.createElement("a");
             rc.download = "openbaton" + '.rc';
             rc.href = 'data:application/x-shellscript,' + encodeURIComponent(response);
             document.body.appendChild(rc);
             rc.click()
             document.body.removeChild(rc);
             delete key;

            
         })
         .error(function (response, status) {
             showError(status, response);
         });
}

  function waitCharts() {
  if (!chartsHere) {
      if ($scope.quota !== null) {
        chartsHere = true;
        createCharts();
      }
    }
  }
  $scope.chartsLoaded = function() {
    return chartsHere;
  };
  function createCharts() {

         $.getScript('asset/js/plugins/chart.min.js',function(){
           var ramData = [  {
                 value: $scope.quota.left.ram,
                 color:"#4ED18F",
                 highlight: "#15BA67",
                 label: "Availaible"
             },
             {
                 value: $scope.quota.total.ram - $scope.quota.left.ram,
                 color: "#B22222",
                 highlight: "#15BA67",
                 label: "Used"
             }

             ]
             if ( $scope.quota.total.ram === 0) {
               var ramData = [{
                     value: 1,
                     color:"#4ED18F",
                     highlight: "#15BA67",
                     label: "No resources available"
                 }]
             }

             var instData = [  {
                   value: $scope.quota.left.instances,
                   color:"#4ED18F",
                   highlight: "#15BA67",
                   label:  "Availaible"
               },
               {
                   value: $scope.quota.total.instances - $scope.quota.left.instances,
                   color: "#B22222",
                   highlight: "#15BA67",
                   label: "Used"
               }

               ]

               if ( $scope.quota.total.instances === 0) {
                 var instData = [{
                       value: 1,
                       color:"#4ED18F",
                       highlight: "#15BA67",
                       label: "No resources available"
                   }]
               }

               var cpuData = [  {
                     value: $scope.quota.left.cores,
                     color:"#4ED18F",
                     highlight: "#15BA67",
                     label:  "Availaible"
                 },
                 {
                     value: $scope.quota.total.cores - $scope.quota.left.cores,
                     color: "#B22222",
                     highlight: "#15BA67",
                     label: "Used"
                 }

                 ]

                 if ( $scope.quota.total.cores === 0) {
                   var cpuData = [{
                         value: 1,
                         color:"#4ED18F",
                         highlight: "#15BA67",
                         label: "No resources available"
                     }]
                 }

                 var ipData = [  {
                       value: $scope.quota.left.floatingIps,
                       color:"#4ED18F",
                       highlight: "#15BA67",
                       label:  "Availaible"
                   },
                   {
                       value: $scope.quota.total.floatingIps - $scope.quota.left.floatingIps,
                       color: "#B22222",
                       highlight: "#15BA67",
                       label: "Used"
                   }

                   ]

                   if ( $scope.quota.total.floatingIps === 0) {
                     var ipData = [{
                           value: 1,
                           color:"#4ED18F",
                           highlight: "#15BA67",
                           label: "No resources available"
                       }]
                   }

             var options = {
               responsive : true,
               showTooltips: true
             };

             //Get the context of the canvas element we want to select
             var c = $('#cpuChart');
             var cp = c.get(0).getContext('2d');

             cpuChart = new Chart(cp).Doughnut(cpuData, options);

             var r = $('#ramChart');
             var ra = r.get(0).getContext('2d');

             ramChart = new Chart(ra).Doughnut(ramData, options);

             var i = $('#ipChart');
             var ip = i.get(0).getContext('2d');

             ipChart = new Chart(ip).Doughnut(ipData, options);

             var h = $('#instChart');
             var hd = h.get(0).getContext('2d');

             hddChart = new Chart(hd).Doughnut(instData, options);

         })

 };





});
