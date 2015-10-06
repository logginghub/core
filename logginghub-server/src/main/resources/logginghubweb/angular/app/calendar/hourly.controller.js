(function () {

    'use strict';

    angular.module('loggingHub').controller('HourlyController', HourlyController);

    function HourlyController($http, $state, $stateParams) {
        var vm = this;
        vm.result = {};
        vm.error = "";
        vm.select = select;
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
                method: "GET", url: "/getHourly", params: {
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

        function select(cell) {
            logger.info("Selecting cell %o", cell);

            if (cell.isSelected) {
                $state.go("minutely", {year: vm.year, month: vm.month, day: vm.day, hour: vm.hour, minute: cell.index });
            }

            _.forEach(vm.result.rows, function (row) {
                _.forEach(row.cells, function (cell) {
                    delete cell.isSelected;
                })
            });

            cell.isSelected = true;
        }

    }
}());