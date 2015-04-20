package se.karpestam.mediashow.Grid;

import android.app.ActivityManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import se.karpestam.mediashow.MediaDecoder.MediaItem;
import se.karpestam.mediashow.MediaDecoder.MediaItemDecoder;
import se.karpestam.mediashow.R;

public class GridAdapter extends CursorAdapter implements MediaItemDecoder.MediaItemListener {

    private LruCache<Integer, Bitmap> mBitmapLruCache;
    private MediaItemDecoder mMediaItemDecoder;
    private int mGridItemSize;
    private final String mListenerId = this.toString();

    public GridAdapter(Context context, Cursor c, boolean autoRequery, int screenWidth, int numColumns) {
        super(context, c, autoRequery);

        int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        int cacheSize = 1024 * 1024 * memClass / 8;

        mBitmapLruCache = new LruCache<Integer, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Integer key, Bitmap value) {
                return value.getByteCount();
            }
        };
        mGridItemSize = screenWidth / numColumns;
        mMediaItemDecoder = MediaItemDecoder.getInstance();
        mMediaItemDecoder.addListener(this.toString(), this);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View gridItem = parent.inflate(context, R.layout.grid_item, null);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mGridItemSize, mGridItemSize);
        gridItem.setLayoutParams(params);
        return gridItem;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
        ImageView imageView = (ImageView) view.findViewById(R.id.grid_image);
        imageView.setTag(id);
        int orientation = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
        if (mBitmapLruCache.get(id) != null) {
            imageView.setRotation(orientation);
            imageView.setImageBitmap(mBitmapLruCache.get(id));
        } else {
            imageView.setImageBitmap(null);
            String data = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            MediaItem mediaItem = new MediaItem(id, data, orientation);
            mediaItem.mImageView = imageView;
            mediaItem.mListenerId = mListenerId;
            mMediaItemDecoder.decode(mediaItem);
        }
    }

    @Override
    public void onMediaItem(MediaItem mediaItem) {
        if ((int) mediaItem.mImageView.getTag() == mediaItem.mId && mediaItem.mListenerId.equals(mListenerId)) {
            mBitmapLruCache.put(mediaItem.mId, mediaItem.mBitmap.get());
            mediaItem.mImageView.setRotation(mediaItem.mOrientation);
            mediaItem.mImageView.setImageBitmap(mediaItem.mBitmap.get());
        }
    }

    public void destroy() {
        mMediaItemDecoder.removeListener(mListenerId);
    }
}
