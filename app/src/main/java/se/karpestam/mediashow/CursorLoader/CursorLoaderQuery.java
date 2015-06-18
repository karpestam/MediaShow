package se.karpestam.mediashow.CursorLoader;

import android.net.Uri;

public abstract class CursorLoaderQuery {
    int mQueryFilter;
    private final Uri mUri;
    private final String[] mProjection;
    private final String mSelection;
    private final String[] mSelectionArgs;
    private final String mSortOrder;
    public CursorLoaderQuery(Uri uri, String[] projection, String selection,
                             String[] selectionArgs, String sortOrder, int queryFilter) {
        mUri = uri;
        mProjection = projection;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mSortOrder = sortOrder;
        mQueryFilter = queryFilter;
    }

    public Uri getUri() {
        return mUri;
    }

    public String[] getProjection() {
        return mProjection;
    }

    public String getSelection() {
        return mSelection;
    }

    public String[] getSelectionArgs() {
        return mSelectionArgs;
    }

    public String getSortOrder() {
        return mSortOrder;
    }

    abstract void setQueryFilter(int queryFilter);
}
