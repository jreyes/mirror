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