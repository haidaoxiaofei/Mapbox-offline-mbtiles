**This is pre-alpha software. It may be broken at any time in development,
and there is zero guarantee of API stability. Kick the tires at your own
risk, they may explode.**

[![Build Status](https://travis-ci.org/mapbox/mapbox-android-sdk.png?branch=master)](https://travis-ci.org/mapbox/mapbox-android-sdk)

# Mapbox Android SDK

An open source alternative for native maps on Android.

## Including the library in your project

The development of the SDK is on its very earliest stage, so many of its elements
will be unstable and not fully tested. Otherwise you can build the library from
source, and experiment with the latest additions to the project.

### Building from source

**With Gradle** *(recommended)*

We use Gradle to generate a built version of our latest code in a matter of seconds.
To use it with your IDE, make sure that Gradle is installed on your machine and
import the project by selecting build.gradle as the project file.

If you don't have Gradle installed we also provide a small shell script that
auto-extracts Gradle and builds the project. Run `build.sh` in a terminal, and you're set.

**Manually**

Building from source means you get the very latest version of our code. The first step is to clone the repository to a directory in your system

```git clone https://github.com/mapbox/mapbox-android-sdk.git ```

#### Building in IntelliJ IDEA or Android Studio

* Created an Android project in IDEA
* Go to Import Module... and select the OSMDroid folder
* Repeat the step with the SDK
* Go to File-Project Structure-Modules, and add both projects as module dependencies of your app project.

## [Quick-start Guide](https://github.com/mapbox/mapbox-android-sdk/blob/master/QUICKSTART.md)
