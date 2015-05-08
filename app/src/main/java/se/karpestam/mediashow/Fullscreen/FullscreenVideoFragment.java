package se.karpestam.mediashow.Fullscreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import se.karpestam.mediashow.Constants;
import se.karpestam.mediashow.Media.BitmapRequester;
import se.karpestam.mediashow.Media.BitmapRequest;
import se.karpestam.mediashow.Media.BitmapResultListener;
import se.karpestam.mediashow.Media.BitmapResult;
import se.karpestam.mediashow.R;
import se.karpestam.mediashow.Video.VideoPlayer;

public class FullscreenVideoFragment extends Fragment implements BitmapResultListener,
        VideoPlayer.VideoListener {

    private final String mListenerId = toString();
    private VideoPlayer mVideoPlayer;
    private ImageButton mPlayPauseButton;
    private ImageButton mSoundButton;
    private SeekBar mSeekBar;
    private boolean mIsVisibleToUser;
    private boolean mIsVideoInitialized;
    private String mData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(Constants.LOG_TAG, FullscreenVideoFragment.class
                .getSimpleName() + " onCreateView() " + savedInstanceState);


        BitmapRequester.getInstance(getActivity().getApplicationContext())
                .addListener(mListenerId, this);
        WindowManager windowManager = (WindowManager)getActivity()
                .getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        View view = inflater.inflate(R.layout.fullscreen_video_item, container, false);
        /* Get values. */
        Bundle bundle = getArguments();
        final String data = bundle.getString(MediaStore.Files.FileColumns.DATA);
        mData = data;
        final int orientation = bundle.getInt(ImageColumns.ORIENTATION);
        final int mediaType = bundle.getInt(MediaStore.Files.FileColumns.MEDIA_TYPE);
        final int width = bundle.getInt(MediaStore.Video.VideoColumns.WIDTH);
        final int height = bundle.getInt(MediaStore.Video.VideoColumns.HEIGHT);
        float aspectRatio = (float)width / height;
        MediaMetadataRetriever m = new MediaMetadataRetriever();
        m.setDataSource(data);
        ImageView imageView = (ImageView)view.findViewById(R.id.fullscreen_image);
        int rotation = (Integer
                .valueOf(m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)));
        TextureView textureView = (TextureView)view.findViewById(R.id.fullscreen_video);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)textureView.getLayoutParams();
        params.width = point.x;
        params.height = (int)(point.x / aspectRatio);
        mVideoPlayer = new VideoPlayer(textureView, this, point.x, (int)(point.x / aspectRatio));
        imageView.setTag(data);
        Bitmap bitmap = BitmapRequester.getInstance(getActivity().getApplicationContext())
                .requestBitmap(
                        new BitmapRequest(data, orientation, imageView, mListenerId, true, width,
                                height, mediaType));
        imageView.setImageBitmap(bitmap);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mVideoPlayer.setDataSource(mData);
    }

    @Override
    public void onRequestResult(BitmapResult bitmapResult) {
        Log.d(Constants.LOG_TAG, FullscreenVideoFragment.class
                .getSimpleName() + " onRequestResult() " + bitmapResult);
        String tag = (String)bitmapResult.mImageView.getTag();
        if (tag.equals(bitmapResult.mPath) && bitmapResult.mListenerId.equals(mListenerId)) {
            bitmapResult.mImageView.setImageBitmap(bitmapResult.mBitmap);
        }
    }

    @Override
    public void onDestroyView() {
        Log.d(Constants.LOG_TAG,
                FullscreenVideoFragment.class.getSimpleName() + " onDestroyView()");
        super.onDestroyView();

        BitmapRequester.getInstance(getActivity().getApplicationContext())
                .removeListener(mListenerId);

        mVideoPlayer.release();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        Log.d(Constants.LOG_TAG, FullscreenVideoFragment.class
                .getSimpleName() + " setUserVisibleHint() isVisibleToUser=" + isVisibleToUser);
        super.setUserVisibleHint(isVisibleToUser);
        mIsVisibleToUser = isVisibleToUser;
        if (mIsVisibleToUser) {
            startVideo();
        } else if (mVideoPlayer != null) {
            mVideoPlayer.pause();
        }
    }

    @Override
    public void onStarted(int progress, int duration) {
        Log.d(Constants.LOG_TAG, FullscreenVideoFragment.class
                .getSimpleName() + " onStarted() progress=" + progress + " duration=" + duration);
        mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
        mSeekBar.setMax(duration);
        mSeekBar.setProgress(progress);
        getView().findViewById(R.id.fullscreen_video).animate().setDuration(1000).alpha(1).start();
        getView().findViewById(R.id.video_controls).animate().setDuration(1000).alpha(1).start();
    }

    @Override
    public void onStopped() {
        mSeekBar.setProgress(0);
        mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
    }

    @Override
    public void onPaused() {
        mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
    }

    @Override
    public void onCompleted() {
        mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
    }

    @Override
    public void onInitialized() {
        Log.d(Constants.LOG_TAG,
                FullscreenVideoFragment.class.getSimpleName() + " onInitialized()");
        mPlayPauseButton = (ImageButton)getView().findViewById(R.id.play_pause_button);
        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoPlayer.isPlaying()) {
                    mVideoPlayer.pause();
                } else {
                    mVideoPlayer.play();
                }
            }
        });
        mSeekBar = (SeekBar)getView().findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mVideoPlayer.seekTo(seekBar.getProgress());
            }
        });
        mSoundButton = (ImageButton)getView().findViewById(R.id.sound_button);
        mSoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoPlayer.getVolume() == 0f) {
                    mVideoPlayer.setVolume(1.0f);
                    mSoundButton.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
                } else {
                    mVideoPlayer.setVolume(0f);
                    mSoundButton.setImageResource(android.R.drawable.ic_lock_silent_mode);
                }
            }
        });
        mIsVideoInitialized = true;
        startVideo();
    }

    @Override
    public void onProgress(int progress) {
        if (!mSeekBar.isPressed())
        mSeekBar.setProgress(progress);
    }

    private void startVideo() {
        if (mIsVideoInitialized && mIsVisibleToUser) {
            mVideoPlayer.play();
        }
    }

}
