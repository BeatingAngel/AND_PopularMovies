package com.goldencrow.android.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * I created this file with the schema of the Udacity-Course "Developing Android Apps"
 * Lesson09 T09.07
 */
public class MoviesContentProvider extends ContentProvider {

    private static final int MOVIES = 100;
    private static final int MOVIE_WITH_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(Contracts.AUTHORITY, Contracts.MOVIES_PATH, MOVIES);
        uriMatcher.addURI(Contracts.AUTHORITY, Contracts.MOVIES_PATH + "/#", MOVIE_WITH_ID);

        return uriMatcher;
    }

    private MoviesDbHelper mMoviesDbHelper;

    @Override
    public boolean onCreate() {
        mMoviesDbHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        Uri toReturn;
        final SQLiteDatabase db = mMoviesDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                long id = db.insert(Contracts.MovieEntry.TABLE_NAME, null, values);

                if ( id > 0 ) {
                    toReturn = ContentUris.withAppendedId(Contracts.MovieEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Could not insert row into " + uri);
                }

                break;
            default:
                throw new UnsupportedOperationException("Unsupported uri: " + uri);
        }

        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return toReturn;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String where, @Nullable String[] selectionArgs) {

        int deletedCount;
        final SQLiteDatabase db = mMoviesDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE_WITH_ID:
                // get the ID from the element which needs to be deleted.
                String id = uri.getPathSegments().get(1);

                deletedCount = db.delete(
                        Contracts.MovieEntry.TABLE_NAME,
                        Contracts.MovieEntry._ID + "=?",
                        new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unsupported uri: " + uri);
        }

        if (deletedCount != 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return deletedCount;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {

        Cursor retCursor;
        final SQLiteDatabase db = mMoviesDbHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                retCursor = db.query(Contracts.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case MOVIE_WITH_ID:
                String id = uri.getPathSegments().get(1);

                retCursor = db.query(Contracts.MovieEntry.TABLE_NAME,
                        projection,
                        Contracts.MovieEntry._ID + "=?",
                        new String[]{id},
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported uri: " + uri);
        }

        if (getContext() != null) {
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
