angular.module('app').factory('serviceAPI', function(http, $q) {

    var services = {};
    var serviceToLaunch;
    /*
     * 
     * @returns {Array} Array of locations
     */
    services.getLocations = function() {
        var locations = [];
        http.get('/api/rest/admin/v2/vim-instances').success(function(response) {
            for (var i = 0; i < response.length; i++)
                locations.push(response[i].location.name);
        });
        return locations;
    };
    /*
     * 
     * @returns {List} List of Services
     */
    services.getServices = function() {
        var deferred = $q.defer();
        http.get('/api/rest/orchestrator/v2/packages').success(function(data, status) {
            deferred.resolve(data);
        });
        return deferred.promise;
    };
    /* From uptime to string
     * 
     * @param {type} long
     * @returns {unresolved} Return a string like N weeks, M days 
     */

    services.returnStringUptime = function(long) {


        var upTime = new Date(long);
        var now = new Date($.now());
        Date.dateDiff = function(datepart, fromdate, todate) {
            datepart = datepart.toLowerCase();
            var diff = todate - fromdate;
            var divideBy = {
                w: 604800000,
                d: 86400000,
                h: 3600000,
                n: 60000,
                s: 1000
            };
            return Math.floor(diff / divideBy[datepart]);
        };
        var result = {};
        var time = {
            'y': 'years',
            'm': 'months',
            'w': 'weeks',
            'd': 'days',
            'h': 'hours',
            'n': 'minutes',
            's': 'seconds'
        };
        var timeN = {
            'years': 1,
            'months': 12,
            'weeks': 4,
            'days': 7,
            'hours': 24,
            'minutes': 60,
            'seconds': 60
        };
        $.each(time, function(i, k) {
            var diff = Date.dateDiff(i, upTime, now);
            if (!_.isNaN(diff) && diff !== 0)
                result[k] = diff;
        });
        var uptimeString = '';
        var i = 0;
        $.each(result, function(key, value) {
            if (i <= 1) {
                if (value > timeN[key]) {
                    time = value % timeN[key];
                    uptimeString += time + ' ' + key + ', ';
                } else {
                    time = value;
                    uptimeString += time + ' ' + key + ', ';
                }
                i++;
            }
        });
        var stringUptime = uptimeString.substring(0, uptimeString.length - 2);
        return stringUptime;
    };
    /* Random number
     * 
     * @returns {Number}
     */
    services.getRandom = function() {
        return Math.floor((Math.random() * 100) + 1);
    };
    /**Clean a service Json
     *
     * @param {type} service
     * @returns {unresolved}
     */
    services.cleanService = function(service) {
        delete service.id;
        delete service.version;
        for (var i = 0; i < service.configuration.parameters.length; i++) {
            delete service.configuration.parameters[i].id;
        }
        delete service.configuration.configurationId;
        delete service.configuration.configurationName;
        return service;
    };


    services.cleanDC = function(dc) {
        delete dc.id;
        delete dc.version;
        delete dc.configuration.id;
        delete dc.configuration.version;
        for (var i = 0; i < dc.configuration.parameters.length; i++) {
            delete dc.configuration.parameters[i].id;
            delete dc.configuration.parameters[i].version;
        }
        delete dc.switch.configurationId;
        for (var i = 0; i < dc.switch.ports.length; i++) {
            delete dc.switch.ports[i].id;
            delete dc.switch.ports[i].version;
        }
        for (var i = 0; i < dc.subnets.length; i++) {
            delete dc.subnets[i].id;
            delete dc.subnets[i].version;
        }
        delete dc.switch.id;
        delete dc.switch.version;
        delete dc.location.id;
        delete dc.location.version;
        return dc;
    };
    /** return an array of keys by value
     *
     * @param {type} obj
     * @param {type} value
     * @returns {Array}
     */
    services.returnKeys = function returnKeys(obj, value) {
        var keys = [];
        _.each(obj, function(val, key) {
            if (val === value) {
                keys.push(key);
            }
        });
        return keys;
    };
    /** return a string with list of keys by value separated by comma
     *
     * @param {type} obj
     * @param {type} value
     * @returns {String}
     */
    services.returnKeysString = function returnKeysString(obj, value) {
        var keys = '';
        _.each(obj, function(val, key) {
            if (val === value) {
                keys += key + ',';
            }
        });
        return keys.substring(0, keys.length - 1);
    };
    /** the list of relations
     *
     * @returns {Array}
     */
    //packages.getRelations = function() {
    //    var relations = [];
    //    http.get('/api/rest/orchestrator/v2/relations').success(function(data, status) {
    //        relations = data;
    //    });
    //    return relations;
    //};
    /** Set lat and lon in the dataJson by city
     *
     * @param {type} city
     * @param {type} dataJson
     * @returns {unresolved}
     */
    services.setCoordinates = function(city, dataJson) {

        var url = 'http://maps.googleapis.com/maps/api/geocode/json?address=' + city + '&sensor=false';
// for synchronizing the call with the controller
        var deferred = $q.defer();
        http.get(url).success(function(data, status) {
            dataJson.location.name = city;
            dataJson.location.latitude = data.results[0].geometry.location.lat;
            dataJson.location.longitude = data.results[0].geometry.location.lng;
            deferred.resolve(dataJson);
//            console.log(dataJson);

        });
        return deferred.promise;
    };
    return services;
});