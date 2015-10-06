(function () {

    'use strict';

    angular.module('loggingHub').controller('SecondlyController', SecondlyController);

    function SecondlyController($http, $state, $stateParams) {

        var vm = this;
        vm.second = {};
        vm.error = "";
        vm.select = selectMillisecond;

        vm.year = $stateParams.year;
        vm.month = $stateParams.month;
        vm.day = $stateParams.day;
        vm.hour = $stateParams.hour;
        vm.minute = $stateParams.minute;
        vm.second = $stateParams.second;

        //vm.selectHour = selectHour;

        logger.info("Instantiated MinutelyController");

        activate();

        function activate() {

            var request = {
                method: "GET", url: "/getSecondly", params: {
                    year: vm.year, month: vm.month, day: vm.day, hour: vm.hour, minute: vm.minute, second: vm.second
                }
            };

            $http(request).then(function (response) {
                logger.info("Response : %o", response);
                vm.second = response.data;
            }, function (error) {
                vm.error = error;
            });
        }

        function selectMillisecond(millisecond) {
            logger.info("Selecting millisecond %o", millisecond);

            //if (second.isSelected) {
            //    $state.go("secondly", {year: vm.day.year, month: vm.day.month, day: vm.day.day, hour: vm.hour, minute: vm.minute, second: second.second});
            //}
            //
            //_.forEach(vm.minute.segments, function (segment) {
            //    _.forEach(segment.seconds, function (second) {
            //        delete second.isSelected;
            //    })
            //});

            millisecond.isSelected = true;
        }

    }

}());