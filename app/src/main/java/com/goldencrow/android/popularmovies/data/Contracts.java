package com.goldencrow.android.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Philipp
 */

public class Contracts {

    static final String AUTHORITY = "com.goldencrow.android.popularmovies";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    static final String MOVIES_PATH = "movies";
    //public static final String REVIEWS_PATH = "reviews";
    //public static final String TRAILERS_PATH = "trailers";

    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(MOVIES_PATH).build();

        static final String TABLE_NAME = "movies";

        public static final String COLUMN_VOTE_AVERAGE = "average_vote";
        public static final String COLUMN_TITLE = "title";
        //      I just realised that saving the URL to the image is worthless.
        //      The main usage of saving the data should be to present it while offline.
        //      And if someone is offline, then logically the Url can't be loaded.
        //TODO: Change String-URL to BLOB
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";

        static final String CREATE_TABLE_SQL =
                        "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID +                   " INTEGER PRIMARY KEY, " +
                        COLUMN_VOTE_AVERAGE +   " FLOAT NOT NULL, " +
                        COLUMN_TITLE +          " TEXT NOT NULL, " +
                        COLUMN_POSTER_PATH +    " TEXT NOT NULL, " +
                        COLUMN_BACKDROP_PATH +  " TEXT NOT NULL, " +
                        COLUMN_OVERVIEW +       " TEXT NOT NULL, " +
                        COLUMN_RELEASE_DATE +   " TEXT NOT NULL);";

        //public static final String DROP_TABLE_SQL =
        //        "DROP TABLE IF EXISTS " + TABLE_NAME;

    }

}
