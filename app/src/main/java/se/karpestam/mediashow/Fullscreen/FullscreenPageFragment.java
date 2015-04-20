package se.karpestam.mediashow.Fullscreen;

import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import se.karpestam.mediashow.MediaDecoder.MediaItem;
import se.karpestam.mediashow.MediaDecoder.MediaItemDecoder;
import se.karpestam.mediashow.R;

public class FullscreenPageFragment extends Fragment implements MediaItemDecoder.MediaItemListener {

    private MediaItem mMediaItem;
    private MediaItemDecoder mMediaItemDecoder;
    private final String mListenerId = toString();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        mMediaItem = new MediaItem(bundle.getInt(MediaStore.MediaColumns._ID), bundle.getString(MediaStore.MediaColumns.DATA), bundle.getInt(MediaStore.Images.ImageColumns.ORIENTATION));
        mMediaItem.mListenerId = mListenerId;
        getActivity().getActionBar().hide();
        mMediaItemDecoder = MediaItemDecoder.getInstance();
        mMediaItemDecoder.addListener(mListenerId, this);
        return inflater.inflate(R.layout.fullscreen_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView imageView = (ImageView) view.findViewById(R.id.fullscreen_image);
        imageView.setTag(mMediaItem.mId);
        if (mMediaItem.mBitmap != null) {
            imageView.setImageBitmap(mMediaItem.mBitmap);
        } else {
            mMediaItem.mImageView = imageView;
            mMediaItemDecoder.decode(mMediaItem);
        }
    }

    @Override
    public void onMediaItem(MediaItem mediaItem) {
        if ((int) mediaItem.mImageView.getTag() == mediaItem.mId && mediaItem.mListenerId.equals(mListenerId)) {
            mediaItem.mImageView.setRotation(mediaItem.mOrientation);
            mediaItem.mImageView.setImageBitmap(mediaItem.mBitmap);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mMediaItemDecoder.removeListener(mListenerId);
    }
}
