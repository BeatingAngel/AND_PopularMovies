
/*
 * Author: Philipp Hermüller
 */

package com.goldencrow.android.popularmovies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.goldencrow.android.popularmovies.entities.Movie;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    public static final String INTENT_EXTRA_MOVIE_KEY = "movie";

    @BindView(R.id.title_tv)
    TextView mMovieTitleTv;
    @BindView(R.id.movie_poster_iv)
    ImageView mMoviePosterIv;
    @BindView(R.id.release_date_tv)
    TextView mReleaseDateTv;
    @BindView(R.id.average_vote_tv)
    TextView mAverageVoteTv;
    @BindView(R.id.plot_synopsis_tv)
    TextView mPlotSynopsisTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent.hasExtra(INTENT_EXTRA_MOVIE_KEY)) {
            Movie movie = intent.getParcelableExtra(INTENT_EXTRA_MOVIE_KEY);

            mMovieTitleTv.setText(movie.getTitle());

            Picasso.with(this)
                    .load(MainActivity.MOVIE_POSTER_BASE_PATH + movie.getPosterPath())
                    .error(R.drawable.ic_launcher_background)
                    .into(mMoviePosterIv);

            String releaseDateString = movie.getReleaseDateAsString();
            mReleaseDateTv.setText(releaseDateString);

            String averageVoteString = getString(R.string.vote_average, movie.getVoteAverage());
            mAverageVoteTv.setText(averageVoteString);

            mPlotSynopsisTv.setText(movie.getOverview());
        } else {
            mMovieTitleTv.setText(getString(R.string.error_movie_not_found));
            mMoviePosterIv.setVisibility(View.INVISIBLE);
        }
    }
}
