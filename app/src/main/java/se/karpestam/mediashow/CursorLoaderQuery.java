package se.karpestam.mediashow;

import android.net.Uri;

/**
 * Created by Mats on 2015-05-05.
 */
public abstract class CursorLoaderQuery {
    Uri mUri;
    String[] mProjection;
    String mSelection;
    String[] mSelectionArgs;
    String mSortOrder;

    public CursorLoaderQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        mUri = uri;
        mProjection = projection;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mSortOrder = sortOrder;
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
}
