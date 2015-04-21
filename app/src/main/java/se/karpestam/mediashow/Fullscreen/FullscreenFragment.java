package se.karpestam.mediashow.Fullscreen;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import se.karpestam.mediashow.R;

public class FullscreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String FRAGMENT_TAG = FullscreenFragment.class.getSimpleName();
    private Context mContext;
    private WindowManager mWindowManager;
    private int mStartPosition;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mStartPosition = getArguments().getInt("START_POSITION");
        } else {
            mStartPosition = savedInstanceState.getInt("START_POSITION");
        }
        return inflater.inflate(R.layout.fullscreen_slide_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = getActivity().getApplicationContext();
        mWindowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getLoaderManager().destroyLoader(0);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(mContext, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null,
                null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Point point = new Point();
        mWindowManager.getDefaultDisplay().getSize(point);
        FullscreenAdapter fullscreenAdapter = new FullscreenAdapter(cursor, getFragmentManager());
        final ViewPager viewPager = (ViewPager)getView().findViewById(R.id.pager);
        viewPager.setAdapter(fullscreenAdapter);
        viewPager.setCurrentItem(mStartPosition);
        viewPager.setPageTransformer(true, new DepthPageTransformer());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        ViewPager viewPager = (ViewPager)getView().findViewById(R.id.pager);
        outState.putInt("START_POSITION", viewPager.getCurrentItem());
        super.onSaveInstanceState(outState);
    }

    private class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);
            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);
            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
}
