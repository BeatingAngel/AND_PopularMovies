
/*
 * Author: Philipp Hermüller
 */

package com.goldencrow.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.goldencrow.android.popularmovies.entities.Movie;
import com.goldencrow.android.popularmovies.entities.Review;
import com.goldencrow.android.popularmovies.entities.Trailer;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    public static final String INTENT_EXTRA_MOVIE_KEY = "movie";

    private static final String TRAILER_JSON_KEY = "results";
    private static final String TRAILER_API_KEY = "videos";
    private static final String REVIEWS_API_KEY = "reviews";

    @BindView(R.id.movie_poster_iv)
    ImageView mMoviePosterIv;
    @BindView(R.id.release_date_tv)
    TextView mReleaseDateTv;
    @BindView(R.id.average_vote_tv)
    TextView mAverageVoteTv;
    @BindView(R.id.plot_synopsis_tv)
    TextView mPlotSynopsisTv;
    @BindView(R.id.video_preview_tv)
    TextView mTrailerTv;
    @BindView(R.id.trailer_container_ll)
    LinearLayout mTrailerContainerLl;
    @BindView(R.id.reviews_tv)
    TextView mReviewTv;
    @BindView(R.id.reviews_container_ll)
    LinearLayout mReviewsContainerLl;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.app_bar_layout)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.collapsing_toolbar_layout)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.toolbar_iv)
    ImageView mToolbarIv;

    private Menu menu;

    private RequestQueue mQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        // Instantiate the RequestQueue.
        mQueue = Volley.newRequestQueue(this);

        materializeAppBar();

        Intent intent = getIntent();
        if (intent.hasExtra(INTENT_EXTRA_MOVIE_KEY)) {
            Movie movie = intent.getParcelableExtra(INTENT_EXTRA_MOVIE_KEY);

            mCollapsingToolbarLayout.setTitle(movie.getTitle());

            Picasso.with(this)
                    .load(MainActivity.MOVIE_POSTER_BASE_PATH + movie.getPosterPath())
                    .error(R.drawable.ic_launcher_background)
                    .into(mMoviePosterIv);
            Picasso.with(this)
                    .load(MainActivity.MOVIE_POSTER_BASE_PATH + movie.getBackdrop_path())
                    .error(R.drawable.ic_launcher_background)
                    .into(mToolbarIv);

            String releaseDateString = movie.getReleaseDateAsString();
            mReleaseDateTv.setText(releaseDateString);

            String averageVoteString = getString(R.string.vote_average, movie.getVoteAverage());
            mAverageVoteTv.setText(averageVoteString);

            mPlotSynopsisTv.setText(movie.getOverview());

            queueJsonRequest(movie.getId(), TRAILER_API_KEY);
            queueJsonRequest(movie.getId(), REVIEWS_API_KEY);
        } else {
            mCollapsingToolbarLayout.setTitle(getString(R.string.error_movie_not_found));
            mMoviePosterIv.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Request additional information from a movie and handle/display the result
     * of the API-request (async).
     *
     * @param movieId   the movieId from which more information is needed.
     * @param category  the type of information which is needed.
     */
    private void queueJsonRequest(int movieId, final String category) {
        String apiUri = getApiUri(movieId, category);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                apiUri,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonResponse) {
                        Gson gson = new Gson();
                        try {
                            String jsonArrayMovies = jsonResponse.get(TRAILER_JSON_KEY).toString();
                            if (category.equals(TRAILER_API_KEY)) {
                                Trailer[] trailers = gson.fromJson(jsonArrayMovies, Trailer[].class);
                                if (trailers.length > 0) {
                                    displayTrailers(trailers);
                                } else {
                                    mTrailerTv.setVisibility(View.GONE);
                                }
                            } else if (category.equals(REVIEWS_API_KEY)) {
                                Review[] reviews = gson.fromJson(jsonArrayMovies, Review[].class);
                                if (reviews.length > 0) {
                                    displayReviews(reviews);
                                } else {
                                    mReviewTv.setVisibility(View.GONE);
                                }
                            }
                        } catch (JSONException e) {
                            // Error while parsing Json.
                            Toast.makeText(
                                    DetailActivity.this,
                                    "Error while parsing!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Video trailers could not be loaded
                        Toast.makeText(
                                DetailActivity.this,
                                "Could not retrieve data!",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        // Add the request to the RequestQueue.
        mQueue.add(request);
    }

    /**
     * Display all trailers in a Linear Layout on the screen.
     *
     * @param trailers  the list of all trailers.
     */
    private void displayTrailers(Trailer[] trailers) {
        LinearLayout trailerList = new LinearLayout(DetailActivity.this);
        trailerList.setOrientation(LinearLayout.VERTICAL);
        trailerList.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));

        for (Trailer trailer : trailers) {
            ImageView trailerPreviewIv = createTrailer(trailer);

            TextView trailerNameTv = new TextView(DetailActivity.this);
            trailerNameTv.setLayoutParams(new LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
            trailerNameTv.setText(trailer.getName());

            trailerList.addView(trailerNameTv);
            trailerList.addView(trailerPreviewIv);
        }

        mTrailerContainerLl.addView(trailerList);
    }

    /**
     * Creates a ImageView Widget which plays the trailer in YouTube with a click.
     *
     * @param trailer   the trailer-object from which a ImageView will be generated.
     * @return          the imageView widget.
     */
    private ImageView createTrailer(final Trailer trailer) {
        ImageView trailerPreviewIv =
                new ImageView(DetailActivity.this);
        trailerPreviewIv.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        trailerPreviewIv.setAdjustViewBounds(true);
        trailerPreviewIv.setPadding(0,0,0, 25);

        trailerPreviewIv.setImageResource(R.drawable.ic_launcher_foreground);

        if (trailer.getSite().toLowerCase().equals("youtube")) {
            Picasso.with(DetailActivity.this)
                    .load("https://img.youtube.com/vi/"
                            + trailer.getKey() + "/hqdefault.jpg")
                    .into(trailerPreviewIv);

            trailerPreviewIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri trailerUri = Uri.parse("http://www.youtube.com/embed/" + trailer.getKey());

                    Intent youTubeIntent = new Intent(Intent.ACTION_VIEW, trailerUri);

                    if (youTubeIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(youTubeIntent);
                    }
                }
            });
        }

        return trailerPreviewIv;
    }

    /**
     * Displays all movie reviews on the screen.
     *
     * Not a nice way to display reviews, cards would be better.
     * But this is simpler.
     *
     * @param reviews   the reviews to display.
     */
    private void displayReviews(Review[] reviews) {
        LinearLayout trailerList = new LinearLayout(DetailActivity.this);
        trailerList.setOrientation(LinearLayout.VERTICAL);
        trailerList.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));

        for (Review review : reviews) {

            TextView reviewTv = new TextView(DetailActivity.this);
            LayoutParams params = new LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
            params.setMargins(0,0,0,50);
            reviewTv.setLayoutParams(params);
            reviewTv.setPadding(10,10,10,10);

            reviewTv.setBackgroundColor(getColor(R.color.review_background));
            String reviewText = review.getAuthor() + "\n" + review.getContent();
            reviewTv.setText(reviewText);

            trailerList.addView(reviewTv);
        }

        mReviewsContainerLl.addView(trailerList);
    }

    /**
     * builds the Uri to themoviedb.org-API.
     *
     * @param movieId   the ID from the selected movie.
     * @param category  the info from the movie (reviews/videos).
     * @return          the built uri as string.
     */
    private String getApiUri(int movieId, String category) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(MainActivity.API_SCHEME)
                .authority(MainActivity.API_AUTHORITY)
                .appendPath(MainActivity.API_VERSION_PATH)
                .appendPath(MainActivity.API_MOVIES_PATH)
                .appendPath(String.valueOf(movieId))
                .appendPath(category)
                .appendQueryParameter(
                        MainActivity.API_KEY_NAME, getString(R.string.TheMovieDb_API_KEY));
        return builder.build().toString();
    }

    //region 3rd Party Code

    // ================================
    //   Code beginning from this region is from the following websites:
    //   https://www.journaldev.com/13927/android-collapsingtoolbarlayout-example
    //   https://antonioleiva.com/collapsing-toolbar-layout/
    // ================================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        hideOption(R.id.action_favorite);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_favorite) {
            //TODO: favorite code here!
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void materializeAppBar() {
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true;
                    showOption(R.id.action_favorite);
                } else if (isShow) {
                    isShow = false;
                    hideOption(R.id.action_favorite);
                }
            }
        });
    }

    private void hideOption(int id) {
        MenuItem item = menu.findItem(id);
        item.setVisible(false);
    }

    private void showOption(int id) {
        MenuItem item = menu.findItem(id);
        item.setVisible(true);
    }

    // ================================
    //   End of 3rd party code
    // ================================

    //endregion
}
