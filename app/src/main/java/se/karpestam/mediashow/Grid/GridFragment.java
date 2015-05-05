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

import se.karpestam.mediashow.Constants;
import se.karpestam.mediashow.R;


public class GridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String FRAGMENT_TAG = GridFragment.class.getSimpleName();
    private static final String GRID_POSITION = "grid_position";
    private Context mContext;
    private WindowManager mWindowManager;
    private int mGridStartPosition = 0;
    private GridAdapter mGridAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(Constants.LOG_TAG, GridFragment.class.getSimpleName() + " onCreateView() " +
                "savedInstanceState=" + savedInstanceState);
        mContext = getActivity().getApplicationContext();
        if (savedInstanceState != null) {
            mGridStartPosition = savedInstanceState.getInt(GRID_POSITION);
        }
        RecyclerView recyclerView = (RecyclerView)inflater
                .inflate(R.layout.recyclerview, container, false);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext,
                mContext.getResources().getInteger(R.integer.grid_columns));
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);
        return recyclerView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWindowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
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
        return new CursorLoader(mContext, MediaStore.Files.getContentUri("external"), null,
                MediaStore.Files.FileColumns.MEDIA_TYPE + " = ? OR " + MediaStore.Files
                        .FileColumns.MEDIA_TYPE + " = ?",
                new String[]{String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE), String
                        .valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)},
                MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
        if (mGridAdapter == null) {
            int numColumns = mContext.getResources().getInteger(R.integer.grid_columns);
            final Point point = new Point();
            mWindowManager.getDefaultDisplay().getSize(point);
            final RecyclerView gridView = (RecyclerView)getView().findViewById(R.id.grid_view);
            mGridAdapter = new GridAdapter(cursor, mContext, point.x, numColumns, 0);
            mGridAdapter.setHasStableIds(true);
            gridView.setAdapter(mGridAdapter);
        } else {
            mGridAdapter.swapCursor(cursor);
        }
//        gridView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
//            @Override
//            public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id,
//                    boolean checked) {
//                actionMode.setTitle("" + gridView.getCheckedItemCount());
//                gridAdapter.notifyDataSetInvalidated();
//            }
//
//            @Override
//            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
//                actionMode.getMenuInflater().inflate(R.menu.menu_actions, menu);
//                return true;
//            }
//
//            @Override
//            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
//                return false;
//            }
//
//            @Override
//            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
//                switch (menuItem.getItemId()) {
//                    case R.id.action_share:
//                        Log.d(Constants.LOG_TAG, "Share=" + gridView.getCheckedItemPositions());
//                        SparseBooleanArray items = gridView.getCheckedItemPositions();
//                        for (int i = 0; i < items.size(); i++) {
//                            if (items.valueAt(i)) {
//                                Cursor cursor = (Cursor)gridAdapter.getItem(i);
//                                Log.d("MATS", "selected path=" + cursor.getString(
//                                        cursor.getColumnIndex(MediaStore.Files.FileColumns
// .DATA)));
//                            }
//                        }
//                        return true;
//                    case R.id.action_delete:
//                        return true;
//                }
//                return false;
//            }
//
//            @Override
//            public void onDestroyActionMode(ActionMode actionMode) {
//
//            }
//        });
//        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Fragment fragment = new FullscreenFragment();
//                Bundle bundle = new Bundle();
//                bundle.putInt("START_POSITION", position);
//                fragment.setArguments(bundle);
//                getFragmentManager().beginTransaction()
//                        .replace(R.id.fragment, fragment, FullscreenFragment.FRAGMENT_TAG)
//                        .addToBackStack(FullscreenFragment.FRAGMENT_TAG).commit();
//            }
//        });
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
//            ((RecyclerView)getView().findViewById(R.id.grid_view))
//                    .setSelection(savedInstanceState.getInt(GRID_POSITION));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (mGridAdapter != null) {
            mGridAdapter.destroy();
        }

    }
}
