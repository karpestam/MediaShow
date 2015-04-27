package se.karpestam.mediashow.Grid;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import se.karpestam.mediashow.Media.BitmapRequester;
import se.karpestam.mediashow.Media.RequestJob;
import se.karpestam.mediashow.Media.RequestListener;
import se.karpestam.mediashow.Media.RequestResult;
import se.karpestam.mediashow.R;

public class GridAdapter extends CursorAdapter implements RequestListener {

    private int mGridItemSize;
    private final String mListenerId = this.toString();
    private Context mContext;
    public GridAdapter(Context context, Cursor c, boolean autoRequery, int screenWidth,
            int numColumns, int spacing) {
        super(context, c, autoRequery);
        mContext = context;
        mGridItemSize = (screenWidth / numColumns) - spacing;
        BitmapRequester.getInstance(context).addListener(mListenerId, this);
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
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        /* Get cursor values. */
        final String data = cursor
                .getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
        final int mediaType = cursor
                .getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE));
        ImageView imageView = (ImageView)view;
        imageView.setImageBitmap(null);
        imageView.setTag(data);
        int orientation = cursor
                .getInt(cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
        Bitmap bitmap = BitmapRequester.getInstance(context).requestBitmap(
                new RequestJob(data, orientation, imageView, mListenerId, false, mGridItemSize,
                        mGridItemSize, mediaType));
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public void onRequestResult(RequestResult requestResult) {
        String tag = (String)requestResult.mImageView.getTag();
        if (tag.equals(requestResult.mPath) && requestResult.mListenerId.equals(mListenerId)) {
            if (requestResult.mIsResultOk) {
                requestResult.mImageView.setImageBitmap(requestResult.mBitmap);
            } else {
                requestResult.mImageView.setBackgroundColor(Color.RED);
            }
        }
    }

    /**
     * Should be called when calling Context is getting destroyed.
     */
    public void destroy() {
        BitmapRequester.getInstance(mContext).removeListener(mListenerId);
    }
}
