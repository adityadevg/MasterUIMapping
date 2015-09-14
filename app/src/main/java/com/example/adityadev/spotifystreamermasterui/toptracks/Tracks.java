package com.example.adityadev.spotifystreamermasterui.toptracks;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by adityadev.
 */
public class Tracks implements Parcelable {

    protected Tracks(Parcel in) {
        albumImageURL = in.readString();
        albumName = in.readString();
        trackName = in.readString();
        artistName = in.readString();
        previewUrl = in.readString();
        externalSpotifyLink = in.readString();
    }

    public Tracks(String albumName, String albumImageURL, String trackName, String artistName, String previewUrl, String externalSpotifyLink) {
        this.albumName = albumName;
        this.albumImageURL = albumImageURL;
        this.trackName = trackName;
        this.artistName = artistName;
        this.previewUrl = previewUrl;
        this.externalSpotifyLink = externalSpotifyLink;
    }

    public static final Creator<Tracks> CREATOR = new Creator<Tracks>() {
        @Override
        public Tracks createFromParcel(Parcel in) {
            return new Tracks(in);
        }

        @Override
        public Tracks[] newArray(int size) {
            return new Tracks[size];
        }
    };

    String artistName;

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    String previewUrl;

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    String trackName;

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    String albumImageURL;

    public String getAlbumImageURL() {
        return albumImageURL;
    }

    public void setAlbumImageURL(String albumImageURL) {
        this.albumImageURL = albumImageURL;
    }

    String albumName;

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String artistName) {
        this.albumName = artistName;
    }

    String externalSpotifyLink;

    public String getExternalSpotifyLink() {
        return externalSpotifyLink;
    }

    public void setExternalSpotifyLink(String externalSpotifyLink) {
        this.externalSpotifyLink = externalSpotifyLink;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(albumImageURL);
        dest.writeString(albumName);
        dest.writeString(trackName);
        dest.writeString(artistName);
        dest.writeString(previewUrl);
        dest.writeString(externalSpotifyLink);
    }
}