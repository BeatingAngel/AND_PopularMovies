package com.goldencrow.android.popularmovies.customUIs;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.goldencrow.android.popularmovies.MovieAdapter;
import com.goldencrow.android.popularmovies.entities.Movie;

import java.util.Arrays;

/**
 * code is from the following resource:
 * https://gist.github.com/FrantisekGazo/a9cc4e18cee42199a287
 */
public class StatefulRecyclerView extends RecyclerView {

    private static final String SAVED_SUPER_STATE = "super-state";
    private static final String SAVED_LAYOUT_MANAGER = "layout-manager-state";
    private static final String SAVED_SCROLL_POSITION = "scroll-position";

    private Parcelable mLayoutManagerSavedState;

    public StatefulRecyclerView(Context context) {
        super(context);
    }

    public StatefulRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StatefulRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(SAVED_SUPER_STATE, super.onSaveInstanceState());
        LayoutManager layoutManager = this.getLayoutManager();
        if(layoutManager != null){
            int pos = RecyclerView.NO_POSITION;
            if (layoutManager instanceof GridLayoutManager) {
                pos = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
            }
            bundle.putParcelable(SAVED_LAYOUT_MANAGER, layoutManager.onSaveInstanceState());
            bundle.putInt(SAVED_SCROLL_POSITION, pos);
            Movie[] movies = ((MovieAdapter)this.getAdapter()).getMovieData();
            bundle.putParcelableArray("movies", movies);
        }
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mLayoutManagerSavedState = bundle.getParcelable(SAVED_LAYOUT_MANAGER);
            state = bundle.getParcelable(SAVED_SUPER_STATE);

            Parcelable[] moviesParc = bundle.getParcelableArray("movies");
            Movie[] movies = null;
            if (moviesParc != null) {
                movies = Arrays.copyOf(moviesParc, moviesParc.length, Movie[].class);
            }
            ((MovieAdapter)this.getAdapter()).setMovieData(movies);

            LayoutManager layoutManager = this.getLayoutManager();
            layoutManager.onRestoreInstanceState(mLayoutManagerSavedState);

            int pos = bundle.getInt(SAVED_SCROLL_POSITION);
            int count = this.getChildCount();
            if(pos != RecyclerView.NO_POSITION && pos < count){
                this.scrollToPosition(pos);
            }
        }
        super.onRestoreInstanceState(state);
    }

    /**
     * Restores scroll position after configuration change.
     * <p>
     * <b>NOTE:</b> Must be called after adapter has been set.
     */
    private void restorePosition() {
        if (mLayoutManagerSavedState != null) {
            this.getLayoutManager().onRestoreInstanceState(mLayoutManagerSavedState);
            mLayoutManagerSavedState = null;
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        restorePosition();
    }
}
