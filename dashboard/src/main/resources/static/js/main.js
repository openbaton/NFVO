require({
    baseUrl: 'js',
    paths: {
        jquery: 'libs/jquery/jquery.min',
        jquery_ui: 'libs/jquery/jquery-ui.min',
        bootstrapJS: "../bower_components/bootstrap/dist/js/bootstrap.min",
        metisMenu: "../bower_components/metisMenu/dist/metisMenu.min",
        sb_admin_2: "../dist/js/sb-admin-2",
        underscore: "libs/underscore/underscore-min",
        raphael: "libs/raphael",
        morris: "morris/morris-0.4.3.min",
        angular: "libs/angular/angular.min",
        jQueryRotate: "libs/jquery/jQueryRotate",
        angular_route: "libs/angular/angular-route.min",
        angular_cookies: "libs/angular/angular-cookies.min",
        d3: "libs/d3.min",
        ui_bootstrap: "libs/angular/ui-bootstrap-tpls-0.10.0.min",
        app: "app",
        angular_sanitize: "libs/angular/angular-sanitize",
        tables: 'tablesorter/tables',
        tablesorter: 'tablesorter/jquery.tablesorter',
        httpService: "services/httpService",
        authService:"services/authService",
        servicesServices: "services/servicesServices",
        servicesController: "controllers/servicesController",
        vimInstanceController: "controllers/vimInstanceController",
        topologiesServices: "services/topologiesServices",
        nsdController: "controllers/nsdController",
        nsrController: "controllers/nsrController",
        jquery_jsPlumb: "libs/jquery/jquery.jsPlumb-1.5.3-min",
        indexController: "controllers/indexController",
        dragDropController: "controllers/dragDropController",
        switchController: "controllers/switchController",
        infrastructuresController: "controllers/infrastructuresController",
        controllersController: 'controllers/controllersController',
        chainsController: 'controllers/chainsController'
    },
    shim: {
        jquery: {
            exports: '$'
        },
        sb_admin_2: {
            deps: ['jquery', 'metisMenu']
        },
        metisMenu: {
            deps: ['jquery']
        },
        bootstrapJS: {
            deps: ['jquery']
        },
        angular: {
            exports: 'angular',
            deps: ['jquery', 'bootstrapJS', 'metisMenu', 'sb_admin_2']
        },
        d3: {
            exports: 'd3'
        },
        jQueryRotate: {
            deps: ['jquery']
        },
        boot: {
            deps: ['jquery']
        },
        jquery_ui: {
            deps: ['jquery']
        },
        jquery_jsPlumb: {
            deps: ['jquery', 'jquery_ui', 'underscore']
        },
        bootstrap: {
            deps: ['app']
        },
        underscore: {
            exports: '_',
            deps: ['jquery']
        },
        common: {
            deps: ['morris']
        },
        morris: {
            deps: ['jquery', 'raphael']
        },
        app: {
            deps: ['angular', 'angular_route', 'angular_sanitize', 'ui_bootstrap']
        },
        angular_route: {
            deps: ['angular']
        },
        angular_cookies: {
            deps: ['angular']
        },
        angular_sanitize: {
            deps: ['angular']
        },
        ui_bootstrap: {
            deps: ['angular']
        },
        authService:{
            deps:['app']
        },
        servicesServices: {
            deps: ['app']
        },
        nsdController: {
            deps: ['app', 'servicesServices', 'httpService', 'underscore', 'topologiesServices', 'angular_cookies']
        },
        nsrController: {
            deps: ['app', 'servicesServices', 'httpService', 'underscore', 'topologiesServices', 'angular_cookies']
        },
        vimInstanceController: {
            deps: ['app', 'servicesServices', 'httpService']
        },
        switchController: {
            deps: ['app', 'servicesServices', 'httpService']
        },
        servicesController: {
            deps: ['app', 'servicesServices', 'httpService', 'angular_cookies']
        },
        infrastructuresController: {
            deps: ['app', 'servicesServices', 'httpService', 'topologiesServices', 'jQueryRotate']
        },
        controllersController: {
            deps: ['app', 'servicesServices', 'httpService']
        },
        chainsController: {
            deps: ['app', 'servicesServices', 'httpService', 'topologiesServices']
        },
        indexController: {
            deps: ['app', 'httpService', 'servicesServices', 'morris','authService']
        },
        httpService: {
            deps: ['app']
        },
        dragDropController: {
            deps: ['app', 'httpService', 'jquery_jsPlumb', 'servicesServices']
        },
        topologiesServices: {
            deps: ['app', 'httpService', 'd3', 'jquery_jsPlumb', 'servicesServices']
        }
    }
}), require([
    'require',
    'bootstrapJS',
    'metisMenu',
    'sb_admin_2',
    'underscore',
    'jquery_jsPlumb',
    'angular',
    'angular_route',
    'indexController',
    'servicesController',
    'servicesController',
    'nsdController',
    'nsrController',
    'vimInstanceController',
    'switchController',
    'infrastructuresController',
    'controllersController',
    'dragDropController',
    'chainsController'

], function(require) {
    return require(['bootstrap']);
});