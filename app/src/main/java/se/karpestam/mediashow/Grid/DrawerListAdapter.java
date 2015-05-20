package se.karpestam.mediashow.Grid;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import se.karpestam.mediashow.CursorLoaderQuery;
import se.karpestam.mediashow.R;

public class DrawerListAdapter extends RecyclerView.Adapter implements LoaderManager
        .LoaderCallbacks<Cursor> {

    private String[] mData = new String[]{"All", "Photos and videos", "Photos", "Videos",
            "Folders"};

    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_ITEM = 2;
    private DrawerItemClickListener mDrawerItemClickListener;

    public DrawerListAdapter(DrawerItemClickListener drawerItemClickListener) {
        mDrawerItemClickListener = drawerItemClickListener;
    }

    interface DrawerItemClickListener {
        void onDrawerItemClicked(CursorLoaderQuery cursorLoaderQuery);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                return new HeaderViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.drawer_header, parent, false));
            default:
                return new ItemViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.drawer_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_HEADER:
                ((HeaderViewHolder)holder).bindView(mData[position]);
                break;
            default:
                ((ItemViewHolder)holder).bindView(mData[position]);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
            case 4:
                return VIEW_TYPE_HEADER;
            default:
                return VIEW_TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return mData.length;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextView;

        public HeaderViewHolder(View v) {
            super(v);
            mTextView = (TextView)v.findViewById(R.id.drawer_header_text);
        }

        void bindView(String text) {
            mTextView.setText(text);
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
        private TextView mTextView;

        public ItemViewHolder(View v) {
            super(v);
            mTextView = (TextView)v.findViewById(R.id.drawer_item_text);
            mTextView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Log.d("MATS", "onClick");
            CursorLoaderQuery cursorLoaderQuery = null;
            if (mTextView.getText().equals(mData[1])) {
                cursorLoaderQuery = CursorLoaderQuery.getCursorLoaderQuery(0);
            } else if (mTextView.getText().equals(mData[2])) {
                cursorLoaderQuery = CursorLoaderQuery.getCursorLoaderQuery(1);
            } else if (mTextView.getText().equals(mData[3])) {
                cursorLoaderQuery = CursorLoaderQuery.getCursorLoaderQuery(3);
            }
            mDrawerItemClickListener.onDrawerItemClicked(cursorLoaderQuery);
        }

        void bindView(String text) {
            mTextView.setText(text);
        }
    }
}
