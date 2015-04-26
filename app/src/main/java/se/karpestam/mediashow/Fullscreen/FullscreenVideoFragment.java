package se.karpestam.mediashow.Fullscreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import se.karpestam.mediashow.Constants;
import se.karpestam.mediashow.Media.BitmapRequester;
import se.karpestam.mediashow.Media.RequestJob;
import se.karpestam.mediashow.Media.RequestListener;
import se.karpestam.mediashow.Media.RequestResult;
import se.karpestam.mediashow.R;
import se.karpestam.mediashow.Video.VideoPlayer;

public class FullscreenVideoFragment extends Fragment implements RequestListener, VideoPlayer.VideoListener {

    private final String mListenerId = toString();
    private VideoPlayer mVideoPlayer;
    private BitmapRequester mBitmapRequester;
    private ImageButton mPlayPauseButton;
    private ImageButton mSoundButton;
    private SeekBar mSeekBar;
    private boolean mIsVisibleToUser;
    private boolean mIsVideoInitialized;

    private String mData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN);
        Log.d(Constants.LOG_TAG, FullscreenVideoFragment.class.getSimpleName() + " onCreateView() " + savedInstanceState);
        WindowManager windowManager = (WindowManager) getActivity()
                .getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        View view = inflater.inflate(R.layout.fullscreen_video_fragment, container, false);
        /* Get values. */
        Bundle bundle = getArguments();
        final String data = bundle.getString(MediaStore.Files.FileColumns.DATA);
        mData = data;
        final int mediaType = bundle.getInt(MediaStore.Files.FileColumns.MEDIA_TYPE);
        final int width = bundle.getInt(MediaStore.Video.VideoColumns.WIDTH);
        final int height = bundle.getInt(MediaStore.Video.VideoColumns.HEIGHT);
        float aspectRatio = (float) width / height;
        MediaMetadataRetriever m = new MediaMetadataRetriever();
        m.setDataSource(data);
        int rotation = (Integer.valueOf(m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)));
        TextureView surfaceView = (TextureView) view.findViewById(R.id.fullscreen_video);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) surfaceView.getLayoutParams();
        params.width = point.x;
        params.height = (int) (point.x / aspectRatio);
        mVideoPlayer = new VideoPlayer(surfaceView, this, point.x, (int) (point.x / aspectRatio));

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mVideoPlayer.setDataSource(mData);
//        Log.d("MATS", "video width " + width + " height " + height + " aspect ratio " + aspectRatio + " rotation " + rotation);


    }

    @Override
    public void onRequestResult(RequestResult requestResult) {
        Log.d(Constants.LOG_TAG, FullscreenVideoFragment.class.getSimpleName() + " onRequestResult() " + requestResult);
        String tag = (String) requestResult.mImageView.getTag();
        if (tag.equals(requestResult.mPath) && requestResult.mListenerId.equals(mListenerId)) {
            if (requestResult.mIsResultOk) {
                requestResult.mImageView.setImageBitmap(requestResult.mBitmap);
                Log.d("MATS", "requestResult " + requestResult.mBitmap.getWidth());

//                Log.d("MATS", "surface width " + mSurfaceView.getWidth());
//                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mSurfaceView.getLayoutParams();
//                params.width = point.x;
//                params.height = (int)(point.x/aspectRatio);
//                mSurfaceView.setLayoutParams(params);

            } else {
                requestResult.mImageView.setBackgroundColor(Color.RED);
            }
        }
    }

    @Override
    public void onDestroyView() {
        Log.d(Constants.LOG_TAG, FullscreenVideoFragment.class.getSimpleName() + " onDestroyView()");
        super.onDestroyView();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        Log.d(Constants.LOG_TAG, FullscreenVideoFragment.class.getSimpleName() + " setUserVisibleHint() isVisibleToUser=" + isVisibleToUser);
        super.setUserVisibleHint(isVisibleToUser);
        mIsVisibleToUser = isVisibleToUser;
        if (mIsVisibleToUser) {
            startVideo();
        } else if (mVideoPlayer != null) {
            mVideoPlayer.release();
        }
    }

    @Override
    public void onStarted(int progress, int duration) {
        Log.d(Constants.LOG_TAG, FullscreenVideoFragment.class.getSimpleName() + " onStarted() progress=" + progress + " duration=" + duration);
        mSeekBar.setMax(duration);
        mSeekBar.setProgress(progress);
        getView().findViewById(R.id.fullscreen_video).animate().setDuration(500).alpha(1).start();
    }

    @Override
    public void onCompleted() {
        mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
    }

    @Override
    public void onInitialized() {
        Log.d(Constants.LOG_TAG, FullscreenVideoFragment.class.getSimpleName() + " onInitialized()");
        mPlayPauseButton = (ImageButton) getView().findViewById(R.id.play_pause_button);
        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoPlayer.isPlaying()) {
                    mVideoPlayer.pause();
                    mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    mVideoPlayer.play();
                    mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
                }
            }
        });
        mSeekBar = (SeekBar) getView().findViewById(R.id.seek_bar);
        mSoundButton = (ImageButton) getView().findViewById(R.id.sound_button);
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
        mSeekBar.setProgress(progress);
    }

    private void startVideo() {
        if (mIsVideoInitialized && mIsVisibleToUser) {
            mVideoPlayer.play();
        }
    }


}
