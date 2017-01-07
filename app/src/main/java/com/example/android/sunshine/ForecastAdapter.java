package com.example.android.sunshine;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts to a
 * {@link android.support.v7.widget.RecyclerView}
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {

  private String[] mWeatherData;

  public interface ForecastAdapterOnClickHandler {
    void onClick(String text);
  }

  private final ForecastAdapterOnClickHandler mClickHandler;

  public ForecastAdapter(ForecastAdapterOnClickHandler handler) {
    mClickHandler = handler;
  }

  /**
   * Cache of the children views for a forecast list item.
   */
  public class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public final TextView mWeatherTextView;

    public ForecastAdapterViewHolder(View view) {
      super(view);
      mWeatherTextView = (TextView) view.findViewById(R.id.tv_weather_data);
      view.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
      mClickHandler.onClick(String.valueOf(mWeatherTextView.getText()));
    }

  }

  /**
   * This gets called when each new ViewHolder is created. This happens when the RecyclerView
   * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
   *
   * @param viewGroup The ViewGroup that these ViewHolders are contained within.
   * @param viewType  If your RecyclerView has more than one type of item (which ours doesn't) you
   *                  can use this viewType integer to provide a different layout. See
   *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
   *                  for more details.
   * @return A new ForecastAdapterViewHolder that holds the View for each list item
   */
  @Override
  public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
    Context context = viewGroup.getContext();
    LayoutInflater inflater = LayoutInflater.from(context);

    View view = inflater.inflate(R.layout.forecast_list_item, viewGroup, false);
    return new ForecastAdapterViewHolder(view);
  }

  /**
   * OnBindViewHolder is called by the RecyclerView to display the data at the specified
   * position. In this method, we update the contents of the ViewHolder to display the weather
   * details for this particular position, using the "position" argument that is conveniently
   * passed into us.
   *
   * @param forecastAdapterViewHolder The ViewHolder which should be updated to represent the
   *                                  contents of the item at the given position in the data set.
   * @param position                  The position of the item within the adapter's data set.
   */
  @Override
  public void onBindViewHolder(ForecastAdapterViewHolder forecastAdapterViewHolder, int position) {
    String weatherForThisDay = mWeatherData[position];
    forecastAdapterViewHolder.mWeatherTextView.setText(weatherForThisDay);
  }

  @Override
  public int getItemCount() {
    return mWeatherData != null ? mWeatherData.length : 0;
  }

  public void setWeatherData(String[] weatherData) {
    mWeatherData = weatherData;
    notifyDataSetChanged();
  }
}