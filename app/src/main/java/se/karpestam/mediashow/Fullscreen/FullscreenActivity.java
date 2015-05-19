package se.karpestam.mediashow.Fullscreen;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.WindowManager;

import se.karpestam.mediashow.Constants;
import se.karpestam.mediashow.CursorLoaderQuery;
import se.karpestam.mediashow.R;

public class FullscreenActivity extends FragmentActivity implements LoaderManager
        .LoaderCallbacks<Cursor> {
    public static final String CURSOR_START_POSITION = "cursor_start_position";
    private WindowManager mWindowManager;
    private int mStartPosition;
    private CursorLoaderQuery mCursorLoaderQuery;
    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCursorLoaderQuery = CursorLoaderQuery.getCursorLoaderQuery(
                getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE)
                        .getInt(Constants.PREFS_FILTER, 0));
        if (savedInstanceState != null) {
            mStartPosition = savedInstanceState.getInt(CURSOR_START_POSITION);
        } else {
            mStartPosition = getIntent().getIntExtra(CURSOR_START_POSITION, 0);
        }
        mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        setContentView(R.layout.fullscreen_activity);
        mViewPager = (ViewPager)findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setPageTransformer(true, new DepthPageTransformer());
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent();
        intent.putExtra(CURSOR_START_POSITION, mStartPosition);
        setResult(RESULT_OK, intent);
        getSupportLoaderManager().destroyLoader(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        mStartPosition = mViewPager.getCurrentItem();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getApplicationContext(), mCursorLoaderQuery.getUri(),
                mCursorLoaderQuery.getProjection(), mCursorLoaderQuery.getSelection(),
                mCursorLoaderQuery.getSelectionArgs(), mCursorLoaderQuery.getSortOrder());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Point point = new Point();
        mWindowManager.getDefaultDisplay().getSize(point);
        mViewPagerAdapter.setCursor(cursor);
        if (mViewPager.getAdapter() == null) {
            mViewPager.setAdapter(mViewPagerAdapter);
        }
        mViewPager.setCurrentItem(mStartPosition);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURSOR_START_POSITION, mStartPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onApplyThemeResource(Theme theme, int resid, boolean first) {
        int savedTheme = getApplicationContext()
                .getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE)
                .getInt(Constants.THEME, 0);
        switch (savedTheme) {
            case 0:
                resid = R.style.AppThemeDefault_Fullscreen;
                break;
            case 1:
                resid = R.style.AppThemeLight_Fullscreen;
                break;
            case 2:
                resid = R.style.AppThemeDark_Fullscreen;
                break;
        }
        super.onApplyThemeResource(theme, resid, first);
    }
}
