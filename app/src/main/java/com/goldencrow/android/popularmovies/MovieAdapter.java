
/*
 * Author: Philipp Hermüller
 */

package com.goldencrow.android.popularmovies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.goldencrow.android.popularmovies.entities.Movie;
import com.squareup.picasso.Picasso;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private Context mContext;
    private Movie[] mMovies;

    MovieAdapter(@NonNull Context context) {
        mContext = context;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        int layoutId = R.layout.movie_list_item;

        View view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
        view.setFocusable(true);

        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        Movie movie = mMovies[position];

        Picasso.with(mContext)
                .load("http://image.tmdb.org/t/p/w185//" + movie.getPosterPath())
                .error(R.drawable.ic_launcher_background)
                .into(holder.mMoviePosterIv);
    }

    @Override
    public int getItemCount() {
        return mMovies == null ? 0 : mMovies.length;
    }

    void setMovieData(Movie[] movies) {
        mMovies = movies;
        notifyDataSetChanged();
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {

        ImageView mMoviePosterIv;

        MovieViewHolder(View itemView) {
            super(itemView);

            mMoviePosterIv = itemView.findViewById(R.id.movie_poster_iv);
        }
    }
}
