
/*
 * Author: Philipp Hermüller
 */

package com.goldencrow.android.popularmovies;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
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
import com.goldencrow.android.popularmovies.data.Contracts;
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

    private static final String TRAILER_SITE_YOUTUBE = "youtube";

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
    @BindView(R.id.fab)
    FloatingActionButton mFavoriteFaBtn;

    private Menu menu;

    private Movie mMovie;
    private Trailer mTrailer;

    private RequestQueue mQueue;
    private boolean isFavorite = false;

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
            mMovie = intent.getParcelableExtra(INTENT_EXTRA_MOVIE_KEY);

            mCollapsingToolbarLayout.setTitle(mMovie.getTitle());

            Picasso.with(this)
                    .load(MainActivity.MOVIE_POSTER_BASE_PATH + mMovie.getPosterPath())
                    .error(R.drawable.ic_launcher_background)
                    .into(mMoviePosterIv);
            Picasso.with(this)
                    .load(MainActivity.MOVIE_POSTER_BASE_PATH + mMovie.getBackdrop_path())
                    .error(R.drawable.ic_launcher_background)
                    .into(mToolbarIv);

            String releaseDateString = mMovie.getReleaseDateAsString();
            mReleaseDateTv.setText(releaseDateString);

            String averageVoteString = getString(R.string.vote_average, mMovie.getVoteAverage());
            mAverageVoteTv.setText(averageVoteString);

            mPlotSynopsisTv.setText(mMovie.getOverview());

            queueJsonRequest(mMovie.getId(), TRAILER_API_KEY);
            queueJsonRequest(mMovie.getId(), REVIEWS_API_KEY);
        } else {
            mCollapsingToolbarLayout.setTitle(getString(R.string.error_movie_not_found));
            mMoviePosterIv.setVisibility(View.INVISIBLE);
        }

        handleFavorite();
    }

    /**
     * initializes the FavoriteButton with the code what should happen if a FavoriteBtn is clicked.
     * It defines when a button inserts or deletes a entry from the DB.
     */
    private void handleFavorite() {
        mFavoriteFaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFavorite) {
                    unfavoriteMovie();
                } else {
                    favoriteMovie();
                }
                isFavorite = !isFavorite;
            }
        });

        Uri movieUri = ContentUris.withAppendedId(Contracts.MovieEntry.CONTENT_URI, mMovie.getId());
        Cursor cursor = getContentResolver().query(movieUri,
                null,
                null,
                null,
                null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                mFavoriteFaBtn.setImageResource(android.R.drawable.btn_star_big_on);
                isFavorite = true;
            }
            cursor.close();
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
                                    mTrailer = trailers[0];
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
                                    R.string.error_while_parsing,
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
                                R.string.error_while_retrieving,
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

        if (trailer.getSite().toLowerCase().equals(TRAILER_SITE_YOUTUBE)) {
            Picasso.with(DetailActivity.this)
                    .load(getString(R.string.youtube_poster, trailer.getKey()))
                    .into(trailerPreviewIv);

            trailerPreviewIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri trailerUri = Uri.parse(getString(R.string.youtube_link, trailer.getKey()));

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

    /**
     * Adds the current movie to the favorites. (inserts entry into DB).
     */
    private void favoriteMovie() {
        ContentValues contentValues = new ContentValues();

        contentValues.put(Contracts.MovieEntry._ID, mMovie.getId());
        contentValues.put(Contracts.MovieEntry.COLUMN_VOTE_AVERAGE, mMovie.getVoteAverage());
        contentValues.put(Contracts.MovieEntry.COLUMN_TITLE, mMovie.getTitle());
        contentValues.put(Contracts.MovieEntry.COLUMN_POSTER_PATH, mMovie.getPosterPath());
        contentValues.put(Contracts.MovieEntry.COLUMN_BACKDROP_PATH, mMovie.getBackdrop_path());
        contentValues.put(Contracts.MovieEntry.COLUMN_OVERVIEW, mMovie.getOverview());
        contentValues.put(Contracts.MovieEntry.COLUMN_RELEASE_DATE, mMovie.getReleaseDateAsString());

        Uri uri = getContentResolver().insert(Contracts.MovieEntry.CONTENT_URI, contentValues);

        if(uri != null) {
            Toast.makeText(this, R.string.added_favorites, Toast.LENGTH_SHORT).show();
            mFavoriteFaBtn.setImageResource(android.R.drawable.btn_star_big_on);
            menu.findItem(R.id.action_favorite).setIcon(getDrawable(android.R.drawable.btn_star_big_on));
        }
    }

    /**
     * Removes the current movie from the Favorites. (removes the entry from the DB).
     */
    private void unfavoriteMovie() {
        Uri toDelete = ContentUris.withAppendedId(Contracts.MovieEntry.CONTENT_URI, mMovie.getId());
        int deleted = getContentResolver().delete(toDelete, null, null);

        if(deleted > 0) {
            Toast.makeText(this, R.string.removed_favorites, Toast.LENGTH_SHORT).show();
            mFavoriteFaBtn.setImageResource(android.R.drawable.btn_star_big_off);
            menu.findItem(R.id.action_favorite).setIcon(getDrawable(android.R.drawable.btn_star_big_off));
        } else {
            Toast.makeText(this, R.string.could_not_remove_favorite,
                    Toast.LENGTH_SHORT).show();
        }
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
        hideFavoriteOption();
        if (isFavorite) {
            this.menu.findItem(R.id.action_favorite).setIcon(getDrawable(android.R.drawable.btn_star_big_on));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_favorite) {
            if (isFavorite) {
                unfavoriteMovie();
            } else {
                favoriteMovie();
            }
            isFavorite = !isFavorite;
            return true;
        } else if (id == R.id.action_share_trailer
                && mTrailer.getSite().toLowerCase().equals(TRAILER_SITE_YOUTUBE)) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    getString(R.string.link_share_text, mMovie.getTitle(), mTrailer.getKey()));
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(
                    shareIntent,
                    getString(R.string.link_share_title)));
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Initialized the ActionBar.
     * If the ActionBar was scrolled enough, then hide the FloatingButton and
     * show the ActionButton in the ToolBar instead.
     */
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
                    showFavoriteOption();
                } else if (isShow) {
                    isShow = false;
                    hideFavoriteOption();
                }
            }
        });
    }

    /**
     * Hides the ActionButton in the ToolBar.
     */
    private void hideFavoriteOption() {
        MenuItem item = menu.findItem(R.id.action_favorite);
        item.setVisible(false);
    }

    /**
     * Displays the ActionButton in the ToolBar.
     */
    private void showFavoriteOption() {
        MenuItem item = menu.findItem(R.id.action_favorite);
        item.setVisible(true);
    }

    // ================================
    //   End of 3rd party code
    // ================================

    //endregion
}
