package com.example.android.sunshine;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.utils.NetworkUtils;
import com.example.android.sunshine.utils.OpenWeatherJsonUtils;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements
    ForecastAdapter.ForecastAdapterOnClickHandler, LoaderManager.LoaderCallbacks<String[]> {

  private static final int FORECAST_LOADER_ID = 0;
  private static final String TAG = "MainActivity";

  private RecyclerView mRecyclerView;
  private ForecastAdapter mForecastAdapter;
  private TextView mErrorMessageDisplay;
  private ProgressBar mLoadingIndicator;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_forecast);
    mForecastAdapter = new ForecastAdapter(this);

    mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);
    mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
    mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_forecast);

    LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    mRecyclerView.setLayoutManager(layoutManager);
    mRecyclerView.setHasFixedSize(true);
    mRecyclerView.setAdapter(mForecastAdapter);

    /* Once all of our views are setup, we can load the weather data. */
    LoaderManager.LoaderCallbacks<String[]> callback = MainActivity.this;
    getSupportLoaderManager().initLoader(FORECAST_LOADER_ID, null, callback);
  }

  @Override
  public void onClick(String weatherForDay) {
    Context context = this;
    Class destinationClass = DetailActivity.class;
    Intent intentToStartDetailActivity = new Intent(context, destinationClass);
    intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, weatherForDay);
    startActivity(intentToStartDetailActivity);
  }

  @Override
  public Loader<String[]> onCreateLoader(int id, Bundle args) {
    return new AsyncTaskLoader<String[]>(this) {

      String[] mWeatherData;

      @Override
      protected void onStartLoading() {
        if (mWeatherData != null) {
          deliverResult(mWeatherData);
        } else {
          mLoadingIndicator.setVisibility(View.VISIBLE);
          forceLoad();
        }
      }

      @Override
      public String[] loadInBackground() {
        String locationQuery = SunshinePreferences.getPreferredWeatherLocation(MainActivity.this);
        URL weatherRequestUrl = NetworkUtils.buildUrl(locationQuery);

        try {
          String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl);
          return OpenWeatherJsonUtils.getSimpleWeatherStringsFromJson
              (MainActivity.this, jsonWeatherResponse);
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
      }

      @Override
      public void deliverResult(String[] data) {
        mWeatherData = data;
        super.deliverResult(data);
      }
    };
  }

  @Override
  public void onLoadFinished(Loader<String[]> loader, String[] data) {
    mLoadingIndicator.setVisibility(View.INVISIBLE);
    mForecastAdapter.setWeatherData(data);
    if (data != null) {
      showWeatherDataView();
    } else {
      showErrorMessage();
    }
  }

  @Override
  public void onLoaderReset(Loader<String[]> loader) {
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.forecast, menu);
    /* Return true so that the menu is displayed in the Toolbar */
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_refresh:
        invalidateData();
        getSupportLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
        return true;
      case R.id.action_geo_test:
        openLocationInMap();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void invalidateData() {
    mForecastAdapter.setWeatherData(null);
  }

  private void openLocationInMap() {

    String addressString = "geo:55.820163, 37.514126";
    Uri geoLocation = Uri.parse(addressString);

    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.setData(geoLocation);
    if (intent.resolveActivity(getPackageManager()) != null) {
      startActivity(intent);
    } else {
      Log.d(TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
    }
  }

  private void showWeatherDataView() {
    mErrorMessageDisplay.setVisibility(View.INVISIBLE);
    mRecyclerView.setVisibility(View.VISIBLE);
  }

  private void showErrorMessage() {
    mRecyclerView.setVisibility(View.INVISIBLE);
    mErrorMessageDisplay.setVisibility(View.VISIBLE);
  }
}