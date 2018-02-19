
/*
 * Author: Philipp Hermüller
 */

package com.goldencrow.android.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.goldencrow.android.popularmovies.entities.Movie;
import com.squareup.picasso.Picasso;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private final Context mContext;
    private final MovieOnClickHandler mClickHandler;

    private Movie[] mMovies;

    /**
     * the interface which handles onClicks.
     */
    public interface MovieOnClickHandler {
        void onClick(Movie movie);
    }

    /**
     * Constructor for the Adapter.
     *
     * @param context       the context where the list is located. (required)
     * @param clickHandler  the clickHandler which contains the logic to handle the click.
     */
    MovieAdapter(@NonNull Context context, MovieOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
    }

    /**
     * Create a custom view for the movie poster.
     *
     * @param parent    the parent of the view.
     * @param viewType  viewType to differentiate between custom views. (not used)
     * @return          a new MovieViewHolder.
     */
    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        int layoutId = R.layout.movie_list_item;

        View view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
        view.setFocusable(true);

        return new MovieViewHolder(view);
    }

    /**
     * populate the view with actual data.
     *
     * @param holder    the view which will be populated with data.
     * @param position  position of the item in the list.
     */
    @Override
    public void onBindViewHolder(final MovieViewHolder holder, int position) {
        final Movie movie = mMovies[position];

        Picasso.Builder builder = new Picasso.Builder(mContext);
        builder.listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                // quick solution for offline mode:
                // if offline and an image can't be displayed -> display the title.
                holder.mMovieTitleTv.setText(movie.getTitle());
                holder.mMovieTitleTv.setVisibility(View.VISIBLE);
            }
        });

        builder.build()
                .load(MainActivity.MOVIE_POSTER_BASE_PATH + movie.getPosterPath())
                .error(R.drawable.ic_launcher_background)
                .into(holder.mMoviePosterIv);
    }

    /**
     * Returns the size of the list.
     *
     * @return the size of the list.
     */
    @Override
    public int getItemCount() {
        return mMovies == null ? 0 : mMovies.length;
    }

    /**
     * changes the displayed data to the new provided movies.
     *
     * @param movies    to display in the grid.
     */
    void setMovieData(Movie[] movies) {
        mMovies = movies;
        notifyDataSetChanged();
    }

    /**
     * custom viewHolder for a list item.
     */
    class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView mMoviePosterIv;
        final TextView mMovieTitleTv;

        /**
         * the constructor for the custom ViewHolder.
         *
         * @param itemView  the view where the widgets are displayed.
         */
        MovieViewHolder(View itemView) {
            super(itemView);

            mMoviePosterIv = itemView.findViewById(R.id.movie_poster_iv);
            mMovieTitleTv = itemView.findViewById(R.id.movie_title_tv);

            itemView.setOnClickListener(this);
        }

        /**
         * handles the click on a movie poster.
         * It uses the provided method from the constructor for the click.
         *
         * @param view  the view which was clicked on.
         */
        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            mClickHandler.onClick(mMovies[position]);
        }
    }
}
