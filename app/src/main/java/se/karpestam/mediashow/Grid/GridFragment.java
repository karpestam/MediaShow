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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toolbar;
import android.widget.Toolbar.OnMenuItemClickListener;

import java.util.Collection;
import java.util.HashMap;

import se.karpestam.mediashow.Constants;
import se.karpestam.mediashow.CursorLoaderQuery;
import se.karpestam.mediashow.Fullscreen.FullscreenFragment;
import se.karpestam.mediashow.Grid.GridAdapter.SelectionListener;
import se.karpestam.mediashow.R;


public class GridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        OnSharedPreferenceChangeListener, SelectionListener, OnMenuItemClickListener {

    public static final String FRAGMENT_TAG = GridFragment.class.getSimpleName();
    private Context mContext;
    private int mLastFirstVisibleItem = 0;
    private RecyclerView mRecyclerView;
    private StaggeredGridLayoutManager mGridLayoutManager;
    private GridAdapter mGridAdapter;
    private WindowManager mWindowManager;
    private Toolbar mToolbar;
    private boolean mIsInSelectionMode;
    private ImageButton mCancelSelectionButton;
    private Spinner mSpinner;
    private CursorLoaderQuery mCursorLoaderQuery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        final int cursorLoaderQuery = mContext.getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME,
                Context.MODE_PRIVATE).getInt(Constants.PREFS_FILTER, 0);
        mCursorLoaderQuery = CursorLoaderQuery.getCursorLoaderQuery(cursorLoaderQuery
        );
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                final int[] currentFirstVisibleItem = mGridLayoutManager
                        .findFirstVisibleItemPositions(null);

                if (currentFirstVisibleItem[0] > mLastFirstVisibleItem) {
                    mToolbar.setVisibility(View.GONE);
//                    mToolbar.animate().setDuration(100).alpha(0f).start();
                } else if (currentFirstVisibleItem[0] < mLastFirstVisibleItem) {
//                    mToolbar.animate().setDuration(100).alpha(1.0f).start();
                    mToolbar.setVisibility(View.VISIBLE);
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
        mToolbar.inflateMenu(R.menu.menu_main);
        mToolbar.setOnMenuItemClickListener(this);
        mSpinner = (Spinner) view.findViewById(R.id.toolbar_spinner);
        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("MATS", "onItemSelected " + i);
                mContext.getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME,
                        Context.MODE_PRIVATE).edit().putInt(Constants.PREFS_FILTER, i).apply();
                mCursorLoaderQuery = CursorLoaderQuery.getCursorLoaderQuery(i);
                getLoaderManager().restartLoader(0, null, GridFragment.this);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mSpinner.setSelection(cursorLoaderQuery);
        mCancelSelectionButton = (ImageButton) view.findViewById(R.id.selection_cancel_button);
        mCancelSelectionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveSelectionMode();
            }
        });
        getLoaderManager().initLoader(0, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(mContext, mCursorLoaderQuery.getUri(),
                mCursorLoaderQuery.getProjection(), mCursorLoaderQuery.getSelection(),
                mCursorLoaderQuery.getSelectionArgs(), mCursorLoaderQuery.getSortOrder());
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
        if (mIsInSelectionMode) {
            mGridAdapter.setSelected(data, view, position);
            mToolbar.setSubtitle(mGridAdapter.getSelected().size() + " items selected");
        } else {
            Fragment fragment = new FullscreenFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(FullscreenFragment.CURSOR_START_POSITION, position);
            fragment.setArguments(bundle);
//            setCustomAnimations(R.anim.enter_anim, R.anim.enter_anim, R.anim.enter_anim, R
// .anim.enter_anim)
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment, fragment, FullscreenFragment.FRAGMENT_TAG)
                    .addToBackStack(null).commit();
        }
    }

    @Override
    public void onLongClicked(final int position, String data, View view) {
        if (!mIsInSelectionMode) {
            enterSelectionMode();
            onClicked(position, data, view);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_delete:
                leaveSelectionMode();
                return true;
            case R.id.action_share:
                leaveSelectionMode();
                return true;
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

//                LayoutParams params = (LayoutParams)mToolbar.getLayoutParams();
//                params.height
                return true;
//            case R.id.action_light:
//                mContext.getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME,
//                        Context.MODE_PRIVATE).edit().putInt(Constants.THEME, 1).apply();
//                getActivity().recreate();
//                return true;
//            case R.id.action_dark:
//                mContext.getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME,
//                        Context.MODE_PRIVATE).edit().putInt(Constants.THEME, 2).apply();
//                getActivity().recreate();
//                return true;
//            case R.id.action_default:
//                mContext.getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME,
//                        Context.MODE_PRIVATE).edit().putInt(Constants.THEME, 0).apply();
//                getActivity().recreate();
//                return true;
        }
        return false;
    }

    private void enterSelectionMode() {
        mToolbar.findViewById(R.id.toolbar_spinner).setVisibility(View.GONE);
        mCancelSelectionButton.setVisibility(View.VISIBLE);
        mToolbar.getMenu().clear();
        mToolbar.inflateMenu(R.menu.menu_actions);
        mIsInSelectionMode = true;
    }

    private void leaveSelectionMode() {
        mCancelSelectionButton.setVisibility(View.GONE);
        mGridAdapter.clearAllSelected();
        mToolbar.findViewById(R.id.toolbar_spinner).setVisibility(View.VISIBLE);
        mToolbar.setSubtitle("");
        mToolbar.getMenu().clear();
        mToolbar.inflateMenu(R.menu.menu_main);
        mIsInSelectionMode = false;
    }
}
