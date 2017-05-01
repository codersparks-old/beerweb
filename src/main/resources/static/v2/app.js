var app = angular.module('BeerwebV2', ['ngMaterial', 'ngMessages']);

app.factory('moment', function ($window) {
    return $window.moment;
});
app.factory('CanvasJS', function ($window) {
    return $window.CanvasJS;

});
app.factory('Chartist', function ($window) {
    return $window.Chartist;
})

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

app.controller("PumpSummaryController", function ($scope, $location, moment, Chartist) {

    $scope.pumpSummaryMap = {};

    $scope.initialisationMessage = true;

    var stompClient = null;

    var lineColors = [
        "#D32F2F",
        "#7B1FA2",
        "#303F9F",
        "#0288D1",
        "#00796B",
        "#689F38",
        "#FBC02D",
        "#F57C00",
        "#455A64"
    ];

    var bgColors = [
        "#EF9A9A",
        "#CE93D8",
        "#9FA8DA",
        "#81D4FA",
        "#80CBC4",
        "#C5E1A5",
        "#FFF59D",
        "#FFCC80",
        "#B0BEC5"
    ];

    var updateVisualisation = function (d) {



        var data = {
            labels: [],
            series: [
                []
            ]
        };

        for(x in d) {

            pumpSummary = d[x];

            console.log("Pump Summary");
            console.log(pumpSummary);

            data.labels.push(pumpSummary.pumpName);
            data.series[0].push(pumpSummary.average);
        }

        console.log("data");
        console.log(data);

        new Chartist.Bar('.ct-chart', data);


    }

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
                updateVisualisation($scope.pumpSummaryMap);
            });
        });

    };

    this.checkForEmptyData = function () {
        emptyData = angular.equals({}, $scope.pumpSummaryMap);
        console.log("Result of check for empty summary: " + emptyData);
        return emptyData;
    };

    var updatePumpSummary = function (data) {
        console.log("updatePumpSummary called with data: ");
        console.log(data);

        var pumps = Object.keys(data).sort();

        $scope.pumpSummaryMap = {};
        // Trying to get the pumps in alphabetical order
        for (var i = 0; i < pumps.length; i++) {
            $scope.pumpSummaryMap[pumps[i]] = data[pumps[i]];
        }

        $scope.$apply();
    };

    var parseDate = function (date) {
        var _momentDate = moment(date);
        //console.log("Date:");
        //console.log(_momentDate);

        return _momentDate
    }

    this.formatDate = function (date) {
        return parseDate(date).format("HH:mm:ss - DD/MM/Y");
    };

    angular.element(document).ready(function () {
        console.log("Loading from v2/app.js");
        connect();
        // Force the spring controller to send the current data
        setTimeout(function () {
            console.log("Sending initialisation message");
            stompClient.send("/app/pumpsummary", {}, "");
        }, 2500);
    });
});