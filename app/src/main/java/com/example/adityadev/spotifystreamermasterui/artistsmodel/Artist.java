package com.example.adityadev.spotifystreamermasterui.artistsmodel;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by adityadev.
 */
public class Artist implements Parcelable {
    String imageURL;
    String artistName;
    String artistID;

    public Artist(String artistName, String imageURL, String artistID) {
        this.artistName = artistName;
        this.imageURL = imageURL;
        this.artistID = artistID;
    }

    private Artist(Parcel in){
        imageURL = in.readString();
        artistName = in.readString();
        artistID = in.readString();
    }

    public String toString() { return artistName + "--" + artistID; }

    public String getArtistID() {
        return artistID;
    }

    public void setArtistID(String artistID) {
        this.artistID = artistID;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     * by the Parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageURL);
        dest.writeString(artistName);
        dest.writeString(artistID);
    }

    public static final Parcelable.Creator<Artist> CREATOR = new Parcelable.Creator<Artist>() {
        @Override
        public Artist createFromParcel(Parcel parcel) {
            return new Artist(parcel);
        }

        @Override
        public Artist[] newArray(int i) {
            return new Artist[i];
        }

    };

}