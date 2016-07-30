Another Smart Mirror
====================

[![Join the chat at https://gitter.im/jreyes/mirror](https://badges.gitter.im/jreyes/mirror.svg)](https://gitter.im/jreyes/mirror?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/jreyes/mirror.png?branch=master)](https://travis-ci.org/jreyes/mirror)
![](https://img.shields.io/badge/Licence-Apache%20v2-green.svg)
![](https://img.shields.io/badge/platform-android-green.svg)
![](https://img.shields.io/badge/Min%20SDK-21-green.svg)

This is another Smart Mirror project done in Android. Based on [Max Braun](https://github.com/maxbbraun/mirror) and
[Nick Hall](https://github.com/ineptech/mirror) smart mirror projects.

Currently this project is being rewritten using [Robopupu](https://robopupu.com/) as the base framework for this project.

[![Demo of Another Smart Mirror v1](http://img.youtube.com/vi/7EBSTNqeX6Q/0.jpg)](http://www.youtube.com/watch?v=7EBSTNqeX6Q)

## Thanks

I would like to give a big thanks to [Marko Salmela](https://github.com/Fuusio) for creating the
[Robopupu](https://github.com/Fuusio/Robopupu) library and his constant support in supporting me. Without your great
work, much of the configurable aspect of this project would not be possible.

## Todo

- [X] New grid layout, tentative 15/15/40/15/15 layout
- [X] Draggable panels
    - [X] Main container in middle that gets replaced by current action
    - [X] Dismiss panels by dragging to the left or right borders
    - [X] On minimized panels, mute audio for videos? but no for audio play?
- [ ] New logo
- [ ] Multiple user support
- [ ] Integration with Alexa
    -   [ ] Smarthings support
- [ ] Improve Gesture control module
    - [ ] Integration with Leap Motion
    - [ ] Improve OpenCV module
- [ ] Find alternative for low energy bluetooth beacons
- [ ] Configurable commands
    - [X] Use NanoHttp
    - [X] Use Angular
    - [X] Use Bootstrap
    - [X] Store and load configuration from preferences
    - [X] Add voice command for the configuration screen
    - [ ] Use secure preferences
    - [X] Run once on installation and on voice command after that
- [ ] WebView using custom tabs (Chrome)
- [ ] Add new commands
    - [ ] Add selfie command
    - [X] Add video chat command
        -   [X] Pause / Mute when is on the sidelines
        -   [X] Add end call button
    - [ ] Livecoding command?
    - [ ] Reddit news
    - [ ] NPR news
- [ ] Hot word support
    - [ ] Add support for snowboy
    - [ ] Support multiple hotword modules
- [X] Update commands
    - [ ] Web module
        - [ ] Add a proxy running on port 34000
    - [X] Spotify command
        - [X] Add pause/play button
        - [X] Make it configurable
        - [X] Add a spotify client check (Remove WebView login)

## Hardware

For the hardware I've used:
* An [Odroid C2](http://ameridroid.com/products/odroid-c2)
* A [Logitech C920](http://www.amazon.com/Logitech-Webcam-Widescreen-Calling-Recording/dp/B006JH8T3S) webcam
* A [Radius Network RadBeacon](http://www.amazon.com/Radius-Networks-RadBeacon-Dot-Technology/dp/B00JJ4P864)
* A [HDMI monitor](http://www.amazon.com/VG278HE-1920x1080-Ergonomic-Back-lit-Monitor/dp/B00906HM6K)
* A [shadowbox](http://www.michaels.com/frames/display-cases-and-shadowboxes/840874378)
* [Mirror tint](http://www.amazon.com/Window-Film-Mirror-Silver-36in/dp/B00CWGIHBE)
* A [powered usb hub](http://www.amazon.com/Anker-4-Port-Adapter-Charger-Included/dp/B0192LPK5M)
* A [Wifi adapter](http://ameridroid.com/products/wifi-module-4)
* A [bluetooth adapter](http://ameridroid.com/products/usb-bluetooth-module-2)
* Some wood, nails, and glue

## Libraries

### Pocketsphinx

Hotword detection is being driven by the Pocketsphinx library. Implementation and instructions how to use this library
it can be found on its [website](http://cmusphinx.sourceforge.net/wiki/tutorialandroid).

### Houndify

For Voice Commands, [Houndify](http://www.houndify.com) is being used. They have a free tier with a daily quota of how
 many queries you can request per day. You need to register your application and get the Hound client ID and client key
 and set those values at [mirror.properties](https://github.com/jreyes/mirror/blob/master/app/src/main/assets/mirror.properties)

### Proximity Recognition

To enable proximity recognition you need to have a Bluetooth beacon on hand like the
[Radius Networks RadBeacon](http://www.amazon.com/Radius-Networks-RadBeacon-Dot-Technology/dp/B00JJ4P864). After that
you need to follow the instructions detailed [here](https://github.com/RadiusNetworks/proximitykit-android).

### Spotify

Song support is provided by Spotify. To use Spotify you need to register your application at
[https://developer.spotify.com](https://developer.spotify.com) and also own a Premium Spotify account. Once you get
your Spotify Client ID, add it to [mirror.properties](https://github.com/jreyes/mirror/blob/master/app/src/main/assets/mirror.properties)

### HandWave Library

Credit goes to [Kriss](https://github.com/kritts/HandWave) for the original HandWave library and
[Koalified](https://github.com/Koalified/NewHandwave) for the updated version.

### OpenCV Library

More information about OpenCV can be found in its [website](http://opencv.org/). This library is needed to use the
HandWave library.

#License

The contents of this repository are covered under the [Apache License](LICENSE).