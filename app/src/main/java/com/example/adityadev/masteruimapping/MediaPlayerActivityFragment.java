package com.example.adityadev.masteruimapping;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
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

import com.example.adityadev.masteruimapping.toptracks.Tracks;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class MediaPlayerActivityFragment extends DialogFragment implements MediaService.MediaPlayerControlInterface {

    private static final int IMG_SIZE = 600;
    private static final int MILLIS = 1000;
    private static final int SECS = 60;
    private static final long SEEK_BAR_UPDATE_INTERVAL = 500;
    private String previewUrl;
    public List<Tracks> listOfTracks;
    private Tracks currentTrack;
    private int position;
    private int trackPosition;
    private int trackDuration = 0;
    private boolean isTrackPlaying = false;
    private int currentPosition;
    private boolean isServiceOn = false;
    private View rootView;
    private TextView artistName_tv;
    private TextView albumName_tv;
    private TextView mediaTrackName_tv;
    private ImageView albumImage_iv;
    private ImageButton prevTrack_ib;
    private ImageButton playPauseTrack_ib;
    private ImageButton nextTrack_ib;
    private TextView elapsedTime_tv;
    private TextView trackDuration_tv;
    private SeekBar seekBar;
    private ToggleButton notificationsToggleMenu;

    public MediaPlayerActivityFragment() {
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null == savedInstanceState || !savedInstanceState.containsKey(getString(R.string.track_list_key))) {
        }
            /*if (null != getArguments() && getArguments().containsKey(KEY_TRACK_LIST) && getArguments().containsKey(KEY_TRACK_NUMBER)) {
            trackList = getArguments().getParcelableArrayList(KEY_TRACK_LIST);
            currentTrackPosition = getArguments().getInt(KEY_TRACK_NUMBER);*/

        Intent mediaServiceIntent = new Intent(getActivity(), MediaService.class);
        mediaServiceIntent.putParcelableArrayListExtra(getString(R.string.track_list_key), (ArrayList<? extends Parcelable>) listOfTracks);
        mediaServiceIntent.putExtra(getString(R.string.track_position_key), position);
        mediaServiceIntent.setAction(getString(R.string.ACTION_NOW_PLAYING));
        getActivity().startService(mediaServiceIntent);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    /**
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
         * Callback for when a track has been started.
         */
        public void onSelectedTrackCompleted();
    }

    /**
     * A dummy implementation of the {@link MediaPlayerCallbacksInterface} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static MediaPlayerCallbacksInterface sDummyMediaPlayerCallbacksInterface = new MediaPlayerCallbacksInterface() {
        @Override
        public void onSelectedTrackStarted(String externalURL) {

        }

        @Override
        public void onSelectedTrackCompleted() {

        }
    };

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private MediaPlayerCallbacksInterface mMediaPlayerCallbacksInterface = sDummyMediaPlayerCallbacksInterface;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof MediaPlayerCallbacksInterface)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }
        mMediaPlayerCallbacksInterface = (MediaPlayerCallbacksInterface) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMediaPlayerCallbacksInterface = sDummyMediaPlayerCallbacksInterface;
    }


    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putParcelableArrayList(getString(R.string.track_list_key), (ArrayList<? extends Parcelable>) listOfTracks);
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
                        listOfTracks = baseIntent.getParcelableArrayListExtra(getString(R.string.track_list_key));
                        currentPosition = baseIntent.getIntExtra(getString(R.string.track_position_key), -1);
                        currentTrack = listOfTracks.get(currentPosition);
                        Intent startServiceIntent = new Intent(getActivity(), MediaService.class);
                        startServiceIntent.putParcelableArrayListExtra(getString(R.string.track_list_key), (ArrayList<? extends Parcelable>) listOfTracks);
                        startServiceIntent.putExtra(getString(R.string.track_position_key), currentPosition);
                        startServiceIntent.setAction(getString(R.string.action_play));
                        getActivity().startService(startServiceIntent);
                    }
                } else {
                    playPauseTrack_ib.setImageResource(android.R.drawable.ic_media_play);
                }
                Intent startServiceIntent = new Intent(getActivity(), MediaService.class);
                startServiceIntent.setAction(getString(R.string.action_play));
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
                    listOfTracks = intent.getParcelableArrayListExtra(getString(R.string.track_list_key));
                    currentPosition = intent.getIntExtra(getString(R.string.track_position_key), -1);
                    Intent startServiceIntent = new Intent(getActivity(), MediaService.class);
                    startServiceIntent.putParcelableArrayListExtra(getString(R.string.track_list_key), (ArrayList<? extends Parcelable>) listOfTracks);
                    startServiceIntent.putExtra(getString(R.string.track_position_key), currentPosition);
                    startServiceIntent.putExtra(getString(R.string.Seekbar_Current_Position), seekBar.getProgress());
                    startServiceIntent.setAction(getString(R.string.action_play));
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
            if (savedInstanceState == null || !savedInstanceState.containsKey(getString(R.string.track_list_key))) {
                Intent intent = getActivity().getIntent();
                listOfTracks = intent.getParcelableArrayListExtra(getString(R.string.track_list_key));
                currentPosition = intent.getIntExtra(getString(R.string.track_position_key), -1);
                Intent startServiceIntent = new Intent(getActivity(), MediaService.class);
                startServiceIntent.putParcelableArrayListExtra(getString(R.string.track_list_key), (ArrayList<? extends Parcelable>) listOfTracks);
                startServiceIntent.putExtra(getString(R.string.track_position_key), currentPosition);
                startServiceIntent.setAction(getString(R.string.action_play));
                getActivity().startService(startServiceIntent);
            } else {
                listOfTracks = savedInstanceState.getParcelableArrayList(getString(R.string.track_list_key));
                currentPosition = savedInstanceState.getInt(getString(R.string.track_number_key), -1);
                currentTrack = savedInstanceState.getParcelable(getString(R.string.current_track_key));
            }

            if (null != listOfTracks && listOfTracks.size() > 0) {
                currentTrack = listOfTracks.get(currentPosition);
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
        this.listOfTracks = trackList;
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
