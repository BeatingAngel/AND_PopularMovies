
/*
 * Author: Philipp Hermüller
 */

package com.goldencrow.android.popularmovies;

import android.content.Intent;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.goldencrow.android.popularmovies.entities.Movie;
import com.squareup.picasso.Picasso;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    public static final String INTENT_EXTRA_MOVIE_KEY = "movie";

    @BindView(R.id.movie_poster_iv)
    ImageView mMoviePosterIv;
    @BindView(R.id.release_date_tv)
    TextView mReleaseDateTv;
    @BindView(R.id.average_vote_tv)
    TextView mAverageVoteTv;
    @BindView(R.id.plot_synopsis_tv)
    TextView mPlotSynopsisTv;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.app_bar_layout)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.collapsing_toolbar_layout)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.toolbar_iv)
    ImageView mToolbarIv;

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

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
        } else {
            mCollapsingToolbarLayout.setTitle(getString(R.string.error_movie_not_found));
            mMoviePosterIv.setVisibility(View.INVISIBLE);
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
        hideOption(R.id.action_favorite);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_reviews) {
            return true;
        } else if (id == R.id.action_favorite) {
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
