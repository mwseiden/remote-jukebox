angular.module('monitor', [])
.controller('status', function($scope, $http, $interval) {
    $scope.display_ticks = 0;
    $scope.song_id = 0;
    $scope.song = null;

    $interval(function() {
        if ($scope.display_ticks > 0) $scope.display_ticks--;
        $http.get('api/v1/status').
            then(function(response) {
                if (response.data.id != $scope.song_id) {
                    $scope.display_ticks = 5;
                    $scope.song = response.data;
                }
            });
    }, 2000);
});