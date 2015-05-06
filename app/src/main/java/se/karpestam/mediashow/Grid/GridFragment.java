package se.karpestam.mediashow.Grid;

import android.app.ActionBar;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import se.karpestam.mediashow.Constants;
import se.karpestam.mediashow.CursorLoaderQuery;
import se.karpestam.mediashow.R;


public class GridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        OnSharedPreferenceChangeListener {

    public static final String FRAGMENT_TAG = GridFragment.class.getSimpleName();
    private Context mContext;
    private GridAdapter mGridAdapter;
    private RecyclerView mGridView;
    private CursorLoaderQuery mCursorLoaderQuery;
    private GridLayoutManager mGridLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(Constants.LOG_TAG, GridFragment.class.getSimpleName() + " onCreateView() " +
                "savedInstanceState=" + savedInstanceState);
        mContext = getActivity().getApplicationContext();
        mCursorLoaderQuery = CursorLoaderQuery.getCursorLoaderQuery(
                mContext.getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME,
                        Context.MODE_PRIVATE).getInt(Constants.PREFS_FILTER, 0));
        mGridView = (RecyclerView)inflater.inflate(R.layout.recyclerview, container, false);
        mGridLayoutManager = new GridLayoutManager(mContext,
                mContext.getResources().getInteger(R.integer.grid_columns));
        mGridView.setLayoutManager(mGridLayoutManager);
        mGridView.setHasFixedSize(true);

        return mGridView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(Constants.LOG_TAG, GridFragment.class.getSimpleName() + "onViewCreated()");
        super.onViewCreated(view, savedInstanceState);
        WindowManager windowManager = (WindowManager)mContext
                .getSystemService(Context.WINDOW_SERVICE);
        int numColumns = mContext.getResources().getInteger(R.integer.grid_columns);
        final Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        mGridAdapter = new GridAdapter(mContext, point.x, numColumns, 0, getFragmentManager());
        mGridAdapter.setHasStableIds(true);
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
        return new CursorLoader(mContext, mCursorLoaderQuery.getUri(),
                mCursorLoaderQuery.getProjection(), mCursorLoaderQuery.getSelection(),
                mCursorLoaderQuery.getSelectionArgs(), mCursorLoaderQuery.getSortOrder());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
        Log.d(Constants.LOG_TAG, GridFragment.class.getSimpleName() + " onLoadFinished()");
        mGridAdapter.swapCursor(cursor);
        if (mGridView.getAdapter() == null) {
            Log.d(Constants.LOG_TAG, GridFragment.class.getSimpleName() + " onLoadFinished() " +
                    "setting adapter");
            mGridView.setAdapter(mGridAdapter);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(Constants.LOG_TAG, GridFragment.class.getSimpleName() + " onLoaderReset()");
//        mGridView.setAdapter(null);
//        mGridAdapter.destroy();
    }

    @Override
    public void onDestroyView() {
        Log.d(Constants.LOG_TAG, GridFragment.class.getSimpleName() + " onDestroyView()");
        super.onDestroyView();

        mGridView.setAdapter(null);
        if (mGridAdapter != null) {
            mGridAdapter.destroy();
            mGridAdapter = null;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Log.d(Constants.LOG_TAG, GridFragment.class.getSimpleName() + " " +
                "onSharedPreferenceChanged() " + s);
        if (s.equals(Constants.PREFS_FILTER)) {

        }
    }
}
