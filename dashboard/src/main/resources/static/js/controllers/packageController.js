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
app.controller('PackageCtrl', function ($scope, serviceAPI, $routeParams, http, $cookieStore, AuthService, $interval) {

    var url = $cookieStore.get('URL') + "/api/v1/vnf-packages/";
    var urlTosca = $cookieStore.get('URL') + "/api/v1/csar-vnfd/";
    var dropzoneUrl = url;
    var myDropzone;
    $scope.csarPackage = false;

    $scope.alerts = [];
    $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
    };

    loadTable();


    $scope.deletePackage = function (data) {
        http.delete(url + data.id)
            .success(function (response) {
                showOk('VNF Package deleted.');
                loadTable();
            })
            .error(function (response, status) {
                showError(response, status);
            });
    };

    $scope.deleteScript = function (data) {
        http.delete(url + $routeParams.packageid + '/scripts/' + data.id)
            .success(function (response) {
                showOk('Script deleted.');

            })
            .error(function (response, status) {
                showError(response, status);
            });
    };
    $scope.editScript = function (data) {
        http.get(url + $routeParams.packageid + '/scripts/' + data.id)
            .success(function (response) {
                $scope.scriptToEdit = response;
                $scope.editingScript = data;
                $("#modalEditScript").modal("show");
            })
            .error(function (response, status) {
                showError(response, status);
            });
    };
    $scope.sendScript = function (scriptToEdit) {
        http.put(url + $routeParams.packageid + '/scripts/' + $scope.editingScript.id, scriptToEdit)
            .success(function (response) {
                showOk('Script updated!');
            })
            .error(function (response, status) {
                showError(response, status);
            });
    };

    $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
    };

    /* -- multiple delete functions Start -- */

    $scope.multipleDeleteReq = function () {
        var ids = [];
        angular.forEach($scope.selection.ids, function (value, k) {
            if (value) {
                ids.push(k);
            }
        });
        console.log(ids);
        http.post(url + 'multipledelete', ids)
            .success(function (response) {
                showOk('Event: ' + ids.toString() + ' deleted.');
                loadTable();
            })
            .error(function (response, status) {
                showError(response, status);
            });
        $scope.multipleDelete = false;
        $scope.selection = {};
        $scope.selection.ids = {};

    };
    $scope.$watch('mainCheckbox', function (newValue, oldValue) {
        //console.log(newValue);
        //console.log($scope.selection.ids);


        angular.forEach($scope.selection.ids, function (value, k) {
            /*     console.log(k);
             console.log(value);*/

            $scope.selection.ids[k] = newValue;
        });
        //console.log($scope.selection.ids);

    });
    $scope.$watch('selection', function (newValue, oldValue) {
        console.log(newValue);
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

    $scope.$watch('csarPackage', function (newValue, oldValue) {
        if ($scope.csarPackage) {
            myDropzone.options.url = urlTosca;
        } else {
            myDropzone.options.url = url;
        }
        //console.log(myDropzone.options.url);
    });

    $scope.multipleDelete = true;

    $scope.selection = {};
    $scope.selection.ids = {};
    /* -- multiple delete functions END -- */

    function loadTable() {
        if (!angular.isUndefined($routeParams.packageid))
            http.get(url + $routeParams.packageid)
                .success(function (response, status) {
                    //console.log(response);
                    $scope.vnfpackage = response;
                    $scope.vnfpackageJSON = JSON.stringify(response, undefined, 4);

                }).error(function (data, status) {
                    showError(data, status);
                });
        else {
            http.get(url)
                .success(function (response) {
                    $scope.vnfpackages = response;
                })
                .error(function (data, status) {
                    showError(data, status);
                });
        }

    }

    function showError(data, status) {
        if (status === 500) {
            $scope.alerts.push({
                type: 'danger',
                msg: 'An error occured and could not be handled properly, please, report to us and we will fix it as soon as possible'
            });
        } else {
            $scope.alerts.push({
                type: 'danger',
                msg: data.message + '. Error code: ' + status
            });
        }
        $('.modal').modal('hide');
        if (status === 401) {
            console.log(status + ' Status unauthorized')
            AuthService.logout();
        }
    }

    function showOk(msg) {
        $scope.alerts.push({ type: 'success', msg: msg });
        window.setTimeout(function () {
            for (i = 0; i < $scope.alerts.length; i++) {
                if ($scope.alerts[i].type == 'success') {
                    $scope.alerts.splice(i, 1);
                }
            }
        }, 5000);
        loadTable();
        $('.modal').modal('hide');
    }


    angular.element(document).ready(function () {
        if (angular.isUndefined($routeParams.packageid)) {
            var previewNode = document.querySelector("#template");
            previewNode.id = "";
            var previewTemplate = previewNode.parentNode.innerHTML;
            previewNode.parentNode.removeChild(previewNode);

            var header = {};

            if ($cookieStore.get('token') !== '')
                header = { 'Authorization': 'Bearer ' + $cookieStore.get('token') };

            header['project-id'] = $cookieStore.get('project').id;
            myDropzone = new Dropzone('#my-dropzone', {
                url: dropzoneUrl, // Set the url
                method: "POST",
                parallelUploads: 20,
                previewTemplate: previewTemplate,
                autoProcessQueue: false, // Make sure the files aren't queued until manually added
                previewsContainer: "#previews", // Define the container to display the previews
                headers: header,
                init: function () {
                    var submitButton = document.querySelector("#submit-all");
                    myDropzone = this; // closure

                    submitButton.addEventListener("click", function () {
                        $scope.$apply(function ($scope) {
                            myDropzone.processQueue();
                            loadTable();
                        });
                    });
                    this.on("success", function (file, responseText) {
                        $scope.$apply(function ($scope) {
                            showOk("Uploaded the VNF Package");
                            myDropzone.removeAllFiles(true);
                            loadTable();
                            
                        });

                    });
                    this.on("error", function (file, responseText) {
                        if (responseText === "Server responded with 500 code.") {
                            $scope.$apply(function ($scope) {
                                showError({ message: "error" }, 500);
                            });
                        } else {
                            console.log(responseText);
                            $scope.$apply(function ($scope) {
                                showError(responseText, responseText.code);
                            });
                        }
                        myDropzone.removeAllFiles(true);
                        });
                }
            });




            // Update the total progress bar
            myDropzone.on("totaluploadprogress", function (progress) {
                $('.progress .bar:first').width = progress + "%";
            });

            myDropzone.on("sending", function (file, xhr, formData) {
                // Show the total progress bar when upload starts
                $('.progress .bar:first').opacity = "1";


            });

            // Hide the total progress bar when nothing's uploading anymore
            myDropzone.on("queuecomplete", function (progress) {
                $('.progress .bar:first').opacity = "0";

            });



            $(".cancel").onclick = function () {
                myDropzone.removeAllFiles(true);
            };
        }
    });


});
