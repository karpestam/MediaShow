package se.karpestam.mediashow.Grid;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.transition.Explode;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toolbar;

import java.util.Collection;
import java.util.HashMap;

import se.karpestam.mediashow.Constants;
import se.karpestam.mediashow.CursorLoaderQuery;
import se.karpestam.mediashow.Fullscreen.FullscreenFragment;
import se.karpestam.mediashow.Grid.GridAdapter.SelectionListener;
import se.karpestam.mediashow.R;


public class GridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        OnSharedPreferenceChangeListener, SelectionListener {

    public static final String FRAGMENT_TAG = GridFragment.class.getSimpleName();
    private Context mContext;
    private int mLastFirstVisibleItem = 0;
    private RecyclerView mRecyclerView;
    private StaggeredGridLayoutManager mGridLayoutManager;
    private GridAdapter mGridAdapter;
    private WindowManager mWindowManager;
    private Toolbar mToolbar;
    private Toolbar mActionToolbar;
    private ActionMode mActionMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new Explode());
        setExitTransition(new Explode());
        setReturnTransition(new Explode());
        setReenterTransition(new Explode());
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            mLastFirstVisibleItem = savedInstanceState.getInt("position");
        }
        mContext = getActivity().getApplicationContext();
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int numColumns = mContext.getResources().getInteger(R.integer.grid_columns);
        final Point point = new Point();
        mWindowManager.getDefaultDisplay().getSize(point);
        mGridAdapter = new GridAdapter(mContext, point.x, point.y, numColumns, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        setExitTransition(new Explode());
//        setEnterTransition(new Explode());
//        setReturnTransition(new Explode());
        Log.d(Constants.LOG_TAG, GridFragment.class.getSimpleName() + " onCreateView() " +
                "savedInstanceState=" + savedInstanceState);
        return inflater.inflate(R.layout.grid_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                final int[] currentFirstVisibleItem = mGridLayoutManager
                        .findFirstVisibleItemPositions(null);

                if (currentFirstVisibleItem[0] > mLastFirstVisibleItem) {
//                    getActivity().getActionBar().hide();
                } else if (currentFirstVisibleItem[0] < mLastFirstVisibleItem) {
//                    getActivity().getActionBar().show();
                }
                mLastFirstVisibleItem = currentFirstVisibleItem[0];
            }
        });
        mGridLayoutManager = new StaggeredGridLayoutManager(
                mContext.getResources().getInteger(R.integer.grid_columns),
                StaggeredGridLayoutManager.VERTICAL);
        mGridLayoutManager
                .setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        getActivity().setActionBar(mToolbar);
        mActionToolbar = (Toolbar) view.findViewById(R.id.settings_toolbar);
        getLoaderManager().initLoader(0, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoaderQuery cursorLoaderQuery = CursorLoaderQuery.getCursorLoaderQuery(
                mContext.getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME,
                        Context.MODE_PRIVATE).getInt(Constants.PREFS_FILTER, 0));
        return new CursorLoader(mContext, cursorLoaderQuery.getUri(),
                cursorLoaderQuery.getProjection(), cursorLoaderQuery.getSelection(),
                cursorLoaderQuery.getSelectionArgs(), cursorLoaderQuery.getSortOrder());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
        Log.d(Constants.LOG_TAG, GridFragment.class.getSimpleName() + " onLoadFinished()");

        mGridAdapter.setCursor(cursor);
        if (mRecyclerView.getAdapter() == null) {
            mRecyclerView.setAdapter(mGridAdapter);
        }
        mRecyclerView.smoothScrollToPosition(mLastFirstVisibleItem);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(Constants.LOG_TAG, GridFragment.class.getSimpleName() + " onLoaderReset()");
        int[] firstPositions = mGridLayoutManager.findFirstVisibleItemPositions(null);
        mLastFirstVisibleItem = firstPositions[0];
    }

    @Override
    public void onDestroyView() {
        Log.d(Constants.LOG_TAG, GridFragment.class.getSimpleName() + " onDestroyView()");
        super.onDestroyView();

        getLoaderManager().destroyLoader(0);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Log.d(Constants.LOG_TAG, GridFragment.class.getSimpleName() + " " +
                "onSharedPreferenceChanged() " + s);
        if (s.equals(Constants.PREFS_FILTER)) {

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                int columns = mGridLayoutManager.getSpanCount() + 1;
                mGridAdapter.setColumns(columns);
                mGridLayoutManager.setSpanCount(columns);
                mGridAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_reduce:
                int columnsToReduce = mGridLayoutManager.getSpanCount() - 1;
                mGridAdapter.setColumns(columnsToReduce);
                mGridLayoutManager.setSpanCount(columnsToReduce);
                mGridAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_settings:
                return true;
            case R.id.action_light:
                mContext.getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE).edit().putInt(Constants.THEME, 1).apply();
                getActivity().recreate();
                return true;
            case R.id.action_dark:
                mContext.getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE).edit().putInt(Constants.THEME, 2).apply();
                getActivity().recreate();
                return true;
            case R.id.action_default:
                mContext.getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE).edit().putInt(Constants.THEME, 0).apply();
                getActivity().recreate();
                return true;
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("position", mLastFirstVisibleItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        Log.d(Constants.LOG_TAG, GridFragment.class.getSimpleName() + " " +
                "onDestroy()");
        super.onDestroy();

        mRecyclerView.setAdapter(null);
        mGridAdapter.destroy();
    }

    @Override
    public void onClicked(int position, String data, View view) {
        if (mActionMode != null) {
            mGridAdapter.setSelected(data, view, position);
            mActionMode.setTitle(mGridAdapter.getSelected().size() + " items selected");
        } else {
            Fragment fragment = new FullscreenFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(FullscreenFragment.CURSOR_START_POSITION, position);
            fragment.setArguments(bundle);
//            fragment.setEnterTransition(new Explode());
//            fragment.setExitTransition(new Explode());
//            fragment.setReenterTransition(new Explode());
//            setCustomAnimations(R.anim.enter_anim, R.anim.enter_anim, R.anim.enter_anim, R.anim.enter_anim)
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment, fragment, FullscreenFragment.FRAGMENT_TAG)
                    .addToBackStack(null).commit();
        }
    }

    @Override
    public void onLongClicked(final int position, String data, View view) {
        if (mActionMode == null) {
            mActionMode = getActivity().startActionMode(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    mode.getMenuInflater().inflate(R.menu.menu_actions, menu);
                    getActivity().getActionBar().hide();
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_delete:
                            Collection<Integer> values = mGridAdapter.getSelected().values();
                            for (int position : values) {
                                mGridAdapter.notifyItemRemoved(position);
                            }
                            mActionMode.finish();
                            return true;
                        case R.id.action_share:
                            return true;
                    }
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    mActionMode = null;
                    mGridAdapter.clearAllSelected();
                    getActivity().getActionBar().show();
                }
            });
            onClicked(position, data, view);
        }
    }
}
