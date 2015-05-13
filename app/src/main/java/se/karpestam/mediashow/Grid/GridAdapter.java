package se.karpestam.mediashow.Grid;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import se.karpestam.mediashow.Media.BitmapRequester;
import se.karpestam.mediashow.Media.BitmapRequest;
import se.karpestam.mediashow.Media.BitmapResultListener;
import se.karpestam.mediashow.Media.BitmapResult;
import se.karpestam.mediashow.R;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> implements
        BitmapResultListener {

    private Cursor mCursor;
    private Context mContext;
    private final String mListenerId = this.toString();
    private final int mScreenWidth;
    private final int mScreenHeight;
    private int mNumColumns;
    private int mPreviousPosition;
    private SelectionListener mSelectionListener;
    private HashMap<String, Integer> mSelectedList;

    public GridAdapter(Context context, int screenWidth, int screenHeight, int numColumns,
            SelectionListener selectionListener) {
        super();
        mContext = context;
        mScreenWidth = screenWidth;
        mScreenHeight = screenHeight;
        mNumColumns = numColumns;
        mSelectionListener = selectionListener;
        mSelectedList = new HashMap<>();
        BitmapRequester.getInstance(context).addListener(mListenerId, this);
    }

    interface SelectionListener {
        void onClicked(int position, String data, View view);
        void onLongClicked(int position, String data, View view);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View gridItem = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.grid_item, viewGroup, false);
        return new ViewHolder(gridItem);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        mCursor.moveToPosition(position);
        final String data = mCursor
                .getString(mCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
        final int mediaType = mCursor
                .getInt(mCursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE));
        final int orientation = mCursor
                .getInt(mCursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
        int width = mCursor.getInt(mCursor.getColumnIndex(FileColumns.WIDTH));
        int height = mCursor.getInt(mCursor.getColumnIndex(FileColumns.HEIGHT));
        float aspectRatio = (float)width / height;
        boolean isLandscape = true;
        switch (orientation) {
            case 90:
            case 270:
                isLandscape = false;
                break;
        }
        viewHolder.bindViewHolder(position, data);
        viewHolder.mImageView.setTag(data);
        StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager
                .LayoutParams)viewHolder.mImageView
                .getLayoutParams();
        int imageMaxWidth = mScreenWidth / mNumColumns;
        if (isLandscape) {
            params.width = imageMaxWidth;
            params.height = (int)(params.width / aspectRatio);
        } else {
            params.width = imageMaxWidth;
            params.height = (int)(params.width * aspectRatio);
        }
        Log.d("MATS", "params width=" + params.width + " params height=" + params.height);
        viewHolder.mImageView.setLayoutParams(params);
        Bitmap bitmap = BitmapRequester.getInstance(mContext).requestBitmap(
                new BitmapRequest(data, orientation, viewHolder.mImageView, mListenerId, false,
                        params.width, params.height, mediaType));
        viewHolder.mImageView.setImageBitmap(bitmap);
//        preRequestBitmap(position);
        mPreviousPosition = position;
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    @Override
    public void onRequestResult(BitmapResult bitmapResult) {
        if (bitmapResult.mImageView != null) {
            String tag = (String)bitmapResult.mImageView.getTag();
            if (tag.equals(bitmapResult.mPath) && bitmapResult.mListenerId.equals(mListenerId)) {
                bitmapResult.mImageView.setImageBitmap(bitmapResult.mBitmap);
            }
        }
    }

    public void setColumns(int columns) {
        mNumColumns = columns;
    }
    public void setCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public HashMap getSelected() {
        return mSelectedList;
    }

    public void setSelected(String data, View view, int position) {
        boolean isSelected = mSelectedList.containsKey(data);
        if (isSelected) {
            mSelectedList.remove(data);
        } else {
            mSelectedList.put(data, position);
        }
        notifyDataSetChanged();
    }

    public void clearAllSelected() {
        mSelectedList.clear();
        notifyDataSetChanged();
    }

    /**
     * Should be called when calling Context is getting destroyed.
     */
    public void destroy() {
        BitmapRequester.getInstance(mContext).removeListener(mListenerId);
    }

    private void preRequestBitmap(int position) {
        int maxPos = mCursor.getCount() - 1;
        if (position < maxPos) {
            if (position > mPreviousPosition) {
                for (int i = position; i < maxPos && (i < position + 6); i++) {
                    mCursor.moveToPosition(i);
                    final String data = mCursor
                            .getString(mCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                    final int mediaType = mCursor.getInt(mCursor
                            .getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE));
                    final int orientation = mCursor
                            .getInt(mCursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
                    int width = mCursor.getInt(mCursor.getColumnIndex(FileColumns.WIDTH));
                    int height = mCursor.getInt(mCursor.getColumnIndex(FileColumns.HEIGHT));
                    float aspectRatio = (float)width / height;
                    boolean isLandscape = true;
                    switch (orientation) {
                        case 90:
                        case 270:
                            isLandscape = false;
                            break;
                    }
                    int imageMaxWidth = mScreenWidth / mNumColumns;
                    if (isLandscape) {
                        width = imageMaxWidth;
                        height = (int)(width / aspectRatio);
                    } else {
                        width = imageMaxWidth;
                        height = (int)(width * aspectRatio);
                    }
                    BitmapRequester.getInstance(mContext).requestBitmap(
                            new BitmapRequest(data, orientation, null, mListenerId, false, width,
                                    height, mediaType));
                }
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View
            .OnLongClickListener {
        public ImageView mImageView;
        private int mPosition;
        private boolean mIsSelected;
        private String mData;

        public ViewHolder(View v) {
            super(v);
            mImageView = (ImageView)v.findViewById(R.id.grid_image);
            mImageView.setOnClickListener(this);
            mImageView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mSelectionListener.onClicked(mPosition, mData, mImageView);
        }

        @Override
        public boolean onLongClick(View view) {
            mSelectionListener.onLongClicked(mPosition, mData, mImageView);
            return true;
        }

        public void bindViewHolder(int position, String data) {
            mPosition = position;
            mData = data;
            mIsSelected = mSelectedList.containsKey(mData);
            mImageView.setSelected(mIsSelected);
        }
    }
}
