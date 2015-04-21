package se.karpestam.mediashow.Grid;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import se.karpestam.mediashow.MediaDecoder.MediaItem;
import se.karpestam.mediashow.MediaDecoder.MediaItemDecoder;
import se.karpestam.mediashow.R;

public class GridAdapter extends CursorAdapter implements MediaItemDecoder.MediaItemListener {


    private MediaItemDecoder mMediaItemDecoder;
    private int mGridItemSize;
    private final String mListenerId = this.toString();

    public GridAdapter(Context context, Cursor c, boolean autoRequery, int screenWidth,
            int numColumns) {
        super(context, c, autoRequery);

        mGridItemSize = screenWidth / numColumns;
        mMediaItemDecoder = MediaItemDecoder.getInstance();
        mMediaItemDecoder.addListener(mListenerId, this);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View gridItem = parent.inflate(context, R.layout.grid_item, null);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mGridItemSize,
                mGridItemSize);
        gridItem.setLayoutParams(params);
        return gridItem;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
        String data = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        int orientation = cursor
                .getInt(cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
        ImageView imageView = (ImageView)view.findViewById(R.id.grid_image);
        imageView.setTag(id);
        MediaItem mediaItem = new MediaItem(id, data, orientation);
        if (mMediaItemDecoder.getBitmap(mediaItem) != null) {
            imageView.setRotation(orientation);
            imageView.setImageBitmap(mMediaItemDecoder.getBitmap(mediaItem));
        } else {
            imageView.setImageBitmap(null);
            mediaItem.mImageView = imageView;
            mediaItem.mListenerId = mListenerId;
            mMediaItemDecoder.decode(mediaItem);
        }
    }

    @Override
    public void onMediaItem(MediaItem mediaItem) {
        if ((int)mediaItem.mImageView.getTag() == mediaItem.mId && mediaItem.mListenerId
                .equals(mListenerId)) {
            if (mediaItem.mIsResultOk) {
                mediaItem.mImageView.setRotation(mediaItem.mOrientation);
                mediaItem.mImageView.setImageBitmap(mediaItem.mBitmap);
            } else {
                mediaItem.mImageView.setBackgroundColor(Color.RED);
            }
        }
    }


    public void destroy() {
        mMediaItemDecoder.removeListener(mListenerId);
    }
}
