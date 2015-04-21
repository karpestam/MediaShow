package se.karpestam.mediashow.Fullscreen;

import android.graphics.Bitmap;
import android.graphics.Color;
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

public class FullscreenPageFragment extends Fragment implements MediaItemDecoder
        .MediaItemListener {

    private MediaItem mMediaItem;
    private MediaItemDecoder mMediaItemDecoder;
    private final String mListenerId = toString();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        mMediaItem = new MediaItem(bundle.getInt(MediaStore.MediaColumns._ID),
                bundle.getString(MediaStore.MediaColumns.DATA),
                bundle.getInt(MediaStore.Images.ImageColumns.ORIENTATION));
        mMediaItem.mListenerId = mListenerId;
        getActivity().getActionBar().hide();
        return inflater.inflate(R.layout.fullscreen_page_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMediaItemDecoder = MediaItemDecoder.getInstance();
        mMediaItemDecoder.addListener(mListenerId, this);
        ImageView imageView = (ImageView) view.findViewById(R.id.fullscreen_image);
        mMediaItem.mRequestHighQuality = true;
        Bitmap bitmap = mMediaItemDecoder.getBitmap(mMediaItem);
        if (bitmap != null) {
                imageView.setRotation(mMediaItem.mOrientation);
                imageView.setImageBitmap(bitmap);
        }
        mMediaItem.mImageView = imageView;
        mMediaItem.mImageView.setTag(mMediaItem.mId);
        mMediaItemDecoder.decode(mMediaItem);
    }

    @Override
    public void onMediaItem(MediaItem mediaItem) {
        if ((int) mediaItem.mImageView.getTag() == mediaItem.mId && mediaItem.mListenerId
                .equals(mListenerId)) {
            if (mediaItem.mIsResultOk) {
                mediaItem.mImageView.setRotation(mediaItem.mOrientation);
                mediaItem.mImageView.setImageBitmap(mediaItem.mBitmap);
            } else {
                mediaItem.mImageView.setBackgroundColor(Color.RED);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mMediaItemDecoder.removeListener(mListenerId);
    }
}
