/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {

    // Application Constructor
    initialize:function () {
        this.bindEvents();
        this.bindActions();
    },
    bindActions:function () {
        $("#test_camera").live("click", {}, this.testCamera);
    },
    testCamera:function (event) {
        navigator.camera.getPicture(app.cameraSuccess, app.cameraFail, { quality:50,
            destinationType:Camera.DestinationType.DATA_URL
        });
    },
    cameraSuccess:function (imageData) {
        alert(imageData);
    },
    cameraFail:function (message) {
        alert('Failed because: ' + message);
    },
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents:function () {
        document.addEventListener('deviceready', this.onDeviceReady, false);
        document.addEventListener("resume", this.onResume, false);
        document.addEventListener("pause", this.onPause, false);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicity call 'app.receivedEvent(...);'
    onDeviceReady:function () {
        navigator.notification.alert(
            'Device ready,' + 'Device Name: ' + device.name + '<br />' +
                'Device Cordova: ' + device.cordova + '<br />' +
                'Device Platform: ' + device.platform + '<br />' +
                'Device UUID: ' + device.uuid + '<br />' +
                'Device Model: ' + device.model + '<br />' +
                'Device Version: ' + device.version + '<br />', // message
            this.alertDismissed, // callback
            '提示', // title
            'Done'                  // buttonName
        );
    },
    onResume:function () {
        navigator.notification.beep(1);
    },
    onPause:function () {
        navigator.notification.vibrate(500);
    },
    alertDismissed:function () {

    }
};
