package com.example.adityadev.masteruimapping;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.example.adityadev.masteruimapping.artistsmodel.Artist;
import com.example.adityadev.masteruimapping.toptracks.Tracks;

import java.util.ArrayList;
import java.util.List;


/**
 * An activity representing a list of Artists. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link TopTracksActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link MainArtistActivityFragment} and the item details
 * (if present) is a {@link TopTracksActivityFragment}.
 * <p/>
 */
public class MainArtistActivity extends AppCompatActivity
        implements MainArtistActivityFragment.ArtistCallbacksInterface,
        TopTracksActivityFragment.TopTracksCallbacksInterface,
        MediaPlayerCallbacksInterface {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    protected boolean mTwoPane;
    protected static Bundle arguments;
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private MediaPlayerDialog mediaPlayerDialog;
    private BroadcastReceiver broadcastReceiver;
    private String externalURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_artist);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getString(R.string.ACTION_NOW_PLAYING));
        intentFilter.addAction(getString(R.string.MISC_ACTION));

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(getString(R.string.ACTION_NOW_PLAYING))){

                }
            }
        };

        if (null != findViewById(R.id.frame_top_tracks)) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

        }
        getSupportActionBar().setTitle(getString(R.string.app_name));

        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link MainArtistActivityFragment.ArtistCallbacksInterface}
     * indicating that the item with the given ID was selected.
     */
    public void onArtistSelected(Artist selectedArtist) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail topTracksActivityFragment using a
            // topTracksActivityFragment transaction.
            Bundle arguments = new Bundle();
            arguments.putParcelable(getString(R.string.artist_id), selectedArtist);
            TopTracksActivityFragment topTracksActivityFragment = new TopTracksActivityFragment();
            topTracksActivityFragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.frame_top_tracks, topTracksActivityFragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent topTracksIntent = new Intent(this, TopTracksActivity.class);
            topTracksIntent.putExtra(getString(R.string.artist_id), selectedArtist);
            startActivity(topTracksIntent);
        }
    }

    @Override
    public void onTopTrackSelected(List<Tracks> listOfTracksForPlayer, int selectedTrackPosition) {
        if (mTwoPane) {
            mediaPlayerDialog = new MediaPlayerDialog();
            Bundle arguments = new Bundle();
            arguments.putParcelableArrayList(getString(R.string.track_list_key), (ArrayList<? extends Parcelable>) listOfTracksForPlayer);
            arguments.putInt(getString(R.string.track_position_key), selectedTrackPosition);
            mediaPlayerDialog.setArguments(arguments);
            MediaService.setMediaControlInterfaceObj(mediaPlayerDialog);
            mediaPlayerDialog.show(fragmentManager, getString(R.string.fragment_media_player));

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent mediaPlayerIntent = new Intent(this, MediaPlayerActivity.class);
            mediaPlayerIntent.putParcelableArrayListExtra(getString(R.string.track_list_key), (ArrayList<? extends Parcelable>) listOfTracksForPlayer);
            mediaPlayerIntent.putExtra(getString(R.string.track_position_key), selectedTrackPosition);
            startActivity(mediaPlayerIntent);
        }
    }

    @Override
    public void onSelectedTrackStarted(String externalURL) {
        this.externalURL = externalURL;
    }

    @Override
    public void onSelectedTrackCompleted() {
        mediaPlayerDialog.dismiss();
        MediaService.unsetTrackEventListenerInterface();

        this.externalURL = "";
    }
}
