package se.karpestam.mediashow.Grid;

import android.content.ContentResolver;
import android.content.Context;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorJoiner;
import android.database.CursorWrapper;
import android.database.DataSetObserver;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.Media;
import android.provider.MediaStore.Video.VideoColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.GridView;

import se.karpestam.mediashow.Constants;
import se.karpestam.mediashow.Fullscreen.FullscreenFragment;
import se.karpestam.mediashow.R;


public class GridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String FRAGMENT_TAG = GridFragment.class.getSimpleName();
    private static final String GRID_POSITION = "grid_position";
    private Context mContext;
    private WindowManager mWindowManager;

    private int mGridStartPosition = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(Constants.LOG_TAG, GridFragment.class.getSimpleName() + " onCreateView() " +
                "savedInstanceState=" + savedInstanceState);
        mContext = getActivity().getApplicationContext();
        if (savedInstanceState != null) {
            mGridStartPosition = savedInstanceState.getInt(GRID_POSITION);
        }
        return inflater.inflate(R.layout.grid_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWindowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this).forceLoad();
    }

    @Override
    public void onPause() {
        super.onPause();
        mGridStartPosition = ((GridView)getView().findViewById(R.id.grid_view))
                .getFirstVisiblePosition();
        getLoaderManager().destroyLoader(0);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(mContext, MediaStore.Files.getContentUri("external"), null,
                MediaColumns.MIME_TYPE + " = ? OR " + MediaColumns.MIME_TYPE + " = ?", new
                String[]{"image/jpeg",
                "video/mp4"},
                MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
        int numColumns = mContext.getResources().getInteger(R.integer.grid_columns);
        Point point = new Point();
        mWindowManager.getDefaultDisplay().getSize(point);
        final GridView gridView = (GridView)getView().findViewById(R.id.grid_view);
        int spacing = gridView.getHorizontalSpacing();
        CursorAdapter mediaGridAdapter = new GridAdapter(mContext, cursor, false, point.x,
                numColumns, spacing);
        gridView.setAdapter(mediaGridAdapter);
        gridView.setSelection(mGridStartPosition);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Fragment fragment = new FullscreenFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("START_POSITION", position);
                fragment.setArguments(bundle);
                getFragmentManager().beginTransaction()
                        .replace(R.id.main_view, fragment, FullscreenFragment.FRAGMENT_TAG)
                        .addToBackStack(FullscreenFragment.FRAGMENT_TAG).commit();
            }
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
                    long id) {
                view.setSelected(true);
                return false;
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(GRID_POSITION, mGridStartPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            ((GridView)getView().findViewById(R.id.grid_view))
                    .setSelection(savedInstanceState.getInt(GRID_POSITION));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (getView() != null) {
            GridView gridView = (GridView)getView().findViewById(R.id.grid_view);
            if (gridView != null && gridView.getAdapter() != null) {
                ((GridAdapter)gridView.getAdapter()).destroy();
            }
        }

    }
}
