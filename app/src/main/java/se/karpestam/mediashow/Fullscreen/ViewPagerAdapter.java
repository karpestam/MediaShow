package se.karpestam.mediashow.Fullscreen;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private Cursor mCursor;

    public ViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int i) {
        mCursor.moveToPosition(i);
        final String data = mCursor
                .getString(mCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
        final int orientation = mCursor
                .getInt(mCursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
        final int mediaType = mCursor
                .getInt(mCursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE));
        final int width = mCursor
                .getInt(mCursor.getColumnIndex(MediaStore.Video.VideoColumns.WIDTH));
        final int height = mCursor
                .getInt(mCursor.getColumnIndex(MediaStore.Video.VideoColumns.HEIGHT));
        Bundle bundle = new Bundle();
        bundle.putInt(MediaStore.Images.ImageColumns.ORIENTATION, orientation);
        bundle.putString(MediaStore.Files.FileColumns.DATA, data);
        bundle.putInt(MediaStore.Files.FileColumns.MEDIA_TYPE, mediaType);
        bundle.putInt(MediaStore.Video.VideoColumns.WIDTH, width);
        bundle.putInt(MediaStore.Video.VideoColumns.HEIGHT, height);
        switch (mediaType) {
            case MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO:
                FullscreenVideoFragment videoFragment = new FullscreenVideoFragment();
                videoFragment.setArguments(bundle);
                return videoFragment;
            default:
                FullscreenImageFragment fullscreenFragment = new FullscreenImageFragment();
                fullscreenFragment.setArguments(bundle);
                return fullscreenFragment;
        }
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    public void setCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }
}