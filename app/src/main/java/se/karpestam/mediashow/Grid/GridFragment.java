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
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

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
                MediaStore.Files.FileColumns.MEDIA_TYPE + " = ? OR " + MediaStore.Files
                        .FileColumns.MEDIA_TYPE + " = ?",
                new String[]{String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE), String
                        .valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)},
                MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
        int numColumns = mContext.getResources().getInteger(R.integer.grid_columns);
        final Point point = new Point();
        mWindowManager.getDefaultDisplay().getSize(point);
        final GridView gridView = (GridView)getView().findViewById(R.id.grid_view);
        int spacing = gridView.getHorizontalSpacing();
        final CursorAdapter gridAdapter = new GridAdapter(mContext, cursor, false, point.x,
                numColumns, spacing);
        gridView.setAdapter(gridAdapter);
        gridView.setSelection(mGridStartPosition);
        gridView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id,
                    boolean checked) {
                actionMode.setTitle("" + gridView.getCheckedItemCount());
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                actionMode.getMenuInflater().inflate(R.menu.menu_actions, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }
        });
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
