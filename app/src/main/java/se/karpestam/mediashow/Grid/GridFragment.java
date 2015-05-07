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
    private int mStartGridPosition = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(Constants.LOG_TAG, GridFragment.class.getSimpleName() + " onCreateView() " +
                "savedInstanceState=" + savedInstanceState);
        getActivity().getActionBar().show();
        if (savedInstanceState != null) {
            mStartGridPosition = savedInstanceState.getInt("position");
        }
        mContext = getActivity().getApplicationContext();

        return inflater.inflate(R.layout.recyclerview, container, false);
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
        WindowManager windowManager = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        int numColumns = mContext.getResources().getInteger(R.integer.grid_columns);
        final Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        GridAdapter adapter = new GridAdapter(cursor, mContext, point.x, numColumns, 0, getFragmentManager());
        adapter.setHasStableIds(true);
        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new GridSpacingDecoration());
        recyclerView.setLayoutManager(new GridLayoutManager(mContext,
                mContext.getResources().getInteger(R.integer.grid_columns)));
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(mStartGridPosition);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(Constants.LOG_TAG, GridFragment.class.getSimpleName() + " onLoaderReset()");
        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
        mStartGridPosition = ((GridLayoutManager)recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        GridAdapter adapter = (GridAdapter)recyclerView.getAdapter();
        adapter.destroy();
        recyclerView.setAdapter(null);
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
        outState.putInt("position", mStartGridPosition);
        super.onSaveInstanceState(outState);
    }
}
