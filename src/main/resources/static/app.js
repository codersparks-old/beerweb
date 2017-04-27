var app = angular.module('BlankApp', ['ngMaterial']);

app.controller("PumpDataController", ['$scope', function ($scope) {

    $scope.pumpData = { };

    var stompClient = null;

    var updatePumpData = function (data) {
        console.log("updatePumpData called with data:");
        console.log(data);
        $scope.pumpData = data;
        $scope.$apply();
    }

    var connect = function () {
        var socket = new SockJS('/beerweb-websocket');
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

}]);