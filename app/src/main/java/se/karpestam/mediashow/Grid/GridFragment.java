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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import se.karpestam.mediashow.Constants;
import se.karpestam.mediashow.CursorLoaderQuery;
import se.karpestam.mediashow.R;


public class GridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        OnSharedPreferenceChangeListener {

    public static final String FRAGMENT_TAG = GridFragment.class.getSimpleName();
    private Context mContext;
    private int mLastFirstVisibleItem = 0;
    private RecyclerView mRecyclerView;
    private GridLayoutManager mGridLayoutManager;
    private GridAdapter mGridAdapter;
    private WindowManager mWindowManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getActionBar().show();
        if (savedInstanceState != null) {
            mLastFirstVisibleItem = savedInstanceState.getInt("position");
        }
        mContext = getActivity().getApplicationContext();

        mWindowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        int numColumns = mContext.getResources().getInteger(R.integer.grid_columns);
        final Point point = new Point();
        mWindowManager.getDefaultDisplay().getSize(point);
        mGridAdapter = new GridAdapter(mContext, point.x, numColumns, 0, getFragmentManager());
        mGridAdapter.setHasStableIds(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(Constants.LOG_TAG, GridFragment.class.getSimpleName() + " onCreateView() " +
                "savedInstanceState=" + savedInstanceState);

        mRecyclerView = (RecyclerView)inflater.inflate(R.layout.recyclerview, container, false);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new GridSpacingDecoration());
        mRecyclerView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                final int currentFirstVisibleItem = mGridLayoutManager.findFirstVisibleItemPosition();

                if (currentFirstVisibleItem > mLastFirstVisibleItem) {
                    getActivity().getActionBar().hide();
                } else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
                    getActivity().getActionBar().show();
                }

                mLastFirstVisibleItem = currentFirstVisibleItem;

            }
        });
        mGridLayoutManager = new GridLayoutManager(mContext,
                mContext.getResources().getInteger(R.integer.grid_columns));
        mRecyclerView.setLayoutManager(mGridLayoutManager);

        return mRecyclerView;
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
        mRecyclerView.scrollToPosition(mLastFirstVisibleItem);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(Constants.LOG_TAG, GridFragment.class.getSimpleName() + " onLoaderReset()");
        mLastFirstVisibleItem = mGridLayoutManager.findFirstCompletelyVisibleItemPosition();
    }

    @Override
    public void onDestroyView() {
        Log.d(Constants.LOG_TAG, GridFragment.class.getSimpleName() + " onDestroyView()");
        super.onDestroyView();
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
        super.onDestroy();

        mRecyclerView.setAdapter(null);
        mGridAdapter.destroy();
    }
}
