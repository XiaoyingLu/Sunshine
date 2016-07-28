package com.udacity.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.udacity.sunshine.data.WeatherContract;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {

    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE_DAY = 1;
    private final Context mContext;
    private Cursor mCursor;
    private final ForecastAdapterOnClickHandler mClickHandler;
    private final View mEmptyView;
    final private ItemChoiceManager mICM;

    // Flag to determine if we want to use a separate view for "today".
    private boolean mUseTodayLayout = true;

    public class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public final ImageView mIconView;
        public final TextView mDateView;
        public final TextView mDescriptionView;
        public final TextView mHighTempView;
        public final TextView mLowTempView;

        public ForecastAdapterViewHolder(View view) {
            super(view);
            mIconView = (ImageView) view.findViewById(R.id.list_item_icon);
            mDateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            mDescriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            mHighTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            mLowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int dateColumnIndex = mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
            mClickHandler.onClick(mCursor.getLong(dateColumnIndex), this);
            mICM.onClick(this);
        }
    }

    public static interface ForecastAdapterOnClickHandler {
        void onClick(Long date, ForecastAdapterViewHolder vh);
    }

    public ForecastAdapter(Context context, ForecastAdapterOnClickHandler dh, View emptyView, int choiceMode){
        mContext = context;
        mClickHandler = dh;
        mEmptyView = emptyView;
        mICM = new ItemChoiceManager(this);
        mICM.setChoiceMode(choiceMode);
    }


    /*
        This takes advantage of the fact that the viewGroup passed to onCreateViewHolder is the
        RecyclerView that will be used to contain the view, so that it can get the current
        ItemSelectionManager from the view.
        One could implement this pattern without modifying RecyclerView by taking advantage
        of the view tag to store the ItemChoiceManager.
     */
    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if ( parent instanceof RecyclerView ) {
            int layoutId = -1;
            switch (viewType) {
                case VIEW_TYPE_TODAY:
                    layoutId = R.layout.list_item_forecast_today;
                    break;
                case VIEW_TYPE_FUTURE_DAY:
                    layoutId = R.layout.list_item_forecast;
                    break;
            }
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            view.setFocusable(true);
            return new ForecastAdapterViewHolder(view);
        }else {
            throw new RuntimeException("Not bound to RecyclerViewSelection");
        }
    }

    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        // Read weather icon ID from cursor
        int weatherId = mCursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int defaultImage;

        switch (getItemViewType(position)){
            case VIEW_TYPE_TODAY:
                defaultImage = Utility.getArtResourceForWeatherCondition(weatherId);
                break;
            default:
                defaultImage = Utility.getIconResourceForWeatherCondition(weatherId);
                break;
        }

        if (Utility.usingLocalGraphics(mContext)){
            holder.mIconView.setImageResource(defaultImage);
        } else {
            Glide.with(mContext)
                    .load(Utility.getArtUrlForWeatherCondition(mContext, weatherId))
                    .error(defaultImage)
                    .crossFade()
                    .into(holder.mIconView);
        }


        // Read date from cursor
        long dateInMillis = mCursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        // Find TextView and set formatted date on it
        holder.mDateView.setText(Utility.getFriendlyDayString(mContext, dateInMillis));

        // Read weather forecast from cursor
        String description = mCursor.getString(ForecastFragment.COL_WEATHER_DESC);
        // Find TextView and set weather forecast on it
        holder.mDescriptionView.setText(description);
        holder.mDescriptionView.setContentDescription(description);

        // For accessibility, we don't want a content description for the icon field
        // because the information is repeated in the description view and the icon
        // is not individually selectable

        // Read high temperature from cursor
        String high = Utility.formatTemperature(
                mContext, mCursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP));
        holder.mHighTempView.setText(high);
        holder.mHighTempView.setContentDescription(mContext.getString(R.string.a11y_high_temp, high));

        // Read low temperature from cursor
        String low = Utility.formatTemperature(
                mContext, mCursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));
        holder.mLowTempView.setText(low);
        holder.mLowTempView.setContentDescription(mContext.getString(R.string.a11y_low_temp, low));

        mICM.onBindViewHolder(holder, position);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mICM.onRestoreInstanceState(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        mICM.onSaveInstanceState(outState);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    public int getSelectedItemPosition() {
        return mICM.getSelectedItemPosition();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if ( viewHolder instanceof ForecastAdapterViewHolder ) {
            ForecastAdapterViewHolder vfh = (ForecastAdapterViewHolder)viewHolder;
            vfh.onClick(vfh.itemView);
        }
    }
}
