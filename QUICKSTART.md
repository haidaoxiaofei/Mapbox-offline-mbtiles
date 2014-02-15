##Getting started with the Mapbox Android SDK

This guide will take you through the process of adding a map to your Android app. It assumes you have a Java IDE (like Eclipse or IntelliJ IDEA) with the [Android SDK](http://developer.android.com/sdk/index.html) installed, and an app project open.

###Adding the SDK to your project

Clone the repository into your system and import build.gradle in the app project.

### Required Permissions

Ensure the following *core* permissions are requested in your `AndroidManifest.xml` file:

```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

If your project needs to access location services, it'll also need the following permissions too:

```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```

###The MapView
The ```MapView``` class is the key component of our library. It behaves like any other ```ViewGroup``` and its behavior can be changed statically with an [XML layout](http://developer.android.com/guide/topics/ui/declaring-layout.html) file, or programmatically during runtime.

#### XML layout
To add the ```MapView``` as a layout element, add the following to your xml file:
```xml
<com.mapbox.mapboxsdk.views.MapView
android:id="@+id/mapview"
android:layout_width="fill_parent"
android:layout_height="fill_parent"
mapid="Your MapBox mapid" />
```


And then you can call it programmatically with

```java
this.findViewById(R.id.mapview);
```

#### On runtime

On runtime you can create a new MapView by specifying the context of the application and a valid [MapBox mapid](https://www.mapbox.com/developers/api-overview/), a TileJSON file or a zxy image template.

```java
MapView mapView = new MapView(this, "examples.map-vyofok3q");
```

And set it as the current view like this:
```java	
this.setContentView(mapView);
```

### Overlays

Anything visual that is displayed over the map, maintaining its geographical position, we call it an ```Overlay```. To access a MapView's overlays at any point during runtime, use:

```java
mapView.getOverlays();
```

#### Markers

Adding a marker with the default styling is as simple as calling this for every marker you want to add:

```java
mapView.addMarker(latitude, longitude, title, text);
```

#### Location overlay

The location of the user can be displayed on the view using ```MyLocationNewOverlay```
```java
MyLocationNewOverlay myLocationOverlay = new MyLocationNewOverlay(this, mapView);
myLocationOverlay.enableMyLocation();
myLocationOverlay.setDrawAccuracyEnabled(true);
mapView.getOverlays().add(myLocationOverlay);
```

####Paths

Paths are treated as any other ```Overlay```, and are drawn like this:
```java
PathOverlay line = new PathOverlay(Color.RED, this);
line.addPoint(new GeoPoint(51.2, 0.1));
line.addPoint(new GeoPoint(51.7, 0.3));
mapView.getOverlays().add(line);
```

####Drawing anything into the map
To add anything with a higher degree of  customization you can declare your own ```Overlay``` subclass and define what to draw by overriding the ```draw``` method. It will give you a Canvas object for you to add anything to it:

```java
class AnyOverlay extends Overlay{
    @Override
    protected void draw(Canvas canvas, MapView mapView, boolean shadow) {		
        //do anything with the Canvas object
    }
}
```

### Switching layers *(experimental)*
We're making switchable layers as simple as possible in the SDK. You can try toggling between several maps (identified by a valid MapBox ID, a TileJSON file or a zxy image template) using the switchToLayer method

```java
mapView.switchToLayer("examples.map-vyofok3q");
```
