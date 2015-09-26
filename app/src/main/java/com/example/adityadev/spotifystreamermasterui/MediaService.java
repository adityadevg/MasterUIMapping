package com.example.adityadev.spotifystreamermasterui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.adityadev.spotifystreamermasterui.toptracks.Tracks;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaService extends Service
        implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private static final String LOG_TAG = MediaService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 7;

    //media player
    private MediaPlayer spotifyPlayer;

    //Track preview URL
    String musicUrl;

    //Track name
    String currentTrackName = "";

    int currentPositionInTrack;

    //song list
    public static List<Tracks> listOfTracks;
    //current position
    private int songPosn;
    public static Tracks currentTrack;
    private int position;
    RemoteViews remoteNotificationView;

    private NotificationManager notifManager = null;
    private Notification notif;

    private BroadcastReceiver broadcastReceiver = null;
    private SharedPreferences sharedPreferences = null;
    private int isNotifControlVisible = Integer.MIN_VALUE;
    private NotificationCompat.Builder notifCompatBuilder;
    private PendingIntent pendingListIntent;
    Intent baseIntent;

    public MediaService() {
        listOfTracks = new ArrayList<Tracks>();
    }


    public interface MediaPlayerControlInterface {
        public void onPreviousTrack();

        public void onNextTrack();

        public void onTrackPaused();

        public void onTrackResumed();

        public void onTrackCompleted();

        public void setTrackDuration(int duration);

        public void setElapsedTime(int elapsedTime);

        public void setTrackNo(int trackNo);

        public void setTrackList(List<Tracks> trackList);

        public void isSpotifyPlayerOn(boolean isOn);

        public void onTrackStarted();

        public void isSpotifyPlayerPaused(boolean isPaused);

        public void nowPlayingStateUpdated();
    }

    public static MediaPlayerControlInterface dummyMediaPlayerControlInterfaceObj = new MediaPlayerControlInterface() {
        @Override
        public void onPreviousTrack() {

        }

        @Override
        public void onNextTrack() {

        }

        @Override
        public void onTrackPaused() {

        }

        @Override
        public void onTrackResumed() {

        }

        @Override
        public void onTrackCompleted() {

        }

        @Override
        public void setTrackDuration(int duration) {

        }

        @Override
        public void setElapsedTime(int elapsedTime) {

        }

        @Override
        public void setTrackNo(int trackNo) {

        }

        @Override
        public void setTrackList(List<Tracks> trackList) {

        }

        @Override
        public void isSpotifyPlayerOn(boolean isOn) {

        }

        @Override
        public void onTrackStarted() {

        }

        @Override
        public void isSpotifyPlayerPaused(boolean isPaused) {

        }

        @Override
        public void nowPlayingStateUpdated() {

        }
    };

    public static MediaPlayerControlInterface mediaPlayerControlInterfaceObj = dummyMediaPlayerControlInterfaceObj;

    public static void setMediaControlInterfaceObj(MediaPlayerControlInterface localMediaPlayerControlInterfaceObj) {
        mediaPlayerControlInterfaceObj = localMediaPlayerControlInterfaceObj;
    }

    public static void unsetMediaControlInterfaceObj() {
        mediaPlayerControlInterfaceObj = dummyMediaPlayerControlInterfaceObj;
    }


    public interface TrackEventListenerInterface {
        public void onTrackCompleted();

        public void onTrackStarted(String spotifyExternalURL);
    }

    private static TrackEventListenerInterface dummyTrackEventListenerObj = new TrackEventListenerInterface() {
        @Override
        public void onTrackCompleted() {

        }

        @Override
        public void onTrackStarted(String spotifyExternalURL) {

        }
    };

    private static TrackEventListenerInterface mTrackEventListenerObj = dummyTrackEventListenerObj;

    public static void setTrackEventListenerInterface(TrackEventListenerInterface trackEventListenerInterfaceObj) {
        mTrackEventListenerObj = trackEventListenerInterfaceObj;

    }

    public static void unsetTrackEventListenerInterface() {
        mTrackEventListenerObj = dummyTrackEventListenerObj;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        remoteNotificationView = new RemoteViews(getPackageName(), R.layout.notification_layout);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context baseContext, Intent baseIntent) {
                if (baseIntent.getAction().equals(getString(R.string.lock_screen_controls))) {
                    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    if (sharedPreferences.getBoolean(getString(R.string.lock_screen_controls), true)) {
                        isNotifControlVisible = Notification.VISIBILITY_PUBLIC;
                    } else {
                        isNotifControlVisible = Notification.VISIBILITY_PRIVATE;
                    }
                    notifCompatBuilder.setVisibility(isNotifControlVisible);
                    notifManager.notify(NOTIFICATION_ID, notif);
                }
            }
        };
    }

    public void initSpotifyPlayer() {
        spotifyPlayer = new MediaPlayer();
        spotifyPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        spotifyPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        spotifyPlayer.setOnPreparedListener(this);
        spotifyPlayer.setOnCompletionListener(this);
        spotifyPlayer.setOnErrorListener(this);
    }

    @Override
    public int onStartCommand(Intent baseIntent, int flags, int startId) {
        this.baseIntent = baseIntent;

        Intent notifIntent = new Intent(this, MediaPlayerActivity.class);
        Intent prevIntent = new Intent(this, MediaService.class);
        Intent playIntent = new Intent(this, MediaService.class);
        Intent nextIntent = new Intent(this, MediaService.class);

        prevIntent.setAction(getString(R.string.ACTION_PREV));
        playIntent.setAction(getString(R.string.ACTION_PLAY_PAUSE));
        nextIntent.setAction(getString(R.string.ACTION_NEXT));

        pendingListIntent = PendingIntent.getActivity(this, 0, notifIntent, 0);
        PendingIntent pendingPrevListIntent = PendingIntent.getService(this, 0, prevIntent, 0);
        PendingIntent pendingPlayIntent = PendingIntent.getService(this, 0, playIntent, 0);
        PendingIntent pendingNextListIntent = PendingIntent.getService(this, 0, nextIntent, 0);


        if (null != baseIntent && baseIntent.getAction().equals(getString(R.string.ACTION_PLAY))) {

            if (null == listOfTracks || listOfTracks.isEmpty()) {
                listOfTracks = baseIntent.getParcelableArrayListExtra(getString(R.string.track_list_key));
            }
            position = baseIntent.getIntExtra(getString(R.string.track_position_key), -1);
            currentPositionInTrack = baseIntent.getIntExtra(getString(R.string.elapsed_time), 0);

            if (position > -1 && null != listOfTracks && listOfTracks.size() > 0) {
                currentTrack = listOfTracks.get(position);
                if (null != currentTrack) {
                    musicUrl = currentTrack.getPreviewUrl();
                    currentTrackName = currentTrack.getTrackName();
                }
            } else {
                Log.d(LOG_TAG, "Empty current track");
            }
            remoteNotificationView.setImageViewResource(R.id.play_btn, android.R.drawable.ic_media_pause);

            if (null == spotifyPlayer) {
                initSpotifyPlayer();
                if (null != musicUrl && !musicUrl.isEmpty()) {
                    if (spotifyPlayer.isPlaying()) {
                        spotifyPlayer.stop();
                    }
                    spotifyPlayer.reset();
                    startStreamingMusic(musicUrl);

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    if (sharedPreferences.getBoolean(getString(R.string.lock_screen_controls), true)) {
                        isNotifControlVisible = Notification.VISIBILITY_PUBLIC;
                    } else {
                        isNotifControlVisible = Notification.VISIBILITY_PRIVATE;
                    }

                    notifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                    builder.setSmallIcon(R.drawable.notification_template_icon_bg)
                            .setContentIntent(pendingListIntent)
                            .setContent(remoteNotificationView)
                            .setOngoing(true)
                            .setVisibility(isNotifControlVisible);
                    notif = builder.build();
                    startForeground(NOTIFICATION_ID, notif);

                    remoteNotificationView.setOnClickPendingIntent(R.id.prev_btn, pendingPrevListIntent);
                    remoteNotificationView.setOnClickPendingIntent(R.id.play_btn, pendingPlayIntent);
                    remoteNotificationView.setOnClickPendingIntent(R.id.next_btn, pendingNextListIntent);
                }
            } else {
                if (null != musicUrl && !musicUrl.isEmpty()) {
                    if (spotifyPlayer.isPlaying()) {
                        spotifyPlayer.stop();
                    }
                    spotifyPlayer.reset();
                    try {
                        spotifyPlayer.setDataSource(musicUrl);
                    } catch (IOException ie) {
                        Log.i(LOG_TAG, ie.getMessage());
                    }
                    startStreamingMusic(musicUrl);
                }
            }
        } else if (null != baseIntent && baseIntent.getAction().equals(getString(R.string.ACTION_PREV))) {
            if (spotifyPlayer.isPlaying()) {
                spotifyPlayer.stop();
            }
            remoteNotificationView.setImageViewResource(R.id.play_btn, android.R.drawable.ic_media_pause);

            notifManager.notify(NOTIFICATION_ID, notif);
            if (position > 0 && null != listOfTracks && listOfTracks.size() > 0) {
                position--;
                currentTrack = listOfTracks.get(position);
                musicUrl = currentTrack.getPreviewUrl();
                startStreamingMusic(musicUrl);

                if (null != mediaPlayerControlInterfaceObj) {
                    mediaPlayerControlInterfaceObj.onPreviousTrack();
                    mediaPlayerControlInterfaceObj.setTrackNo(position);
                }
            }
        } else if (null != baseIntent && baseIntent.getAction().equals(getString(R.string.ACTION_NEXT))) {
            if (null == spotifyPlayer) {
                initSpotifyPlayer();
            }
            if (spotifyPlayer.isPlaying()) {
                spotifyPlayer.stop();
            }
            remoteNotificationView.setImageViewResource(R.id.play_btn, android.R.drawable.ic_media_pause);
            notifManager.notify(NOTIFICATION_ID, notif);
            if (null != listOfTracks && listOfTracks.size() > 0 && position < listOfTracks.size() - 1) {
                position++;
                currentTrack = listOfTracks.get(position);
                if (null != musicUrl) {
                    musicUrl = currentTrack.getPreviewUrl();
                    startStreamingMusic(musicUrl);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.reached_end_of_playlist), Toast.LENGTH_SHORT).show();
                }

                if (null != mediaPlayerControlInterfaceObj) {
                    mediaPlayerControlInterfaceObj.onNextTrack();
                    mediaPlayerControlInterfaceObj.setTrackNo(position);
                }
            }
        } else if (null != baseIntent && baseIntent.getAction().equals(getString(R.string.ACTION_PLAY_PAUSE))) {
            if (null == spotifyPlayer) {
                initSpotifyPlayer();
            }
            if (spotifyPlayer.isPlaying()) {
                spotifyPlayer.pause();
                remoteNotificationView.setImageViewResource(R.id.play_btn, android.R.drawable.ic_media_play);
                notifManager.notify(NOTIFICATION_ID, notif);
                if (null != mediaPlayerControlInterfaceObj) {
                    mediaPlayerControlInterfaceObj.onTrackPaused();
                }
            } else {
                spotifyPlayer.start();
                remoteNotificationView.setImageViewResource(R.id.play_btn, android.R.drawable.ic_media_pause);
                if (null != mediaPlayerControlInterfaceObj) {
                    mediaPlayerControlInterfaceObj.onTrackResumed();
                }
                notifManager.notify(NOTIFICATION_ID, notif);
            }

        } else if (null != baseIntent && baseIntent.getAction().equals(getString(R.string.ACTION_SEEKBAR_POS_CHANGED))) {
            int seekBarProgress = baseIntent.getIntExtra(getString(R.string.Seekbar_Current_Position), 0);
            if (null != spotifyPlayer) {
                spotifyPlayer.seekTo(seekBarProgress);
                spotifyPlayer.start();

            }
        } else if (null != baseIntent && baseIntent.getAction().equals(getString(R.string.ACTION_SEEKBAR_PROGRESS))) {
            if (null != spotifyPlayer && null != mediaPlayerControlInterfaceObj) {
                mediaPlayerControlInterfaceObj.setElapsedTime(spotifyPlayer.getCurrentPosition());
            }
        } else if (null != baseIntent && baseIntent.getAction().equals(getString(R.string.ACTION_NOW_PLAYING))) {
            if (null != mediaPlayerControlInterfaceObj) {
                if (null != spotifyPlayer) {
                    mediaPlayerControlInterfaceObj.isSpotifyPlayerOn(true);
                    mediaPlayerControlInterfaceObj.setElapsedTime(spotifyPlayer.getCurrentPosition());
                    mediaPlayerControlInterfaceObj.setTrackDuration(spotifyPlayer.getDuration());
                    mediaPlayerControlInterfaceObj.setTrackList(listOfTracks);
                    mediaPlayerControlInterfaceObj.setTrackNo(songPosn);
                    mediaPlayerControlInterfaceObj.isSpotifyPlayerPaused(spotifyPlayer.isPlaying());
                } else {
                    mediaPlayerControlInterfaceObj.isSpotifyPlayerOn(false);
                }
                mediaPlayerControlInterfaceObj.nowPlayingStateUpdated();
            }
        }
        return super.onStartCommand(baseIntent, flags, startId);
    }

    private void startStreamingMusic(String musicUrl) {
        if (null != spotifyPlayer) {
            if (spotifyPlayer.isPlaying()) {
                spotifyPlayer.stop();
            }
            spotifyPlayer.reset();
            try {
                if (!musicUrl.isEmpty()) {
                    spotifyPlayer.setDataSource(musicUrl);
                }
                spotifyPlayer.prepareAsync(); // prepare async to not block main thread
            } catch (IOException e) {
                Log.d(LOG_TAG, e.getMessage());
                Log.d(LOG_TAG, "Exception during setting data source. Music url is not empty");
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCompletion(MediaPlayer spotifyPlayer) {
        if (null != mTrackEventListenerObj)
            mTrackEventListenerObj.onTrackCompleted();
        if (null != mediaPlayerControlInterfaceObj)
            mediaPlayerControlInterfaceObj.onTrackCompleted();
        if (null != this.spotifyPlayer)
            this.spotifyPlayer.release();
        this.spotifyPlayer = null;
        stopForeground(true);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer spotifyPlayer) {
        spotifyPlayer.seekTo(currentPositionInTrack);
        spotifyPlayer.start();
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(getString(R.string.ACTION_NOW_PLAYING));
        broadcastIntent.putExtra(getString(R.string.spotify_external_url), currentTrack.getExternalSpotifyLink());
        broadcastIntent.putParcelableArrayListExtra(getString(R.string.track_list_key), (ArrayList<? extends Parcelable>) listOfTracks);
        sendBroadcast(broadcastIntent);
        if (null != mediaPlayerControlInterfaceObj) {
            mediaPlayerControlInterfaceObj.setTrackDuration(spotifyPlayer.getDuration());
            mediaPlayerControlInterfaceObj.onTrackStarted();
            mediaPlayerControlInterfaceObj.isSpotifyPlayerOn(true);
            mediaPlayerControlInterfaceObj.setElapsedTime(spotifyPlayer.getCurrentPosition());
            mediaPlayerControlInterfaceObj.setTrackDuration(spotifyPlayer.getDuration());
            mediaPlayerControlInterfaceObj.setTrackList(listOfTracks);
            mediaPlayerControlInterfaceObj.setTrackNo(listOfTracks.indexOf(currentTrack));
            mediaPlayerControlInterfaceObj.isSpotifyPlayerPaused(false);
        }
        remoteNotificationView.setImageViewResource(R.id.play_btn, android.R.drawable.ic_media_pause);


        remoteNotificationView.setTextViewText(R.id.trackName_tv, currentTrack.getTrackName());
        Handler notifHandler = new Handler(Looper.getMainLooper());
        if (null == notif) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setSmallIcon(R.drawable.notification_template_icon_bg)
                    .setContentIntent(pendingListIntent)
                    .setContent(remoteNotificationView)
                    .setOngoing(true)
                    .setVisibility(isNotifControlVisible);
            notif = builder.build();
        }
        notifHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!currentTrack.getAlbumImageURL().isEmpty()) {
                    Picasso
                            .with(MediaService.this)
                            .load(currentTrack.getAlbumImageURL())
                            .into(remoteNotificationView, R.id.album_thumbnail_imageview, NOTIFICATION_ID, notif);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        if (null != mediaPlayerControlInterfaceObj) {
            mediaPlayerControlInterfaceObj = dummyMediaPlayerControlInterfaceObj;
        }
        super.onDestroy();
    }
}
