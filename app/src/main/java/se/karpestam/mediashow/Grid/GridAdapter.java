package se.karpestam.mediashow.Grid;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.HashMap;

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
    private GridClickListener mGridClickListener;
    private HashMap<Uri, Integer> mSelectedList;

    public GridAdapter(Context context, int screenWidth, int screenHeight, int numColumns,
                       GridClickListener gridClickListener) {
        super();
        mContext = context;
        mScreenWidth = screenWidth;
        mScreenHeight = screenHeight;
        mNumColumns = numColumns;
        mGridClickListener = gridClickListener;
        mSelectedList = new HashMap<>();
        BitmapRequester.getInstance(context).addListener(mListenerId, this);
    }

    interface GridClickListener {
        void onClicked(int position, Uri uri, View view);

        void onLongClicked(int position, Uri uri, View view);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View gridItem = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_item, parent, false);
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
        final int id = mCursor.getInt(mCursor.getColumnIndex(MediaStore.MediaColumns._ID));
        Uri uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id);
        int width = mCursor.getInt(mCursor.getColumnIndex(FileColumns.WIDTH));
        int height = mCursor.getInt(mCursor.getColumnIndex(FileColumns.HEIGHT));
        float aspectRatio = (float) width / height;
        boolean isLandscape = true;
        switch (orientation) {
            case 90:
            case 270:
                isLandscape = false;
                break;
        }
        viewHolder.bindViewHolder(position, uri);
        viewHolder.mImageView.setTag(data);
        StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager
                .LayoutParams) viewHolder.mImageView
                .getLayoutParams();
        int imageMaxWidth = mScreenWidth / mNumColumns;
        if (isLandscape) {
            params.width = imageMaxWidth;
            params.height = (int) (params.width / aspectRatio);
        } else {
            params.width = imageMaxWidth;
            params.height = (int) (params.width * aspectRatio);
        }
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
            String tag = (String) bitmapResult.mImageView.getTag();
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

    public void setSelected(Uri uri, int position) {
        boolean isSelected = mSelectedList.containsKey(uri);
        if (isSelected) {
            mSelectedList.remove(uri);
        } else {
            mSelectedList.put(uri, position);
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
                    float aspectRatio = (float) width / height;
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
                        height = (int) (width / aspectRatio);
                    } else {
                        width = imageMaxWidth;
                        height = (int) (width * aspectRatio);
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
        private Uri mUri;

        public ViewHolder(View v) {
            super(v);
            mImageView = (ImageView) v.findViewById(R.id.grid_image);
            mImageView.setOnClickListener(this);
            mImageView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mGridClickListener.onClicked(mPosition, mUri, mImageView);
        }

        @Override
        public boolean onLongClick(View view) {
            mGridClickListener.onLongClicked(mPosition, mUri, mImageView);
            return true;
        }

        public void bindViewHolder(int position, Uri uri) {
            mPosition = position;
            mUri = uri;
            mIsSelected = mSelectedList.containsKey(mUri);
            mImageView.setSelected(mIsSelected);
        }
    }
}
