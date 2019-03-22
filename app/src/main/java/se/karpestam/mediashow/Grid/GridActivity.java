package se.karpestam.mediashow.Grid;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources.Theme;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ShareActionProvider;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toolbar;
import android.widget.Toolbar.OnMenuItemClickListener;

import java.util.Set;

import se.karpestam.mediashow.Constants;
import se.karpestam.mediashow.CursorLoader.CursorLoaderQuery;
import se.karpestam.mediashow.CursorLoader.FolderQuery;
import se.karpestam.mediashow.CursorLoader.FoldersQuery;
import se.karpestam.mediashow.CursorLoader.PhotosAndVideosQuery;
import se.karpestam.mediashow.CursorLoader.PhotosQuery;
import se.karpestam.mediashow.CursorLoader.VideosQuery;
import se.karpestam.mediashow.Fullscreen.FullscreenActivity;
import se.karpestam.mediashow.Grid.GridAdapter.GridClickListener;
import se.karpestam.mediashow.MainApplication;
import se.karpestam.mediashow.R;


public class GridActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>,
        GridClickListener, OnMenuItemClickListener {

    private static final int GRID_CURSOR_LOADER = 1;
    private int mLastFirstVisibleItem = 0;
    private RecyclerView mGridView;
    private StaggeredGridLayoutManager mGridLayoutManager;
    private GridAdapter mGridAdapter;
    private WindowManager mWindowManager;
    private ActionMode mActionMode;
    private ShareActionProvider mShareActionProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int numColumns = getResources().getInteger(R.integer.grid_columns);
        final Point point = new Point();
        mWindowManager.getDefaultDisplay().getSize(point);
        mGridAdapter = new GridAdapter(getApplicationContext(), point.x, point.y, numColumns,
                this);
        setContentView(R.layout.grid_activity);
        mGridView = (RecyclerView) findViewById(R.id.grid_view);
        mGridLayoutManager = new StaggeredGridLayoutManager(
                getResources().getInteger(R.integer.grid_columns),
                StaggeredGridLayoutManager.VERTICAL);
        mGridLayoutManager
                .setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        mGridView.setLayoutManager(mGridLayoutManager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(toolbar);
        getLoaderManager().initLoader(GRID_CURSOR_LOADER, null, this);
        FloatingActionButton filterActionButton = (FloatingActionButton) findViewById(
                R.id.filter_action_button);
        filterActionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final int queryFilter = getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME,
                        Context.MODE_PRIVATE).getInt(Constants.FILTER, 0);
                switch (queryFilter) {
                    case 0:
                        ((MainApplication) getApplication())
                                .setCursorLoaderQuery(new PhotosQuery(0));
                        getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME,
                                Context.MODE_PRIVATE).edit().putInt(Constants.FILTER, 1).apply();
                        getLoaderManager().restartLoader(GRID_CURSOR_LOADER, null, GridActivity
                                .this);
                        getActionBar().setTitle("Photos");
                        break;
                    case 1:
                        ((MainApplication) getApplication())
                                .setCursorLoaderQuery(new VideosQuery(0));
                        getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME,
                                Context.MODE_PRIVATE).edit().putInt(Constants.FILTER, 2).apply();
                        getLoaderManager().restartLoader(GRID_CURSOR_LOADER, null, GridActivity
                                .this);
                        getActionBar().setTitle("Videos");
                        break;
                    case 2:
                        ((MainApplication) getApplication())
                                .setCursorLoaderQuery(new PhotosAndVideosQuery(0));
                        getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME,
                                Context.MODE_PRIVATE).edit().putInt(Constants.FILTER, 0).apply();
                        getLoaderManager().restartLoader(GRID_CURSOR_LOADER, null, GridActivity
                                .this);
                        getActionBar().setTitle("Photos and videos");
                        break;
                }
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case GRID_CURSOR_LOADER:
                CursorLoaderQuery cursorLoaderQuery = ((MainApplication) getApplication())
                        .getCursorLoaderQuery();
                return new CursorLoader(getApplicationContext(), cursorLoaderQuery.getUri(),
                        cursorLoaderQuery.getProjection(), cursorLoaderQuery.getSelection(),
                        cursorLoaderQuery.getSelectionArgs(), cursorLoaderQuery.getSortOrder());
            default:
                throw new RuntimeException("Cursor loader id not valid.");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
        Log.d(Constants.LOG_TAG, GridActivity.class.getSimpleName() + " onLoadFinished()");

        switch (loader.getId()) {
            case GRID_CURSOR_LOADER:
                mGridAdapter.setCursor(cursor);
                if (mGridView.getAdapter() == null) {
                    mGridView.setAdapter(mGridAdapter);
                }
                break;
            default:
                throw new RuntimeException("Cursor loader id not valid");
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(Constants.LOG_TAG, GridActivity.class.getSimpleName() + " onLoaderReset()");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options, menu);
        Spinner themeSpinner = (Spinner) menu.findItem(R.id.menu_item_theme).getActionView();
        ArrayAdapter<String> themesArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.theme_arrays));
        themeSpinner.setAdapter(themesArrayAdapter);
        final int theme = getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME,
                Context.MODE_PRIVATE).getInt(Constants.THEME, 0);
        themeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position,
                                       long l) {
                if (position != theme) {
                    getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE)
                            .edit().putInt(Constants.THEME, position).apply();
                    recreate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        themeSpinner.setSelection(theme);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_select:
                createActionMode();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(Constants.LOG_TAG, GridActivity.class.getSimpleName() + " " +
                "onDestroy()");
        super.onDestroy();
        getLoaderManager().destroyLoader(GRID_CURSOR_LOADER);
        mGridView.setAdapter(null);
        mGridAdapter.destroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mLastFirstVisibleItem = data.getIntExtra(FullscreenActivity.CURSOR_START_POSITION, 0);
        mGridView.scrollToPosition(mLastFirstVisibleItem);
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onClicked(int position, Uri uri, View view) {
        if (mActionMode != null) {
            mGridAdapter.setSelected(uri, position);
            final int size = mGridAdapter.getSelected().size();
            if (size > 1) {
                mActionMode.setSubtitle(mGridAdapter.getSelected().size() + " items selected");
            } else if (size == 1) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent = Intent.createChooser(intent, "Share a picture...");
                Set<Uri> keySet = mGridAdapter.getSelected().keySet();
                for (Uri uris : keySet) {
                    Log.d("MATS", "uri " + uris);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                }
                mShareActionProvider.setShareIntent(intent);
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
    public void onLongClicked(final int position, Uri uri, View view) {
        if (mActionMode == null) {
            createActionMode();
            onClicked(position, uri, view);
        }
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
                android.util.Log.d("MATS", "GridActivity.onApplyThemeResource()");
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

    private void createActionMode() {
        startActionMode(new Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                mActionMode = actionMode;
                mActionMode.getMenuInflater().inflate(R.menu.menu_actions, menu);
                MenuItem menuItem = menu.findItem(R.id.menu_item_share);
                mShareActionProvider = (ShareActionProvider) menuItem.getActionProvider();
                mActionMode.setTitle("Select items");
                mActionMode.setSubtitle("No item selected");
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
                        mGridAdapter.getSelected();
                        mActionMode.finish();
                        return true;
                    case R.id.menu_item_share:
//                        startActivity(mShareActionProvider.);
                        mActionMode.finish();
                        return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                mGridAdapter.clearAllSelected();
                mShareActionProvider = null;
                mActionMode = null;
            }
        });
    }
}
