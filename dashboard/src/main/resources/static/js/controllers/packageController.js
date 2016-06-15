var app = angular.module('app');
app.controller('PackageCtrl', function ($scope, serviceAPI, $routeParams, http, $cookieStore, AuthService) {

    var url = $cookieStore.get('URL') + "/api/v1/vnf-packages/";

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
                showError(response,status);
            });
    };

    $scope.deleteScript = function (data) {
        http.delete(url +$routeParams.packageid+'/scripts/'+ data.id)
            .success(function (response) {
                showOk('Script deleted.');

            })
            .error(function (response, status) {
                showError(response,status);
            });
    };
    $scope.editScript = function (data) {
        http.get(url +$routeParams.packageid+'/scripts/'+ data.id)
            .success(function (response) {
                $scope.scriptToEdit = response;
                $scope.editingScript = data;
                $("#modalEditScript").modal("show");
            })
            .error(function (response, status) {
                showError(response,status);
            });
    };
    $scope.sendScript = function (scriptToEdit) {
        http.put(url +$routeParams.packageid+'/scripts/'+ $scope.editingScript.id, scriptToEdit)
            .success(function (response) {
                showOk('Script updated!');
            })
            .error(function (response, status) {
                showError(response,status);
            });
    };

    $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
    };

    /* -- multiple delete functions Start -- */

    $scope.multipleDeleteReq = function(){
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

    };
    $scope.$watch('mainCheckbox', function (newValue, oldValue) {
        console.log(newValue);
        console.log($scope.selection.ids);


        angular.forEach($scope.selection.ids, function (value, k) {
            /*     console.log(k);
             console.log(value);*/

            $scope.selection.ids[k] = newValue;
        });
        console.log($scope.selection.ids);

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

    $scope.multipleDelete = true;

    $scope.selection = {};
    $scope.selection.ids = {};
    /* -- multiple delete functions END -- */

    function loadTable() {
        if (!angular.isUndefined($routeParams.packageid))
            http.get(url + $routeParams.packageid)
                .success(function (response, status) {
                    console.log(response);
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


    angular.element(document).ready(function () {
        if (angular.isUndefined($routeParams.packageid)) {
            var previewNode = document.querySelector("#template");
            previewNode.id = "";
            var previewTemplate = previewNode.parentNode.innerHTML;
            previewNode.parentNode.removeChild(previewNode);

            var header = {};

            if ($cookieStore.get('token') !== '')
                header = {'Authorization': 'Bearer ' + $cookieStore.get('token')};

            header['project-id'] = $cookieStore.get('project').id;
            var myDropzone = new Dropzone('#my-dropzone', {
                url: url, // Set the url
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
                    this.on("queuecomplete", function (file, responseText) {
                        $scope.$apply(function ($scope) {
                            showOk("Uploaded the VNF Package");
                            loadTable();
                        });

                    });
                    this.on("error", function (file, responseText) {
                        console.log(responseText);
                        $scope.$apply(function ($scope) {
                            showError(responseText.message, "422");
                        });
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



