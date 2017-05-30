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

app.config(function ($mdThemingProvider, $mdIconProvider) {
    $mdThemingProvider.theme('default')
        .primaryPalette('blue-grey', {
            'default': '900',
            'hue-1': '700',
            'hue-2': '500',
            'hue-3': '300'
        })
        .accentPalette('teal');

    $mdIconProvider
        .defaultFontSet('FontAwesome')
        .fontSet('fa', 'FontAwesome');
});

app.controller("toolbarCtrl", function($scope) {
    $scope.showManual = false;

    $scope.toggleManual = function() {
        $scope.showManual = ! $scope.showManual;
    }
});

app.controller("BeerwebV2Summary", function($scope, $http) {

    $scope.showManual = false;

    $scope.toggleManual = function() {
        $scope.showManual = ! $scope.showManual;
    }

    $scope.beerId = null;
    $scope.beerSummary = null;

    $scope.beerIdChange = function(beerId) {
        console.log("Selected beerId: " + beerId);

        if($scope.beerId === null || $scope.beerId === "" || $scope.beerId == undefined) {
            $scope.beerSummary = null;
        } else {
            $http.get("/v2/api/beer/" + $scope.beerId).then(function (response) {
                $scope.beerSummary = response.data;
                console.log("Beer Summary:");
                console.log($scope.beerSummary);
            })
        }
    }

    angular.element(document).ready(function () {
        console.log("Loading  BeerwebV2Summary from v2/app.js");

        $http.get("/v2/api/beer/ids/").then(function (response) {
            $scope.beerIds = response.data;

            console.log("BeerIds:");
            console.log($scope.beerIds);


        })
    });
});

app.controller("BeerwebV2Manual", function ($scope, $http, $mdToast, $mdDialog) {


    $scope.registerData = {};
    $scope.rateDate = {};

    $scope.processRegisterBeerForm = function () {
        console.log("Pump name: " + $scope.registerData.pumpName + " Rfid: " + $scope.registerData.rfid);
        console.log("Submitting data");

        var url = "/v2/api/pump/" + $scope.registerData.pumpName + "/beer/" + $scope.registerData.rfid;

        console.log("url: " + url);

        $http({
            method: "POST",
            url: url,
            data: ""
        })
            .then(function successCallback(data) {
                console.log("Register Beer form submitted, and returned");
                console.log(data);
                $scope.showSimpleToast("Beer registered successfully");

            }, function errorCallback(respone) {
                console.error("Error registering beer");
                console.error(response);
                $scope.showSimpleToast("Error rgistering beer");
            });

    };

    $scope.processRateBeerForm = function () {
        console.log("Pump name: " + $scope.rateData.pumpName + " rating: " + $scope.rateData.rfid);
        console.log("Submitting data");

        var url = "/v2/api/pump/" + $scope.rateData.pumpName + "/rating/" + $scope.rateData.rating;

        console.log("url: " + url);

        $http({
            method: "POST",
            url: url,
            data: ""
        })
            .then(function successCallback(data) {
                console.log("Rate Beer form submitted, and returned");
                console.log(data);
                $scope.showSimpleToast("Beer rated successfully");

            }, function errorCallback(respone) {
                console.error("Error rating beer");
                console.error(response);
                $scope.showSimpleToast("Error rating beer");
            });

    };

    var last = {
        bottom: true,
        top: false,
        left: false,
        right: true
    };

    $scope.toastPosition = angular.extend({}, last);

    $scope.getToastPosition = function () {
        sanitizePosition();

        return Object.keys($scope.toastPosition)
            .filter(function (pos) {
                return $scope.toastPosition[pos];
            })
            .join(' ');
    };

    function sanitizePosition() {
        var current = $scope.toastPosition;

        if (current.bottom && last.top) current.top = false;
        if (current.top && last.bottom) current.bottom = false;
        if (current.right && last.left) current.left = false;
        if (current.left && last.right) current.right = false;

        last = angular.extend({}, current);
    }

    $scope.showSimpleToast = function (text) {
        var pinTo = $scope.getToastPosition();

        $mdToast.show(
            $mdToast.simple()
                .textContent(text)
                .position(pinTo)
                .hideDelay(2000)
        );
    };

    $scope.showConfirm = function (ev) {
        // Appending dialog to document.body to cover sidenav in docs app
        var confirm = $mdDialog.confirm()
            .title('Are you sure?')
            .textContent('Do you really want to delete all the data in the db')
            .ariaLabel('Delete Data')
            .targetEvent(ev)
            .ok('Yes, Please delete!')
            .cancel('Nope, please do not delete');

        $mdDialog.show(confirm).then(function () {
            $http({
                method: "DELETE",
                url: "/v2/api/",
                data: ""
            }).then(function (response) {
                console.log("Deleted data, response:");
                console.log(response);
                $scope.showSimpleToast("All data has been deleted");
            }, function (response) {
                console.error("Error received when attempting to delete data, resonse:");
                console.error(response);
            })
        }, function () {
            console.log("I have not delete the data");
        });
    };

});

app.controller("PumpSummaryController", function ($scope, $location, moment, Chartist) {

    $scope.showManual = false;

    $scope.toggleManual = function() {
        $scope.showManual = ! $scope.showManual;
        console.log("ToggleManual value: " + $scope.showManual);
    }
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


        var averageData = {
            labels: [],
            series: [
                []
            ]
        };

        var latestData = {
            labels: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
            series: []
        };

        for (x in d) {

            pumpSummary = d[x];

            console.log("Pump Summary");
            console.log(pumpSummary);

            averageData.labels.push(pumpSummary.pumpName);
            averageData.series[0].push(pumpSummary.average);

            var pos = 0;

            var pumpLatestRatings = [];

            var valuesToFill = 10 - pumpSummary.last10Ratings.length;
            console.log("Pump: " + pumpSummary.pumpName + " valuesToFill: " + valuesToFill);

            for (var i = 0; i < 10; i++) {

                if (i >= pumpSummary.last10Ratings.length) {
                    pumpLatestRatings.push(null);
                } else {
                    pumpLatestRatings.push(pumpSummary.last10Ratings[i].rating);
                }

            }

            latestData.series.push(pumpLatestRatings);
        }

        console.log("averageData");
        console.log(averageData);
        console.log("latestData:");
        console.log(latestData);

        new Chartist.Bar('#aveChart', averageData);
        new Chartist.Line('#latestChart', latestData, {
            low: 0
        });


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

                if (! angular.equals({}, $scope.pumpSummaryMap)) {

                    updateVisualisation($scope.pumpSummaryMap);
                }
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