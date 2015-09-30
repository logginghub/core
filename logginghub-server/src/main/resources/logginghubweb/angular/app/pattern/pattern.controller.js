(function () {

    'use strict';

    angular.module('loggingHub').controller('PatternController', PatternsController);

    function PatternsController($http) {

        var vm = this;
        vm.pattern = {};
        vm.error = "";

        logger.info("Instantiated PatternController");

        activate();

        function activate() {
            $http({
                method: "GET", url: "/getPattern"
            }).then(function(response) {
                logger.info("Stats response : %o", response);
                vm.pattern = response.data.value;
            }, function(error) {
                vm.error = error;
            });
        }

    }

}());