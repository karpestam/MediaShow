package se.karpestam.mediashow.Grid;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources.Theme;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toolbar;
import android.widget.Toolbar.OnMenuItemClickListener;

import se.karpestam.mediashow.Constants;
import se.karpestam.mediashow.CursorLoaderQuery;
import se.karpestam.mediashow.Fullscreen.FullscreenActivity;
import se.karpestam.mediashow.Grid.DrawerListAdapter.DrawerItemClickListener;
import se.karpestam.mediashow.Grid.GridAdapter.GridClickListener;
import se.karpestam.mediashow.R;


public class GridActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>,
        OnSharedPreferenceChangeListener, GridClickListener, OnMenuItemClickListener,
        DrawerItemClickListener {

    private int mLastFirstVisibleItem = 0;
    private RecyclerView mGridView;
    private StaggeredGridLayoutManager mGridLayoutManager;
    private GridAdapter mGridAdapter;
    private WindowManager mWindowManager;
    //    private Spinner mThemeSpinner;
    private CursorLoaderQuery mCursorLoaderQuery;
    //    private LinearLayout mSettingsLayout;
    private ActionMode mActionMode;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private RecyclerView mDrawerList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mLastFirstVisibleItem = savedInstanceState.getInt("position");
        }
        mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        int numColumns = getResources().getInteger(R.integer.grid_columns);
        final Point point = new Point();
        mWindowManager.getDefaultDisplay().getSize(point);
        mGridAdapter = new GridAdapter(getApplicationContext(), point.x, point.y, numColumns,
                this);
        setContentView(R.layout.grid_activity);
        final int cursorLoaderQuery = getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME,
                Context.MODE_PRIVATE).getInt(Constants.PREFS_FILTER, 0);
        final int theme = getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME,
                Context.MODE_PRIVATE).getInt(Constants.THEME, 0);
        mCursorLoaderQuery = CursorLoaderQuery.getCursorLoaderQuery(cursorLoaderQuery);
        mGridView = (RecyclerView)findViewById(R.id.grid_view);
        mGridView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                final int[] currentFirstVisibleItem = mGridLayoutManager
                        .findFirstVisibleItemPositions(null);

                if (currentFirstVisibleItem[0] > mLastFirstVisibleItem) {
//                    mToolbar.setVisibility(View.GONE);
                } else if (currentFirstVisibleItem[0] < mLastFirstVisibleItem) {
                }
                mLastFirstVisibleItem = currentFirstVisibleItem[0];
            }
        });
//        mSettingsLayout = (LinearLayout)view.findViewById(R.id.settings_layout);
        mGridLayoutManager = new StaggeredGridLayoutManager(
                getResources().getInteger(R.integer.grid_columns),
                StaggeredGridLayoutManager.VERTICAL);
        mGridLayoutManager
                .setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        mGridView.setLayoutManager(mGridLayoutManager);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setActionBar(toolbar);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.open_navigation_drawer, R.string.close_navigation_drawer) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mDrawerList = (RecyclerView)findViewById(R.id.drawer_list);
        mDrawerList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mDrawerList.setAdapter(new DrawerListAdapter(this));
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
//        mThemeSpinner = (Spinner)view.findViewById(R.id.theme_spinner);
//        mThemeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                if (i != theme) {
//                    mContext.getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME,
//                            Context.MODE_PRIVATE).edit().putInt(Constants.THEME, i).apply();
//                    getActivity().recreate();
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });
//        mThemeSpinner.setSelection(theme);
//        DrawerLayout drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getApplicationContext(), mCursorLoaderQuery.getUri(),
                mCursorLoaderQuery.getProjection(), mCursorLoaderQuery.getSelection(),
                mCursorLoaderQuery.getSelectionArgs(), mCursorLoaderQuery.getSortOrder());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.theme_default:
                getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE).edit()
                        .putInt(Constants.THEME, 0).apply();
                recreate();
                return true;
            case R.id.theme_light:
                getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE).edit()
                        .putInt(Constants.THEME, 1).apply();
                recreate();
                return true;
            case R.id.theme_dark:
                getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE).edit()
                        .putInt(Constants.THEME, 2).apply();
                recreate();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
            mDrawerLayout.closeDrawer(Gravity.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
        Log.d(Constants.LOG_TAG, GridActivity.class.getSimpleName() + " onLoadFinished()");

        mGridAdapter.setCursor(cursor);
        if (mGridView.getAdapter() == null) {
            mGridView.setAdapter(mGridAdapter);
        }
        mGridView.smoothScrollToPosition(mLastFirstVisibleItem);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(Constants.LOG_TAG, GridActivity.class.getSimpleName() + " onLoaderReset()");
        int[] firstPositions = mGridLayoutManager.findFirstVisibleItemPositions(null);
        mLastFirstVisibleItem = firstPositions[0];
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Log.d(Constants.LOG_TAG, GridActivity.class.getSimpleName() + " " +
                "onSharedPreferenceChanged() " + s);
        if (s.equals(Constants.PREFS_FILTER)) {

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("position", mLastFirstVisibleItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        Log.d(Constants.LOG_TAG, GridActivity.class.getSimpleName() + " " +
                "onDestroy()");
        super.onDestroy();
        getLoaderManager().destroyLoader(0);
        mGridView.setAdapter(null);
        mGridAdapter.destroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        mLastFirstVisibleItem = data.getIntExtra(FullscreenActivity.CURSOR_START_POSITION, 0);
    }

    @Override
    public void onClicked(int position, String data, View view) {
        if (mActionMode != null) {
            mGridAdapter.setSelected(data, view, position);
            final int size = mGridAdapter.getSelected().size();
            if (size > 1) {
                mActionMode.setSubtitle(mGridAdapter.getSelected().size() + " items selected");
            } else if (size == 1) {
                mActionMode.setSubtitle(mGridAdapter.getSelected().size() + " item selected");
            } else {
                mActionMode.setSubtitle("No item selected");
            }
        } else {
            Intent intent = new Intent(getApplicationContext(), FullscreenActivity.class);
            intent.putExtra(FullscreenActivity.CURSOR_START_POSITION, position);
            final Point point = new Point();
            mWindowManager.getDefaultDisplay().getSize(point);

            startActivityForResult(intent, 0, ActivityOptions
                    .makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight())
                    .toBundle());
        }
    }

    @Override
    public void onLongClicked(final int position, String data, View view) {
        if (mActionMode == null) {
            startActionMode(new Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                    mActionMode = actionMode;
                    mActionMode.getMenuInflater().inflate(R.menu.menu_actions, menu);
                    mActionMode.setTitle("Select items");
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.action_delete:
                            mActionMode.finish();
                            return true;
                        case R.id.action_share:
                            mActionMode.finish();
                            return true;
                    }
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode actionMode) {
                    mGridAdapter.clearAllSelected();
                    mActionMode = null;
                }
            });
            onClicked(position, data, view);
        }
    }

    @Override
    public void onDrawerItemClicked(CursorLoaderQuery cursorLoaderQuery) {
        Log.d("MATS", "onDrawerItemClicked");
        mCursorLoaderQuery = cursorLoaderQuery;
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
//            case R.id.action_add:
//                int columns = mGridLayoutManager.getSpanCount() + 1;
//                mGridAdapter.setColumns(columns);
//                mGridLayoutManager.setSpanCount(columns);
//                mGridAdapter.notifyDataSetChanged();
//                return true;
//            case R.id.action_reduce:
//                int columnsToReduce = mGridLayoutManager.getSpanCount() - 1;
//                mGridAdapter.setColumns(columnsToReduce);
//                mGridLayoutManager.setSpanCount(columnsToReduce);
//                mGridAdapter.notifyDataSetChanged();
//                return true;
            case R.id.action_settings:
//                mSettingsLayout.setVisibility(
//                        mSettingsLayout.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
//                LayoutParams params = (LayoutParams)mToolbar.getLayoutParams();
//                params.height
                return true;
        }
        return false;
    }

    @Override
    protected void onApplyThemeResource(Theme theme, int resid, boolean first) {
        int savedTheme = getApplicationContext()
                .getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE)
                .getInt(Constants.THEME, 0);
        switch (savedTheme) {
            case 0:
                resid = R.style.AppThemeDefault;
                break;
            case 1:
                resid = R.style.AppThemeLight;
                break;
            case 2:
                resid = R.style.AppThemeDark;
                break;
        }
        super.onApplyThemeResource(theme, resid, first);
    }
}
