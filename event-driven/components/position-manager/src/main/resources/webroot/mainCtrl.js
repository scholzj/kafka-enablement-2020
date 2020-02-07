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
    /*$scope.addresses = $resource("positionmanager/:addressId",{addressId:'@id'},{
        'update': { method:'PUT' }
    });*/

    $scope.positions = $resource("positionmanager/");

    $scope.tableParams = new NgTableParams({
            page: 1,
            count: 9999,
            noPager: true
        }, {
            counts: [],
            total: 1,
            getData: function(params) {
                /*var queryParams = {page:params.page()-1 , size:params.count()};
                var sortingProp = Object.keys(params.sorting());
                if(sortingProp.length == 1){
                    queryParams["sort"] = sortingProp[0];
                    queryParams["sortDir"] = params.sorting()[sortingProp[0]];
                }
                return $scope.addresses.query(queryParams, function(data, headers) {
                    var totalRecords = headers("PAGING_INFO").split(",")[0].split("=")[1];
                    params.total(totalRecords);
                    console.log(params.total());
                    return data;
                }).$promise;*/

                return $scope.positions.query(function(data) {
                    return data;
                }).$promise;
            }
        });

    $scope.addNewPosition = function(){
        $scope.position = {code:"CODE", amount: 100};
        ngDialog.open({ template: 'newTemplateId',	scope: $scope, className: 'ngdialog-theme-default' });
    }

    $scope.save = function(){
        ngDialog.close('ngdialog1');
        $rootScope.loading = true;
        $scope.createPosition();
    }

    $scope.createPosition = function(){
        $scope.positions.save($scope.position, function(){
            $scope.tableParams.reload();
            $rootScope.loading = false;
        });
    }

    $scope.reload = $interval(function() {$scope.tableParams.reload()}, 5000);
});