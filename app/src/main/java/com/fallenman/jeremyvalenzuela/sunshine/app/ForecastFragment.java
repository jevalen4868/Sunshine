package com.fallenman.jeremyvalenzuela.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    private final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private ArrayAdapter<String> forecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar clicks. It will auto handle clicks on home/up button,
        // as long as we specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.action_refresh)
        {
            updateWeather();
            return true;
        }
        if(id == R.id.action_open_map)
        {
            openPreferredLocationInMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        // Need array adapter to display data dynamically, without using too much memory.
        // It will take our data, and populate the listview.
        forecastAdapter = new ArrayAdapter<>(
                // Current context.
                getActivity(),
                // Id of list item layout.
                R.layout.list_item_forecast,
                // Id of text view to populate.
                R.id.list_item_forecast_textview,
                // Forecast data.
                new ArrayList<String>(7));
        // Get the forecast list view to set forecast array adapter on.
        ListView forecastListView = (ListView)rootView.findViewById(R.id.listview_forecast);
        forecastListView.setAdapter(forecastAdapter);
        forecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecast = forecastAdapter.getItem(position);
                // Executed in an Activity, so 'getActivity' is the Context
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                // Stick some "extra text" on it.
                detailIntent.putExtra(Intent.EXTRA_TEXT, forecast);
                // Start the intent.
                getActivity().startActivity(detailIntent);
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    /**
     * Display map based on geoLocation.
     */
    private void openPreferredLocationInMap() {
        // Retrieve users preference.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        // Build query parameter constants
        final String QUERY_PARAM = "q";
        String location = sp.getString(
                getString(R.string.location_key),
                getString(R.string.location_default));
        Uri geoLoc = Uri.parse("geo:0,0?")
                .buildUpon()
                .appendQueryParameter(
                        QUERY_PARAM,
                        location)
                .build();
        // Build intent.
        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
        mapIntent.setData(geoLoc);
        if(mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Weeeeeeeeeeee!
            startActivity(mapIntent);
        }
        else {
            Log.d(LOG_TAG, "Couldn't call " + location + ", no apps to recieve." );
        }
    }

    private void updateWeather()
    {
        // Retrieve weather data! Adapter is set by the onPostExecute method.
        FetchWeatherTask fwt = new FetchWeatherTask(getActivity(), forecastAdapter);
        // Retrieve users pref for zip before executing.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String locationPref = sharedPreferences.getString(getString(R.string.location_key), getString(R.string.location_default));
        Log.v(LOG_TAG, locationPref);
        fwt.execute(locationPref);
    }
}
