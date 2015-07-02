package com.fallenman.jeremyvalenzuela.sunshine.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {
    private final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    private ShareActionProvider shareActionProvider;

    public DetailActivityFragment() {
        // MUST DO THIS IF DONE IN FRAGMENT.
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        // Grab calling intent, so we can take it's data! Muahahaha!
        Intent intent = getActivity().getIntent();
        // check if intent has the data we are expecting.
        if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            String forecastData = intent.getStringExtra(Intent.EXTRA_TEXT);
            TextView weatherView = (TextView) rootView.findViewById(R.id.textview_forecast);
            weatherView.setText(forecastData);
        }
        // Voila!!!
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detail_fragment, menu);
        // Locate the share menu item.
        MenuItem item = menu.findItem(R.id.action_share);
        // Fetch and store the action provider.
        shareActionProvider = (ShareActionProvider)MenuItemCompat.getActionProvider(item);
        if(shareActionProvider != null) {
            shareActionProvider.setShareIntent(createShareIntent());
        }
    }

    private Intent createShareIntent() {
        // Nab the forecast data from the activity.
        TextView forecastView = (TextView) getActivity().findViewById(R.id.textview_forecast);
        String forecastData = forecastView.getText() + " #SunshineApp";
        // Now create the intent for share action.
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.putExtra(Intent.EXTRA_TEXT, forecastData);
        shareIntent.setType("text/plain");
        return shareIntent;
    }
}
