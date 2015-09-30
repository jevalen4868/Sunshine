package com.fallenman.jeremyvalenzuela.sunshine.app;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.fallenman.jeremyvalenzuela.sunshine.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String DETAIL_URI = "URI";
    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_WEATHER_CONDITION_ID = 5;
    static final int COL_HUMIDITY = 6;
    static final int COL_PRESSURE = 7;
    static final int COL_WIND_SPEED = 8;
    static final int COL_DEGREES = 9;
    private static final int DETAIL_LOADER = 0;
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES
    };
    private final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private String forecast;
    private Uri uri;
    private ShareActionProvider shareActionProvider;

    // all views associated to this fragment.
    private ImageView mIconView;
    private TextView mFriendlyDateView;
    private TextView mDateView;
    private TextView mDescriptionView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;

    public DetailActivityFragment() {
        // MUST DO THIS IF DONE IN FRAGMENT.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            this.uri = args.getParcelable(DetailActivityFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        // Voila!!!
        this.mIconView = (ImageView) rootView.findViewById(R.id.weather_icon);
        this.mFriendlyDateView = (TextView) rootView.findViewById(R.id.friendly_date_textview);
        this.mDateView = (TextView) rootView.findViewById(R.id.date_textview);
        this.mDescriptionView = (TextView) rootView.findViewById(R.id.forecast_textview);
        this.mHighTempView = (TextView) rootView.findViewById(R.id.high_textview);
        this.mLowTempView = (TextView) rootView.findViewById(R.id.low_textview);
        this.mHumidityView = (TextView) rootView.findViewById(R.id.humidity_textview);
        this.mWindView = (TextView) rootView.findViewById(R.id.wind_textview);
        this.mPressureView = (TextView) rootView.findViewById(R.id.pressure_textview);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detail_fragment, menu);
        // Locate the share menu item.
        MenuItem item = menu.findItem(R.id.action_share);
        // Fetch and store the action provider.
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if (forecast != null) {
            shareActionProvider.setShareIntent(createShareIntent());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DETAIL_LOADER, savedInstanceState, this);
    }

    public void onLocationChanged(String location) {
        // replace the uri, since the location has changed
        Uri uri = this.uri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, date);
            this.uri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    private Intent createShareIntent() {
        // Nab the forecast data from the activity.
        TextView forecastView = (TextView) getActivity().findViewById(R.id.forecast_textview);
        String forecastData = forecastView.getText() + " #SunshineApp";
        // Now create the intent for share action.
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.putExtra(Intent.EXTRA_TEXT, forecastData);
        shareIntent.setType("text/plain");
        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != this.uri) {
            return new CursorLoader(
                    getActivity(),
                    this.uri,
                    FORECAST_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && !cursor.moveToFirst()) {
            return;
        }
        Activity activity = getActivity();
        // Prepare all values from db.
        int weatherId = cursor.getInt(COL_WEATHER_ID);
        // image - use condition id and layout id
        // Retrieve weather icons based on layoutId and conditionId.
        int conditionId = cursor.getInt(COL_WEATHER_CONDITION_ID);
        // Use weather art image
        Glide.with(this)
                .load(Utility.getArtUrlForWeatherCondition(getActivity(), conditionId))
                .error(Utility.getArtResourceForWeatherCondition(conditionId))
                .crossFade()
                .into(mIconView);
                // date
        long date = cursor.getLong(COL_WEATHER_DATE);
        String friendlyDateText = Utility.getDayName(activity, date);
        this.mFriendlyDateView.setText(friendlyDateText);
        String dateText = Utility.getFormattedMonthDay(activity, date);
        this.mDateView.setText(dateText);
        // description.
        String weatherDescription = cursor.getString(COL_WEATHER_DESC);
        this.mDescriptionView.setText(weatherDescription);
        mDescriptionView.setContentDescription(getString(R.string.a11y_forecast, weatherDescription));

        // For accessibility, add a content description to the icon field. Because the ImageView
        // is independently focusable, it's better to have a description of the image. Using
        // null is appropriate when the image is purely decorative or when the image already
        // has text describing it in the same UI component.
        mIconView.setContentDescription(getString(R.string.a11y_forecast_icon, weatherDescription));

        // temps
        // Read high temperature from cursor
        double high = cursor.getDouble(COL_WEATHER_MAX_TEMP);
        String highString = Utility.formatTemperature(activity, high);
        this.mHighTempView.setText(highString);
        mHighTempView.setContentDescription(getString(R.string.a11y_high_temp, highString));

        // Read low temperature from cursor
        double low = cursor.getDouble(COL_WEATHER_MIN_TEMP);
        String lowString = Utility.formatTemperature(activity, low);
        this.mLowTempView.setText(lowString);
        mLowTempView.setContentDescription(getString(R.string.a11y_low_temp, lowString));

        // Wind info.
        float windSpeed = cursor.getFloat(COL_WIND_SPEED);
        float degrees = cursor.getFloat(COL_DEGREES);
        String wind = Utility.getFormattedWind(activity, windSpeed, degrees);
        this.mWindView.setText(wind);
        mWindView.setContentDescription(mWindView.getText());

        // Humidity.
        String humidity = activity.getString(R.string.format_humidity, cursor.getFloat(COL_HUMIDITY));
        this.mHumidityView.setText(humidity);
        mHumidityView.setContentDescription(mHumidityView.getText());

        // Pressure.
        String pressure = activity.getString(R.string.format_pressure, cursor.getFloat(COL_PRESSURE));
        this.mPressureView.setText(pressure);
        mPressureView.setContentDescription(mPressureView.getText());

        // TODO figure this out at some point.
        /*
        if(AccessibilityManager.getInstance(mContext).isEnabled()) {
        sendAcc...(AccEvent.TYPE_VIEW_TEXT_CHANGED);
        @Override
        public boolean dispatPopAcc...(AccEvent ev) {
        ev.getText().add(windSpeedDir);
        return true;
        }
         */

        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(createShareIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
