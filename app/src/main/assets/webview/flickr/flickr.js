/*
 * Copyright 2016 Johann Reyes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

angular
    .module('FlickrApp', [])
    .config(function ($locationProvider) {
        $locationProvider.html5Mode({
            enabled: true,
            requireBase: false
        });
    })
    .controller('flickrController', function ($scope, $location, $http, $interval) {

        $scope.flickrId = $location.search().flickrId;
        $scope.animationSpeed = $location.search().animationSpeed * 60 * 1000; // minutes
        $scope.updateInterval = $location.search().updateInterval * 60 * 60 * 1000; // hours
        $scope.displayPhotoInterval = null;

        this.getParams = function () {
            var params = '?';
            params += 'format=json';
            params += '&lang=en-us';
            params += '&id=' + $scope.flickrId;
            params += '&nojsoncallback=1';
            return {
                params: {url: 'https://www.flickr.com/services/feeds/photos_public.gne' + params}
            }
        };

        this.grabPhotos = function () {
            $http
                .get('http://localhost:4000/proxy', this.getParams())
                .then(function (response) {
                    if (angular.isDefined($scope.displayPhotoInterval)) {
                        $interval.cancel($scope.displayPhotoInterval);
                    }

                    $scope.index = 0;
                    $scope.items = response.data.items;
                    $scope.displayPhotoInterval = $interval($scope.displayPhoto, $scope.animationSpeed);
                    $scope.displayPhoto();
                });
        };
        $interval(this.grabPhotos(), $scope.updateInterval);

        $scope.displayPhoto = function () {
            if ($scope.index >= $scope.items.length) {
                $scope.index = 0;
            }

            $scope.title = $scope.items[$scope.index].title;
            $scope.img = $scope.items[$scope.index].media.m;
            $scope.index++;
        };

    });