
/*
 * Author: Philipp Hermüller
 */

package com.goldencrow.android.popularmovies;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

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

    private static final String MOVIE_LIST_JSON_KEY = "results";

    @BindView(R.id.movie_list_rv)
    RecyclerView mMoviesListRv;
    @BindView(R.id.loading_indicator_pb)
    ProgressBar mLoadingIndicatorPb;
    @BindView(R.id.error_message_tf)
    TextView mErrorMessageTf;
    @BindView(R.id.reload_btn)
    Button mReloadBtn;

    private MovieAdapter mAdapter;
    private RequestQueue mQueue;
    private String mLoadingPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        initializeRecyclerView();
        mReloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoading();
                queueStringRequest(mLoadingPath);
            }
        });

        //This is for later usage in the DetailActivity:
        //Locale current = getResources().getConfiguration().locale;

        // Instantiate the RequestQueue.
        mQueue = Volley.newRequestQueue(this);

        showLoading();

        // show order by popularity as default.
        mLoadingPath = POPULAR_API_PATH;
        queueStringRequest(mLoadingPath);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.main_page, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_sort_order) {
            toggleSortOrder(item);
            showLoading();
            queueStringRequest(mLoadingPath);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showErrorMessage(int message) {
        if (mMoviesListRv.getVisibility() == View.VISIBLE) {
            mMoviesListRv.setVisibility(View.INVISIBLE);
        } else {
            mLoadingIndicatorPb.setVisibility(View.INVISIBLE);
        }

        mReloadBtn.setVisibility(View.VISIBLE);
        mErrorMessageTf.setVisibility(View.VISIBLE);
        mErrorMessageTf.setText(message);
    }

    private void showLoading() {
        mLoadingIndicatorPb.setVisibility(View.VISIBLE);
        mMoviesListRv.setVisibility(View.INVISIBLE);
        if (mErrorMessageTf.getVisibility() == View.VISIBLE) {
            mErrorMessageTf.setVisibility(View.INVISIBLE);
            mReloadBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void showData() {
        mLoadingIndicatorPb.setVisibility(View.INVISIBLE);
        mMoviesListRv.setVisibility(View.VISIBLE);
        if (mErrorMessageTf.getVisibility() == View.VISIBLE) {
            mErrorMessageTf.setVisibility(View.INVISIBLE);
            mReloadBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void toggleSortOrder(MenuItem menuItem) {
        if (mLoadingPath.equals(POPULAR_API_PATH)) {
            mLoadingPath = TOP_RATED_API_PATH;
            menuItem.setTitle(R.string.action_sort_popularity);
        } else {
            mLoadingPath = POPULAR_API_PATH;
            menuItem.setTitle(R.string.action_sort_ratings);
        }
    }

    private void queueStringRequest(String apiPath) {
        String apiUri = getApiUri(apiPath);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                apiUri,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String jsonResponse) {
                        Gson gson = new Gson();
                        try {
                            JSONObject result = new JSONObject(jsonResponse);
                            String jsonArrayMovies = result.get(MOVIE_LIST_JSON_KEY).toString();

                            Movie[] movies = gson.fromJson(jsonArrayMovies, Movie[].class);

                            mAdapter.setMovieData(movies);

                            showData();
                        } catch (JSONException e) {
                            showErrorMessage(R.string.error_message_parsing_json);
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showErrorMessage(R.string.error_message_connecting_to_api);
                    }
                });

        // Add the request to the RequestQueue.
        mQueue.add(request);
    }

    private void initializeRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);

        mAdapter = new MovieAdapter(this);
        mMoviesListRv.setAdapter(mAdapter);

        mMoviesListRv.setLayoutManager(layoutManager);
        mMoviesListRv.setHasFixedSize(true);
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
