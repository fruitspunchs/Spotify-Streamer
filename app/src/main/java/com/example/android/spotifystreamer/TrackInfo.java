package com.example.android.spotifystreamer;

import java.util.ArrayList;

/**
 * Stores track information
 */
public class TrackInfo {
    private final ArrayList<String> mTrackNames = new ArrayList<>();
    private final ArrayList<String> mAlbumNames = new ArrayList<>();
    private final ArrayList<String> mLargeThumbnails = new ArrayList<>();
    private final ArrayList<String> mMediumThumbnails = new ArrayList<>();
    private final ArrayList<Integer> mDefaultColors = new ArrayList<>();
    private final ArrayList<String> mTrackPreviewUrls = new ArrayList<>();


    public ArrayList<String> getTrackPreviewUrls() {
        return mTrackPreviewUrls;
    }

    public ArrayList<String> getTrackNames() {
        return mTrackNames;
    }

    public ArrayList<String> getAlbumNames() {
        return mAlbumNames;
    }

    public ArrayList<String> getLargeThumbnails() {
        return mLargeThumbnails;
    }

    public ArrayList<String> getMediumThumbnails() {
        return mMediumThumbnails;
    }

    public ArrayList<Integer> getDefaultColors() {
        return mDefaultColors;
    }

    public void clear() {
        mTrackNames.clear();
        mAlbumNames.clear();
        mLargeThumbnails.clear();
        mMediumThumbnails.clear();
        mDefaultColors.clear();
        mTrackPreviewUrls.clear();
    }
}
