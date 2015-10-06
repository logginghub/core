(function () {

    'use strict';

    angular.module('loggingHub').controller('CalendarController', PatternsController);

    function PatternsController($http, $state) {

        var vm = this;
        vm.calendar = [];
        vm.error = "";
        vm.select = selectDay;

        logger.info("Instantiated CalendarController");

        activate();

        function activate() {

            $http({
                method: "GET", url: "/getCalendar"
            }).then(function(response) {
                logger.info("Response : %o", response);
                vm.calendar = response.data;
            }, function(error) {
                vm.error = error;
            });
        }

        function selectDay(day) {
            logger.info("Selecting day %o", day);

            if(day.isSelected) {
                $state.go("daily", { year: day.year, month:day.month, day: day.date});
            }

            _.forEach(vm.calendar, function(week) {
                _.forEach(week.days, function(day) {
                    delete day.isSelected;
                })
            });

            day.isSelected=true;
        }

    }

}());