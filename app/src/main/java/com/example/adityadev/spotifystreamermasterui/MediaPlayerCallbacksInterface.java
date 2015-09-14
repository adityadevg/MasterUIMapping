package com.example.adityadev.spotifystreamermasterui;

/**
 * Created by adityadev on 9/13/2015.
 *
 * A callback interface that all activities containing this fragment must
 * implement. This mechanism allows activities to be notified of item
 * selections.
 */
public interface MediaPlayerCallbacksInterface {
    /**
     * Callback for when a track has been started.
     */
    public void onSelectedTrackStarted(String externalURL);

    /**
     * Callback for when a track has completed.
     */
    public void onSelectedTrackCompleted();
}