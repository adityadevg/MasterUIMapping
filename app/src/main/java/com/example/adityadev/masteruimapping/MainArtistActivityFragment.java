package com.example.adityadev.masteruimapping;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adityadev.masteruimapping.artistsmodel.Artist;
import com.example.adityadev.masteruimapping.artistsmodel.ArtistArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;

/**
 * A list fragment representing a list of Artists. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link TopTracksActivityFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class MainArtistActivityFragment extends ListFragment {

    private EditText searchEditText;
    private ArtistArrayAdapter artistArrayAdapter;
    private ArrayList<Artist> listOfArtists;
    private SpotifyApi api;
    private SpotifyService spotify;
    private ListView listView;

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MainArtistActivityFragment() {
        api = new SpotifyApi();
        spotify = api.getService();
        listOfArtists = new ArrayList<Artist>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(getString(R.string
                .saved_before_rotation)))
            listOfArtists = savedInstanceState.getParcelableArrayList(getString(R.string.saved_before_rotation));
        setListAdapter(new ArrayAdapter<Artist>(
                getActivity(),
                R.layout.list_item_artists,
                R.id.list_item_tracks_id_textview,
                listOfArtists
                ));

        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
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
                    }
                }
                return false;
            }
        });

        listView = (ListView) rootView.findViewById(R.id.listview_artists);
        artistArrayAdapter = new ArtistArrayAdapter(getActivity(), listOfArtists);
        listView.setAdapter(artistArrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent topTracksIntent = new Intent(getActivity(), TopTracksActivity.class);
                topTracksIntent.putExtra(getString(R.string.artist_id_key), artistArrayAdapter
                        .getItem(position).getArtistID());
                topTracksIntent.putExtra(getString(R.string.artist_name_key), artistArrayAdapter
                        .getItem(position).getArtistName());
                startActivity(topTracksIntent);
            }
        });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(getString(R.string.saved_before_rotation), listOfArtists);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    public class FetchArtistTask extends AsyncTask<String, Void, List<Artist>> {
        private final String LOG_TAG = MainArtistActivity.class.getSimpleName();
        List<kaaes.spotify.webapi.android.models.Artist> spotifyArtistList;

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
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
                listOfArtists.clear();
                ArtistsPager results = spotify.searchArtists(params[0]);
                if (null != results) {
                    spotifyArtistList = results.artists.items;
                    if (null != spotifyArtistList && !spotifyArtistList.isEmpty()) {
                        int size = spotifyArtistList.size();
                        String currentArtistName;
                        String currentArtistURL;
                        String currentArtistID;
                        for (int i = 0; i < size; i++) {
                            currentArtistName = spotifyArtistList.get(i).name;
                            currentArtistURL = null != spotifyArtistList.get(i).images &&
                                    !spotifyArtistList.get(i).images.isEmpty() && null
                                    != spotifyArtistList
                                    .get(i).images.get(0) ? spotifyArtistList
                                    .get(i).images.get(0).url : "";
                            currentArtistID = spotifyArtistList.get(i).id;
                            listOfArtists.add(new Artist(currentArtistName, currentArtistURL,
                                    currentArtistID));
                        }
                    }
                }
            }
            catch(RetrofitError rfe){
                Log.d(LOG_TAG, rfe.getMessage());
            }
            return listOfArtists;
        }

        @Override
        protected void onPostExecute(List<Artist> artistResult) {
            if (null != artistResult) {
                //artistArrayAdapter.clear();
                // if no artist was found then display message
                if (artistResult.isEmpty())
                    Toast.makeText(getActivity(), getString(R.string.no_results),
                            (Toast.LENGTH_LONG)).show();
                else
                    artistArrayAdapter.addAll(artistResult);
            }
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        if (null != listView){
            listView.setChoiceMode(activateOnItemClick
                    ? ListView.CHOICE_MODE_SINGLE
                    : ListView.CHOICE_MODE_NONE);
        }
    }

    private void setActivatedPosition(int position) {
        if (null != listView) {
            if (position == ListView.INVALID_POSITION) {
                listView.setItemChecked(mActivatedPosition, false);
            } else {
                listView.setItemChecked(position, true);
            }
        }
        mActivatedPosition = position;
    }
}
