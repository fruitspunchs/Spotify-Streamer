package com.example.android.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Stores artist information
 */
public class ArtistInfo implements Parcelable {
    public static final Parcelable.Creator<ArtistInfo> CREATOR
            = new Parcelable.Creator<ArtistInfo>() {
        public ArtistInfo createFromParcel(Parcel in) {
            return new ArtistInfo(in);
        }

        public ArtistInfo[] newArray(int size) {
            return new ArtistInfo[size];
        }
    };
    private ArrayList<String> mArtistNames = new ArrayList<>();
    private ArrayList<String> mArtistImages = new ArrayList<>();
    private ArrayList<String> mIds = new ArrayList<>();

    public ArtistInfo() {

    }

    private ArtistInfo(Parcel in) {
        in.readStringList(mArtistNames);
        in.readStringList(mArtistImages);
        in.readStringList(mIds);
    }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(mArtistNames);
        dest.writeStringList(mArtistImages);
        dest.writeStringList(mIds);
    }
}