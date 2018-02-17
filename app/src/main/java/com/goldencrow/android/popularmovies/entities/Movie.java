
/*
 * Author: Philipp Hermüller
 */

package com.goldencrow.android.popularmovies.entities;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Movie implements Parcelable {
    private final int id;
    private final double vote_average;
    private final String title;
    private final String poster_path;
    private final String overview;
    private Date release_date;

    private Movie(Parcel in) {
        id = in.readInt();
        vote_average = in.readDouble();
        title = in.readString();
        poster_path = in.readString();
        overview = in.readString();
        setReleaseDate(in.readString());
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public double getVoteAverage() {
        return vote_average;
    }

    public String getTitle() {
        return title;
    }

    public String getPosterPath() {
        return poster_path;
    }

    public String getOverview() {
        return overview;
    }

    public String getReleaseDateAsString() {
        DateFormat df = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ENGLISH);
        return df.format(release_date);
    }

    private void setReleaseDate(String releaseDate) {
        DateFormat df = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ENGLISH);

        try {
            this.release_date = df.parse(releaseDate);
        } catch (ParseException e) {
            this.release_date = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeDouble(vote_average);
        parcel.writeString(title);
        parcel.writeString(poster_path);
        parcel.writeString(overview);
        parcel.writeString(getReleaseDateAsString());
    }
}
