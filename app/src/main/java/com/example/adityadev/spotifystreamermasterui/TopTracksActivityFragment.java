package com.example.adityadev.spotifystreamermasterui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.adityadev.spotifystreamermasterui.artistsmodel.Artist;
import com.example.adityadev.spotifystreamermasterui.toptracks.Tracks;
import com.example.adityadev.spotifystreamermasterui.toptracks.TracksArrayAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import retrofit.RetrofitError;


/**
 * A placeholder topTracksActivityFragment containing a simple view.
 */
public class TopTracksActivityFragment extends Fragment {

    private TracksArrayAdapter tracksArrayAdapter;
    private static List<Tracks> listOfTracks;
    private SpotifyApi api;
    private SpotifyService spotifyService;
    private String artistId, artistName;

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private TopTracksCallbacksInterface mTopTracksCallbacksInterface = sDummyTopTracksCallbacksInterface;

    public TopTracksActivityFragment() {
        listOfTracks = new ArrayList<Tracks>();
        api = new SpotifyApi();
        spotifyService = api.getService();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putParcelableArrayList(getString(R.string.track_list_key), (ArrayList<? extends Parcelable>) listOfTracks);
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof TopTracksCallbacksInterface)) {
            throw new IllegalStateException(getString(R.string.activity_must_implement_fragments_callbacks));
        }
        mTopTracksCallbacksInterface = (TopTracksCallbacksInterface) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mTopTracksCallbacksInterface = sDummyTopTracksCallbacksInterface;
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface TopTracksCallbacksInterface {
        /**
         * Callback for when a track has been selected.
         */
        public void onTopTrackSelected(List<Tracks> listOfTracksForPlayer, int selectedTrackPosition);
    }

    /**
     * A dummy implementation of the {@link TopTracksCallbacksInterface} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static TopTracksCallbacksInterface sDummyTopTracksCallbacksInterface = new TopTracksCallbacksInterface() {
        @Override
        public void onTopTrackSelected(List<Tracks> listOfTracksForPlayer, int selectedTrackPosition) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (null == savedInstanceState || !savedInstanceState.containsKey(getString(R.string.track_list_key))) {
            // get artist name from previous baseIntent
            Intent baseIntent = getActivity().getIntent();
            Artist selectedArtist = baseIntent.getParcelableExtra(getString(R.string.artist_key));

            if (null != getArguments() && getArguments().containsKey(getString(R.string.artist_key))){
                selectedArtist = getArguments().getParcelable(getString(R.string.artist_key));
            }

            if(null != selectedArtist){
                artistId = selectedArtist.getArtistID();
                artistName = selectedArtist.getArtistName();
            }
            if(null != artistId){
                new FetchTracksTask().execute(artistId);
            }
        } else {
            listOfTracks = savedInstanceState.getParcelableArrayList(getString(R.string.track_list_key));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        listOfTracks.clear();
        tracksArrayAdapter = new TracksArrayAdapter(getActivity(), listOfTracks);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_tracks);
        listView.setAdapter(tracksArrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mTopTracksCallbacksInterface.onTopTrackSelected(listOfTracks, position);
            }
        });


        return rootView;
    }

    public class FetchTracksTask extends AsyncTask<String, Void, List<Tracks>> {
        private static final int MAX_NO_OF_TRACKS = 10;
        private final String LOG_TAG = TopTracksActivity.class.getSimpleName();
        Map<String, Object> selectCountryOption;
        kaaes.spotify.webapi.android.models.Tracks spotifyListOfTracks;
        private List<Tracks> localListOfTracks = new ArrayList<Tracks>();
        private SharedPreferences sharedPreferences;

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params Accepts artistId to search for top tracks
         * @return List of tracks by the select artist
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected List<Tracks> doInBackground(String... params) {
            try {
                if (null != params[0]) {
                    if (null != params[0]) {
                        listOfTracks.clear();
                        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        selectCountryOption = new HashMap<String, Object>();
                        selectCountryOption.put(getString(R.string.country_key), sharedPreferences.getString(getString(R.string.country_code_pref_key),getString(R.string.default_country_name)));

                        spotifyListOfTracks = spotifyService.getArtistTopTrack(params[0], selectCountryOption);
                        if (null != spotifyListOfTracks.tracks && !spotifyListOfTracks.tracks.isEmpty()) {
                            int size = spotifyListOfTracks.tracks.size() > MAX_NO_OF_TRACKS ? MAX_NO_OF_TRACKS : spotifyListOfTracks.tracks.size();
                            String albumName;
                            String albumThumbnailLink;
                            String trackName;
                            String artistName;
                            String previewUrl;
                            String externalSpotifyLink;

                            kaaes.spotify.webapi.android.models.Track currentTrack;
                            for (int i = 0; i < size; i++) {
                                currentTrack = spotifyListOfTracks.tracks.get(i);
                                if (null != currentTrack && null != currentTrack.album) {
                                    albumName = currentTrack.album.name;
                                    artistName = currentTrack.artists.get(0).name;
                                    previewUrl = currentTrack.preview_url;
                                    externalSpotifyLink = currentTrack.external_urls.get("spotify");
                                    if (null != currentTrack.album.images && !currentTrack.album.images.isEmpty()) {
                                        albumThumbnailLink = currentTrack.album.images.get(0).url;
                                        trackName = currentTrack.name;
                                        localListOfTracks.add(new Tracks(albumName, albumThumbnailLink, trackName, artistName, previewUrl, externalSpotifyLink));
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (
                    RetrofitError rfe) {
                Log.d(LOG_TAG, rfe.getMessage());
            }
            return localListOfTracks;
        }

        @Override
        protected void onPostExecute(List<Tracks> listOfTracksResult) {
            if (null != listOfTracksResult){
                if (!listOfTracksResult.isEmpty()){
                    tracksArrayAdapter.addAll(listOfTracksResult);
                } else if (!TopTracksActivityFragment.listOfTracks.isEmpty()){
                    tracksArrayAdapter.addAll(TopTracksActivityFragment.listOfTracks);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.no_tracks_found), (Toast.LENGTH_LONG)).show();
                }
            }
        }
    }
}
