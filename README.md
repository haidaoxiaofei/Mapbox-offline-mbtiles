[![Build Status](https://travis-ci.org/mapbox/mapbox-android-sdk.svg?branch=mb-pages)](https://travis-ci.org/mapbox/mapbox-android-sdk)

# Mapbox Android SDK

An open source alternative for native maps on Android. This library lets
you use [Mapbox](https://www.mapbox.com/), [OpenStreetMap](http://www.openstreetmap.org/),
and other tile sources in your app, as well as overlays like [GeoJSON](http://geojson.org/)
data and interactive tooltips.

This is a fork of [osmdroid](http://code.google.com/p/osmdroid/), so the entire
core is open source: it doesn't depend on the Google Maps SDK or any components
outside of [AOSP](https://source.android.com/) that would require the [Google Play Store](https://play.google.com/).

## Installation

We recommend using the Mapbox Android SDK with [Gradle](http://www.gradle.org/):
this will automatically install the necessary dependencies and pull the SDK
binaries from the Maven Central repository ( [Mapbox Android SDK on Maven Central](http://search.maven.org/#artifactdetails%7Ccom.mapbox.mapboxsdk%7Cmapbox-android-sdk%7C0.2.3%7Cjar) ).

To install the current **stable** version add this to your `build.gradle`:

```groovy
repositories {
    mavenCentral()
}

dependencies {
    compile ('com.mapbox.mapboxsdk:mapbox-android-sdk:0.5.0@aar'){
        transitive=true
    }
    compile ('com.cocoahero.android:geojson:1.0.0@aar'){
        transitive=true
    }
}
```

To install the current **SNAPSHOT** version add this to your `build.gradle`:

```groovy
repositories {
    mavenCentral()
    maven { url "http://oss.sonatype.org/content/repositories/snapshots/" }
}

dependencies {
    compile ('com.mapbox.mapboxsdk:mapbox-android-sdk:0.5.1-SNAPSHOT@aar'){
        transitive=true
    }
    compile ('com.cocoahero.android:geojson:1.0.0@aar'){
        transitive=true
    }
}
```

For a full example Android project incorporating the SDK in this manner, please see the Mapbox Dev Preview app.

* Source: https://github.com/mapbox/mapbox-android-demo
* Free download to your Android device from Google Play: https://play.google.com/store/apps/details?id=com.mapbox.mapboxandroiddemo

### NOTE: SDK Versions
At any given time there will be 3 different versions of the SDK to use.  You're welcome to use whichever one makes the most sense for your project, just be aware that each comes with a different level of **stability**.

1. Stable / Supported
 * Currently `0.5.0`
2. SNAPSHOT
 * Currently `0.5.1-SNAPSHOT`
3. Source

### Manually / Hardcoding In Project

Download and include the mapbox-android-sdk.aar file and all
artifacts (.aar, .jar files, and Android support / compatibility libraries listed) listed in [`MapboxAndroidSDK / build.gradle`](https://github.com/mapbox/mapbox-android-sdk/blob/mb-pages/MapboxAndroidSDK/build.gradle).  For those new to Gradle the artifacts are listed in the `dependencies` block.
These **will** change over time so please check back regularly.


### Legacy Support (Eclipse) - Experimental

The Mapbox Android SDK is also packaged as a `.apklib` file.  This allows integration with older tools (Eclipse) that don't support the `.aar` format yet.  In order to make this work the project will need to make use of [Maven](http://maven.apache.org), and it the case of Eclipse the [M2Eclipse](http://eclipse.org/m2e/) Maven plugin.  From there configure the Maven `pom.xml` to include the following dependency:

```xml
<dependency>
    <groupId>com.mapbox.mapboxsdk</groupId>
    <artifactId>mapbox-android-sdk</artifactId>
    <version>0.5.0</version>
    <type>apklib</type>
</dependency>
```

For more information on how to use Maven and Eclipse together please see Sonatype's [Developing with Eclipse and Maven](http://books.sonatype.com/m2eclipse-book/reference/) tutorial.

### Building From Source

Building from source means you get the very latest version of our code.
The first step is to clone the repository to a directory in your system

    git clone https://github.com/mapbox/mapbox-android-sdk.git

We use Gradle as a configuration and build tool: to use it with your IDE,
import the project by selecting `build.gradle` in the project root directory
as the project file.

Don't worry about installing Gradle on your system if you don't already have
it:  the project makes use of Gradle Wrapper, so a correct & current project
version of Gradle will automatically be installed and used to run the builds.
To use the Gradle wrapper just look for `gradlew`  or `gradlew.bat` (Windows)
in the project's main directory.  For example:

    cd <PROJECT_ROOT>
     ./gradlew --version

which will produce something like:

```
------------------------------------------------------------
Gradle 1.10
------------------------------------------------------------

Build time:   2013-12-17 09:28:15 UTC
Build number: none
Revision:     36ced393628875ff15575fa03d16c1349ffe8bb6

Groovy:       1.8.6
Ant:          Apache Ant(TM) version 1.9.2 compiled on July 8 2013
Ivy:          2.2.0
JVM:          1.7.0_40 (Oracle Corporation 24.0-b56)
OS:           Mac OS X 10.9.2 x86_64
```

See the [Gradle Wrapper documentation for more details](http://www.gradle.org/docs/current/userguide/gradle_wrapper.html).

Then you can build an archive:

```sh
./gradlew clean assembleRelease

# The archive (mapbox-android-sdk-<VERSION>.aar) will be found in
<PROJECTHOME>/MapboxAndroidSDK/build/libs
```

**Don't forget to then also include the dependencies from `MapboxAndroidSDK / build.gradle` in your classpath!**

## Changes from OSMDroid

This project is a fork of OSMDroid, but is significantly different as the result of major refactoring and rethinking.

* [GeoJSON](http://geojson.org/) and [TileJSON](https://www.mapbox.com/foundations/an-open-platform) support added.
* The Mapbox Android SDK is [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) licensed, and does not include any GPL or copyleft add-ons.
* Mapbox Android SDK is a small core design. OSMDroid's semi-related utilities like GPX uploading, UI zoom buttons, GEM & Zip file support, Scale Bar, Compass Overlay, and more have been removed. These requirements will be better served by separate modules that do one thing well.
* Interfaces and abstract classes are only defined when suitable: most single-use interfaces are removed for simplicity.
* Data objects like points and lines use `double`s instead of the `E6` int convention. This simplifies implementations. The `reuse` pattern is also deemphasized, since it's less necessary with newer JITs.
* Instead of supporting [specific tile layers](https://github.com/osmdroid/osmdroid/tree/mb-pages/osmdroid-android/src/main/java/org/osmdroid/tileprovider/tilesource) with hardcoded paths, Mapbox Android SDK provides an easy-to-configure `TileLayer` class.
* Small modules are used in place of local implementations - [DiskLRUCache](https://github.com/JakeWharton/DiskLruCache) for caching, [OkHttp](http://square.github.io/okhttp/) for connection niceties, and [android-geojson](https://github.com/cocoahero/android-geojson) for GeoJSON parsing.
* Markers can optionally use the Mapbox marker API for customized images.
* Code style follows [the Sun conventions](https://github.com/mapbox/mapbox-android-sdk/blob/mb-pages/checks.xml)
* [Automated tests](https://github.com/mapbox/mapbox-android-sdk/blob/mb-pages/MapboxAndroidSDKTestApp/src/instrumentTest/java/com/mapbox/mapboxsdk/android/testapp/test/MainActivityTest.java) are included.
* [slf4j](http://www.slf4j.org/) dependency is removed

## Contributors Note (aka, Where's the `master` branch?)

The project's `master` branch is actually `mb-pages`.  There is no branch named `master` nor will there be.  The reason for it is that it allows some automatic processing and publishing of documentation behind the scenes.  In practice this shouldn't affect anybody wanting to contribute, but is something that will probably seem a bit "different" to newcomers.  Anyway, that's what's going on.  If you'd like more information please see [#404](https://github.com/mapbox/mapbox-android-sdk/issues/404) .

## [Quick-start Guide](https://github.com/mapbox/mapbox-android-sdk/blob/mb-pages/QUICKSTART.md)
