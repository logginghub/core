(function () {

    'use strict';

    angular.module('loggingHub').controller('MinutelyController', MinutelyController);

    function MinutelyController($http, $state, $stateParams) {

        var vm = this;
        vm.result = {};
        vm.error = "";
        vm.select = selectSecond;
        vm.update = update;

        vm.year = $stateParams.year;
        vm.month = $stateParams.month;
        vm.day = $stateParams.day;
        vm.hour = $stateParams.hour;
        vm.minute = $stateParams.minute;

        vm.independentPatternHeat = true;
        vm.useTotalScale = false;

        //vm.selectHour = selectHour;

        logger.info("Instantiated MinutelyController");

        activate();

        function activate() {
            update();
        }

        function update() {
            var request = {
                method: "GET", url: "/getMinutely", params: {
                    year: vm.year,
                    month: vm.month,
                    day: vm.day,
                    hour: vm.hour,
                    minute: vm.minute,
                    independentPatternHeat: vm.independentPatternHeat,
                    useTotalScale: vm.useTotalScale
                }
            };

            $http(request).then(function (response) {
                logger.info("Response : %o", response);
                vm.result = response.data;
            }, function (error) {
                vm.error = error;
            });
        }


        function selectSecond(second) {
            logger.info("Selecting second %o", second);

            if (second.isSelected) {
                $state.go("secondly", {year: vm.year, month: vm.month, day: vm.day, hour: vm.hour, minute: vm.minute, second: second.index});
            }

            _.forEach(vm.minute.segments, function (segment) {
                _.forEach(segment.seconds, function (second) {
                    delete second.isSelected;
                })
            });

            second.isSelected = true;
        }

    }

}());