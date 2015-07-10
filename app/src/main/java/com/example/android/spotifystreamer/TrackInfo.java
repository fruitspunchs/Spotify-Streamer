package com.example.android.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Stores track information
 */
public class TrackInfo implements Parcelable {
    public static final Parcelable.Creator<TrackInfo> CREATOR
            = new Parcelable.Creator<TrackInfo>() {
        public TrackInfo createFromParcel(Parcel in) {
            return new TrackInfo(in);
        }

        public TrackInfo[] newArray(int size) {
            return new TrackInfo[size];
        }
    };
    private final ArrayList<String> mTrackNames = new ArrayList<>();
    private final ArrayList<String> mAlbumNames = new ArrayList<>();
    private final ArrayList<String> mLargeThumbnails = new ArrayList<>();
    private final ArrayList<String> mMediumThumbnails = new ArrayList<>();
    private final ArrayList<String> mTrackPreviewUrls = new ArrayList<>();

    public TrackInfo() {

    }

    private TrackInfo(Parcel in) {
        in.readStringList(mTrackNames);
        in.readStringList(mAlbumNames);
        in.readStringList(mLargeThumbnails);
        in.readStringList(mMediumThumbnails);
        in.readStringList(mTrackPreviewUrls);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(mTrackNames);
        dest.writeStringList(mAlbumNames);
        dest.writeStringList(mLargeThumbnails);
        dest.writeStringList(mMediumThumbnails);
        dest.writeStringList(mTrackPreviewUrls);
    }

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


    public void clear() {
        mTrackNames.clear();
        mAlbumNames.clear();
        mLargeThumbnails.clear();
        mMediumThumbnails.clear();
        mTrackPreviewUrls.clear();
    }
}
