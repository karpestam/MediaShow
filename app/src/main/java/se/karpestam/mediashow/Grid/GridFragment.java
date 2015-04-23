package se.karpestam.mediashow.Grid;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.transition.ChangeTransform;
import android.transition.Transition;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import se.karpestam.mediashow.Fullscreen.FullScreenActivity;
import se.karpestam.mediashow.Fullscreen.FullscreenFragment;
import se.karpestam.mediashow.R;


public class GridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String FRAGMENT_TAG = GridFragment.class.getSimpleName();
    private static final String GRID_POSITION = "grid_position";
    private Context mContext;
    private WindowManager mWindowManager;

    private int mLatestGridPosition = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("MATS", "GridFragment onCreateView");
        mContext = getActivity().getApplicationContext();
        return inflater.inflate(R.layout.grid_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    public void onResume() {
        Log.d("MATS", "GridFragment onResume");
        super.onResume();
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onPause() {
        Log.d("MATS", "GridFragment onPause");
        super.onPause();
        mLatestGridPosition = ((GridView) getView().findViewById(R.id.grid_view)).getFirstVisiblePosition();
        getLoaderManager().destroyLoader(0);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(mContext, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null,
                null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
        int numColumns = mContext.getResources().getInteger(R.integer.grid_columns);
        Point point = new Point();
        mWindowManager.getDefaultDisplay().getSize(point);
        final GridView gridView = (GridView) getView().findViewById(R.id.grid_view);
        int spacing = gridView.getHorizontalSpacing();
        CursorAdapter mediaGridAdapter = new GridAdapter(mContext, cursor, false, point.x,
                numColumns, spacing);
        gridView.setAdapter(mediaGridAdapter);
        gridView.setSelection(mLatestGridPosition);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mContext, FullScreenActivity.class);
                intent.putExtra("START_POSITION", position);
                Log.d("MATS", "onItemClick " + (view instanceof ImageView));
                Transition transition = new ChangeTransform();
                getActivity().getWindow().setEnterTransition(transition);
                getActivity().getWindow().setExitTransition(transition);
                //ActivityOptions.makeSceneTransitionAnimation(getActivity(), view, "eh").toBundle()
                getActivity().startActivity(intent);
            }
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
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
        Log.d("MATS", "GridFragment onSaveInstanceState " + outState);
        View view = getView();
        if (view != null) {
            outState.putInt(GRID_POSITION,
                    ((GridView) view.findViewById(R.id.grid_view)).getFirstVisiblePosition());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        Log.d("MATS", "GridFragment onViewStateRestored " + savedInstanceState);
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            ((GridView) getView().findViewById(R.id.grid_view))
                    .setSelection(savedInstanceState.getInt(GRID_POSITION));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (getView() != null) {
            GridView gridView = (GridView) getView().findViewById(R.id.grid_view);
            if (gridView != null && gridView.getAdapter() != null) {
                ((GridAdapter) gridView.getAdapter()).destroy();
            }
        }

    }
}
