package se.karpestam.mediashow.Video;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;

import java.io.IOException;

import se.karpestam.mediashow.Constants;

/**
 * Created by 23055395 on 2015-04-24.
 */
public class VideoPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer
        .OnCompletionListener, MediaPlayer.OnErrorListener, SurfaceTextureListener {

    private VideoListener mListener;
    private float mVolume = 0f;
    private Handler mProgressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            mListener.onProgress(mMediaPlayer.getCurrentPosition());
            mProgressHandler.sendEmptyMessageDelayed(0, 250);
        }
    };

    public interface VideoListener {
        void onStarted(int progress, int duration);

        void onCompleted();

        void onInitialized();

        void onProgress(int progress);

        void onPaused();

        void onStopped();
    }

    private MediaPlayer mMediaPlayer;
    private PlayerState mPlayerState = PlayerState.END;
    private String mCurrentDataSource;

    private enum PlayerState {
        END,
        ERROR,
        IDLE,
        INITIALIZED,
        PREPARING,
        PREPARED,
        STARTED,
        PAUSED,
        STOPPED,
        PLAYBACK_COMPLETED
    }

    public VideoPlayer(TextureView textureView, VideoListener videoListener, int width,
            int height) {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setVolume(mVolume, mVolume);
        mListener = videoListener;
        textureView.setSurfaceTextureListener(this);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mPlayerState = PlayerState.PREPARED;
        play();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mPlayerState = PlayerState.PLAYBACK_COMPLETED;
        mProgressHandler.removeCallbacksAndMessages(null);
        mListener.onCompleted();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mPlayerState = PlayerState.ERROR;
        return false;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        mMediaPlayer.setSurface(new Surface(surfaceTexture));
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    public void setDataSource(String data) {
        mCurrentDataSource = data;
        if (mPlayerState != PlayerState.IDLE) {
            try {
                mMediaPlayer.reset();
                mPlayerState = PlayerState.IDLE;
                mMediaPlayer.setDataSource(mCurrentDataSource);
                mPlayerState = PlayerState.INITIALIZED;
                mListener.onInitialized();
            } catch (IOException e) {
                Log.e(Constants.LOG_TAG,
                        VideoPlayer.class.getSimpleName() + " setDataSource() " + e.getMessage());
            }
        }
    }

    public void play() {
        if (mPlayerState == PlayerState.INITIALIZED || mPlayerState == PlayerState.STOPPED) {
            mMediaPlayer.prepareAsync();
            mPlayerState = PlayerState.PREPARING;
        } else if (mPlayerState == PlayerState.PREPARED || mPlayerState == PlayerState.PAUSED ||
                mPlayerState == PlayerState.PLAYBACK_COMPLETED) {
            mMediaPlayer.start();
            mPlayerState = PlayerState.STARTED;
            mListener.onStarted(mMediaPlayer.getCurrentPosition(), mMediaPlayer.getDuration());
            mProgressHandler.sendEmptyMessageDelayed(0, 500);
        }
    }

    public boolean isPlaying() {
        return mPlayerState == PlayerState.STARTED;
    }

    public void pause() {
        if (mPlayerState == PlayerState.STARTED) {
            mMediaPlayer.pause();
            mPlayerState = PlayerState.PAUSED;
            mProgressHandler.removeCallbacksAndMessages(null);
            mListener.onPaused();
        }
    }

    public void stop() {
        if (mPlayerState == PlayerState.STARTED || mPlayerState == PlayerState.PAUSED ||
                mPlayerState == PlayerState.PLAYBACK_COMPLETED) {
            mMediaPlayer.stop();
            mPlayerState = PlayerState.STOPPED;
            mProgressHandler.removeCallbacksAndMessages(null);
            mListener.onStopped();
        }
    }

    public void seekTo(int progress) {
        mMediaPlayer.seekTo(progress);
    }

    public void setVolume(float volume) {
        mVolume = volume;
        mMediaPlayer.setVolume(mVolume, mVolume);
    }

    public float getVolume() {
        return mVolume;
    }

    public void release() {
        mMediaPlayer.setOnCompletionListener(null);
        mMediaPlayer.setOnPreparedListener(null);
        mMediaPlayer.setOnErrorListener(null);
        mProgressHandler.removeCallbacksAndMessages(null);
        mMediaPlayer.release();
        mPlayerState = PlayerState.END;
    }
}
