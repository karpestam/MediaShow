package se.karpestam.mediashow.Grid;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images.ImageColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import se.karpestam.mediashow.Fullscreen.FullscreenFragment;
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
    private FragmentManager mFragmentManager;
    private final int mScreenWidth;
    private final int mScreenHeight;
    private final int mNumColumns;
    private OnClickListener mOnClickListener;

    public GridAdapter(Context context, int screenWidth, int screenHeight, int numColumns, FragmentManager fragmentManager) {
        super();
        mContext = context;
        mScreenWidth = screenWidth;
        mScreenHeight = screenHeight;
        mNumColumns = numColumns;
        mFragmentManager = fragmentManager;
        BitmapRequester.getInstance(context).addListener(mListenerId, this);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View gridItem = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.grid_item, viewGroup, false);

        return new ViewHolder(gridItem, mFragmentManager);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        mCursor.moveToPosition(i);
        final String data = mCursor
                .getString(mCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
        final int mediaType = mCursor
                .getInt(mCursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE));
        final int orientation = mCursor
                .getInt(mCursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
        int width = mCursor.getInt(mCursor.getColumnIndex(FileColumns.WIDTH));
        int height = mCursor.getInt(mCursor.getColumnIndex(FileColumns.HEIGHT));
        float aspectRatio = (float)width/height;
        boolean isLandscape = true;
        switch (orientation) {
            case 90:
            case 270:
                isLandscape = false;
                break;
        }
        Log.d("MATS", "width="+width +  " height="+height + " isLandscape="+isLandscape);
        viewHolder.bindViewHolder(i);
        viewHolder.mImageView.setTag(data);
        StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams)viewHolder
                .mImageView
                .getLayoutParams();
        int imageMaxWidth = mScreenWidth / mNumColumns;
        if (isLandscape) {
            params.width = imageMaxWidth;
            params.height = (int) (params.width / aspectRatio);
        } else {
            params.width = imageMaxWidth;
            params.height = (int)(params.width*aspectRatio);
        }
        Log.d("MATS", "params width="+params.width + " params height="+params.height);
        viewHolder.mImageView.setLayoutParams(params);
        Bitmap bitmap = BitmapRequester.getInstance(mContext).requestBitmap(
                new BitmapRequest(data, orientation, viewHolder.mImageView, mListenerId, false,
                       params.width, params.height, mediaType));
        viewHolder.mImageView.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    @Override
    public void onRequestResult(BitmapResult bitmapResult) {
        String tag = (String)bitmapResult.mImageView.getTag();
        if (tag.equals(bitmapResult.mPath) && bitmapResult.mListenerId.equals(mListenerId)) {
//            bitmapResult.mImageView.setAlpha(0f);
            bitmapResult.mImageView.setImageBitmap(bitmapResult.mBitmap);
//            bitmapResult.mImageView.animate().alpha(1.0f).setDuration(85).start();
        }
    }

    public void setCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    /**
     * Should be called when calling Context is getting destroyed.
     */
    public void destroy() {
        BitmapRequester.getInstance(mContext).removeListener(mListenerId);
    }


    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView mImageView;
        private int mPosition;
        private FragmentManager mFragmentManager;

        public ViewHolder(View v, FragmentManager fragmentManager) {
            super(v);
            mImageView = (ImageView)v.findViewById(R.id.grid_image);
            mImageView.setOnClickListener(this);
            mFragmentManager = fragmentManager;
        }

        @Override
        public void onClick(View v) {
            Fragment fragment = new FullscreenFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(FullscreenFragment.CURSOR_START_POSITION, mPosition);
            fragment.setArguments(bundle);
//            fragment.setEnterTransition(new Slide(Gravity.RIGHT));
//            fragment.setReturnTransition(new Slide(Gravity.LEFT));
            mFragmentManager.beginTransaction()
                    .replace(R.id.fragment, fragment, FullscreenFragment.FRAGMENT_TAG)
                    .addToBackStack(FullscreenFragment.FRAGMENT_TAG).commit();
        }

        public void bindViewHolder(int position) {
            mPosition = position;
        }
    }
}
