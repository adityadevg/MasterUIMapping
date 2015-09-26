package com.example.adityadev.spotifystreamermasterui;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adityadev.spotifystreamermasterui.artistsmodel.Artist;
import com.example.adityadev.spotifystreamermasterui.artistsmodel.ArtistArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;

/**
 * A list topTracksActivityFragment representing a list of Artists. This topTracksActivityFragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link TopTracksActivityFragment}.
 * <p>
 */
public class MainArtistActivityFragment extends Fragment {

    private EditText searchEditText;
    private ArtistArrayAdapter artistArrayAdapter;
    private List<Artist> listOfArtists;
    private SpotifyApi api;
    private SpotifyService spotify;
    private ListView listView;

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private ArtistCallbacksInterface mArtistCallbacksInterface = sDummyArtistCallbacksInterface;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;


    /**
     * Mandatory empty constructor for the topTracksActivityFragment manager to instantiate the
     * topTracksActivityFragment (e.g. upon screen orientation changes).
     */
    public MainArtistActivityFragment() {
        api = new SpotifyApi();
        spotify = api.getService();
        listOfArtists = new ArrayList<Artist>();
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface ArtistCallbacksInterface {
        /**
         * Callback for when an item has been selected.
         */
        public void onArtistSelected(Artist selectedArtist);
    }

    /**
     * A dummy implementation of the {@link ArtistCallbacksInterface} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static ArtistCallbacksInterface sDummyArtistCallbacksInterface = new ArtistCallbacksInterface() {
        @Override
        public void onArtistSelected(Artist artist) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != savedInstanceState && savedInstanceState.containsKey(getString(R.string
                .saved_before_rotation))) {
            listOfArtists = savedInstanceState.getParcelableArrayList(getString(R.string.saved_before_rotation));
        } else {
            listOfArtists.clear();
        }
        // Add this line in order for this topTracksActivityFragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof ArtistCallbacksInterface)) {
            throw new IllegalStateException(getString(R.string.activity_must_implement_fragments_callbacks));
        }
        mArtistCallbacksInterface = (ArtistCallbacksInterface) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mArtistCallbacksInterface = sDummyArtistCallbacksInterface;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_artist, container, false);
        searchEditText = (EditText) rootView.findViewById(R.id.edittext_search_artists);
        searchEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // if user presses search key search the artist name
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String artistToSearch = searchEditText.getText().toString();

                    if (!artistToSearch.isEmpty()) {
                        // search for artist through spotify wrapper and asynctask
                        new FetchArtistTask().execute(artistToSearch);
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.no_artists_found), Toast.LENGTH_LONG).show();
                    }
                }
                return false;
            }
        });

        listView = (ListView) rootView.findViewById(R.id.artist_list_view);
        artistArrayAdapter = new ArtistArrayAdapter(getActivity(), listOfArtists);
        listView.setAdapter(artistArrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mArtistCallbacksInterface.onArtistSelected(listOfArtists.get(position));
            }
        });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(getString(R.string.saved_before_rotation), (ArrayList<? extends Parcelable>) listOfArtists);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(getString(R.string.artist_position_key), mActivatedPosition);
        }
    }

    public class FetchArtistTask extends AsyncTask<String, Void, List<Artist>> {
        private final String LOG_TAG = MainArtistActivity.class.getSimpleName();
        private List<kaaes.spotify.webapi.android.models.Artist> spotifyArtistList;
        private List<Artist> localListOfArtists = new ArrayList<Artist>();

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected List<Artist> doInBackground(String... params) {

            try {
                localListOfArtists.clear();
                ArtistsPager results = spotify.searchArtists(params[0]);
                if (null != results) {
                    spotifyArtistList = results.artists.items;
                    if (null != spotifyArtistList && !spotifyArtistList.isEmpty()) {
                        String currentArtistName;
                        String currentArtistURL;
                        String currentArtistID;
                        int size = spotifyArtistList.size();
                        for (int i = 0; i < size; i++) {
                            currentArtistName = spotifyArtistList.get(i).name;
                            currentArtistURL = null != spotifyArtistList.get(i).images &&
                                    !spotifyArtistList.get(i).images.isEmpty() && null
                                    != spotifyArtistList
                                    .get(i).images.get(0) ? spotifyArtistList
                                    .get(i).images.get(0).url : "";
                            currentArtistID = spotifyArtistList.get(i).id;
                            localListOfArtists.add(new Artist(currentArtistName, currentArtistURL,
                                    currentArtistID));
                        }
                    }
                }
            } catch (RetrofitError rfe) {
                Log.d(LOG_TAG, rfe.getMessage());
            }
            return localListOfArtists;
        }

        @Override
        protected void onPostExecute(List<Artist> artistResult) {
            if (null != artistResult) {
                // If no artist was found then display message
                if (artistResult.isEmpty())
                    Toast.makeText(getActivity(), getString(R.string.no_artists_found),
                            (Toast.LENGTH_LONG)).show();
                else {
                    artistArrayAdapter.addAll(artistResult);
                }
            }
        }
    }
}
