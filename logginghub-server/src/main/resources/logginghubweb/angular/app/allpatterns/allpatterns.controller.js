(function () {

    'use strict';

    angular.module('loggingHub').controller('AllPatternsController', AllPatternsController);

    function AllPatternsController($http, $scope) {

        var vm = this;
        vm.patterns = {};
        vm.error = "";

        vm.labels = ["January", "February", "March", "April", "May", "June", "July"];
        vm.series = ['Series A', 'Series B'];
        vm.data = [[1, 2, 3, 4, 5, 6, 7], [28, 48, 40, 19, 86, 27, 90]];

        vm.options = { animation: false,  pointDot : false, bezierCurve : true};

        logger.info("Instantiated AllPatternsController");

        activate();

        function activate() {

            var config = {
                method: "get", url: "/getAllPatterns", params: {}
            }

            $http(config).then(function (response) {
                logger.info("Stats response : %o", response);

                vm.patterns = response.data;

                // Copy the data into the angular charting structures
                vm.labels.length = 0;
                vm.data = [];

                _.forEach(vm.patterns.names, function (name, index) {
                    vm.data.push([]);
                });

                _.forEach(vm.patterns.times, function (time, timeIndex) {
                    if(timeIndex % 10 == 0) {
                        vm.labels.push(time.time);
                    }else{
                        vm.labels.push("");
                    }

                    _.forEach(time.values, function (value, index) {
                        vm.data[index].push(value);
                    });
                });

                vm.series = vm.patterns.names;

            }, function (error) {
                vm.error = error;
            });
        }

    }

}());