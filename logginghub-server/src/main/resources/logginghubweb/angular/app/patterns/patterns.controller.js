(function () {

    'use strict';

    angular.module('loggingHub').controller('PatternsController', PatternsController);

    function PatternsController($http) {

        var vm = this;
        vm.patterns = [];
        vm.error = "";

        logger.info("Instantiated PatternsController");

        activate();

        function activate() {
            $http({
                method: "GET", url: "/getPatterns"
            }).then(function(response) {
                logger.info("Stats response : %o", response);
                vm.patterns = response.data.value;
            }, function(error) {
                vm.error = error;
            });
        }

    }

}());