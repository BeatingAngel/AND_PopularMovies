
/*
 * Author: Philipp Hermüller
 */

package com.goldencrow.android.popularmovies;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.goldencrow.android.popularmovies.entities.Movie;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String API_SCHEME = "http";
    private static final String API_AUTHORITY = "api.themoviedb.org";
    private static final String API_VERSION_PATH = "3";
    private static final String API_MOVIES_PATH = "movie";
    private static final String POPULAR_API_PATH = "popular";
    private static final String TOP_RATED_API_PATH = "top_rated";
    private static final String API_KEY_NAME = "api_key";
    private static final String API_KEY
            = "";  // insert API-KEY from themoviedb.org HERE

    private Movie[] mMovies;

    @BindView(R.id.json_tf)
    TextView mJsonTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        //This is for later usage in the DetailActivity:
        //Locale current = getResources().getConfiguration().locale;

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String apiUri = getApiUri(POPULAR_API_PATH);

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                apiUri,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String jsonResponse) {
                        Gson gson = new Gson();
                        try {
                            JSONObject result = new JSONObject(jsonResponse);
                            String jsonArrayMovies = result.get("results").toString();

                            mMovies = gson.fromJson(jsonArrayMovies, Movie[].class);
                        } catch (JSONException e) {
                            Toast.makeText(
                                    MainActivity.this,
                                    "Movie data could not be read!",
                                    Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(
                                MainActivity.this,
                                "There was a network Error!",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private String getApiUri(String apiPath) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(API_SCHEME)
                .authority(API_AUTHORITY)
                .appendPath(API_VERSION_PATH)
                .appendPath(API_MOVIES_PATH)
                .appendPath(apiPath)
                .appendQueryParameter(API_KEY_NAME, API_KEY);
        return builder.build().toString();
    }
}
