package se.karpestam.mediashow.Grid;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridView;

import se.karpestam.mediashow.Constants;
import se.karpestam.mediashow.CursorLoaderQuery;
import se.karpestam.mediashow.PhotosAndVideosQuery;
import se.karpestam.mediashow.R;


public class GridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String FRAGMENT_TAG = GridFragment.class.getSimpleName();
    private Context mContext;
    private GridAdapter mGridAdapter;
    private RecyclerView mGridView;
    private CursorLoaderQuery mCursorLoaderQuery;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(Constants.LOG_TAG, GridFragment.class.getSimpleName() + " onCreateView() " +
                "savedInstanceState=" + savedInstanceState);
        mCursorLoaderQuery = new PhotosAndVideosQuery();
        mContext = getActivity().getApplicationContext();
        mGridView = (RecyclerView) inflater
                .inflate(R.layout.recyclerview, container, false);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext,
                mContext.getResources().getInteger(R.integer.grid_columns));
        mGridView.setLayoutManager(gridLayoutManager);
        mGridView.setHasFixedSize(true);

        return mGridView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
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
        return new CursorLoader(mContext, mCursorLoaderQuery.getUri(), mCursorLoaderQuery.getProjection(),
                mCursorLoaderQuery.getSelection(),
                mCursorLoaderQuery.getSelectionArgs(),
                mCursorLoaderQuery.getSortOrder());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
        mGridAdapter.swapCursor(cursor);
        if (mGridView.getAdapter() == null) {
            mGridView.setAdapter(mGridAdapter);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mGridAdapter.swapCursor(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mGridView.setAdapter(null);
        if (mGridAdapter != null) {
            mGridAdapter.destroy();
            mGridAdapter = null;
        }
    }
}
