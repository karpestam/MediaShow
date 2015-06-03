package se.karpestam.mediashow.Drawer;

import se.karpestam.mediashow.CursorLoader.CursorLoaderQuery;

public class DrawerItem {
    private final int mItemViewType;
    private final int mTextResourceId;
    private final String mText;
    private final String mBucketId;

    public DrawerItem(int itemViewType, int textResourceId) {
        mItemViewType = itemViewType;
        mTextResourceId = textResourceId;
        mText = null;
        mBucketId = null;
    }
        public DrawerItem( int itemViewType, String text){
            mItemViewType = itemViewType;
            mTextResourceId = -1;
            mText = text;
            mBucketId = null;
        }

        public DrawerItem( int itemViewType, String text, String bucketId){
            mItemViewType = itemViewType;
            mTextResourceId = -1;
            mText = text;
            mBucketId = bucketId;
        }

    public int getItemViewType() {
        return mItemViewType;
    }

    public int getTextResourceId() {
        return mTextResourceId;
    }

    public String getText() {
        return mText;
    }

    public String getBucketId() {
        return mBucketId;
    }
}
