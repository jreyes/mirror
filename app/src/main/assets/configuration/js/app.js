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
    .module('Mirror', ['ngSanitize', 'schemaForm', 'pascalprecht.translate', 'ngSchemaFormFile'])
    .controller('MirrorController', function ($scope, $http) {

        $scope.decorator = 'bootstrap-decorator';
        $scope.modelData = {};
        $scope.currentIndex = -1;
        $scope.done = false;

        $scope.modules = [
            /* {{modules}} */
        ];

        $scope.setNewData = function (index) {
            var data = $scope.modules[index];
            $scope.currentIndex = index;
            $scope.component = data.component;
            $scope.schema = data.schema;
            $scope.form = data.form;
            $scope.modelData = data.model || {};
            $scope.done = false;
        };

        $scope.init = function () {
            $scope.setNewData(0);
        };

        $scope.exit = function () {
            $http.head('/');
        };

        $scope.submitForm = function (form) {
            // First we broadcast an event so all fields validate themselves
            $scope.$broadcast('schemaFormValidate');
            // Then we check if the form is valid
            if (form.$valid) {
                $http
                    .post($scope.component, $scope.modelData)
                    .then(function (response) {
                        $scope.currentIndex++;
                        if ($scope.currentIndex < $scope.modules.length) {
                            $scope.setNewData($scope.currentIndex);
                        } else {
                            $scope.currentIndex = -1;
                            $scope.component = '';
                            $scope.schema = {};
                            $scope.form = {};
                            $scope.modelData = {};
                            $scope.done = true;
                        }
                    }, function (response) {
                        jQuery('body').showMessage({
                            thisMessage: [response.data], className: 'fail', autoClose: true, delayTime: 3000
                        });
                    });
            }
        };
    })
    .config(['$translateProvider', function ($translateProvider) {
        // Simply register translation table as object hash
        $translateProvider.translations('en', {
            'modules.upload.dndNotSupported': 'Drag n drop not supported by your browser',
            'modules.attribute.fields.required.caption': 'Required',
            'modules.upload.descriptionSinglefile': 'Drop your file here',
            'modules.upload.descriptionMultifile': 'Drop your file(s) here',
            'buttons.add': 'Open file browser',
            'modules.upload.field.filename': 'Filename',
            'modules.upload.field.preview': 'Preview',
            'modules.upload.multiFileUpload': 'Multifile upload',
            'modules.upload.field.progress': 'Progress',
            'buttons.upload': 'Upload'
        });
        $translateProvider.preferredLanguage('en');
        $translateProvider.useSanitizeValueStrategy('sanitize');
    }]);