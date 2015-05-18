package se.karpestam.mediashow;

import android.net.Uri;

/**
 * Created by Mats on 2015-05-05.
 */
public abstract class CursorLoaderQuery {
    private final Uri mUri;
    private final String[] mProjection;
    private final String mSelection;
    private final String[] mSelectionArgs;
    private final String mSortOrder;

    public CursorLoaderQuery(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        mUri = uri;
        mProjection = projection;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mSortOrder = sortOrder;
    }

    public static CursorLoaderQuery getCursorLoaderQuery(int number) {
        switch (number) {
            case 0:
                return new PhotosAndVideosQuery();
            case 1:
                return new PhotosQuery();
            case 2:
                return new VideosQuery();
            default:
                return new PhotosAndVideosQuery();
        }
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
