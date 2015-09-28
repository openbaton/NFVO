angular.module('app', ['ngRoute', 'ngSanitize', 'ui.bootstrap', 'ngCookies'])
    .config(function ($routeProvider) {

        $routeProvider.
            when('/login', {
                templateUrl: 'login.html',
                controller: 'LoginController'
            }).
            when('/', {
                templateUrl: 'pages/contents.html',
                controller: 'MenuCtrl'
            }).
            when('/services', {
                templateUrl: 'pages/services/services.html',
                controller: 'ServiceCtrl'
            }).
            when('/services/:serviceid', {
                templateUrl: 'pages/services/serviceinfo.html',
                controller: 'ServiceCtrl'
            }).
            when('/nsdescriptors', {
                templateUrl: 'pages/nsdescriptors/nsdescriptors.html',
                controller: 'NsdCtrl'
            }).
            when('/nsdescriptors/:nsdescriptorId', {
                templateUrl: 'pages/nsdescriptors/nsdescriptorinfo.html',
                controller: 'NsdCtrl'
            }).
            when('/nsdescriptors/:nsdescriptorId/vnfdescriptors/', {
                templateUrl: 'pages/nsdescriptors/vnfdescriptors.html',
                controller: 'NsdCtrl'
            }).
            when('/nsdescriptors/:nsdescriptorId/vnfdescriptors/:vnfdescriptorId', {
                templateUrl: 'pages/nsdescriptors/vnfdescriptor.html',
                controller: 'NsdCtrl'
            }).
            when('/nsdescriptors/:nsdescriptorId/vnfdescriptors/:vnfdescriptorId/vdus/:vduId', {
                templateUrl: 'pages/nsdescriptors/vdu.html',
                controller: 'NsdCtrl'
            })
            .when('/nsrecords/:nsdescriptorId/vnfrecords/:vnfrecordId/vdus/:vduId', {
                templateUrl: 'pages/nsrecords/vdu.html',
                controller: 'NsrCtrl'
            }).
            when('/nsdescriptors/:nsdescriptorId/vnfdependencies/:vnfdependencyId', {
                templateUrl: 'pages/nsdescriptors/vnfdependency.html',
                controller: 'NsdCtrl'
            }).
            when('/nsdescriptors/:nsdescriptorId/vnfdependencies/', {
                templateUrl: 'pages/nsdescriptors/vnfdependencies.html',
                controller: 'NsdCtrl'
            }).
            when('/nsdescriptors/:nsdescriptorId/graph', {
                templateUrl: 'pages/nsdescriptors/graph.html',
                controller: 'NsdCtrl'
            }).
            when('/nsrecords', {
                templateUrl: 'pages/nsrecords/nsrecords.html',
                controller: 'NsrCtrl'
            }).
            when('/nsrecords/:nsrecordId', {
                templateUrl: 'pages/nsrecords/nsrecordinfo.html',
                controller: 'NsrCtrl'
            }).
            when('/nsrecords/:nsrecordId/graph', {
                templateUrl: 'pages/nsrecords/graph.html',
                controller: 'NsrCtrl'
            }).
            when('/nsrecords/:nsrecordId/vnfrecords/:vnfrecordId', {
                templateUrl: 'pages/nsrecords/vnfrecord.html',
                controller: 'NsrCtrl'
            }).
            when('/nsrecords/:nsrecordId/vnfrecords/', {
                templateUrl: 'pages/nsrecords/vnfrecords.html',
                controller: 'NsrCtrl'
            }).
            when('/nsrecords/:nsrecordId/vnfdependencies/', {
                templateUrl: 'pages/nsrecords/vnfdependencies.html',
                controller: 'NsrCtrl'
            }).
            when('/nsrecords/:nsrecordId/vnfdependencies/:vnfdependencyId', {
                templateUrl: 'pages/nsrecords/vnfdependency.html',
                controller: 'NsrCtrl'
            }).
            when('/infrastructures', {
                templateUrl: 'pages/infrastructures/infrastructures.html',
                controller: 'InfrastructureCtrl'
            }).
            when('/infrastructures/:infrastructureid', {
                templateUrl: 'pages/infrastructures/infrastructureinfo.html',
                controller: 'InfrastructureCtrl'
            }).
            when('/switches', {
                templateUrl: 'pages/switches/switches.html',
                controller: 'SwitchCtrl'
            }).
            when('/switches/:switchid', {
                templateUrl: 'pages/switches/switchinfo.html',
                controller: 'SwitchCtrl'
            }).
            when('/flow/:flowid', {
                templateUrl: 'pages/switches/flow.html',
                controller: 'SwitchCtrl'
            }).
            when('/vim-instances', {
                templateUrl: 'pages/vim-instances/vim-instances.html',
                controller: 'vimInstanceCtrl'
            }).
            when('/vim-instances/:vimInstanceId', {
                templateUrl: 'pages/vim-instances/vim-instanceinfo.html',
                controller: 'vimInstanceCtrl'
            }).
            when('/controllers/:controllerid', {
                templateUrl: 'pages/controllers/controllerinfo.html',
                controller: 'ControllerCtrl'
            }).
            when('/controllers', {
                templateUrl: 'pages/controllers/controllers.html',
                controller: 'ControllerCtrl'
            }).
            when('/chains', {
                templateUrl: 'pages/chains/chains.html',
                controller: 'ChainsCtrl'
            }).
            when('/chains/:chainid', {
                templateUrl: 'pages/chains/chaininfo.html',
                controller: 'ChainsCtrl'
            }).
            when('/dragdrop', {
                templateUrl: 'pages/tabset.html',
                controller: 'DragDropCtrl'
            }).
            when('/copyright', {
                templateUrl: 'pages/copyright.html',
                controller: ''
            }).
            otherwise({
//                        redirectTo: '/'
            });

    });

/**
 *
 * Redirects an user not logged in
 *
 */

angular.module('app').run(function ($rootScope, $location, $cookieStore, $route) {
    //$route.reload();
    $rootScope.$on('$routeChangeStart', function (event, next) {

//        console.log($cookieStore.get('logged'));
        if ($cookieStore.get('logged') === false || angular.isUndefined($cookieStore.get('logged'))) {
            // no logged user, we should be going to #login
            if (next.templateUrl === "login.html") {
                // already going to #login, no redirect needed
            } else {
                // not going to #login, we should redirect now
                $location.path("/login");

            }
        }

    });
});


/*
 * MenuCtrl
 *
 * shows the modal for changing the Settings
 */

angular.module('app').controller('MenuCtrl', function ($scope, http) {
    $scope.config = {};
    var url = 'http://localhost:8080/api/v1';
//    http.syncGet('/api/rest/admin/v2/configs/').then(function(data)
//    {
//        $scope.config = data;
////        console.log($scope.config.parameters);
//
//    });


    $scope.numberNSR = 0;
    $scope.numberNSD = 0;
    $scope.numberVNF = 8;
    $scope.numberUnits = 5;
    http.syncGet(url + '/ns-descriptors/').then(function (data) {
        $scope.numberNSD = data.length;

    });
    http.syncGet(url + '/ns-records/').then(function (data) {
        $scope.numberNSR = data.length;


    });


    $scope.saveSetting = function (config) {
        console.log(config);
        $('.modal').modal('hide');
        $('#modalSend').modal('show');

        http.post('/api/rest/admin/v2/configs/', config)
            .success(function (response) {
                $('.modal').modal('hide');
                alert('Configurations Updated! ' + response);

            })
            .error(function (response, status) {
                $('.modal').modal('hide');
                alert('ERROR: <strong>HTTP</strong> status:' + status + ' response <strong>response:</strong>' + response);
            });
    };

//    window.onresize = function() {
//        window.location.reload();
//    };


});