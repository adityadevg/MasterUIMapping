package com.example.adityadev.masteruimapping;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.adityadevg.spotifystreamer.toptracks.Tracks;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class MediaPlayerActivityFragment extends Fragment implements MediaService.MediaPlayerControlInterface {

    private static final int IMG_SIZE = 600;
    private static final int MILLIS = 1000;
    private static final int SECS = 60;
    private static final long SEEK_BAR_UPDATE_INTERVAL = 500;
    private String previewUrl;
    public List<Tracks> tracksList;
    private Tracks currentTrack;
    private int position;
    private int trackPosition;
    private int trackDuration = 0;
    private boolean isTrackPlaying = false;
    private int currentPosition;
    private boolean isServiceOn = false;

    public MediaPlayerActivityFragment() {
        setHasOptionsMenu(true);
    }

    private View rootView;
    TextView artistName_tv;
    TextView albumName_tv;
    TextView mediaTrackName_tv;
    ImageView albumImage_iv;
    ImageButton prevTrack_ib;
    ImageButton playPauseTrack_ib;
    ImageButton nextTrack_ib;
    TextView elapsedTime_tv;
    TextView trackDuration_tv;
    SeekBar seekBar;
    ToggleButton notificationsToggleMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
/*
        super.onCreate(savedInstanceState);
        Intent baseIntent = getActivity().getIntent();
        tracksList = baseIntent.getParcelableArrayListExtra(getString(R.string.track_list_key));
        position = baseIntent.getIntExtra(getString(R.string.track_position_key), -1);

        Intent mediaServiceIntent = new Intent(getActivity(), MediaService.class);
        mediaServiceIntent.setAction(MediaService.ACTION_PLAY);
        mediaServiceIntent.putParcelableArrayListExtra(getString(R.string.track_list_key), (ArrayList<? extends Parcelable>) this.tracksList);
        mediaServiceIntent.putExtra(getString(R.string.track_position_key), this.position);
        getActivity().startService(mediaServiceIntent);*/

        super.onCreate(savedInstanceState);
        Intent mediaServiceIntent = new Intent(getActivity(), MediaService.class);
        mediaServiceIntent.putParcelableArrayListExtra(getString(R.string.track_list_key), (ArrayList<? extends Parcelable>) tracksList);
        mediaServiceIntent.putExtra(getString(R.string.track_position_key), position);
        mediaServiceIntent.setAction(getString(R.string.ACTION_NOW_PLAYING));
        getActivity().startService(mediaServiceIntent);

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putParcelableArrayList(getString(R.string.tracklist_parcel_key), (ArrayList<? extends Parcelable>) tracksList);
        bundle.putInt(getString(R.string.track_number_key), currentPosition);
        bundle.putParcelable(getString(R.string.current_track_key), currentTrack);
        super.onSaveInstanceState(bundle);
    }

    private Intent createPreviewShareIntent() {
        Intent previewShareIntent = new Intent(Intent.ACTION_SEND);
        previewShareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        previewShareIntent.setType(getString(R.string.text_plain));
        previewShareIntent.putExtra(Intent.EXTRA_TEXT, currentTrack.getExternalSpotifyLink());
        Log.i("External Spotify Link: ", currentTrack.getExternalSpotifyLink());
        return previewShareIntent;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem shareMenuItem = menu.findItem(R.id.action_share);

        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider((shareMenuItem));
        if (null != mShareActionProvider) {
            mShareActionProvider.setShareIntent(createPreviewShareIntent());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_media, container, false);

        albumName_tv = (TextView) rootView.findViewById(R.id.albumNameTextView);
        mediaTrackName_tv = (TextView) rootView.findViewById(R.id.mediaTrackNameTextView);
        artistName_tv = (TextView) rootView.findViewById(R.id.artistNameTextView);
        albumImage_iv = (ImageView) rootView.findViewById(R.id.albumArtImageView);
        prevTrack_ib = (ImageButton) rootView.findViewById(R.id.prevTrackBtn);
        playPauseTrack_ib = (ImageButton) rootView.findViewById(R.id.pauseTrackBtn);
        nextTrack_ib = (ImageButton) rootView.findViewById(R.id.nextTrackBtn);
        seekBar = (SeekBar) rootView.findViewById(R.id.mediaSeekBar);
        elapsedTime_tv = (TextView) rootView.findViewById(R.id.elapsedTimeTextView);
        trackDuration_tv = (TextView) rootView.findViewById(R.id.trackDurationTextView);

        notificationsToggleMenu = (ToggleButton) inflater.inflate(R.layout.toggle_switch, container, false).findViewById(R.id.switchForActionBar);
        notificationsToggleMenu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast.makeText(getActivity(), "Hi", Toast.LENGTH_LONG).show();
            }
        });

        playPauseTrack_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTrackPlaying) {
                    playPauseTrack_ib.setImageResource(android.R.drawable.ic_media_pause);
                    if (trackPosition == trackDuration && trackPosition > 0) {
                        Intent baseIntent = getActivity().getIntent();
                        tracksList = baseIntent.getParcelableArrayListExtra(getString(R.string.track_list_key));
                        currentPosition = baseIntent.getIntExtra(getString(R.string.track_position_key), -1);
                        currentTrack = tracksList.get(currentPosition);
                        Intent startServiceIntent = new Intent(getActivity(), MediaService.class);
                        startServiceIntent.putParcelableArrayListExtra(getString(R.string.track_list_key), (ArrayList<? extends Parcelable>) tracksList);
                        startServiceIntent.putExtra(getString(R.string.track_position_key), currentPosition);
                        startServiceIntent.setAction(MediaService.ACTION_PLAY);
                        getActivity().startService(startServiceIntent);
                    }
                } else {
                    playPauseTrack_ib.setImageResource(android.R.drawable.ic_media_play);
                }
                Intent startServiceIntent = new Intent(getActivity(), MediaService.class);
                startServiceIntent.setAction(MediaService.ACTION_PLAY);
                getActivity().startService(startServiceIntent);
            }
        });

        prevTrack_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startServiceIntent = new Intent(getActivity(), MediaService.class);
                startServiceIntent.setAction(getString(R.string.ACTION_PREV));
                getActivity().startService(startServiceIntent);
            }
        });

        nextTrack_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startServiceIntent = new Intent(getActivity(), MediaService.class);
                startServiceIntent.setAction(getString(R.string.ACTION_NEXT));
                getActivity().startService(startServiceIntent);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // if the song is finished playing
                if (trackPosition == trackDuration && trackPosition > 0) {
                    Intent intent = getActivity().getIntent();
                    tracksList = intent.getParcelableArrayListExtra(getString(R.string.track_list_key));
                    currentPosition = intent.getIntExtra(getString(R.string.track_position_key), -1);
                    Intent startServiceIntent = new Intent(getActivity(), MediaService.class);
                    startServiceIntent.putParcelableArrayListExtra(getString(R.string.track_list_key), (ArrayList<? extends Parcelable>) tracksList);
                    startServiceIntent.putExtra(getString(R.string.track_position_key), currentPosition);
                    startServiceIntent.putExtra(getString(R.string.Seekbar_Current_Position), seekBar.getProgress());
                    startServiceIntent.setAction(MediaService.ACTION_PLAY);
                    getActivity().startService(startServiceIntent);

                } else {
                    Intent startServiceIntent = new Intent(getActivity(), MediaService.class);
                    startServiceIntent.putExtra(getString(R.string.Seekbar_Current_Position), seekBar.getProgress());
                    startServiceIntent.setAction(getString(R.string.ACTION_SEEKBAR_POS_CHANGED));
                    getActivity().startService(startServiceIntent);
                }
            }
        });

        if (!isServiceOn) {
            if (savedInstanceState == null || !savedInstanceState.containsKey(getString(R.string.tracklist_parcel_key))) {
                Intent intent = getActivity().getIntent();
                tracksList = intent.getParcelableArrayListExtra(getString(R.string.track_list_key));
                currentPosition = intent.getIntExtra(getString(R.string.track_position_key), -1);
                Intent startServiceIntent = new Intent(getActivity(), MediaService.class);
                startServiceIntent.putParcelableArrayListExtra(getString(R.string.track_list_key), (ArrayList<? extends Parcelable>) tracksList);
                startServiceIntent.putExtra(getString(R.string.track_position_key), currentPosition);
                startServiceIntent.setAction(MediaService.ACTION_PLAY);
                getActivity().startService(startServiceIntent);
            } else {
                tracksList = savedInstanceState.getParcelableArrayList(getString(R.string.tracklist_parcel_key));
                currentPosition = savedInstanceState.getInt(getString(R.string.track_number_key), -1);
                currentTrack = savedInstanceState.getParcelable(getString(R.string.current_track_key));
            }

            if (null != tracksList && tracksList.size() > 0) {
                currentTrack = tracksList.get(currentPosition);
                if (null != currentTrack) {
                    artistName_tv.setText(currentTrack.getArtistName());
                    albumName_tv.setText(currentTrack.getAlbumName());
                    mediaTrackName_tv.setText(currentTrack.getTrackName());
                    previewUrl = currentTrack.getPreviewUrl();
                    if (null != currentTrack.getAlbumImageURL() && !currentTrack.getAlbumImageURL().isEmpty())
                        Picasso.with(getActivity()).load(currentTrack.getAlbumImageURL()).resize(IMG_SIZE, IMG_SIZE).centerCrop().into(albumImage_iv);
                }
            }

        } else {
            refreshScreen();
        }

        return rootView;
    }

    private void refreshScreen() {
        if ((null != currentTrack.getAlbumImageURL()) && (!currentTrack.getAlbumImageURL().isEmpty())) {
            Picasso.with(getActivity()).load(currentTrack.getAlbumImageURL()).resize(IMG_SIZE, IMG_SIZE).centerInside().into(albumImage_iv);
        }
    }

    @Override
    public void onPreviousTrack() {
        refreshScreen();
    }

    @Override
    public void onNextTrack() {
        refreshScreen();
    }

    @Override
    public void onTrackPaused() {
        isTrackPlaying = false;
    }

    @Override
    public void onTrackResumed() {
        isTrackPlaying = true;
    }

    @Override
    public void onTrackCompleted() {
        trackPosition = trackDuration;
        playPauseTrack_ib.setImageResource(android.R.drawable.ic_media_play);
        sendElapsedTime(trackPosition);
    }

    @Override
    public void sendTrackDuration(int duration) {
        trackDuration = duration;
        seekBar.setMax(duration);
        if (isAdded())
            trackDuration_tv.setText(getDurationInMinutes(duration));
    }


    private String getDurationInMinutes(int duration) {

        int durationInSeconds = duration / MILLIS;
        int minutes = durationInSeconds / SECS;
        int seconds = durationInSeconds % SECS;
        String time = String.valueOf(minutes) + " : " + String.valueOf(seconds);
        return time;
    }

    @Override
    public void sendElapsedTime(int elapsedTime) {
        trackPosition = position;
        seekBar.setProgress(trackPosition);
        elapsedTime_tv.setText(getDurationInMinutes(trackPosition));
        refreshScreen();
    }

    @Override
    public void sendTrackNo(int trackNo) {
        position = trackNo;
    }

    @Override
    public void sendTrackList(List<Tracks> trackList) {
        this.tracksList = trackList;
    }

    @Override
    public void isSpotifyPlayerOn(boolean isOn) {
        isServiceOn = isOn;
    }

    @Override
    public void onTrackStarted() {
        isTrackPlaying = true;
        playPauseTrack_ib.setImageResource(android.R.drawable.ic_media_pause);
        new RefreshSeekBarTask().execute();
    }

    @Override
    public void isSpotifyPlayerPaused(boolean isPaused) {
        isTrackPlaying = !isPaused;
    }

    @Override
    public void nowPlayingStateUpdated() {
        if (isServiceOn) {
            refreshScreen();
        } else {
            Toast.makeText(getActivity(), getString(R.string.no_song_playing), Toast.LENGTH_SHORT).show();
        }
    }


    public class RefreshSeekBarTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            if (isAdded()) {
                Intent startServiceIntent = new Intent(getActivity(), MediaService.class);
                while (trackPosition <= trackDuration && isAdded()) {
                    try {
                        Thread.sleep(SEEK_BAR_UPDATE_INTERVAL);
                        // set action play
                        if (null != startServiceIntent && isAdded()) {
                            startServiceIntent.setAction(getString(R.string.ACTION_SEEKBAR_PROGRESS));
                            if (null != getActivity())
                                getActivity().startService(startServiceIntent);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
    }
}
