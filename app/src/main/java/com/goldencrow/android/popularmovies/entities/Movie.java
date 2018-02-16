
/*
 * Author: Philipp Hermüller
 */

package com.goldencrow.android.popularmovies.entities;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Movie {
    private double vote_average;
    private String title;
    private String poster_path;
    private String overview;
    private Date release_date;

    public Movie() {
    }

    public Movie(double voteAverage, String title, String posterPath, String overview, Date releaseDate) {
        this.vote_average = voteAverage;
        this.title = title;
        this.poster_path = posterPath;
        this.overview = overview;
        this.release_date = releaseDate;
    }

    public double getVoteAverage() {
        return vote_average;
    }

    public void setVoteAverage(double voteAverage) {
        this.vote_average = voteAverage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterPath() {
        return poster_path;
    }

    public void setPosterPath(String posterPath) {
        this.poster_path = posterPath;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public Date getReleaseDate() {
        return release_date;
    }

    public String getReleaseDateAsString(Locale locale) {
        DateFormat df = SimpleDateFormat.getDateInstance(DateFormat.SHORT, locale);
        return df.format(release_date);
    }

    public void setReleaseDate(String releaseDate) {
        // I'm suppressing Lint because the format below isn't system specific but the format
        //     from the API-server. So I can't use the System Format here.
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        try {
            this.release_date = format.parse(releaseDate);
        } catch (ParseException e) {
            this.release_date = null;
        }
    }
}
