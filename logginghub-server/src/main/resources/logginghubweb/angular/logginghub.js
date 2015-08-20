'use strict';

function url(s) {
       var l = window.location;
       return ((l.protocol === "https:") ? "wss://" : "ws://") + l.hostname + (((l.port != 80) && (l.port != 443)) ? ":" + l.port : "") + l.pathname + s;
}

function pad(n, width, z) {
  z = z || '0';
  n = n + '';
  return n.length >= width ? n : new Array(width - n.length + 1).join(z) + n;
}

var loggingHubModule = angular.module("loggingHub", ['ui.bootstrap','ui.router', 'chart.js' ]);

loggingHubModule.config(function($stateProvider, $urlRouterProvider){

      $urlRouterProvider.otherwise("/home")

      $stateProvider

        .state('home', {
            url: "/home",
            templateUrl: "home.html"
        })

        .state('stats', {
            url: "/stats",
            templateUrl: "stats.html"
        })

        .state('yearstats', {
            url: "/yearstats/:year",
            templateUrl: "yearstats.html"
        })

        .state('monthstats', {
            url: "/monthstats?year&month",
            templateUrl: "monthstats.html"
        })

        .state('daystats', {
            url: "/daystats?year&month&day",
            templateUrl: "daystats.html"
        })

        .state('hourstats', {
            url: "/hourstats?year&month&day&hour",
            templateUrl: "hourstats.html"
        })

        .state('minutestats', {
            url: "/minutestats?year&month&day&hour&minute",
            templateUrl: "minutestats.html"
        })

        .state('secondstats', {
            url: "/secondstats?year&month&day&hour&minute&second",
            templateUrl: "secondstats.html"
        })
});


loggingHubModule.filter('pad', function () {
    return function (n, len) {
        var num = parseInt(n, 10);
        len = parseInt(len, 10);
        if (isNaN(num) || isNaN(len)) {
            return n;
        }
        num = ''+num;
        while (num.length < len) {
            num = '0'+num;
        }
        return num;
    };
});

loggingHubModule.controller('homeController', function($scope, $stateParams, $http, $q, $interval) {

   $scope.requestData = function() {

   }

   $scope.requestData();

});

loggingHubModule.controller('statsController', function($scope, $stateParams, $http, $q, $interval) {

   $scope.requestData = function() {

      console.log("Requesting main stats page");
      var promise = $http({
          method: "GET",
          url: "/stats/"
      });

      promise.then(function(response) {
          console.log("Stats response : %o", response);
          $scope.data = response.data
      }, function(error) {
          $scope.error = error
      });

   }

   $scope.requestData();

});


loggingHubModule.controller('yearstatsController', function($scope, $stateParams, $http, $q, $interval, $state) {

$scope.labels = ["January", "February", "March", "April", "May", "June", "July"];
  $scope.series = ['Series A', 'Series B'];
  $scope.chartData = [
    [65, 59, 80, 81, 56, 55, 40],
    [28, 48, 40, 19, 86, 27, 90]
  ];
  $scope.onClick = function (points, evt) {
    if(points.length > 0) {
       var label = points[0].label
       var split = label.split(" ");

       var date = split[0];

       var dateSplit = date.split("/");

       var year = dateSplit[0];
       var month = dateSplit[1];

       $state.go('monthstats' ,{year: year, month: month});
    }
  };

  $scope.options = {
     scaleBeginAtZero:true,
                                animationSteps: 10
  }


   $scope.requestData = function() {

      console.log("Requesting yearly data : " + $stateParams.year);
      var promise = $http({
          method: "GET",
          url: "/yearstats",
          params: { "year" : $stateParams.year }
      });

      promise.then(function(response) {
          console.log("Stats response : %o", response);
          $scope.data = response.data

           var series = ['Events per month']
            var labels = []
            var seriesData = [];

            var months = response.data.months;
            for(var i = 0; i < months.length; i++) {
                 var month = months[i];
                 labels.push(month.year + "/" + month.month);
                 seriesData.push(month.events);
            }

            $scope.chartData = [seriesData];
            $scope.labels = labels;
            $scope.series = series;

      }, function(error) {
          $scope.error = error
      });

   }

   $scope.requestData();

});


loggingHubModule.controller('monthstatsController', function($scope, $stateParams, $http, $q, $interval, $state) {

  $scope.labels = ["January", "February", "March", "April", "May", "June", "July"];
  $scope.series = ['Series A', 'Series B'];
  $scope.chartData = [
    [65, 59, 80, 81, 56, 55, 40],
    [28, 48, 40, 19, 86, 27, 90]
  ];

  $scope.onClick = function (points, evt) {
     if(points.length > 0) {
           var label = points[0].label
           var split = label.split(" ");

           var date = split[0];
           var time = split[1];

           var dateSplit = date.split("/");

           var year = dateSplit[0];
           var month = dateSplit[1];
           var day = dateSplit[2];

           $state.go('daystats' ,{year: year, month: month, day: day});
        }
  };

  $scope.options = {
     scaleBeginAtZero:true,
      animationSteps: 10
  }

   $scope.perfOptions = {
         scaleBeginAtZero:true,
         animationSteps: 10
      }

     $scope.perfLabels = ['2006', '2007', '2008', '2009', '2010', '2011', '2012'];
      $scope.perfSeries = ['Series A', 'Series B'];

      $scope.perfData = [
        [65, 59, 80, 81, 56, 55, 40],
        [28, 48, 40, 19, 86, 27, 90]
      ];

   $scope.previousMonth = function() {

       var year =  parseInt($stateParams.year)
       var month = parseInt(+$stateParams.month)

       month = month - 1

       if(month == 0) {
          month = 12
          year = year -1
       }

       console.log("Going to  month %s year %s", month, year);
       $state.go('monthstats' ,{year: year, month: month});

   }

   $scope.nextMonth = function() {
       var year =  parseInt($stateParams.year)
       var month = parseInt(+$stateParams.month)

       month = month + 1

       if(month == 13) {
          month = 1
          year = year + 1
       }

       console.log("Going to  month %s year %s", month, year);
       $state.go('monthstats' ,{year: year, month: month});
   }

   $scope.requestData = function() {

      console.log("Requesting montly data : " + $stateParams.year);
      var promise = $http({
          method: "GET",
          url: "/monthstats",
          params: { "year" : $stateParams.year, "month" : $stateParams.month }
      });

      promise.then(function(response) {
          console.log("Stats response : %o", response);
          $scope.data = response.data

          var series = ['Events per day']
          var labels = []
          var seriesData = [];

          var days = response.data.days;
          for(var i = 0; i < days.length; i++) {
               var day = days[i];
               labels.push(day.year + "/" + day.month + "/" + day.day);
               seriesData.push(day.events);
          }

          $scope.chartData = [seriesData];
          $scope.labels = labels;
          $scope.series = series;

           // Build the performance chart
           var perfSeries = ['Min', '50%', '95%', '98%', 'Max']
           var perfLabels = []
           var perfSeriesData = [[],[],[],[],[]];

           console.log("Before data %o", perfSeriesData);

           var days = response.data.days;
           for(var i = 0; i < days.length; i++) {
                var day = days[i];
                var patternStats = day.patternStats

                perfLabels.push(day.year + "/" + pad(day.month,2) + "/" + pad(day.day,2));

                perfSeriesData[0].push(patternStats.percentiles[0]);
                perfSeriesData[1].push(patternStats.percentiles[5]);
                perfSeriesData[2].push(patternStats.percentiles[11]);
                perfSeriesData[3].push(patternStats.percentiles[12]);
                perfSeriesData[4].push(patternStats.percentiles[10]);
           }

           console.log("Data %o", perfSeriesData);

           $scope.perfLabels = perfLabels;
           $scope.perfData = perfSeriesData;
           $scope.perfSeries = perfSeries;

      }, function(error) {
          $scope.error = error
      });

   }

   $scope.requestData();


});


loggingHubModule.controller('daystatsController', function($scope, $stateParams, $http, $q, $interval, $state) {

 $scope.labels = ["January", "February", "March", "April", "May", "June", "July"];
  $scope.series = ['Series A', 'Series B'];
  $scope.chartData = [
    [65, 59, 80, 81, 56, 55, 40],
    [28, 48, 40, 19, 86, 27, 90]
  ];

  $scope.onClick = function (points, evt) {
    if(points.length > 0) {
       var label = points[0].label
       var split = label.split(" ");

       var date = split[0];
       var time = split[1];

       var dateSplit = date.split("/");
       var timeSplit = time.split(":");

       var year = dateSplit[0];
       var month = dateSplit[1];
       var day = dateSplit[2];

       var hour = timeSplit[0];
       var minute = timeSplit[1];


       $state.go('hourstats' ,{year: year, month: month, day: day, hour: hour});
    }
  };

  $scope.options = {
     scaleBeginAtZero:true,
                                animationSteps: 10
  }

    $scope.perfOptions = {
       scaleBeginAtZero:true,
                                  animationSteps: 10
    }

   $scope.perfLabels = ['2006', '2007', '2008', '2009', '2010', '2011', '2012'];
    $scope.perfSeries = ['Series A', 'Series B'];

    $scope.perfData = [
      [65, 59, 80, 81, 56, 55, 40],
      [28, 48, 40, 19, 86, 27, 90]
    ];

   $scope.requestData = function() {

      console.log("Requesting daily data : " + $stateParams.year);
      var promise = $http({
          method: "GET",
          url: "/daystats",
          params: { "year" : $stateParams.year, "month" : $stateParams.month, "day" : $stateParams.day }
      });

      promise.then(function(response) {
          console.log("Stats response : %o", response);
          $scope.data = response.data

           // Build the count charts
           var series = ['Events per hour']
           var labels = []
           var seriesData = [];

           var hours = response.data.hours;
           for(var i = 0; i < hours.length; i++) {
                var hour = hours[i];
                labels.push(hour.year + "/" + pad(hour.month, 2) + "/" + pad(hour.day, 2) + " " + pad(hour.hour, 2) + ":00");
                seriesData.push(hour.events);
           }

           $scope.chartData = [seriesData];
           $scope.labels = labels;
           $scope.series = series;

           // Build the performance chart
           var perfSeries = ['Min', '50%', '95%', '98%', 'Max']
           var perfLabels = []
           var perfSeriesData = [[],[],[],[],[]];

           console.log("Before data %o", perfSeriesData);

           var hours = response.data.hours;
           for(var i = 0; i < hours.length; i++) {
                var hour = hours[i];
                var patternStats = hour.patternStats

                perfLabels.push(hour.year + "/" + pad(hour.month,2) + "/" + pad(hour.day,2) + " " + pad(hour.hour,2) + ":00");

                perfSeriesData[0].push(patternStats.percentiles[0]);
                perfSeriesData[1].push(patternStats.percentiles[5]);
                perfSeriesData[2].push(patternStats.percentiles[11]);
                perfSeriesData[3].push(patternStats.percentiles[12]);
                perfSeriesData[4].push(patternStats.percentiles[10]);
           }

           console.log("Data %o", perfSeriesData);

           $scope.perfLabels = perfLabels;
           $scope.perfData = perfSeriesData;
           $scope.perfSeries = perfSeries;


      }, function(error) {
          $scope.error = error
      });

   }

   $scope.requestData();

});

loggingHubModule.controller('hourstatsController', function($scope, $stateParams, $http, $q, $interval, $state) {

$scope.labels = ["January", "February", "March", "April", "May", "June", "July"];
  $scope.series = ['Series A', 'Series B'];
  $scope.chartData = [
    [65, 59, 80, 81, 56, 55, 40],
    [28, 48, 40, 19, 86, 27, 90]
  ];
  $scope.onClick = function (points, evt) {
    if(points.length > 0) {
       var label = points[0].label
       var split = label.split(" ");

       var date = split[0];
       var time = split[1];

       var dateSplit = date.split("/");
       var timeSplit = time.split(":");

       var year = dateSplit[0];
       var month = dateSplit[1];
       var day = dateSplit[2];

       var hour = timeSplit[0];
       var minute = timeSplit[1];

       $state.go('minutestats' ,{year: year, month: month, day: day, hour: hour, minute: minute});
       }
  };

  $scope.options = {
     scaleBeginAtZero:true,
                                animationSteps: 10
  }

   $scope.perfOptions = {
         scaleBeginAtZero:true,
                                    animationSteps: 10
      }

     $scope.perfLabels = ['2006', '2007', '2008', '2009', '2010', '2011', '2012'];
      $scope.perfSeries = ['Series A', 'Series B'];

      $scope.perfData = [
        [65, 59, 80, 81, 56, 55, 40],
        [28, 48, 40, 19, 86, 27, 90]
      ];


   $scope.requestData = function() {

      console.log("Requesting hourly data : " + $stateParams.year);
      var promise = $http({
          method: "GET",
          url: "/hourstats",
          params: { "year" : $stateParams.year, "month" : $stateParams.month, "day" : $stateParams.day, "hour" : $stateParams.hour }
      });

      promise.then(function(response) {
          console.log("Stats response : %o", response);
          $scope.data = response.data

          var series = ['Events per minute']
          var labels = []
          var seriesData = [];

          var minutes = response.data.minutes;
          for(var i = 0; i < minutes.length; i++) {
               var minute = minutes[i];
               labels.push(minute.year + "/" + pad(minute.month,2) + "/" + pad(minute.day,2) + " " + pad(minute.hour,2) + ":" + pad(minute.minute,2));
               seriesData.push(minute.events);
          }

          $scope.chartData = [seriesData];
          $scope.labels = labels;
          $scope.series = series;

           // Build the performance chart
           var perfSeries = ['Min', '50%', '95%', '98%', 'Max']
           var perfLabels = []
           var perfSeriesData = [[],[],[],[],[]];

           console.log("Before data %o", perfSeriesData);

           var minutes = response.data.minutes;
           for(var i = 0; i < minutes.length; i++) {
                var minute = minutes[i];
                var patternStats = minute.patternStats

                perfLabels.push(minute.year + "/" + pad(minute.month,2) + "/" + pad(minute.day,2) + " " + pad(minute.hour,2) + ":" + pad(minute.minute,2));

                perfSeriesData[0].push(patternStats.percentiles[0]);
                perfSeriesData[1].push(patternStats.percentiles[5]);
                perfSeriesData[2].push(patternStats.percentiles[11]);
                perfSeriesData[3].push(patternStats.percentiles[12]);
                perfSeriesData[4].push(patternStats.percentiles[10]);
           }

           console.log("Data %o", perfSeriesData);

           $scope.perfLabels = perfLabels;
           $scope.perfData = perfSeriesData;
           $scope.perfSeries = perfSeries;

      }, function(error) {
          $scope.error = error
      });

   }

   $scope.requestData();

});

loggingHubModule.controller('minutestatsController', function($scope, $stateParams, $http, $q, $interval, $state) {

$scope.labels = ["January", "February", "March", "April", "May", "June", "July"];
  $scope.series = ['Series A', 'Series B'];
  $scope.chartData = [
    [65, 59, 80, 81, 56, 55, 40],
    [28, 48, 40, 19, 86, 27, 90]
  ];
  $scope.onClick = function (points, evt) {
    if(points.length > 0) {
       var label = points[0].label
       var split = label.split(" ");

       var date = split[0];
       var time = split[1];

       var dateSplit = date.split("/");
       var timeSplit = time.split(":");

       var year = dateSplit[0];
       var month = dateSplit[1];
       var day = dateSplit[2];

       var hour = timeSplit[0];
       var minute = timeSplit[1];
       var second = timeSplit[2];

       $state.go('secondstats' ,{year: year, month: month, day: day, hour: hour, minute: minute, second: second});
     }
  };

   $scope.perfOptions = {
         scaleBeginAtZero:true,
         animationSteps: 10
      }

     $scope.perfLabels = ['2006', '2007', '2008', '2009', '2010', '2011', '2012'];
      $scope.perfSeries = ['Series A', 'Series B'];

      $scope.perfData = [
        [65, 59, 80, 81, 56, 55, 40],
        [28, 48, 40, 19, 86, 27, 90]
      ];

  $scope.options = {
     scaleBeginAtZero:true
     ,
           animationSteps: 10
  }



   $scope.requestData = function() {

      console.log("Requesting minute data : " + $stateParams.year);
      var promise = $http({
          method: "GET",
          url: "/minutestats",
          params: { "year" : $stateParams.year, "month" : $stateParams.month, "day" : $stateParams.day, "hour" : $stateParams.hour, "minute" : $stateParams.minute }
      });

      promise.then(function(response) {
          console.log("Stats response : %o", response);
          $scope.data = response.data

           var series = ['Events per second']
            var labels = []
            var seriesData = [];

            var seconds = response.data.seconds;
            for(var i = 0; i < seconds.length; i++) {
                 var second = seconds[i];
                 labels.push(second.year + "/" + second.month + "/" + second.day + " " + second.hour + ":" + second.minute + ":" + second.second);
                 seriesData.push(second.events);
            }

            $scope.chartData = [seriesData];
            $scope.labels = labels;
            $scope.series = series;

             // Build the performance chart
               var perfSeries = ['Min', '50%', '95%', '98%', 'Max']
               var perfLabels = []
               var perfSeriesData = [[],[],[],[],[]];

               console.log("Before data %o", perfSeriesData);

               var seconds = response.data.seconds;
               for(var i = 0; i < seconds.length; i++) {
                    var second = seconds[i];
                    var patternStats = second.patternStats

                    perfLabels.push(second.year + "/" + pad(second.month,2) + "/" + pad(second.day,2) + " " + pad(second.hour,2) + ":" + pad(second.minute,2) + ":" + pad(second.second,2));

                    perfSeriesData[0].push(patternStats.percentiles[0]);
                    perfSeriesData[1].push(patternStats.percentiles[5]);
                    perfSeriesData[2].push(patternStats.percentiles[11]);
                    perfSeriesData[3].push(patternStats.percentiles[12]);
                    perfSeriesData[4].push(patternStats.percentiles[10]);
               }

               console.log("Data %o", perfSeriesData);

               $scope.perfLabels = perfLabels;
               $scope.perfData = perfSeriesData;
               $scope.perfSeries = perfSeries;

      }, function(error) {
          $scope.error = error
      });

   }

   $scope.requestData();

});

loggingHubModule.controller('secondstatsController', function($scope, $stateParams, $http, $q, $interval, $state) {

   $scope.requestData = function() {

      console.log("Requesting minute data : " + $stateParams.year);
      var promise = $http({
          method: "GET",
          url: "/secondstats",
          params: { "year" : $stateParams.year, "month" : $stateParams.month, "day" : $stateParams.day, "hour" : $stateParams.hour, "minute" : $stateParams.minute, "second" : $stateParams.second }
      });

      promise.then(function(response) {
          console.log("Stats response : %o", response);
          $scope.data = response.data
      }, function(error) {
          $scope.error = error
      });

   }

   $scope.requestData();

});