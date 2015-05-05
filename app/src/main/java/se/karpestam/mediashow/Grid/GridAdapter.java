package se.karpestam.mediashow.Grid;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import se.karpestam.mediashow.Media.BitmapRequester;
import se.karpestam.mediashow.Media.BitmapRequest;
import se.karpestam.mediashow.Media.BitmapResultListener;
import se.karpestam.mediashow.Media.BitmapResult;
import se.karpestam.mediashow.R;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> implements
        BitmapResultListener {

    private Cursor mCursor;
    private Context mContext;
    private final int mGridItemSize;
    private final String mListenerId = this.toString();

    public GridAdapter(Cursor cursor, Context context, int screenWidth, int numColumns,
            int spacing) {
        super();
        mCursor = cursor;
        mContext = context;
        mGridItemSize = (screenWidth / numColumns) - spacing;
        BitmapRequester.getInstance(context).addListener(mListenerId, this);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View gridItem = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.grid_item, viewGroup, false);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mGridItemSize,
                mGridItemSize);
        gridItem.setLayoutParams(params);
        return new ViewHolder(gridItem);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        mCursor.moveToPosition(i);
        final String data = mCursor
                .getString(mCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
        final int mediaType = mCursor
                .getInt(mCursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE));
        viewHolder.mImageView.setTag(data);
        int orientation = mCursor
                .getInt(mCursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
        Bitmap bitmap = BitmapRequester.getInstance(mContext).requestBitmap(
                new BitmapRequest(data, orientation, viewHolder.mImageView, mListenerId, false,
                        mGridItemSize, mGridItemSize, mediaType));
        viewHolder.mImageView.setImageBitmap(bitmap);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    @Override
    public void onRequestResult(BitmapResult bitmapResult) {
        String tag = (String)bitmapResult.mImageView.getTag();
        if (tag.equals(bitmapResult.mPath) && bitmapResult.mListenerId.equals(mListenerId)) {
            bitmapResult.mImageView.setImageBitmap(bitmapResult.mBitmap);
        }
    }

    public void swapCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    /**
     * Should be called when calling Context is getting destroyed.
     */
    public void destroy() {
        BitmapRequester.getInstance(mContext).removeListener(mListenerId);
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;

        public ViewHolder(View v) {
            super(v);
            mImageView = (ImageView)v.findViewById(R.id.grid_image);
        }
    }
}
