var app = angular.module('app');
app.controller('PackageCtrl', function ($scope, serviceAPI, $routeParams, http, $cookieStore, AuthService) {

    var url = '/api/v1/vnf-packages/';
    //var url = 'http://localhost:8080/api/v1/vnf-packages/';

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


    $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
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

            var myDropzone = new Dropzone('#my-dropzone', {
                url: url, // Set the url
                method: "POST",
                parallelUploads: 20,
                previewTemplate: previewTemplate,
                autoQueue: false, // Make sure the files aren't queued until manually added
                previewsContainer: "#previews", // Define the container to display the previews
                headers: header

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

            myDropzone.on("sending", function (file, xhr, formData) {
                // Show the total progress bar when upload starts
                $('.progress .bar:first').opacity = "1";

                // And disable the start button
                file.previewElement.querySelector(".start").setAttribute("disabled", "disabled");
            });

// Hide the total progress bar when nothing's uploading anymore
            myDropzone.on("queuecomplete", function (progress) {
                $('.progress .bar:first').opacity = "0";
                showOk("Uploaded the VNF Package");
                loadTable();
            });


            $(".start").onclick = function () {
                myDropzone.enqueueFiles(myDropzone.getFilesWithStatus(Dropzone.ADDED));
            };
            $(".cancel").onclick = function () {
                myDropzone.removeAllFiles(true);
            };
        }
    });


});



