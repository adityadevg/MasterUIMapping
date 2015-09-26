package com.example.adityadev.spotifystreamermasterui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.adityadev.spotifystreamermasterui.artistsmodel.Artist;
import com.example.adityadev.spotifystreamermasterui.toptracks.Tracks;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a single Artist detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link MainArtistActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link TopTracksActivityFragment}.
 */
public class TopTracksActivity extends AppCompatActivity implements TopTracksActivityFragment.TopTracksCallbacksInterface{

    private Artist selectedArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        selectedArtist = getIntent().getParcelableExtra(getString(R.string.artist_key));
        if (null != selectedArtist){
            getSupportActionBar().setTitle(getString(R.string.title_activity_top_tracks));
            getSupportActionBar().setTitle(selectedArtist.getArtistName());
        }

        // savedInstanceState is non-null when there is topTracksActivityFragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the topTracksActivityFragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (null == savedInstanceState) {
            // Create the detail topTracksActivityFragment and add it to the activity
            // using a topTracksActivityFragment transaction.
            Bundle arguments = new Bundle();
            arguments.putParcelable(getString(R.string.artist_key),
                    getIntent().getParcelableExtra(getString(R.string.artist_key)));
            TopTracksActivityFragment fragment = new TopTracksActivityFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .add(R.id.frame_top_tracks, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, MainArtistActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTopTrackSelected(List<Tracks> listOfTracksForPlayer, int selectedTrackPosition) {
        Intent mediaPlayerIntent = new Intent(this, MediaPlayerActivity.class);
        mediaPlayerIntent.putParcelableArrayListExtra(getString(R.string.track_list_key), (ArrayList<? extends Parcelable>) listOfTracksForPlayer);
        mediaPlayerIntent.putExtra(getString(R.string.track_position_key), selectedTrackPosition);
        startActivity(mediaPlayerIntent);    }
}
