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
import android.transition.Explode;
import android.transition.Fade;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;


import se.karpestam.mediashow.Constants;
import se.karpestam.mediashow.CursorLoaderQuery;
import se.karpestam.mediashow.R;
import se.karpestam.mediashow.VideosQuery;

public class FullscreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String CURSOR_START_POSITION = "cursor_start_position";
    public static final String FRAGMENT_TAG = FullscreenFragment.class.getSimpleName();
    private Context mContext;
    private WindowManager mWindowManager;
    private int mStartPosition;
    private CursorLoaderQuery mCursorLoaderQuery;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mContext = getActivity().getApplicationContext();
        mCursorLoaderQuery = CursorLoaderQuery.getCursorLoaderQuery(
                mContext.getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME,
                        Context.MODE_PRIVATE).getInt(Constants.PREFS_FILTER, 0));
        if (savedInstanceState == null) {
            mStartPosition = getArguments().getInt(CURSOR_START_POSITION);
        } else {
            mStartPosition = savedInstanceState.getInt(CURSOR_START_POSITION);
        }
        return inflater.inflate(R.layout.fullscreen_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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
        mStartPosition = ((ViewPager)getView().findViewById(R.id.pager)).getCurrentItem();
        getLoaderManager().destroyLoader(0);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(mContext, mCursorLoaderQuery.getUri(),
                mCursorLoaderQuery.getProjection(), mCursorLoaderQuery.getSelection(),
                mCursorLoaderQuery.getSelectionArgs(), mCursorLoaderQuery.getSortOrder());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Point point = new Point();
        mWindowManager.getDefaultDisplay().getSize(point);
        final FullscreenAdapter fullscreenAdapter = new FullscreenAdapter(cursor,
                getFragmentManager());
        final ViewPager viewPager = (ViewPager)getView().findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(fullscreenAdapter);
        viewPager.setCurrentItem(mStartPosition);
        viewPager.setPageTransformer(true, new DepthPageTransformer());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        ((ViewPager)getView().findViewById(R.id.pager)).setAdapter(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURSOR_START_POSITION, mStartPosition);
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
