Another Smart Mirror
====================
[![Build Status](https://travis-ci.org/jreyes/mirror.png?branch=master)](https://travis-ci.org/jreyes/mirror)
![](https://img.shields.io/badge/Licence-Apache%20v2-green.svg)
![](https://img.shields.io/badge/platform-android-green.svg)
![](https://img.shields.io/badge/Min%20SDK-21-green.svg)

This is another Smart Mirror project done in Android. Based on [Max Braun](https://github.com/maxbbraun/mirror) and
[Nick Hall](https://github.com/ineptech/mirror) smart mirror projects.

For the hardware I've used:
* An [Odroid C2](http://ameridroid.com/products/odroid-c2)
* A [Logitech C920](http://www.amazon.com/Logitech-Webcam-Widescreen-Calling-Recording/dp/B006JH8T3S) webcam
* A [Radius Network RadBeacon](http://www.amazon.com/Radius-Networks-RadBeacon-Dot-Technology/dp/B00JJ4P864)
* A [HDMI monitor](http://www.amazon.com/VG278HE-1920x1080-Ergonomic-Back-lit-Monitor/dp/B00906HM6K)
* A [shadowbox](http://www.michaels.com/frames/display-cases-and-shadowboxes/840874378)
* A [powered usb hub](http://www.amazon.com/Anker-4-Port-Adapter-Charger-Included/dp/B0192LPK5M)
* A [Wifi adapter](http://ameridroid.com/products/wifi-module-4)
* A [bluetooth adapter](http://ameridroid.com/products/usb-bluetooth-module-2)
* Some wood, nails, and glue

## Pocketsphinx

Hotword detection is being driven by the Pocketsphinx library. Implementation and instructions how to use this library
it can be found on its [website](http://cmusphinx.sourceforge.net/wiki/tutorialandroid).

## Houndify

For Voice Commands, [Houndify](http://www.houndify.com) is being used. They have a free tier with a daily quota of how
 many queries you can request per day. You need to register your application and get the Hound client ID and client key
 and set those values at [mirror.properties](https://github.com/jreyes/mirror/blob/master/app/src/main/assets/mirror.properties)

## Proximity Recognition

To enable proximity recognition you need to have a Bluetooth beacon on hand like the
[Radius Networks RadBeacon](http://www.amazon.com/Radius-Networks-RadBeacon-Dot-Technology/dp/B00JJ4P864). After that
you need to follow the instructions detailed [here](https://github.com/RadiusNetworks/proximitykit-android).

## Spotify

Song support is provided by Spotify. To use Spotify you need to register your application at
[https://developer.spotify.com](https://developer.spotify.com) and also own a Premium Spotify account. Once you get
your Spotify Client ID, add it to [mirror.properties](https://github.com/jreyes/mirror/blob/master/app/src/main/assets/mirror.properties)

## HandWave Library

Credit goes to [Kriss](https://github.com/kritts/HandWave) for the original HandWave library and
[Koalified](https://github.com/Koalified/NewHandwave) for the updated version.

## OpenCV Library

More information about OpenCV can be found in its [website](http://opencv.org/). This library is needed to use the
HandWave library.

##License

Copyright 2016 Johann Reyes

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.