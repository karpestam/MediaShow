package se.karpestam.mediashow.Drawer;

import android.content.ClipData;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import se.karpestam.mediashow.CursorLoader.CursorLoaderQuery;
import se.karpestam.mediashow.R;

public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ItemViewHolder> {

    private final DrawerItemClickListener mDrawerItemClickListener;
    private final Context mContext;
    private CursorLoaderQuery mCursorLoaderQuery;
    private Cursor mBucketCursor = null;

    public DrawerAdapter(Context context, DrawerItemClickListener drawerItemClickListener) {
        mContext = context;
        mDrawerItemClickListener = drawerItemClickListener;
        mCursorLoaderQuery = CursorLoaderQuery.getCursorLoaderQuery(CursorLoaderQuery.CursorQuery.FOLDER);
    }

    public interface DrawerItemClickListener {
        void onDrawerItemAllClicked();

        void onDrawerItemFolderClicked(String bucketId, String bucketDisplayName);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ItemViewType.DRAWER_HEADER:
            case ItemViewType.DRAWER_ITEM_FOLDER_HEADER: {
                View view = parent.inflate(parent.getContext(), R.layout.drawer_header, null);
                ItemViewHolder itemViewHolder = new ItemViewHolder(view);
                itemViewHolder.textView = (TextView) view.findViewById(R.id.item_text);
                return itemViewHolder;
            }
            case ItemViewType.DRAWER_ITEM_ALL:
            case ItemViewType.DRAWER_ITEM_FOLDER: {
                View view = parent.inflate(parent.getContext(), R.layout.drawer_item, null);
                ItemViewHolder itemViewHolder = new ItemViewHolder(view);
                itemViewHolder.textView = (TextView) view.findViewById(R.id.item_text);
                return itemViewHolder;
            }

            default:
                throw new RuntimeException("Not a correct view type.");
        }
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, final int position) {
        switch (position) {

        }
        switch (holder.getItemViewType()) {
            case ItemViewType.DRAWER_HEADER:
            case ItemViewType.DRAWER_ITEM_FOLDER_HEADER:
                holder.textView.setText(R.string.drawer_header_item_all);
                break;
            case ItemViewType.DRAWER_ITEM_ALL:
                holder.textView.setText(R.string.drawer_item_all);
                holder.itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDrawerItemClickListener.onDrawerItemAllClicked();
                    }
                });
                break;
            case ItemViewType.DRAWER_ITEM_FOLDER:
                mBucketCursor.moveToPosition(position - 3);
                final String bucketDisplayName = mBucketCursor.getString(mBucketCursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME));
                holder.textView.setText(bucketDisplayName);
                holder.itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDrawerItemClickListener.onDrawerItemFolderClicked(mBucketCursor.getString(mBucketCursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID)), bucketDisplayName);
                    }
                });
                break;
            default:
                throw new RuntimeException("Not a correct view type.");
        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return ItemViewType.DRAWER_HEADER;
            case 1:
                return ItemViewType.DRAWER_ITEM_ALL;
            case 2:
                return ItemViewType.DRAWER_ITEM_FOLDER_HEADER;
            default:
                return ItemViewType.DRAWER_ITEM_FOLDER;
        }
    }

    @Override
    public int getItemCount() {
        if (mBucketCursor != null && mBucketCursor.getCount() > 0) {
            return 3 + mBucketCursor.getCount();
        }
        return 2;
    }

    public void setBucketCursor(Cursor cursor) {
        mBucketCursor = cursor;
        notifyDataSetChanged();
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
