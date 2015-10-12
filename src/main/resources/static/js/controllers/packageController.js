var app = angular.module('app');
app.controller('PackageCtrl', function ($scope, serviceAPI, $routeParams, http, $cookieStore, AuthService) {

    //var url = '/api/v1/vnf-packages/';
    var url = 'http://localhost:8080/api/v1/vnf-packages/';

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
                showError(status, response);
            });
    };
    $scope.updateServicePut = function (service) {
        var id = service.id;
        serviceAPI.cleanService(service);

        delete service.locations;
        console.log(service);

        $('.modal').modal('hide');

        http.put(url + id, service)
            .success(function (response) {
                $('.modal').modal('hide');

                showOk('Service ' + service.serviceType + ' Updated.');
                loadTable();

            })
            .error(function (data, status) {
                $('.modal').modal('hide');

                showError(data, status);
                loadTable();

            });


    };

    $scope.launchService = function (service) {
        var serv = angular.copy(service);
        serviceAPI.cleanService(serv);
        serv.instanceName = serv.serviceType + '-' + serviceAPI.getRandom();
        $scope.serviceEdit = _.omit(serv, 'maxNumInst', 'minNumInst', 'networkIds', 'flavour');

    };

    $scope.updateService = function (service) {
        $scope.serviceEdit = service;
    };


    $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
    };
    $scope.showData = function (data) {
        $cookieStore.put('service', data);
    };

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

    function showError(status, data) {
        $scope.alerts.push({
            type: 'danger',
            msg: 'ERROR: <strong>HTTP status</strong>: ' + status + ' response <strong>data</strong> : ' + JSON.stringify(data)
        });
        $('.modal').modal('hide');
        if (status === 401) {
            console.log(status + ' Status unauthorized')
            AuthService.logout();
            $window.location.reload();
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

            var myDropzone = new Dropzone(document.body, { // Make the whole body a dropzone
                url: url, // Set the url
                method: "POST",
                parallelUploads: 20,
                previewTemplate: previewTemplate,
                autoQueue: false, // Make sure the files aren't queued until manually added
                previewsContainer: "#previews" // Define the container to display the previews
            });



            myDropzone.on("addedfile", function (file) {
                // Hookup the start button
                file.previewElement.querySelector(".start").onclick = function () {
                    myDropzone.enqueueFile(file);
                };
            });

// Update the total progress bar
            myDropzone.on("totaluploadprogress", function (progress) {
                $('.progress .bar:first').width = progress + "%";
            });

            myDropzone.on("sending", function (file) {
                // Show the total progress bar when upload starts
                $('.progress .bar:first').opacity = "1";
                // And disable the start button
                file.previewElement.querySelector(".start").setAttribute("disabled", "disabled");
            });

// Hide the total progress bar when nothing's uploading anymore
            myDropzone.on("queuecomplete", function (progress) {
                $('.progress .bar:first').opacity = "0";
            });


            $(".start").onclick = function () {
                myDropzone.enqueueFiles(myDropzone.getFilesWithStatus(Dropzone.ADDED));
            };
            $(".cancel").onclick = function () {
                myDropzone.removeAllFiles(true);
            };
        }
    });


    // UPLOAD CLASS DEFINITION


});



