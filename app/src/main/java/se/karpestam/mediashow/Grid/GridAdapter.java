package se.karpestam.mediashow.Grid;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.CursorAdapter;
import android.widget.GridView;
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
    private BitmapRequester mBitmapRequester;

    public GridAdapter(Context context, Cursor c, boolean autoRequery, int screenWidth,
                       int numColumns, int spacing) {
        super(context, c, autoRequery);

        mGridItemSize = (screenWidth / numColumns) - spacing;
        mBitmapRequester = BitmapRequester.getInstance(context);
        mBitmapRequester.addListener(mListenerId, this);
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
        final String data = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        final int orientation = cursor
                .getInt(cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));

        ImageView imageView = (ImageView) view.findViewById(R.id.grid_image);
        imageView.setTag(data);
        Bitmap bitmap = mBitmapRequester.requestBitmap(new RequestJob(data, orientation, imageView, mListenerId, false,
                mGridItemSize, mGridItemSize));
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public void onRequestResult(RequestResult requestResult) {
        String tag = (String) requestResult.mImageView.getTag();
        if (tag.equals(requestResult.mPath) && requestResult.mListenerId.equals(mListenerId)) {
            if (requestResult.mIsResultOk) {
//                requestResult.mImageView.setAlpha(0f);
                requestResult.mImageView.setImageBitmap(requestResult.mBitmap);
//                requestResult.mImageView.animate().setInterpolator(new DecelerateInterpolator()).setDuration(50).alpha(1);
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
        mBitmapRequester.removeListener(mListenerId);
    }
}
