package com.mapbox.mapboxsdk.android.testapp;

import android.content.res.AssetManager;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rui on 14-9-24.
 */
public class RoadRouteRecordManager {
    public static List<Segment> routesList = new ArrayList<Segment>();

    public static List<Segment> getRoutesList(){
        if( routesList == null ){
            routesList = new ArrayList<Segment>();
        }

        if( routesList.size() == 0 ){
            try {
                loadRouteRecord();
            }catch (Exception ex){}
        }

        return routesList;
    }

    private static void loadRouteRecord() throws NumberFormatException, IOException {
        AssetManager assetManager = MainActivity.assetManager;
        InputStream csvStream = assetManager.open("OGIS_Route.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(csvStream));
        String rtRecord = null;
        while ((rtRecord = br.readLine()) != null) {
            String rtInfo[] = rtRecord.split(" ");
            Point point1 = new Point(rtInfo[0], rtInfo[1]);
            Point point2 = new Point(rtInfo[2], rtInfo[3]);
            routesList.add(new Segment(point1, point2));
        }
        br.close();
        csvStream.close();
    }
}
