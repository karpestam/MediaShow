package se.karpestam.mediashow.Fullscreen;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import se.karpestam.mediashow.MediaDecoder.MediaItem;
import se.karpestam.mediashow.MediaDecoder.MediaItemDecoder;
import se.karpestam.mediashow.R;

public class FullscreenSlideFragment extends Fragment implements MediaItemDecoder.MediaItemListener {

    private final Cursor mCursor;
    private MediaItemDecoder mMediaItemDecoder;

    public FullscreenSlideFragment(Cursor cursor) {
        mCursor = cursor;
        mMediaItemDecoder = new MediaItemDecoder(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getActionBar().hide();
        return inflater.inflate(R.layout.fullscreen_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d("MATS", "onViewCreated=" + mCursor.getPosition());
        ImageView imageView = (ImageView) view.findViewById(R.id.fullscreen_image);

        int id = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Images.Media._ID));
        MediaItem mediaItem = new MediaItem();
        imageView.setTag(id);
        mediaItem.mView = imageView;
        mediaItem.mPath = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
        mediaItem.mOrientation =  mCursor.getInt(mCursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
        mediaItem.mId = id;
        mMediaItemDecoder.decode(mediaItem);
    }

    @Override
    public void onMediaItem(MediaItem mediaItem) {
        if ((int) mediaItem.mView.getTag() == mediaItem.mId) {
            ImageView imageView = (ImageView) mediaItem.mView;
            imageView.setRotation(mediaItem.mOrientation);
            imageView.setImageBitmap(mediaItem.mBitmap);
        }
    }
}
