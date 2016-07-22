'use strict';

angular
    .module('ComicApp', [])
    .controller('comicController', function ($scope, $http, $interval) {

        this.fetchComic = function () {
            $http.get('http://localhost:34000/proxy', {
                params: {url: 'http://xkcd.com/info.0.json'}
            }).then(function (response) {
                var comic = response.data;
                $scope.img = comic.img;
                $scope.title = comic.safe_title;
            });
        };

        $interval(this.fetchComic(), 10000 * 60 * 60);
    });