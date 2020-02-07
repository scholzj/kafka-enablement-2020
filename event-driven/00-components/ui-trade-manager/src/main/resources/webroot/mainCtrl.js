var app = angular.module('myApp', ["ngTable","ngResource","ngDialog"]);
app.config(['$httpProvider', function ($httpProvider) {
    $httpProvider.interceptors.push(function ($q,$rootScope) {
        return {
            'responseError': function (responseError) {
                $rootScope.loading = false;
                $rootScope.message = responseError.data.message;
                return $q.reject(responseError);
            }
        };
    });
}]);

app.controller('mainCtrl', function($scope,$interval,NgTableParams,$resource,ngDialog,$rootScope) {
    $rootScope.loading = false;
    $scope.position = {code:"RHT", amount: 100};

    $scope.positions = $resource("positionmanager/");

    $scope.tableParams = new NgTableParams({
            page: 1,
            count: 9999,
            noPager: true
        }, {
            counts: [],
            total: 1,
            getData: function(params) {
                return $scope.positions.query(function(data) {
                    return data;
                }).$promise;
            }
        });

    $scope.addNewPosition = function() {
        $scope.position = {code:"CODE", amount: 100};
        ngDialog.open({ template: 'newTemplateId',	scope: $scope, className: 'ngdialog-theme-default' });
    }

    $scope.save = function(){
        ngDialog.close('ngdialog1');
        $rootScope.loading = true;
        $scope.createPosition();
    }

    $scope.createPosition = function() {
        $scope.positions.save($scope.position, function(){
            $scope.tableParams.reload();
            $rootScope.loading = false;
        });
    }

    $scope.reload = $interval(function() {$scope.tableParams.reload()}, 5000);
});