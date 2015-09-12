package com.example.adityadev.masteruimapping;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.adityadev.masteruimapping.toptracks.Tracks;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

public class MediaService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private static final String LOG_TAG = MediaService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 7;

    //media player
    private MediaPlayer spotifyPlayer = null;

    //Track preview URL
    String musicUrl;

    int currentPositionInTrack;

    //song list
    public List<Tracks> tracksList;
    //current position
    private int songPosn;
    private Tracks currentTrack;
    private int position;
    RemoteViews remoteNotificationView;

    NotificationManager notifManager;
    Notification notif;

    public MediaService() {

    }

    public interface MediaPlayerControlInterface {
        public void onPreviousTrack();

        public void onNextTrack();

        public void onTrackPaused();

        public void onTrackResumed();

        public void onTrackCompleted();

        public void sendTrackDuration(int duration);

        public void sendElapsedTime(int elapsedTime);

        public void sendTrackNo(int trackNo);

        public void sendTrackList(List<Tracks> trackList);

        public void isSpotifyPlayerOn(boolean isOn);

        public void onTrackStarted();

        public void isSpotifyPlayerPaused(boolean isPaused);

        public void nowPlayingStateUpdated();
    }

    public static MediaPlayerControlInterface mediaPlayerControlInterfaceObj;

    public static void setMediaControlInterfaceObj(MediaPlayerControlInterface mediaPlayerControlInterface) {
        mediaPlayerControlInterfaceObj = mediaPlayerControlInterface;
    }

    @Override
    public void onCreate() {
        super.onCreate();
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

        Intent notifIntent = new Intent(this, MediaPlayerActivity.class);
        Intent prevIntent = new Intent(this, MediaService.class);
        Intent playIntent = new Intent(this, MediaService.class);
        Intent nextIntent = new Intent(this, MediaService.class);

        prevIntent.setAction(getString(R.string.ACTION_PREV));
        playIntent.setAction(getString(R.string.ACTION_PLAY_PAUSE));
        nextIntent.setAction(getString(R.string.ACTION_NEXT));

        PendingIntent pendingListIntent = PendingIntent.getActivity(this, 0, notifIntent, 0);
        PendingIntent pendingPrevListIntent = PendingIntent.getService(this, 0, prevIntent, 0);
        PendingIntent pendingPlayIntent = PendingIntent.getService(this, 0, playIntent, 0);
        PendingIntent pendingNextListIntent = PendingIntent.getService(this, 0, nextIntent, 0);


        if (null != baseIntent && baseIntent.getAction().equals(getString(R.string.action_play))) {

            tracksList = baseIntent.getParcelableArrayListExtra(getString(R.string.track_list_key));
            position = baseIntent.getIntExtra(getString(R.string.track_position_key), -1);
            currentPositionInTrack = baseIntent.getIntExtra(getString(R.string.elapsed_time), 0);

            if (position > -1 && null != tracksList && tracksList.size() > 0) {
                currentTrack = tracksList.get(position);
                if (null != currentTrack) {
                    musicUrl = currentTrack.getPreviewUrl();
                }
            } else {
                Log.d(LOG_TAG, "Empty current track");
            }
            initSpotifyPlayer();
            if (null != musicUrl && !musicUrl.isEmpty()) {
                if (spotifyPlayer.isPlaying()) {
                    spotifyPlayer.stop();
                }
                spotifyPlayer.reset();
                startStreamingMusic();
            }
            remoteNotificationView = new RemoteViews(getPackageName(), R.layout.notification_layout);


            notifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            int notificationLockScreenVisibility = Notification.VISIBILITY_PUBLIC;
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setSmallIcon(R.drawable.notification_template_icon_bg)
                    .setContentIntent(pendingListIntent)
                    .setContent(remoteNotificationView)
                    .setOngoing(true)
                    .setVisibility(notificationLockScreenVisibility);
            notif = builder.build();
            startForeground(NOTIFICATION_ID, notif);
            remoteNotificationView.setOnClickPendingIntent(R.id.prev_btn, pendingPrevListIntent);
            remoteNotificationView.setOnClickPendingIntent(R.id.play_btn, pendingPlayIntent);
            remoteNotificationView.setOnClickPendingIntent(R.id.next_btn, pendingNextListIntent);
        } else if (null != baseIntent && baseIntent.getAction().equals(getString(R.string.ACTION_PREV))) {
            if (spotifyPlayer.isPlaying()) {
                spotifyPlayer.stop();
            }
            remoteNotificationView.setImageViewResource(R.id.play_btn, android.R.drawable.ic_media_play);
            notifManager.notify(NOTIFICATION_ID, notif);
            if (position > 0 && null != tracksList && tracksList.size() > 0) {
                position--;
                currentTrack = tracksList.get(position);
                musicUrl = currentTrack.getPreviewUrl();
                startStreamingMusic();

                if (null != mediaPlayerControlInterfaceObj) {
                    mediaPlayerControlInterfaceObj.onPreviousTrack();
                }
            }
        } else if (null != baseIntent && baseIntent.getAction().equals(getString(R.string.ACTION_NEXT))) {
            if (spotifyPlayer.isPlaying()) {
                spotifyPlayer.stop();
            }
            remoteNotificationView.setImageViewResource(R.id.play_btn, android.R.drawable.ic_media_play);
            notifManager.notify(NOTIFICATION_ID, notif);
            if (null != tracksList && tracksList.size() > 0 && position < tracksList.size() - 1) {
                position++;
                currentTrack = tracksList.get(position);
                musicUrl = currentTrack.getPreviewUrl();
                startStreamingMusic();

                if (null != mediaPlayerControlInterfaceObj) {
                    mediaPlayerControlInterfaceObj.onNextTrack();
                }
            }
        } else if (null != baseIntent && baseIntent.getAction().equals(getString(R.string.ACTION_PLAY_PAUSE))) {
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
            }

        } else if (null != baseIntent && baseIntent.getAction().equals(getString(R.string.ACTION_SEEKBAR_POS_CHANGED))) {
            int seekBarProgress = baseIntent.getIntExtra(getString(R.string.Seekbar_Current_Position), 0);
            if (null != spotifyPlayer) {
                spotifyPlayer.seekTo(seekBarProgress);
                spotifyPlayer.start();
            }
        } else if (null != baseIntent && baseIntent.getAction().equals(getString(R.string.ACTION_SEEKBAR_PROGRESS))) {
            if (null != spotifyPlayer && null != mediaPlayerControlInterfaceObj) {
                mediaPlayerControlInterfaceObj.sendElapsedTime(spotifyPlayer.getCurrentPosition());
            }
        } else if (null != baseIntent && baseIntent.getAction().equals(getString(R.string.ACTION_NOW_PLAYING))) {
        if (null != mediaPlayerControlInterfaceObj) {
            if(null != spotifyPlayer){
                mediaPlayerControlInterfaceObj.isSpotifyPlayerOn(true);
                mediaPlayerControlInterfaceObj.sendElapsedTime(spotifyPlayer.getCurrentPosition());
                mediaPlayerControlInterfaceObj.sendTrackDuration(spotifyPlayer.getDuration());
                mediaPlayerControlInterfaceObj.sendTrackList(tracksList);
                mediaPlayerControlInterfaceObj.sendTrackNo(songPosn);
                mediaPlayerControlInterfaceObj.isSpotifyPlayerPaused(spotifyPlayer.isPlaying());
            } else{
                mediaPlayerControlInterfaceObj.isSpotifyPlayerOn(false);
            }
            mediaPlayerControlInterfaceObj.nowPlayingStateUpdated();
        }
    }
        return super.onStartCommand(baseIntent, flags, startId);
    }

    private void startStreamingMusic() {
        if (null != spotifyPlayer){
            spotifyPlayer.reset();
            try {
                spotifyPlayer.setDataSource(musicUrl);
                spotifyPlayer.prepareAsync(); // prepare async to not block main thread
            } catch (IOException e) {
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

        if (null != mediaPlayerControlInterfaceObj) {
            mediaPlayerControlInterfaceObj.sendTrackDuration(spotifyPlayer.getDuration());
            mediaPlayerControlInterfaceObj.onTrackStarted();
        }

        remoteNotificationView.setImageViewResource(R.id.play_btn, android.R.drawable.ic_media_pause);
        remoteNotificationView.setTextViewText(R.id.trackName_tv, currentTrack.getTrackName());
        Handler notifHandler = new Handler(Looper.getMainLooper());
        notifHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!currentTrack.getAlbumImageURL().isEmpty())
                Picasso
                        .with(MediaService.this)
                        .load(currentTrack.getAlbumImageURL())
                        .into(remoteNotificationView, R.id.album_thumbnail_imageview, NOTIFICATION_ID, notif);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null != mediaPlayerControlInterfaceObj){
            mediaPlayerControlInterfaceObj = null;
        }
    }
}
