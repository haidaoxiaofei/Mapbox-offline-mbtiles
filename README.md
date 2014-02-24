**This is pre-alpha software. It may be broken at any time in development,
and there is zero guarantee of API stability. Kick the tires at your own
risk, they may explode.**

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

#### Building in Eclipse

* Download the project mapbox-android-sdk from github
* Go to File -> Import... and select the Android -> Existing Android Code Into Workspace -> 
* Choose directory of downloaded project.
* Will appear two projects: mapbox-android-sdk\src and mapbox-android-sdk\test
* In IDE you will see the errors in class "com.mapbox.mapboxsdk.views.MapView":
* You need to correct the line 21: "import com.mapbox.mapboxforandroid.R;" to "import com.mapbox.mapboxsdk.R;" or just do "Organize imports"(Ctrl+Shift+O)
* Then let's correct project name "src". Preferably, to rename "src" project name to "MapboxSDK", after rename you will get that your SDK project is compiled correctly. 
* We can start using the SDK project, but in future we will need to use markers and to fix an issue in OSMDroidTests we need to add some default pictures which are in OSM android library and were not included in our SDK.
* So what we gonna do is to add all '.png' images from OSM android jar  (https://code.google.com/p/osmdroid/downloads/detail?name=osmdroid-android-4.0.jar&can=1&q=) to resources folder to com.mapbox.mapboxsdk package. The main important image is marker_default.png that will cause you the errors in log if you are going to use markers in OSMDroidTest project.
* SDK is ready to be used.
* Open properties of the second project "OSMDroidTests" ->Android -> at the bottom you will see Library -> click on Add button to add your SDK project.
* That's all now your test project - "OSMDroidTests" is working in Eclipse. Enjoy!

## [Quick-start Guide](https://github.com/mapbox/mapbox-android-sdk/blob/master/QUICKSTART.md)
