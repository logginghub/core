(function () {

    'use strict';

    angular.module('loggingHub').controller('DailyController', DailyController);

    function DailyController($http, $state, $stateParams) {

        var vm = this;
        vm.day = {};
        vm.error = "";
        vm.select = selectHour;

        vm.year = $stateParams.year;
        vm.month = $stateParams.month;
        vm.day = $stateParams.day;

        //vm.selectHour = selectHour;

        logger.info("Instantiated DailyController");

        activate();

        function activate() {

            var request = {
                method: "GET", url: "/getDaily", params: {
                    year: vm.year, month: vm.month, day: vm.day
                }
            };

            $http(request).then(function (response) {
                logger.info("Response : %o", response);
                vm.day = response.data;
            }, function (error) {
                vm.error = error;
            });
        }

        function selectHour(hour) {
            logger.info("Selecting hour %o", hour);

            if (hour.isSelected) {
                $state.go("hourly", {year: vm.day.year, month: vm.day.month, day: vm.day.day, hour: hour.hour});
            }

            _.forEach(vm.day.segments, function (segment) {
                _.forEach(segment.hours, function (hour) {
                    delete hour.isSelected;
                })
            });

            hour.isSelected = true;
        }

    }

}());