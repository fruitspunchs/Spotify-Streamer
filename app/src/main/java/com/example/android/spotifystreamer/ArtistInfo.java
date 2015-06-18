package com.example.android.spotifystreamer;

import java.util.ArrayList;

/**
 * Stores artist information
 */
public class ArtistInfo {
    private ArrayList<String> mArtistNames = new ArrayList<>();
    private ArrayList<String> mArtistImages = new ArrayList<>();
    private ArrayList<String> mIds = new ArrayList<>();

    public ArrayList<String> getArtistNames() {
        return mArtistNames;
    }

    public ArrayList<String> getArtistImages() {
        return mArtistImages;
    }

    public ArrayList<String> getIds() {
        return mIds;
    }

    public void clear() {
        mArtistNames.clear();
        mArtistImages.clear();
        mIds.clear();
    }
}