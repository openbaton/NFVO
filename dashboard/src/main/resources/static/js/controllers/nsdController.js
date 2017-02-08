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

var app = angular.module('app')

app.controller('NsdCtrl', function ($scope, $compile, $cookieStore, $routeParams, $filter, http, serviceAPI, $window, $route, $interval, $http, topologiesAPI, AuthService, NgTableParams) {
    var baseURL = $cookieStore.get('URL') + "/api/v1";

    var url = baseURL + '/ns-descriptors/';
    var urlRecord = baseURL + '/ns-records/';
    var urlVim = baseURL + '/datacenters/';
    var urlVNFD = baseURL + '/vnf-descriptors/';
    var dropzoneUrl = baseURL + '/csar-nsd/';
    var basicConf = {description:"", confKey:"", value:""};
   
    $scope.selectedVNFD = "";
    $scope.list = {}
    $scope.nsdToSend = {};
    $scope.textTopologyJson = '';
    $scope.file = '';
    $scope.alerts = [];
    $scope.vimInstances = [];
    $scope.keys = [];
    $scope.launchKeys = [];
    $scope.launchObj = {"keys": [], configurations:{}};
    $scope.launchNsdVim = [];
    $scope.launchPops = {};
    $scope.launchPopsAvailable = {};
    $scope.vnfdLevelVim = false;
    $scope.vnfdToVIM = [];
    $scope.vduLevelVim = [];
    $scope.vduToVIM = [];
    $scope.vduWithName = 0;
    $scope.tmpVnfd = [];
    $scope.elementName = "";
    $scope.basicConfiguration = {name:"", config:{name:"", configurationParameters:[]}};
    

    loadTable();
    loadKeys();
    loadVIMs();
    $.fn.bootstrapSwitch.defaults.size = 'mini';

    $('#set-flavor').bootstrapSwitch();


    $('#set-flavor').on('switchChange.bootstrapSwitch', function (event, state) {
        $scope.showSetting = state;
        //console.log($scope.showSetting);
        $scope.$apply(function () {
            $scope.showSetting;
        });

    });

    var filteredLaunchKeys = []
    $scope.tableParamsFilteredLaunchKeys = new NgTableParams({
            page: 1,
            count: 5,
            sorting: {
                name: 'asc'     // initial sorting
            },
            filter: {name: ""},
        },
        {
            counts: [5, 10, 20],
            total: filteredLaunchKeys.length,
            getData: function (params) {
                filteredLaunchKeys = params.sorting() ? $filter('orderBy')($scope.launchKeys, params.orderBy()) : $scope.launchKeys;
                //filteredLaunchKeys = params.filter() ? $filter('filter')(filteredLaunchKeys, params.filter()) : filteredLaunchKeys;
                $scope.tableParamsFilteredLaunchKeys.total(filteredLaunchKeys.length);
                filteredLaunchKeys = filteredLaunchKeys.slice((params.page() - 1) * params.count(), params.page() * params.count());
                for (i = filteredLaunchKeys.length; i < params.count(); i++) {
                    filteredLaunchKeys.push({'name': ""})
                }
                return filteredLaunchKeys;
            }
        });

    var filteredKeys = []
    $scope.tableParamsFilteredKeys = new NgTableParams({
            page: 1,
            count: 5,
            sorting: {
                name: 'asc'     // initial sorting
            },
            filter: {name: ""},
        },
        {
            counts: [5, 10, 20],
            total: filteredKeys.length,
            getData: function (params) {
               // console.log($scope.keys);
                filteredKeys = params.sorting() ? $filter('orderBy')($scope.keys, params.orderBy()) : $scope.keys;
                filteredKeys = params.filter() ? $filter('filter')(filteredKeys, params.filter()) : filteredKeys;
                $scope.tableParamsFilteredKeys.total(filteredKeys.length);
                filteredKeys = filteredKeys.slice((params.page() - 1) * params.count(), params.page() * params.count());
                for (i = filteredKeys.length; i < params.count(); i++) {
                    filteredKeys.push({'name': ""})
                }
                return filteredKeys;
            }
        });

    var filteredPops = []
    $scope.tableParamsFilteredPops = new NgTableParams({
            page: 1,
            count: 5,
            sorting: {
                name: 'asc'     // initial sorting
            },
            filter: {name: ""},
        },
        {
            counts: [5, 10, 20],
            total: filteredPops.length,
            getData: function (params) {
                filteredPops = params.sorting() ? $filter('orderBy')($scope.launchPopsAvailable[$scope.selectedVnfd.name].pops, params.orderBy()) : $scope.launchPopsAvailable[$scope.selectedVnfd.name].pops;
                filteredPops = params.filter() ? $filter('filter')(filteredPops, params.filter()) : filteredPops;
                $scope.tableParamsFilteredPops.total(filteredPops.length);
                filteredPops = filteredPops.slice((params.page() - 1) * params.count(), params.page() * params.count());
                for (i = filteredPops.length; i < params.count(); i++) {
                    filteredPops.push({'name': ""})
                }
                return filteredPops;
            }
        });

    var filteredLaunchPops = [];
    $scope.selectedVnfd = "";
    $scope.tableParamsFilteredLaunchPops = new NgTableParams({
            page: 1,
            count: 5,
            sorting: {
                name: 'asc'     // initial sorting
            },
            filter: {name: ""},
        },
        {
            counts: [5, 10, 20],
            total: filteredLaunchPops.length,
            getData: function (params) {
                //console.log($scope.selectedVnfd);
                filteredLaunchPops = params.sorting() ? $filter('orderBy')($scope.launchPops[$scope.selectedVnfd.name].pops, params.orderBy()) : $scope.launchPops[$scope.selectedVnfd.name].pops;
                //filteredLaunchPops = params.filter() ? $filter('filter')(filteredLaunchPops, params.filter()) : filteredLaunchPops;
                $scope.tableParamsFilteredPops.total(filteredLaunchPops.length);
                filteredLaunchPops = filteredLaunchPops.slice((params.page() - 1) * params.count(), params.page() * params.count());
                for (i = filteredLaunchPops.length; i < params.count(); i++) {
                    filteredLaunchPops.push({'name': ""})
                }
                return filteredLaunchPops;
            }
        });

    $scope.selectVnfd = function (vnfd) {
        $scope.selectedVnfd = vnfd;
        $scope.tableParamsFilteredLaunchPops.reload();
        $scope.tableParamsFilteredPops.reload();
        //console.log($scope.selectedVnfd);
    }

    function loadKeys() {

        //console.log($routeParams.userId);
        http.get(baseURL + '/keys')
            .success(function (response) {
                $scope.keys = response;
                //console.log($scope.users.length);

                //console.log($scope.keys);
            })
            .error(function (data, status) {
                showError(data, status);
            });

    }

    function loadVIMs() {

        //console.log($routeParams.userId);
        var promise = http.get(urlVim)
            .success(function (response) {
                $scope.vimInstances = response;
                console.log($scope.vimInstances);
            })
            .error(function (data, status) {
                showError(data, status);
            });
        promise.then(console.log($scope.vimInstances));
        console.log($scope.vimInstances);
    }

    $scope.addLaunchKey = function (key) {
        $scope.launchKeys.push(key);
        console.log($scope.launchKeys);
        remove($scope.keys, key);
        $scope.tableParamsFilteredKeys.reload();
        $scope.tableParamsFilteredLaunchKeys.reload();
    }

    $scope.removeLaunchKey = function (key) {
        $scope.keys.push(key);
        remove($scope.launchKeys, key);
        $scope.tableParamsFilteredKeys.reload();
        $scope.tableParamsFilteredLaunchKeys.reload();
    }

    function remove(arr, item) {
        //console.log(arr);
        //console.log(item);
        for (var i = arr.length; i--;) {
            if (arr[i].name === item.name) {
                arr.splice(i, 1);
            }
        }
        //console.log(arr);
    }

    // http.get(urlVim)
    //     .success(function (response, status) {
    //         $scope.vimInstances = response;
    //         //console.log(response);
    //     })
    //     .error(function (data, status) {
    //         showError(data, status);
    //
    //     });


    // $scope.tabs = [
    //     {active: true},
    //     {active: false},
    //     {active: false}
    // ];

    $scope.selection = [];
    function checkPresence(link, links) {
        console.log(links);
        for (i = 0; i < links.length; i++) {
            console.log(links[i].name + " " + link.name );
            if (links[i].name === link.name) {
                return true;
            }

        }
        return false;

    };
    $scope.addTONSD = function (selectedVNFD) {
        console.log($scope.selectedVNFD);
        $scope.nsdCreateTmp.vnfd.push({id:selectedVNFD.id});
         $scope.tmpVnfd.push(angular.copy(selectedVNFD));
     
        
                selectedVNFD.virtual_link.map(function(link) {
                     console.log(checkPresence(link, $scope.nsdCreateTmp.vld));
                    if (!checkPresence(link, $scope.nsdCreateTmp.vld)) {
                       $scope.nsdCreateTmp.vld.push(link);
                    }
                    
               
                });
        console.log($scope.nsdCreateTmp);
    };


    $scope.saveDependency = function () {
        //console.log($scope.dependency);
        $scope.nsdCreateTmp.vnf_dependency.push(angular.copy($scope.dependency));
        //console.log($scope.nsdCreateTmp.vnf_dependency);
        //console.log($scope.dependency);

        $('#modalDependency').modal('hide');
    };
    $scope.deleteVNFDfromNSD = function(index) {
        $scope.tmpVnfd.splice(index, 1);
        $scope.nsdCreateTmp.vnfd.splice(index, 1);
    };
    $scope.selectedVNFD;
    $scope.vnfdList = [];

    $scope.dependency = {};
    $scope.dependency.parameters = [];

    $scope.addParam = function (par) {
        if (angular.isUndefined(par)) {
            return;
        }
        if (par.length > 0) {
          $scope.dependency.parameters.push(par);
        }
    };

    $scope.removeParam = function (index) {
        $scope.dependency.parameters.splice(index, 1);
    };

    $scope.addVld = function (vld) {
        if (vld) {
            $scope.nsdCreateTmp.vld.push({'name': vld});
        }
    };

    $scope.removeVld = function (index) {
        $scope.nsdCreateTmp.vld.splice(index, 1);
    };

    $scope.deleteDependency = function (index) {
        $scope.nsdCreateTmp.vnf_dependency.splice(index, 1);
    };

    $scope.isArray = function (obj) {
        if (angular.isArray(obj) || angular.isObject(obj))
            return false;
        else
            return true;
    };
    $scope.edit = function (obj) {
        $scope.editObj = obj;
    };

    $scope.updateObj = function () {
        http.put(url + $scope.editObj.id, $scope.editObj)
            .success(function (response) {
                showOk('Network Service Descriptor updated!');
                loadTable();
            })
            .error(function (data, status) {
                console.error('STATUS: ' + status + ' DATA: ' + JSON.stringify(data));
                showError(data, status);
            });
    };

    $scope.updateVNFD = function () {
        http.put(url + $routeParams.nsdescriptorId + '/vnfdescriptors/' + $scope.editObj.id, $scope.editObj)
            .success(function (response) {
                showOk('VNF Descriptor updated!');
                loadTable();
            })
            .error(function (data, status) {
                console.error('STATUS: ' + status + ' DATA: ' + JSON.stringify(data));
                showError(data, status);
            });
    };

    $scope.addNewConfig = function () {
        if (angular.isUndefined($scope.editObj.configurations)) {
            $scope.editObj.configurations = {};
            $scope.editObj.configurations.configurationParameters = [];
        }
        $scope.editObj.configurations.configurationParameters.push({'confKey': '', 'value': ''})
    };
    $scope.removeConfig = function (index) {
        $scope.editObj.configurations.configurationParameters.splice(index, 1);
    };

    $scope.addLifecycleEvent = function (vdu) {
        vdu.lifecycle_event.push({'event': "CONFIGURE", 'lifecycle_events': []})
    };


    $scope.loadVNFD = function () {
        $scope.nsdCreateTmp = {};
        $scope.nsdCreateTmp.name = '';
        $scope.nsdCreateTmp.vendor = '';
        $scope.nsdCreateTmp.version = '';
        $scope.nsdCreateTmp.vnfd = [];
        $scope.nsdCreateTmp.vnf_dependency = [];
        $scope.nsdCreateTmp.vld = [];
        $scope.tmpVnfd = [];

        http.get(urlVNFD)
            .success(function (response, status) {
                $scope.vnfdList = response;
                //console.log(response);
                $('#modalCreateNSD').modal('show');
                $scope.selectedVNFD = $scope.vnfdList[0];
            })
            .error(function (data, status) {
                showError(data, status);
            });
        
            
    };

    $scope.toggleSelection = function toggleSelection(image) {
        var idx = $scope.selection.indexOf(image);
        if (idx > -1) {
            $scope.selection.splice(idx, 1);
        }
        else {
            $scope.selection.push(image);
        }
        //console.log($scope.selection);
        $scope.vduCreate.vm_image = $scope.selection;
    };


    $scope.deleteVDU = function (index) {
        $scope.vnfdCreate.vdu.splice(index, 1);
    };


    $scope.deleteVNFDependency = function (vnfd) {
        http.delete(url + $scope.nsdinfo.id + '/vnfdependencies/' + vnfd.id)
            .success(function (response) {
                showOk('Deleted VNF Dependecy with id: ' + vnfd.id);
                loadTable();
            })
            .error(function (data, status) {
                console.error('STATUS: ' + status + ' DATA: ' + data);
                showError(status, JSON.stringify(data));
            });

    };


    $scope.deleteVNFD = function (vnfd) {
        http.delete(url + $scope.nsdinfo.id + '/vnfdescriptors/' + vnfd.id)
            .success(function (response) {
                showOk('Deleted VNF Descriptors with id: ' + vnfd.id);
                loadTable();
            })
            .error(function (data, status) {
                console.error('STATUS: ' + status + ' DATA: ' + data);
                showError(status, JSON.stringify(data));
            });
    };


    if (!angular.isUndefined($routeParams.vnfdescriptorId))
        $scope.vnfdescriptorId = $routeParams.vnfdescriptorId;


    if (!angular.isUndefined($routeParams.vnfdependencyId))
        $scope.vnfdependencyId = $routeParams.vnfdependencyId;

    if (!angular.isUndefined($routeParams.vduId)) {
        $scope.vduId = $routeParams.vduId;
        //console.log($scope.vduId);
    }


    $scope.sendNSDCreate = function (nsdCreate) {
        $('.modal').modal('hide');
        //console.log($scope.nsdCreateTmp);
        http.post(url, $scope.nsdCreateTmp)
            .success(function (response) {
                showOk('Network Service Descriptor stored!');
                loadTable();
            })
            .error(function (data, status) {
                console.error('STATUS: ' + status + ' DATA: ' + JSON.stringify(data));
                showError(data, status);
            });
    };


    $scope.setFile = function (element) {
        $scope.$apply(function ($scope) {

            var f = element.files[0];
            if (f) {
                var r = new FileReader();
                r.onload = function (element) {
                    var contents = element.target.result;
                    $scope.file = contents;
                };
                r.readAsText(f);
            } else {
                alert("Failed to load file");
            }
        });
    };


    $scope.checkIP = function (str) {
        if (str === '')
            return true;
        else
            return /^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$|^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\-]*[A-Za-z0-9])$|^\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:)))(%.+)?\s*$/.test(str);

    };

    $scope.showTab = function (value) {
        return (value > 0);
    };

    $scope.sendFile = function (textTopologyJson) {
        $('.modal').modal('hide');
        var postNSD;
        var sendOk = true;
        var type = 'topology';
        if ($scope.file !== '') {
            postNSD = $scope.file;
            if (postNSD.charAt(0) === '<')
                type = 'definitions';
        }

        else if (textTopologyJson !== '') {
        
            postNSD = textTopologyJson;
        }

        else {
            alert('Problem with NSD');
            sendOk = false;

        }

        //console.log(postNSD);
        //console.log(type);

        if (sendOk) {
            if (type === 'topology') {
                console.log(postNSD);
                http.post(url, postNSD)
                    .success(function (response) {
                        showOk('Network Service Descriptors stored!');
                        loadTable();

                        //                        window.setTimeout($scope.cleanModal(), 3000);
                    })
                    .error(function (data, status) {
                        showError(data, status);
                    });
            }

            else {
                http.postXML('/api/rest/tosca/v2/definitions/', postNSD)
                    .success(function (response) {
                        showOk('Definition created!');
                        loadTable();
                        //                        window.setTimeout($scope.cleanModal(), 3000);
                    })
                    .error(function (data, status) {
                        showError(data, status);
                    });
            }
        }

        $scope.toggle = false;
        $scope.file !== '';
        //        $scope.packages = [];
    };


    $scope.isEmpty = function (obj) {
        if (angular.equals({}, obj))
            return true;
        else if (angular.equals([], obj))
            return true;
        else return false;
    };

    $scope.deleteNSD = function (data) {
        http.delete(url + data.id)
            .success(function (response) {
                showOk('Deleted Network Service Descriptor with id: ' + data.id);
                loadTable();
            })
            .error(function (data, status) {
                showError(data, status);
            });
    };
    $scope.addPoPtoNSD = function () {
        if (!$scope.vnfdLevelVim) {
            $scope.launchNsdVim.push($scope.vimInstances[0].name);
        }
    };
    $scope.clearPoPs = function () {
        $scope.launchNsdVim.splice(0);
    };
    $scope.addPoPtoVNFD = function (index) {
        if (!$scope.vnfdToVIM[index].vduLevel) {
            $scope.vnfdToVIM[index].vim.push($scope.vimInstances[0].name);
        }
    };

    $scope.clearPoPSVNFD = function (index) {
        $scope.vnfdToVIM[index].vim.splice(0);
    };

    $scope.addPoPtoVDU = function (index, parentindex) {
        //console.log(index, parentindex);
        $scope.vnfdToVIM[parentindex].vdu[index].vim.push($scope.vimInstances[0].name);
    };
    $scope.deletePoPfromVDU = function (parentparentindex, parentindex, index) {
        //console.log(index, parentindex, parentparentindex);
        $scope.vnfdToVIM[parentparentindex].vdu[parentindex].vim.splice(index, 1);
    };
    $scope.launchConfiguration = {"configurations":{}};
    $scope.vnfdnames = [];
    $scope.addConftoLaunch = function(vnfdname) {
        
        $scope.launchConfiguration.configurations[vnfdname].configurationParameters.push({description:"", confKey:"", value:""});
    };
    $scope.removeConf = function(index, vnfdname) {
         $scope.launchConfiguration.configurations[vnfdname].configurationParameters.splice(index, 1);
    };
    
    $scope.launchOption = function (data) {
        $scope.launchConfiguration = {"configurations":{}};
        $scope.vnfdnames = [];
        $scope.nsdToSend = data;
        $scope.nsdToSend.vnfd.map(function (vnfd) {
            $scope.vnfdnames.push(vnfd.name);
            $scope.launchConfiguration.configurations[vnfd.name] = {name:"", configurationParameters:[]};
        });
        console.log($scope.vnfdnames);
        //loadKeys();
        $scope.launchPops = {};
        $scope.vnfdToVIM.splice(0);
        $scope.vimForLaunch = {};
        $scope.vnfdLevelVim = false;
        $scope.vduWithName = 0;
        $scope.launchNsdVim.splice(0);

        $scope.loadVnfdTabs();

        for (i = 0; i < $scope.nsdToSend.vnfd.length; i++) {
            $scope.launchPops[$scope.nsdToSend.vnfd[i].name] = {};
            $scope.launchPops[$scope.nsdToSend.vnfd[i].name].pops = [];
            for (j = 0; j < $scope.nsdToSend.vnfd[i].vdu.length; j++) {
                //console.log($scope.nsdToSend.vnfd[i].vdu[j].id);
                $scope.launchPops[$scope.nsdToSend.vnfd[i].name][$scope.nsdToSend.vnfd[i].vdu[j].name] = [];
            }
            // for (i = 0; i < $scope.nsdToSend.vnfd.length; i++) {
            //     newVNFD = {"vnfdname": $scope.nsdToSend.vnfd[i].name, "vim": [], "vduLevel": false, "vdu": []};
            //     for (j = 0; j < $scope.nsdToSend.vnfd[i].vdu.length; j++) {
            //         //console.log($scope.nsdToSend.vnfd[i].vdu[j].id);
            //         newVDU = {"vduName": $scope.nsdToSend.vnfd[i].vdu[j].name, "vim": []};
            //         if (newVDU.vduName) {
            //             newVNFD.vdu.push(newVDU);
            //         }
            //     }
            //     if (newVNFD.vdu.length > 0) {
            //         $scope.vnfdToVIM.push(newVNFD);
            //     }
            // }
            //console.log('your stuff again' + $scope.vnfdToVIM);
            // $scope.vnfdLevelVim = false;
            //
            // for (i = 0; i < $scope.vnfdToVIM.length; i++) {
            //     for (j = 0; j < $scope.vnfdToVIM[i].vdu.length; j++) {
            //         //console.log($scope.vnfdToVIM[i].vdu[j].vduName);
            //         if ($scope.vnfdToVIM[i].vdu[j].vduName) {
            //             $scope.vduWithName++;
            //         }
            //
            //     }
            // }

            //console.log($scope.vduWithName);
            //$('#madalLaunch').modal('show');
        }
        for (i = 0; i < $scope.nsdToSend.vnfd.length; i++) {
            console.log($scope.vimInstances);
            $scope.launchPopsAvailable[$scope.nsdToSend.vnfd[i].name] = {};
            $scope.launchPopsAvailable[$scope.nsdToSend.vnfd[i].name].pops = angular.copy($scope.vimInstances);
            for (j = 0; j < $scope.nsdToSend.vnfd[i].vdu.length; j++) {
                //console.log($scope.nsdToSend.vnfd[i].vdu[j].id);
                $scope.launchPops[$scope.nsdToSend.vnfd[i].name][$scope.nsdToSend.vnfd[i].vdu[j].name] = angular.copy($scope.vimInstances);
            }
            console.log($scope.launchPopsAvailable[$scope.nsdToSend.vnfd[i].name].pops);
        }
    };
    $scope.noVIMchoicePossible = false;
    $scope.vimForLaunch = {};
    $scope.launch = function () {
        prepareVIMs();
        console.log(JSON.stringify($scope.vimForLaunch));

        //console.log($scope.nsdToSend);
        $scope.launchObj.keys = [];
        $scope.launchObj.vduVimInstances = $scope.vimForLaunch;
        $scope.launchKeys.forEach(function (key) {
            $scope.launchObj.keys.push(key.name);
        });

        // $scope.launchObj.vduVimInstances = $scope.vimForLaunch;
        //console.log($scope.basicConfiguration.name);
        $scope.launchObj.configurations={};
        $scope.launchObj.configurations = $scope.launchConfiguration.configurations;
        console.log(JSON.stringify($scope.launchObj));
        http.post(urlRecord + $scope.nsdToSend.id, $scope.launchObj)
            .success(function (response) {
                showOk("Created Network Service Record from Descriptor with id: \<a href=\'\#nsrecords\'>" + $scope.nsdToSend.id + "<\/a>");
            })
            .error(function (data, status) {
                showError(data, status);
            });
     
        //$scope.launchKeys = [];
        $scope.launchObj = {};
        $scope.launchPops = {};
        $scope.vnfdToVIM.splice(0);
        $scope.vimForLaunch = {};
        $scope.launchConfiguration = {"configurations":{}};
        $scope.vnfdnames = [];
       

    };


    //@param bodyJson the body json is: { "vduVimInstances":{ "vduName":["viminstancename"],
    //"vduName2":["viminstancename2"] }, "keys":["keyname1", "keyname2"] }


    function prepareVIMs() {
        $scope.vimForLaunch = {};
        for (var vnfdName in $scope.launchPops) {
            for (var vduName in $scope.launchPops[vnfdName]) {
                if (vduName != "pops" && vduName != "undefined") {
                    $scope.vimForLaunch[vduName] = [];
                    $scope.launchPops[vnfdName].pops.forEach(
                        function (pop) {
                            
                            $scope.vimForLaunch[vduName].push(pop.name);
                        }
                    );
                    // $scope.vimForLaunch[vduName].push($scope.launchPops[vnfdName].pops);
                }
            }
        }


        // if (!$scope.vnfdLevelVim && $scope.launchNsdVim.length === 0) {
        //     return;
        // }
        //
        // if (!$scope.vnfdLevelVim) {
        //     //console.log("NSD level");
        //     for (i = 0; i < $scope.vnfdToVIM.length; i++) {
        //         for (j = 0; j < $scope.vnfdToVIM[i].vdu.length; j++) {
        //             $scope.vimForLaunch[$scope.vnfdToVIM[i].vdu[j].vduName] = $scope.launchNsdVim;
        //
        //         }
        //     }
        // } else {
        //     //console.log("VNFD level");
        //     for (i = 0; i < $scope.vnfdToVIM.length; i++) {
        //         for (j = 0; j < $scope.vnfdToVIM[i].vdu.length; j++) {
        //             if (!$scope.vnfdToVIM[i].vduLevel) {
        //                 if ($scope.vnfdToVIM[i].vim.length === 0) {
        //                     continue;
        //                 }
        //                 $scope.vimForLaunch[$scope.vnfdToVIM[i].vdu[j].vduName] = $scope.vnfdToVIM[i].vim
        //             } else {
        //                 if ($scope.vnfdToVIM[i].vdu[j].vim.length === 0) {
        //                     continue;
        //                 }
        //                 $scope.vimForLaunch[$scope.vnfdToVIM[i].vdu[j].vduName] = $scope.vnfdToVIM[i].vdu[j].vim;
        //             }
        //
        //         }
        //     }
        // }


    }

    $scope.Jsplumb = function () {
        http.get(url + $routeParams.nsdescriptorId)
            .success(function (response, status) {
                topologiesAPI.Jsplumb(response, 'descriptor');
                //console.log(response);

            }).error(function (data, status) {
            showError(data, status);
        });

    };


    $scope.returnUptime = function (longUptime) {
        var string = serviceAPI.returnStringUptime(longUptime);
        return string;
    };

    $scope.stringContains = function (k1, k2) {
        return k2.indexOf(k1) > -1;
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
        //console.log(ids);
        http.post(url + 'multipledelete', ids)
            .success(function (response) {
                showOk('Items with id: ' + ids.toString() + ' deleted.');
                loadTable();
            })
            .error(function (response, status) {
                showError(response, status);
            });
            //$scope.selection.ids = [];
            $scope.multipleDelete = false;
            $scope.selection.ids = {};
            $scope.selection = {};
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

    function showError(data, status) {
        if (status === 500) {
            $scope.alerts.push({
            type: 'danger',
            msg: 'An error occured and could not be handled properly, please, report to us and we will fix it as soon as possible'
        }); }
        else if (status === 400) {
            $scope.alerts.push({
                type: 'danger',
                msg: 'Something is wrong with your NSD. Common error: you have specified your vim as a string and not as an array in VNFD'
            });
        }

        else {
            $scope.alerts.push({
                type: 'danger',
                msg: data.message + '. Error code: ' + status
            });
        }
        $('.modal').modal('hide');
        if (status === 401) {
            //console.log(status + ' Status unauthorized')
            AuthService.logout();
        }
    }

    function showOk(msg) {
        $scope.alerts.push({type: 'success', msg: msg});
         window.setTimeout(function() { 
        for (i = 0; i < $scope.alerts.length; i++) {
        if ($scope.alerts[i].type == 'success') {
            $scope.alerts.splice(i, 1);
        }
    }
    }, 5000);
        $('.modal').modal('hide');
    }

    function loadTable() {

        if (angular.isUndefined($routeParams.nsdescriptorId))
            http.get(url)
                .success(function (response, status) {
                    $scope.nsdescriptors = response;
                    //console.log(response);
                })
                .error(function (data, status) {
                    showError(data, status);

                });
        else
            http.get(url + $routeParams.nsdescriptorId)
                .success(function (response, status) {
                    $scope.nsdinfo = response;
                    $scope.nsdJSON = JSON.stringify(response, undefined, 4);
                    console.log("here" + $scope.nsdinfo);
                })
                .error(function (data, status) {
                    showError(data, status);
                });
    }

    $scope.addPopToVnfd = function (vnfd, pop) {
        $scope.launchPops[vnfd.name].pops.push(pop);
        console.log($scope.launchPops);
        for (j = 0; j < vnfd.vdu.length; j++) {
            //console.log($scope.nsdToSend.vnfd[i].vdu[j].id);
            vduName = vnfd.vdu[j].name;
            
            // $scope.launchPops[vnfd.name][vduName].push(pop);
        }
        remove($scope.launchPopsAvailable[vnfd.name].pops, pop);
        $scope.tableParamsFilteredLaunchPops.reload();
        $scope.tableParamsFilteredPops.reload();
    }

    $scope.removePopToVnfd = function (vnfd, pop) {
        $scope.launchPopsAvailable[vnfd.name].pops.push(pop);
        for (j = 0; j < vnfd.vdu.length; j++) {
            //console.log($scope.nsdToSend.vnfd[i].vdu[j].id);
            vduName = vnfd.vdu[j].name;
            // $scope.launchPops[vnfd.name][vduName].push(pop);
        }
        remove($scope.launchPops[vnfd.name].pops, pop);
        $scope.tableParamsFilteredLaunchPops.reload();
        $scope.tableParamsFilteredPops.reload();
    }

    $scope.addPopToNsd = function (pop) {
        console.log($scope.launchPops)
        for (var vnfdname in $scope.launchPops) {
            console.log("Name is: " + vnfdname);
            // for (i = 0; i < $scope.launchPops.length; i++) {
            //     vnfdName = $scope.launchPops[i].name;
            var found = false;
            $scope.launchPops[vnfdname].pops.forEach(function (pop1) {
                console.log(pop1.name + " = " + pop.name);
                if (pop1.name == pop.name) {
                    found = true;
                }
            });

            if (!found) {
                $scope.launchPops[vnfdname].pops.push(angular.copy(pop));
            }
            // for (j = 0; j < vnfd.vdu.length; j++) {
            //     //console.log($scope.nsdToSend.vnfd[i].vdu[j].id);
            //     vduName = vnfd.vdu[j].name;
            //     // $scope.launchPops[vnfd.name][vduName].push(pop);
            // }
            remove($scope.launchPopsAvailable[vnfdname].pops, pop);
        }
        ;
        $scope.tableParamsFilteredLaunchPops.reload();
        $scope.tableParamsFilteredPops.reload();
    }

    $scope.loadVnfdTabs = function () {
        $scope.tabs = [];
        var i;
        for (i = 0; i < $scope.nsdToSend.vnfd.length; i++) {
            newVNFD = {"vnfdname": $scope.nsdToSend.vnfd[i].name, "vim": [], "vduLevel": false, "vdu": []};
            console.log(newVNFD);

            var tab = {};
            tab['id'] = i;
            tab['title'] = $scope.nsdToSend.vnfd[i].name;
            tab['active'] = true;
            tab['disabled'] = false;
            tab['vnfd'] = $scope.nsdToSend.vnfd[i]

            $scope.tabs.push(tab);
        }
    }
    
    angular.element(document).ready(function () {
       
        
            var previewNode = document.querySelector("#template");
            if (previewNode === null) {
                console.log("no template");
                return;
            }
            previewNode.id = "";
            var previewTemplate = previewNode.parentNode.innerHTML;
            previewNode.parentNode.removeChild(previewNode);

            var header = {};

            if ($cookieStore.get('token') !== '')
                header = {'Authorization': 'Bearer ' + $cookieStore.get('token')};

            header['project-id'] = $cookieStore.get('project').id;
            var myDropzone = new Dropzone('#my-dropzone', {
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
                            showOk("Uploaded the CSAR NSD");
                            loadTable();
                            myDropzone.removeAllFiles(true);
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
        
    });
});
