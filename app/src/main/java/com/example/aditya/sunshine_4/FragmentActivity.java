package com.example.aditya.sunshine_4;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by aditya on 9/11/2016.
 */
public class FragmentActivity extends Fragment {
    ArrayAdapter<String> adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            FetchweatherTask fetchweatherTask = new FetchweatherTask();
            fetchweatherTask.execute("1269843");
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragView = inflater.inflate(R.layout.fragment_activity, container, false);
        ListView listView = (ListView) fragView.findViewById(R.id.listView);
        String[] stringArray = {
                "Sunday-Cloudy-28C",
                "Monday-Sultry-32C",
                "Tuesday-Rainy-30C",
                "Wednesday-Sunny-35C",
                "Thursday-Snowy-2C",
                "Friday-Windy-19C",
                "Saturday-Foggy-16C"
        };
        List<String> listArray = new ArrayList<>(Arrays.asList(stringArray));
        adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item, R.id.Text_list_item, listArray);
        listView.setAdapter(adapter);
        return fragView;
    }

    public class FetchweatherTask extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
// so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

// Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            //String cityID = "1269843";//id
            String Key = "b18d7fdce09b07b907b542f9a85909d4";//APPID
            String unit = "metric";//units
            String count = "32";
            int cntt = Integer.parseInt(count);//cnt

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String forecast_url = "http://api.openweathermap.org/data/2.5/forecast?";
                final String idParam = "id";
                final String AppidParam = "APPID";
                final String unitParam = "units";
                final String countParam = "cnt";
                Uri builtURi = Uri.parse(forecast_url).buildUpon()
                        .appendQueryParameter(idParam, params[0])
                        .appendQueryParameter(AppidParam, Key)
                        .appendQueryParameter(unitParam, unit)
                        .appendQueryParameter(countParam, count)
                        .build();
                Log.v("URL", "URL is: " + builtURi.toString());
                URL url = new URL(builtURi.toString());
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    forecastJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    forecastJsonStr = null;
                }
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                forecastJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            Log.v("Tag", "Forecast JSON String" + forecastJsonStr);

            WeatherDataParser weatherDataParser = new WeatherDataParser();
            String[] weatherDataArray = new String[cntt];
            try {
                weatherDataArray = weatherDataParser.weatherData(forecastJsonStr,cntt);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.v("Data",weatherDataArray[0]);

            return weatherDataArray;

        }//Occurs in background, takes String(City code as i/p), output's a string array

        @Override
        protected void onPostExecute(String[] strings) {
            if (strings!=null){
                adapter.clear();
                for (String dayFOrecastStr : strings)
                    adapter.add(dayFOrecastStr);
            }
        }
    }

    public class WeatherDataParser {
        public String[] weatherData(String JSONstring, int dayNum) throws JSONException {
            //What params do I Need: a) Date/Day b)Description of weather c)Temperature
            int i;
            String[] weatherData = new String[dayNum];
            JSONObject root = new JSONObject(JSONstring);
            JSONArray listArray = root.getJSONArray("list");
            for (i=0;i<dayNum;i++){
                JSONObject ArrayNumber = listArray.getJSONObject(i);
                JSONObject mainNode = ArrayNumber.getJSONObject("main");
                double temperature = mainNode.getDouble("temp");

                JSONArray weatherArray = ArrayNumber.getJSONArray("weather");
                JSONObject weatherArrayNumber = weatherArray.getJSONObject(0);
                String decription = weatherArrayNumber.getString("description") + " ";
                Log.v("Decription","Weather is: " + decription);

                long date = ArrayNumber.getLong("dt");
                Log.v("Date","The LONG date is:" + date);
                String dateStr = getReadableDateString(date);
                Log.v("DateStr","The String date is:" + dateStr);
                weatherData[i] = dateStr + "- " + decription + "- " + temperature + "°C";
                Log.v("WeatherData","String Number:" + i +" "+ weatherData[i]);
            }
                return weatherData;
        }

        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
           // SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            Date date = new Date(time*1000L); // *1000 is to convert seconds to milliseconds
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd/MM/yyyy"); // the format of your date
            sdf.setTimeZone(TimeZone.getDefault()); // give a timezone reference for formating (see comment at the bottom
            String formattedDate = sdf.format(date);
            SimpleDateFormat sdf2 = new SimpleDateFormat(" k");
            sdf2.setTimeZone(TimeZone.getDefault());
            String entire = formattedDate +","+ sdf2.format(date) + " Hrs";
            return entire;

        }



    }




}
