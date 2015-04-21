package se.karpestam.mediashow.Fullscreen;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class FullscreenAdapter extends FragmentStatePagerAdapter {

    private Cursor mCursor;

    public FullscreenAdapter(Cursor cursor, FragmentManager fragmentManager) {
        super(fragmentManager);
        mCursor = cursor;
    }

    @Override
    public Fragment getItem(int i) {
        mCursor.moveToPosition(i);
        final int id = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Images.Media._ID));
        final String data = mCursor
                .getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
        final int orientation = mCursor
                .getInt(mCursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
        Bundle bundle = new Bundle();
        bundle.putInt(MediaStore.MediaColumns._ID, id);
        bundle.putInt(MediaStore.Images.ImageColumns.ORIENTATION, orientation);
        bundle.putString(MediaStore.MediaColumns.DATA, data);
        FullscreenPageFragment fullscreenFragment = new FullscreenPageFragment();
        fullscreenFragment.setArguments(bundle);
        return fullscreenFragment;
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }
}