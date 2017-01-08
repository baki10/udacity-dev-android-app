package com.example.android.sunshine;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.utils.NetworkUtils;
import com.example.android.sunshine.utils.OpenWeatherJsonUtils;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements ForecastAdapter.ForecastAdapterOnClickHandler {

  private RecyclerView mRecyclerView;
  private ForecastAdapter mForecastAdapter;
  private TextView mErrorMessageDisplay;
  private ProgressBar mLoadingIndicator;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_forecast);

    mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_forecast);
    mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

    LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    mRecyclerView.setLayoutManager(layoutManager);

    mRecyclerView.setHasFixedSize(true);

    mForecastAdapter = new ForecastAdapter(this);
    mRecyclerView.setAdapter(mForecastAdapter);

    mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

    /* Once all of our views are setup, we can load the weather data. */
    loadWeatherData();
  }

  private void loadWeatherData() {
    showWeatherDataView();

    String location = SunshinePreferences.getPreferredWeatherLocation(this);
    new FetchWeatherTask().execute(location);
  }

  @Override
  public void onClick(String weatherForDay) {
    Context context = this;
    Class destinationClass = DetailActivity.class;
    Intent intentToStartDetailActivity = new Intent(context, destinationClass);
    intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, weatherForDay);
    startActivity(intentToStartDetailActivity);
  }

  public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    protected String[] doInBackground(String... params) {

      /* If there's no zip code, there's nothing to look up. */
      if (params.length == 0) {
        return null;
      }

      String location = params[0];
      URL weatherRequestUrl = NetworkUtils.buildUrl(location);

      try {
        String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl);
        return OpenWeatherJsonUtils.getSimpleWeatherStringsFromJson(MainActivity.this, jsonWeatherResponse);

      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    }

    @Override
    protected void onPostExecute(String[] weatherData) {
      mLoadingIndicator.setVisibility(View.INVISIBLE);
      if (weatherData != null) {
        showWeatherDataView();
        mForecastAdapter.setWeatherData(weatherData);
      } else {
        showErrorMessage();
      }
    }
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
    switch (item.getItemId()){
      case R.id.action_refresh:
        mForecastAdapter.setWeatherData(null);
        loadWeatherData();
        return true;
      default:
        return super.onOptionsItemSelected(item);
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