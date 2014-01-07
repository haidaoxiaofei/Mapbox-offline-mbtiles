# MapBox Android SDK

Our work-in-progress for a better MapBox experience on Android.

## Including the library in your project

The development of the SDK is on its very earliest stage, so many of its elements will be unstable and not fully tested. If you want to try it in your app project, the easiest option is to download the latest JAR build [here](./mapbox-android-sdk.jar). Otherwise you can build the library from source, and experiment with the latest additions to the project.


### Installing from JAR

To include the library in your project [as a jar](./mapbox-android-sdk.jar) you just need to copy it somewhere in your app project (conventionally in Android that's the ```lib``` directory), and add the library to your classpath.

* In Eclipse, find the library file in your project's file explorer and right click - Build Path - Add to Build Path...
* In IntelliJ IDEA, right click the file in the project explorer and select "Add as library".

### Building from source

**With Gradle** *(recommended)*

We use Gradle to generate a built version of our latest code in a matter of seconds. To use it with your IDE, make sure that Gradle is installed on your machine and import the project by selecting build.gradle as the project file.

If you don't have Gradle installed we also provide a small shell script that autoextracts Gradle and builds the project. Just run ```build.sh``` in a terminal, and you're set.

**Manually**

Building from source means you get the very latest version of our code. The first step is to clone the repository to a directory in your system

```git clone https://github.com/mapbox/mapbox-android-sdk.git ```



The MapBox Android SDK has the following dependencies. All of them are included in this repository, but they need to be added manually in your IDE:

**Our flavor of OSMDroid**

[OSMDroid](https://code.google.com/p/osmdroid/) is an open source project to display OSM maps in Android. It provides a Google Maps for Android-like MapView object in which the map rests. Our fork of this library resides in ```path/to/sdk/src/main```

**slf4j**

OSMDroid needs slf4j to be added to the project when building from source. It's a logging library that is located within the SDK in ```src/main/java/org/osmdroid```

**The SDK**

Our library itself is located in ```path/to/sdk/src/src```

#### Building in IntelliJ IDEA or Android Studio

* Created an Android project in IDEA
* Go to Import Module... and select the OSMDroid folder
* Repeat the step with the SDK
* Go to File-Project Structure-Modules, and add both projects as module dependencies of your app project. 
* Add **slf4j** like any other library.

**Resources**

OSMDroid is at the moment built as a Java library, not an explicit Android library, so there are references to resource static files that need to be referenced in your classpath. This will be corrected soon, but for now it needs to be done in IntelliJ IDEA. To do so:

* Go to File-Project Structure-SDKs.
* For each SDK, add the path to OSMDroid's resources - ```path/to/mapboxsdk/mapbox-android-sdk/src/main/resources/org/osmdroid```

## Basic usage

The main object to show maps is the MapView.

TO DO: instructions for Eclipse.

TO DO: basic map init instructions and/or link to javadoc.