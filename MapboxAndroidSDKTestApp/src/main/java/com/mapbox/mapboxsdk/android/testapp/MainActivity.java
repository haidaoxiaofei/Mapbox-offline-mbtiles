package com.mapbox.mapboxsdk.android.testapp;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private ArrayList<String> testFragmentNames;
    private int selectedFragmentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
           MapView.setDebugMode(true); //make sure to call this before the view is created!
           */
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        testFragmentNames = new ArrayList<String>();
        testFragmentNames.add(getString(R.string.mainTestMap));
        testFragmentNames.add(getString(R.string.alternateTestMap));
        testFragmentNames.add(getString(R.string.markersTestMap));
        testFragmentNames.add(getString(R.string.itemizedOverlayTestMap));
        testFragmentNames.add(getString(R.string.localGeoJSONTestMap));
        testFragmentNames.add(getString(R.string.diskCacheDisabledTestMap));
        testFragmentNames.add(getString(R.string.offlineCacheTestMap));
        testFragmentNames.add(getString(R.string.programmaticTestMap));
        testFragmentNames.add(getString(R.string.webSourceTileTestMap));
        testFragmentNames.add(getString(R.string.locateMeTestMap));
        testFragmentNames.add(getString(R.string.pathTestMap));
        testFragmentNames.add(getString(R.string.bingTestMap));

        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, testFragmentNames));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawerOpen, R.string.drawerClose) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(testFragmentNames.get(selectedFragmentIndex));
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(R.string.appName);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Set MainTestFragment
        selectItem(0);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        selectedFragmentIndex = position;
        // Create a new fragment and specify the planet to show based on position
        Fragment fragment;

        switch (position) {
            case 0:
                fragment = new MainTestFragment();
                break;
            case 1:
                fragment = new AlternateMapTestFragment();
                break;
            case 2:
                fragment = new MarkersTestFragment();
                break;
            case 3:
                fragment = new ItemizedIconOverlayTestFragment();
                break;
            case 4:
                fragment = new LocalGeoJSONTestFragment();
                break;
            case 5:
                fragment = new DiskCacheDisabledTestFragment();
                break;
            case 6:
                fragment = new OfflineCacheTestFragment();
                break;
            case 7:
                fragment = new ProgrammaticTestFragment();
                break;
            case 8:
                fragment = new WebSourceTileTestFragment();
                break;
            case 9:
                fragment = new LocateMeTestFragment();
                break;
            case 10:
                fragment = new PathTestFragment();
                break;
            case 11:
                fragment = new BingTileTestFragment();
                break;
            default:
                fragment = new MainTestFragment();
                break;
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
            .replace(R.id.content_frame, fragment)
            .commit();

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(testFragmentNames.get(position));
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }
}
