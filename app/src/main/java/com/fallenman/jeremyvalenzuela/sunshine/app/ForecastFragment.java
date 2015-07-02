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
        FetchWeatherTask fwt = new FetchWeatherTask();
        // Retrieve users pref for zip before executing.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String locationPref = sharedPreferences.getString(getString(R.string.location_key), getString(R.string.location_default));
        Log.v(LOG_TAG, locationPref);
        fwt.execute(locationPref);
    }
    /**
     * Created by jeremyvalenzuela on 6/19/15.
     * We need a task to fetch our weather data!
     */
    public class FetchWeatherTask extends AsyncTask<String, Void, String[]>
    {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        /**
         * Apply the data to the global adapter.
         * @param weekForecast
         */
        @Override
        protected void onPostExecute(String[] weekForecast) {
            super.onPostExecute(weekForecast);
            // This adapter was already set as the default adapter for the fragment in onCreateView.
            forecastAdapter.clear();
            for(String day : weekForecast) {
                forecastAdapter.add(day);
            }
        }

        /**
         * parameter 0 - postal code.
         * Right now we are hardcoded for 7 days, and metric units.
         * @param params
         * @return parsed weather data, ready to output to screen.
         */
        @Override
        protected String[] doInBackground(String... params) {
            String postalCode = null;
            int numDays = 7;
            // Parameter handling.
            if(params == null || params.length == 0) {
                // Nothing to do, exit.
                return null;
            }
            // Check parameter 0 - postal code.
            if(params[0].contentEquals("")) {
                // Nothing to do.
                return null;
            }
            // Assign parameters if reached.
            postalCode = params[0];
            // Retrieve forecastJson.
            String forecastJson = getWeatherJsonByPostalCode(postalCode, numDays);
            // If the json is null, we know we have no data, exit.
            if(forecastJson == null) {
                return null;
            }
            // Retrieve parsed data.
            String forecast[] = null;
            try {
                return getWeatherDataFromJson(forecastJson, numDays);
            }
            catch(JSONException je) {
                Log.e(LOG_TAG, "Error ", je);
            }
            return forecast;
        }



        /**
         * Returns JSON retrieved from openWeatherMap API via Http request.
         * @param postalCode
         * @return Weather data in json format.
         */
        private String getWeatherJsonByPostalCode(String postalCode, int numDays) {
            // Connection and reader for the http request.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            // To contain the JSON retrieved from http.
            String forecastJson = null;
            // Hardcode other params for now.
            String format = "json";
            String units = getString(R.string.default_unit);
            // construct the http query and read the json response
            try {
                // Build the Uri using uri builder.
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                Uri weatherUri = Uri.parse(FORECAST_BASE_URL)
                        .buildUpon()
                        .appendQueryParameter(QUERY_PARAM, postalCode)
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, String.valueOf(numDays))
                        .build();
                Log.v(LOG_TAG, weatherUri.toString());
                URL url = new URL(weatherUri.toString());
                // Create the request to open weather map, and open.
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                // Read the input stream into a string.
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // We didn't get any data!
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                // Read the json, one line at a time.
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    // String was empty, let's not parse it.
                    return null;
                }
                forecastJson = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return forecastJson;
        }
        /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Given a string of the form returned by the api call:
         * http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7
         * retrieve the maximum temperature for the day indicated by dayIndex
         * (Note: 0-indexed, so 0 would refer to the first day).
         */
        public double getMaxTemperatureForDay(String weatherJsonStr, int dayIndex)
                throws JSONException {
            // Begin parsing Json, using json object.
            JSONObject weather = new JSONObject(weatherJsonStr);
            // "list" is the object which contains the daily forecast objects,
            JSONArray daysArray = weather.getJSONArray("list");
            // and using the handy getJSONObject, we can nab the dayForecast from the array.
            JSONObject dayForecast  = daysArray.getJSONObject(dayIndex);
            // Now that we have the forecast for the day, get the temp object from the day obj.
            JSONObject tempForDay = dayForecast.getJSONObject("temp");
            // Now that we have the temp object, we can return the max temp.
            return tempForDay.getDouble("max");
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // First, format the temps based on user pref.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

            String unitUserPref = sharedPreferences.getString(getString(R.string.unit_key),getString(R.string.default_unit));
            // If the pref is not the default unit, metric, convert it.
            if( ! unitUserPref.contentEquals(getString(R.string.default_unit))) {
                high = high * (1.8) + 32;
                low = low * (1.8) + 32;
            }
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        public String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for (int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay + i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }
            return resultStrs;
        }
    }
}
