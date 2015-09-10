package com.example.adityadev.masteruimapping;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.example.adityadev.masteruimapping.artistsmodel.Artist;
import com.example.adityadev.masteruimapping.toptracks.Tracks;


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
public class MainArtistActivity extends FragmentActivity
        implements MainArtistActivityFragment.artistCallbacksInterface,
        TopTracksActivityFragment.topTracksCallbacksInterface {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    protected boolean mTwoPane;
    protected static Bundle arguments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_artist);

        if (null != findViewById(R.id.frame_top_tracks)) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link MainArtistActivityFragment.artistCallbacksInterface}
     * indicating that the item with the given ID was selected.
     */
    public void onArtistSelected(Artist selectedArtist) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail topTracksActivityFragment using a
            // topTracksActivityFragment transaction.
            Bundle arguments = new Bundle();
            arguments.putParcelable(TopTracksActivityFragment.ARTIST_ID, selectedArtist);
            TopTracksActivityFragment topTracksActivityFragment = new TopTracksActivityFragment();
            topTracksActivityFragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.frame_top_tracks, topTracksActivityFragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, TopTracksActivity.class);
            detailIntent.putExtra(TopTracksActivityFragment.ARTIST_ID, selectedArtist);
            startActivity(detailIntent);
        }
    }

    @Override
    public void onTopTrackSelected(Tracks selectedTrack) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail topTracksActivityFragment using a
            // topTracksActivityFragment transaction.
            Bundle arguments = new Bundle();
            arguments.putParcelable(TopTracksActivityFragment.TRACK_ID, selectedTrack);
            MediaPlayerActivity mediaPlayerActivity = new MediaPlayerActivity();
            //mediaPlayerActivity.setArguments(arguments);
//            getFragmentManager().beginTransaction()
//                    .replace(R.id.frame_top_tracks, mediaPlayerActivity)
//                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, MediaPlayerActivity.class);
            detailIntent.putExtra(TopTracksActivityFragment.TRACK_ID, selectedTrack);
            startActivity(detailIntent);
        }
    }
}
