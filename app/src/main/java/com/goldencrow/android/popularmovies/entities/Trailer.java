package com.goldencrow.android.popularmovies.entities;

/**
 * Created by Philipp
 */

public class Trailer {

    private final String key;
    private final String name;
    private final String site;

    public Trailer(String key, String name, String site) {
        this.key = key;
        this.name = name;
        this.site = site;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getSite() {
        return site;
    }
}
