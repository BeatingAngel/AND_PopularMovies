
/*
 * Author: Philipp Hermüller
 */

package com.goldencrow.android.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.goldencrow.android.popularmovies.data.Contracts;
import com.goldencrow.android.popularmovies.entities.Movie;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieOnClickHandler {

    // =====     KEYS      =====
    private static final String STATE_SORT_ORDER_KEY = "sortOrder";
    private static final String RESTORE_LIST_POSITION_KEY = "listPos";

    private static final String MOVIE_LIST_JSON_KEY = "results";

    // =====    VALUES     =====
    private static final int GRID_SPAN_SIZE = 2;

    // ===== API URI VALUES =====
    public static final String MOVIE_POSTER_BASE_PATH = "http://image.tmdb.org/t/p/w185//";

    public static final String API_SCHEME = "http";
    public static final String API_AUTHORITY = "api.themoviedb.org";
    public static final String API_VERSION_PATH = "3";
    public static final String API_MOVIES_PATH = "movie";
    public static final String POPULAR_API_PATH = "popular";
    public static final String TOP_RATED_API_PATH = "top_rated";
    public static final String API_KEY_NAME = "api_key";

    // =====     WIDGETS      =====
    @BindView(R.id.movie_list_rv)
    RecyclerView mMoviesListRv;
    @BindView(R.id.loading_indicator_pb)
    ProgressBar mLoadingIndicatorPb;
    @BindView(R.id.error_message_tf)
    TextView mErrorMessageTf;
    @BindView(R.id.reload_btn)
    Button mReloadBtn;

    // =====     OTHER      =====
    private MovieAdapter mAdapter;
    private RequestQueue mQueue;
    private String mLoadingPath = null;

    private int mRvPosition;

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
                queueJsonRequest(mLoadingPath);
            }
        });

        // Instantiate the RequestQueue.
        mQueue = Volley.newRequestQueue(this);

        // if the device is rotated, then the sortOrder should stay the same!
        if (savedInstanceState != null) {
            mLoadingPath = savedInstanceState.getString(STATE_SORT_ORDER_KEY, POPULAR_API_PATH);
        } else {
            mLoadingPath = POPULAR_API_PATH;
        }
        showLoading();
        queueJsonRequest(mLoadingPath);
    }

    /**
     * Save the current sort-order to reapply it when the device was rotated.
     * Also saves the current position from the List to reapply it later.
     *
     * @param outState              the bundle to save the data into.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(STATE_SORT_ORDER_KEY, mLoadingPath);
        outState.putInt(RESTORE_LIST_POSITION_KEY,
                ((LinearLayoutManager)mMoviesListRv.getLayoutManager())
                        .findFirstVisibleItemPosition());
    }

    /**
     * retrieves the last List-Position for later usage.
     *
     * @param savedInstanceState    the saved values.
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mRvPosition = savedInstanceState.getInt(RESTORE_LIST_POSITION_KEY);
    }

    /**
     * Use our custom Menu.
     *
     * @param menu  the menu.
     * @return      TRUE if it was successful.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.main_page, menu);

        return true;
    }

    /**
     * Handles a selected MenuOption.
     * For example: Change the title of the sort order and load data.
     *
     * @param item  the clicked MenuItem.
     * @return      TRUE if it was successful.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_sort_order) {
            toggleSortOrder(item);
            showLoading();
            queueJsonRequest(mLoadingPath);
            return true;
        } else if (id == R.id.action_show_favorite) {
            showLoading();
            displayFavorites();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * retrieves the Favorite Movies from the SQLite Database and displays them.
     * If nothing could be retrieved, then an error message will show.
     */
    private void displayFavorites() {
        Cursor cursor = getContentResolver().query(Contracts.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        if (cursor != null) {
            int movieIdIndex = cursor.getColumnIndex(Contracts.MovieEntry._ID);
            int movieAvgVoteIndex = cursor.getColumnIndex(Contracts.MovieEntry.COLUMN_VOTE_AVERAGE);
            int movieTitleIndex = cursor.getColumnIndex(Contracts.MovieEntry.COLUMN_TITLE);
            int moviePosterIndex = cursor.getColumnIndex(Contracts.MovieEntry.COLUMN_POSTER_PATH);
            int movieBackdropIndex = cursor.getColumnIndex(Contracts.MovieEntry.COLUMN_BACKDROP_PATH);
            int movieOverviewIndex = cursor.getColumnIndex(Contracts.MovieEntry.COLUMN_OVERVIEW);
            int movieReleaseIndex = cursor.getColumnIndex(Contracts.MovieEntry.COLUMN_RELEASE_DATE);

            // Because the RecyclerView-Adapter is used with a Array, I have to convert the
            // Cursor to an array to pass the data to the Adapter.
            cursor.moveToFirst();
            List<Movie> movies = new LinkedList<>();
            while (!cursor.isAfterLast()) {
                Movie movie = new Movie(
                        cursor.getInt(movieIdIndex),
                        cursor.getFloat(movieAvgVoteIndex),
                        cursor.getString(movieTitleIndex),
                        cursor.getString(moviePosterIndex),
                        cursor.getString(movieBackdropIndex),
                        cursor.getString(movieOverviewIndex),
                        cursor.getString(movieReleaseIndex)
                );
                movies.add(movie);
                cursor.moveToNext();
            }
            cursor.close();

            Movie[] arrayMovies = new Movie[movies.size()];
            arrayMovies = movies.toArray(arrayMovies);

            mAdapter.setMovieData(arrayMovies);

            if (arrayMovies.length == 0) {
                Toast.makeText(this, R.string.no_favorites, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this,
                    R.string.could_not_retrieve_favorites, Toast.LENGTH_SHORT).show();
        }

        showData();
    }

    /**
     * Changes the activity to the detail activity of the clicked movie.
     *
     * @param movie     the selected movie.
     */
    @Override
    public void onClick(Movie movie) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.INTENT_EXTRA_MOVIE_KEY, movie);
        startActivity(intent);
    }

    /**
     * Displaying a error message in the center of the screen
     * with a error message text and button to retry.
     *
     * @param message   The message to display (String-Resource-ID)
     */
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

    /**
     * Hide everything except the LoadingIndicator
     * to show that the program is loading.
     */
    private void showLoading() {
        mLoadingIndicatorPb.setVisibility(View.VISIBLE);
        mMoviesListRv.setVisibility(View.INVISIBLE);
        if (mErrorMessageTf.getVisibility() == View.VISIBLE) {
            mErrorMessageTf.setVisibility(View.INVISIBLE);
            mReloadBtn.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Hide everything except the RecyclerView which display the movies.
     */
    private void showData() {
        mLoadingIndicatorPb.setVisibility(View.INVISIBLE);
        mMoviesListRv.setVisibility(View.VISIBLE);
        if (mErrorMessageTf.getVisibility() == View.VISIBLE) {
            mErrorMessageTf.setVisibility(View.INVISIBLE);
            mReloadBtn.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Change to title of the MenuItem and remember which SortOrder
     * to sort by next time.
     *
     * @param menuItem  the selected MenuItem.
     */
    private void toggleSortOrder(MenuItem menuItem) {
        if (mLoadingPath.equals(POPULAR_API_PATH)) {
            mLoadingPath = TOP_RATED_API_PATH;
            menuItem.setTitle(R.string.action_sort_popularity);
        } else {
            mLoadingPath = POPULAR_API_PATH;
            menuItem.setTitle(R.string.action_sort_ratings);
        }
    }

    /**
     * Performs the API-request asynchronously and displays to newly retrieved data
     * in the Grid.
     *
     * Libraries used: Volley and Gson.
     * Volley code is shorter than a code of an AsyncTask with a request in it.
     * Volley can also provide the API-result as a JSON-Object and handle errors.
     * Gson can parse a Json into an Object. It lessens the amount of code.
     *
     * @param apiPath   the api path to the selected sort-order.
     */
    private void queueJsonRequest(String apiPath) {
        String apiUri = getApiUri(apiPath);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                apiUri,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonResponse) {
                        Gson gson = new Gson();
                        try {
                            String jsonArrayMovies = jsonResponse.get(MOVIE_LIST_JSON_KEY).toString();

                            Movie[] movies = gson.fromJson(jsonArrayMovies, Movie[].class);

                            mAdapter.setMovieData(movies);

                            showData();

                            // after the data is displayed -> scroll to the correct position
                            if (mRvPosition != 0) {
                                ((GridLayoutManager)mMoviesListRv.getLayoutManager())
                                        .scrollToPositionWithOffset(mRvPosition, 0);
                            }
                        } catch (JSONException e) {
                            showErrorMessage(R.string.error_message_parsing_json);
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

    /**
     * set up the RecyclerView with empty data.
     */
    private void initializeRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, GRID_SPAN_SIZE);

        mAdapter = new MovieAdapter(this, this);
        mMoviesListRv.setAdapter(mAdapter);

        mMoviesListRv.setLayoutManager(gridLayoutManager);
        //mMoviesListRv.setHasFixedSize(true);
    }

    /**
     * builds the Uri to themoviedb.org-API.
     *
     * @param apiPath   the api path to the selected sort-order.
     * @return          the built uri as string.
     */
    private String getApiUri(String apiPath) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(API_SCHEME)
                .authority(API_AUTHORITY)
                .appendPath(API_VERSION_PATH)
                .appendPath(API_MOVIES_PATH)
                .appendPath(apiPath)
                .appendQueryParameter(API_KEY_NAME, getString(R.string.TheMovieDb_API_KEY));
        return builder.build().toString();
    }
}
