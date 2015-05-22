package se.karpestam.mediashow.Grid;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import se.karpestam.mediashow.R;

public class DrawerListAdapter extends RecyclerView.Adapter<DrawerListAdapter.ItemViewHolder> implements
        LoaderManager
        .LoaderCallbacks<Cursor> {

    private String[] mData = new String[]{"Photos and videos", "Photos", "Videos"};

    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_ITEM = 2;
    private DrawerItemClickListener mDrawerItemClickListener;

    public DrawerListAdapter(DrawerItemClickListener drawerItemClickListener) {
        mDrawerItemClickListener = drawerItemClickListener;
    }

    interface DrawerItemClickListener {
        void onDrawerItemClicked(int position);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER: {
//                View view = LayoutInflater.from(parent.getContext())
//                        .inflate(R.layout.drawer_header, parent, false);
                View view = parent.inflate(parent.getContext(), R.layout.drawer_header, null);
                ItemViewHolder itemViewHolder = new ItemViewHolder(view);
                itemViewHolder.textView = (TextView)view.findViewById(R.id.item_text);
                return itemViewHolder;
            }
            case VIEW_TYPE_ITEM: {
//                View view = LayoutInflater.from(parent.getContext())
//                        .inflate(R.layout.drawer_header, parent, false);
                View view = parent.inflate(parent.getContext(), R.layout.drawer_item, null);
                ItemViewHolder itemViewHolder = new ItemViewHolder(view);
                itemViewHolder.textView = (TextView)view.findViewById(R.id.item_text);
                return itemViewHolder;
            }
            default:
                throw new RuntimeException("Not a correct view type.");
        }
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_HEADER:
                holder.textView.setText(mData[position]);
                break;
            case VIEW_TYPE_ITEM:
                holder.textView.setText(mData[position]);
                holder.itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDrawerItemClickListener.onDrawerItemClicked(position);
                    }
                });
                break;
            default:
                throw new RuntimeException("Not a correct view type.");
        }
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_ITEM;
//        switch (position) {
//        }
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


    static class ItemViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
        TextView textView;

        public ItemViewHolder(View v) {
            super(v);
//            mTextView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
//            Log.d("MATS", "onClick");
//            CursorLoaderQuery cursorLoaderQuery = null;
//            if (mTextView.getText().equals(mData[1])) {
//                cursorLoaderQuery = CursorLoaderQuery.getCursorLoaderQuery(0);
//            } else if (mTextView.getText().equals(mData[2])) {
//                cursorLoaderQuery = CursorLoaderQuery.getCursorLoaderQuery(1);
//            } else if (mTextView.getText().equals(mData[3])) {
//                cursorLoaderQuery = CursorLoaderQuery.getCursorLoaderQuery(3);
//            }
//            mDrawerItemClickListener.onDrawerItemClicked(cursorLoaderQuery);
        }

    }
}
