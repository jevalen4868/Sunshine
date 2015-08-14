package com.fallenman.jeremyvalenzuela.sunshine.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {

    private static final String FORECAST_FRAGMENT_TAG = "ForecastFragmentTag";
    /**
     * Current known location.
     */
    private String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Set location.
        location = Utility.getPreferredLocation(this);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(
                    R.id.container, new ForecastFragment(), FORECAST_FRAGMENT_TAG
            ).commit();
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ForecastFragment ff = (ForecastFragment) getSupportFragmentManager().findFragmentByTag(FORECAST_FRAGMENT_TAG);
        ff.onLocationChanged();
    }
}
