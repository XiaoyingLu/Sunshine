package com.udacity.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.udacity.sunshine.data.WeatherContract;

/**
 * Created by Administrator on 2016/6/2.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int DETAIL_LOADER = 0;
    private static final String[] DETAIL_COLUMNS = {
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
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,

            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_WEAHTER_HUMIDITY = 5;
    static final int COL_WEAHTER_PRESSURE = 6;
    static final int COL_WEAHTER_WIND_SPEED = 7;
    static final int COL_WEAHTER_DEGREES = 8;
    static final int COL_WEAHTER_CONDITION_ID = 9;


    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private String mForecast;
    private ShareActionProvider mShareActionProvider;
    private ImageView mIconView;
    private TextView mDateView;
    private TextView mFriendlyDateView;
    private TextView mDescriptionView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;
    private Uri mUri;
    private TextView mHumidityLabelView;
    private TextView mWindLabelView;
    private TextView mPressureLabelView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.detailfragment, menu);
//
//        // Retrieve the share menu item
//        MenuItem menuItem = menu.findItem(R.id.action_share);
//
//        // Get the provider and hold onto it to set/change the share intent.
//        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
//
//        // Attach an intent to this ShareActionProvider. You can update this at any time,
//        // like when the user selects a new piece of data they might like to share.
//        if(mForecast != null){
//            mShareActionProvider.setShareIntent(createShareForecastIntent());
//        }else{
//            Log.d(LOG_TAG, "Share Action Provider is null?");
//        }

        if ( getActivity() instanceof DetailActivity ){
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detailfragment, menu);
            finishCreatingMenu(menu);
        }
    }

    private void finishCreatingMenu(Menu menu) {
        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(createShareForecastIntent());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if(arguments != null){
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail_start, container, false);
        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
//        mFriendlyDateView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        mDescriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        mHighTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        mLowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        mHumidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        mHumidityLabelView = (TextView) rootView.findViewById(R.id.detail_humidity_label_textview);
        mWindView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        mWindLabelView = (TextView) rootView.findViewById(R.id.detail_wind_label_textview);
        mPressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);
        mPressureLabelView = (TextView) rootView.findViewById(R.id.detail_pressure_label_textview);

//        Intent intent = getActivity().getIntent();
//        if (intent != null) {
//            mForecastStr = intent.getDataString();
//        }
//        mForecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
//        ((TextView) rootView.findViewById(R.id.detail_text)).setText(mForecast);
//
//
        return rootView;
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);

        return shareIntent;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        Log.v(LOG_TAG, "In onCreateLoader");
//        Intent intent = getActivity().getIntent();
//        if (intent == null || intent.getData() == null){
//            return null;
//        }
        if(null != mUri){
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        ViewParent vp = getView().getParent();
        if ( vp instanceof CardView) {
            ((View)vp).setVisibility(View.INVISIBLE);
        }
        return null;

//        return new CursorLoader(getActivity(),
//                intent.getData(),
//                DETAIL_COLUMNS,
//                null,
//                null,
//                null
//        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");

        if( data != null && data.moveToFirst()) {
            ViewParent vp = getView().getParent();
            if ( vp instanceof CardView ) {
                ((View)vp).setVisibility(View.VISIBLE);
            }

            // Read weather condition ID from cursor
            int weatherId = data.getInt(COL_WEAHTER_CONDITION_ID);
//            mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
            Glide.with(this)
                    .load(Utility.getArtUrlForWeatherCondition(getActivity(), weatherId))
                    .error(Utility.getArtResourceForWeatherCondition(weatherId))
                    .crossFade()
                    .into(mIconView);

            long date = data.getLong(COL_WEATHER_DATE);
//            String friendlyDateText = Utility.getDayName(getActivity(), date);
            String dateText = Utility.getFormattedMonthDay(getActivity(), date);
//            mFriendlyDateView.setText(friendlyDateText);
            mDateView.setText(dateText);

            String description = data.getString(COL_WEATHER_DESC);
            mDescriptionView.setText(description);

            mIconView.setContentDescription(description);

            boolean isMetric = Utility.isMetric(getActivity());

            double high = data.getDouble(COL_WEATHER_MAX_TEMP);
            String highString = Utility.formatTemperature(getActivity(), high);
            mHighTempView.setText(highString);

            double low = data.getDouble(COL_WEATHER_MAX_TEMP);
            String lowString = Utility.formatTemperature(getActivity(), high);
            mLowTempView.setText(lowString);

            float humidity = data.getFloat(COL_WEAHTER_HUMIDITY);
            mHumidityView.setText(getActivity().getString(R.string.format_humidity, humidity));
            mHumidityLabelView.setContentDescription(mHumidityView.getContentDescription());

            float windSpeedStr = data.getFloat(COL_WEAHTER_WIND_SPEED);
            float windDirStr = data.getFloat(COL_WEAHTER_DEGREES);
            mWindView.setText(Utility.getFormattedWind(getActivity(), windSpeedStr, windDirStr));
            mWindLabelView.setContentDescription(mWindView.getContentDescription());

            float pressure = data.getFloat(COL_WEAHTER_PRESSURE);
            mPressureView.setText(getActivity().getString(R.string.format_pressure, pressure));
            mPressureLabelView.setContentDescription(mPressureView.getContentDescription());

            mForecast = String.format("%s - %s - %s/%s", dateText, description, high, low);

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null){
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        Toolbar toolbarView = (Toolbar) getView().findViewById(R.id.toolbar);

        // We need to start the enter transition after the data has loaded
        if (activity instanceof DetailActivity) {
            activity.supportStartPostponedEnterTransition();

            if ( null != toolbarView ) {
                activity.setSupportActionBar(toolbarView);

                activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } else {
            if ( null != toolbarView ) {
                Menu menu = toolbarView.getMenu();
                if ( null != menu ) menu.clear();
                toolbarView.inflateMenu(R.menu.detailfragment);
                finishCreatingMenu(toolbarView.getMenu());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    public void onLocationChanged(String newLocation) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }
}
