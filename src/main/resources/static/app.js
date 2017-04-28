var app = angular.module('BlankApp', ['ngMaterial', 'ngMessages']);

app.config(function($mdThemingProvider) {
        $mdThemingProvider.theme('default')
            .primaryPalette('blue-grey', {
                'default': '900',
                'hue-1': '700',
                'hue-2': '500',
                'hue-3': '300'
            })
            .accentPalette('deep-orange');
    });

app.controller("PumpDataController", function ($scope, $location) {

    $scope.pumpData = { };

    // We have to check for openshift in the hostname and modify accordingly

    var host = $location.host();

    var sockjsURL = "/beerweb-websocket";

    if(host.toLowerCase().indexOf("rhcloud.com") > 0) {
        console.log("Detected running on openshift setting url accordingly");
        sockjsURL = "ws://beerweb-codersparks.rhcloud.com:8000" + sockjsURL;
    }

    var stompClient = null;

    var updatePumpData = function (data) {
        console.log("updatePumpData called with data:");
        console.log(data);
        $scope.pumpData = data;
        $scope.$apply();
    }

    var connect = function () {
        var socket = new SockJS(sockjsURL);
        stompClient = Stomp.over(socket);

        stompClient.connect({}, function (frame) {
            console.log("Connected: " + frame);
            stompClient.subscribe('/topic/pumpdata', function (pumpDataMessage) {
               updatePumpData(JSON.parse(pumpDataMessage.body));
            });
        });

    };

    this.checkForEmptyData = function() {

        emptyData = angular.equals({}, $scope.pumpData);
        console.log("Result of check for empty data: " + emptyData);
        return emptyData;
    }





    angular.element(document).ready(function() {
       console.log("Loading from app.js");
       connect();
        // Force the spring controller to send the current data
        setTimeout(function() {
            console.log("Sending initialisation message");stompClient.send("/app/pumpdata", {}, "");
        }, 500);
    });

});

app.controller("ManualController", function($scope, $http, $mdToast, $mdDialog) {

    $scope.processBeerForm = function() {

        var urlString = "/?pump=" + $scope.pumpData.pumpID + "&rfid=" + $scope.pumpData.rfID + "&rating=" + $scope.pumpData.rating;

        $http({
            method: "POST",
            url: urlString,
            data: ""
        })
            .then(function successCallback(data) {
                console.log("Process Beer form submitted, and returned");
                console.log(data);
                $scope.showSimpleToast("Rating submitted successfully");

            }, function errorCallback(respone) {
                console.error("Error submitting beer form");
                console.error(response);
                $scope.showSimpleToast("Error submitting rating");
            });
    };

    var last = {
        bottom: true,
        top: false,
        left: false,
        right: true
    };

    $scope.toastPosition = angular.extend({},last);

    $scope.getToastPosition = function() {
        sanitizePosition();

        return Object.keys($scope.toastPosition)
            .filter(function(pos) { return $scope.toastPosition[pos]; })
            .join(' ');
    };

    function sanitizePosition() {
        var current = $scope.toastPosition;

        if ( current.bottom && last.top ) current.top = false;
        if ( current.top && last.bottom ) current.bottom = false;
        if ( current.right && last.left ) current.left = false;
        if ( current.left && last.right ) current.right = false;

        last = angular.extend({},current);
    }

    $scope.showSimpleToast = function(text) {
        var pinTo = $scope.getToastPosition();

        $mdToast.show(
            $mdToast.simple()
                .textContent(text)
                .position(pinTo )
                .hideDelay(2000)
        );
    };

    $scope.showConfirm = function(ev) {
        // Appending dialog to document.body to cover sidenav in docs app
        var confirm = $mdDialog.confirm()
            .title('Are you sure?')
            .textContent('Do you really want to delete all the data in the db')
            .ariaLabel('Delete Data')
            .targetEvent(ev)
            .ok('Yes, Please delete!')
            .cancel('Nope, please do not delete');

        $mdDialog.show(confirm).then(function() {
            $http({
                method: "DELETE",
                url: "/danger/deleteall",
                data: ""
            }).then(function(response) {
                console.log("Deleted data, response:");
                console.log(response);
                $scope.showSimpleToast("All data has been deleted");
            }, function(response) {
                console.error("Error received when attempting to delete data, resonse:");
                console.error(response);
            })
        }, function() {
            console.log("I have not delete the data");
        });
    };


    angular.element(document).ready(function() {
        console.log("Loading Manual Controller");

        $scope.pumpData = {};
        $scope.pumpData.pumpID = "pump1";
        $scope.pumpData.rfID = "rfid1";
        $scope.pumpData.rating = 0;
        $scope.$apply();
    });
});