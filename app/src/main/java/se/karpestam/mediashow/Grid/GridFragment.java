package se.karpestam.mediashow.Grid;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
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

import se.karpestam.mediashow.Fullscreen.FullscreenFragment;
import se.karpestam.mediashow.R;


public class GridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String GRID_POSITION = "grid_position";
    private Context mContext;
    private WindowManager mWindowManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity().getApplicationContext();
        return inflater.inflate(R.layout.grid_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().getActionBar().hide();
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(mContext, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
        int numColumns = mContext.getResources().getInteger(R.integer.grid_columns);
        Point point = new Point();
        mWindowManager.getDefaultDisplay().getSize(point);
        CursorAdapter mediaGridAdapter = new GridAdapter(mContext, cursor, false, point.x);
        GridView gridView = (GridView) getView().findViewById(R.id.grid_adapter);
        gridView.setNumColumns(point.x <= 720 ? 3 : 4);
        gridView.setAdapter(mediaGridAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.remove(GridFragment.this);
                Log.d("MATS", "gridfragment position=" + position);
                ft.replace(R.id.grid_fragment, new FullscreenFragment(cursor.getPosition()));
                ft.commit();
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(GRID_POSITION, ((GridView) getView().findViewById(R.id.grid_adapter)).getFirstVisiblePosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            ((GridView) getView().findViewById(R.id.grid_adapter)).setSelection(savedInstanceState.getInt(GRID_POSITION));
        }
    }
}
