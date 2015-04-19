package se.karpestam.mediashow.Fullscreen;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import se.karpestam.mediashow.R;

public class FullscreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private Context mContext;
    private WindowManager mWindowManager;
    private int mStartPosition;

    public FullscreenFragment(int position) {
        mStartPosition = position;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fullscreen_slide_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = getActivity().getApplicationContext();
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();

        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(mContext, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Point point = new Point();
        mWindowManager.getDefaultDisplay().getSize(point);
        FullscreenAdapter fullscreenAdapter = new FullscreenAdapter(mContext, getFragmentManager(), cursor);
        ViewPager viewPager = (ViewPager) getView().findViewById(R.id.pager);
        viewPager.setAdapter(fullscreenAdapter);
        viewPager.setCurrentItem(mStartPosition);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

}
