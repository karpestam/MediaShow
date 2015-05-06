package se.karpestam.mediashow.Grid;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import se.karpestam.mediashow.Fullscreen.FullscreenFragment;
import se.karpestam.mediashow.Media.BitmapRequester;
import se.karpestam.mediashow.Media.BitmapRequest;
import se.karpestam.mediashow.Media.BitmapResultListener;
import se.karpestam.mediashow.Media.BitmapResult;
import se.karpestam.mediashow.PhotosAndVideosQuery;
import se.karpestam.mediashow.R;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> implements
        BitmapResultListener {

    private Cursor mCursor;
    private Context mContext;
    private final int mGridItemSize;
    private final String mListenerId = this.toString();
    private FragmentManager mFragmentManager;

    public GridAdapter(Context context, int screenWidth, int numColumns, int spacing,
            FragmentManager fragmentManager) {
        super();
        mContext = context;
        mGridItemSize = (screenWidth / numColumns) - spacing;
        BitmapRequester.getInstance(context).addListener(mListenerId, this);
        mFragmentManager = fragmentManager;
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
        viewHolder.bindViewHolder(i);
        final String data = mCursor
                .getString(mCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
        final int mediaType = mCursor
                .getInt(mCursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE));
        viewHolder.mImageView.setTag(data);
        int orientation = mCursor
                .getInt(mCursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
        Log.d("MATS", "request bitmap " + i);
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


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView mImageView;
        private int mPosition;

        public ViewHolder(View v) {
            super(v);
            mImageView = (ImageView)v.findViewById(R.id.grid_image);
            mImageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Fragment fragment = new FullscreenFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(FullscreenFragment.CURSOR_START_POSITION, mPosition);
            bundle.putString("cursor", PhotosAndVideosQuery.class.getName());
            fragment.setArguments(bundle);
            mFragmentManager.beginTransaction()
                    .replace(R.id.fragment, fragment, FullscreenFragment.FRAGMENT_TAG)
                    .addToBackStack(FullscreenFragment.FRAGMENT_TAG).commit();
        }

        public void bindViewHolder(int position) {
            mPosition = position;
        }
    }
}
