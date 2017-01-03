var app = angular.module('monitor', [])
.controller('status', function($scope, $http, $interval, $location) {
    $scope.always_on = $location.search().always_on;
    $scope.display_ticks = 0;
    $scope.song_id = 0;
    $scope.song = null;

    $interval(function() {
        if ($scope.always_on != "1" && $scope.display_ticks > 0) $scope.display_ticks--;
        $http.get('api/v1/status').
            then(function(response) {
                if (response.data.id != $scope.song_id) {
                    $scope.song_id = response.data.id
                    $scope.display_ticks = 5;
                    $scope.song = response.data;
                }
            });
    }, 2000);
});

app.config(['$locationProvider', function($locationProvider) {
    $locationProvider.html5Mode(true);
}]);