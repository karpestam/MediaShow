package se.karpestam.mediashow.Grid;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import se.karpestam.mediashow.Media.BitmapCache;
import se.karpestam.mediashow.Media.RequestJob;
import se.karpestam.mediashow.Media.BitmapRequester;
import se.karpestam.mediashow.Media.RequestListener;
import se.karpestam.mediashow.Media.RequestResult;
import se.karpestam.mediashow.R;

public class GridAdapter extends CursorAdapter implements RequestListener {

    private int mGridItemSize;
    private final String mListenerId = this.toString();

    public GridAdapter(Context context, Cursor c, boolean autoRequery, int screenWidth,
            int numColumns) {
        super(context, c, autoRequery);

        mGridItemSize = screenWidth / numColumns;
        BitmapRequester.getInstance().addListener(mListenerId, this);
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
        /* Get cursor values. */
        final int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
        final String data = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        final int orientation = cursor
                .getInt(cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));

        ImageView imageView = (ImageView)view.findViewById(R.id.grid_image);

        /* See if there's any cached Bitmap. */
        Bitmap bitmap = BitmapCache.getInstance().get(id);
        if (bitmap != null) {
            /* Set the cached Bitmap. */
            imageView.setImageBitmap(bitmap);
        } else {
            /* No cached Bitmap found, make sure the ImageView doesn't hold any old Bitmap. */
            imageView.setImageBitmap(null);
            /* Request the Bitmap. */
            BitmapRequester.getInstance().requestBitmap(
                    new RequestJob(id, data, orientation, imageView, mListenerId, false,
                            mGridItemSize, mGridItemSize));
        }
    }

    @Override
    public void onRequestResult(RequestResult requestResult) {
        if ((int)requestResult.mImageView
                .getTag() == requestResult.mId && requestResult.mListenerId.equals(mListenerId)) {
            if (requestResult.mIsResultOk) {
                requestResult.mImageView.setImageBitmap(requestResult.mBitmap);
            } else {
                requestResult.mImageView.setBackgroundColor(Color.RED);
            }
        }
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    /**
     * Should be called when calling Context is getting destroyed.
     */
    public void destroy() {
        BitmapRequester.getInstance().removeListener(mListenerId);
    }
}
