var app = angular.module('BeerwebV2', ['ngMaterial', 'ngMessages']);

app.config(function ($mdThemingProvider) {
    $mdThemingProvider.theme('default')
        .primaryPalette('blue-grey', {
            'default': '900',
            'hue-1': '700',
            'hue-2': '500',
            'hue-3': '300'
        })
        .accentPalette('teal');
});

app.controller("PumpSummaryController", function ($scope, $location) {

    $scope.pumpSummary = {};

    $scope.initialisationMessage = true;

    var stompClient = null;

    var connect = function () {

        var host = $location.host();

        var sockjsURL = "/beerweb-pumpsummary";

        if (host.toLowerCase().indexOf("rhcloud.com") > 0) {
            console.log("Detected running on openshift setting url accordingly");
            sockjsURL = "http://beerweb-codersparks.rhcloud.com:8000" + sockjsURL;
        }

        var socket = new SockJS(sockjsURL);
        stompClient = Stomp.over(socket);

        stompClient.connect({}, function (frame) {
            console.log("Connected: " + frame);
            stompClient.subscribe('/topic/pumpsummary', function (pumpDataMessage) {
                $scope.initialisationMessage = false;
                updatePumpSummary(JSON.parse(pumpDataMessage.body));
            });
        });

    };

    this.checkForEmptyData = function () {
        emptyData = angular.equals({}, $scope.pumpSummary);
        console.log("Result of check for empty summary: " + emptyData);
        return emptyData;
    };

    var updatePumpSummary = function(data) {
        console.log("updatePumpSummary called with data: ");
        console.log(data);
        $scope.pumpSummary = data;
        $scope.$apply();
    }

    angular.element(document).ready(function() {
        console.log("Loading from v2/app.js");
        connect();
        // Force the spring controller to send the current data
        setTimeout(function() {
            console.log("Sending initialisation message");stompClient.send("/app/pumpsummary", {}, "");
        }, 2500);
    });
});