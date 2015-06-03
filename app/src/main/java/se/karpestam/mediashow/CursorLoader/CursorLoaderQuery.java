package se.karpestam.mediashow.CursorLoader;

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

    public enum CursorQuery {
        NONE,
        PHOTOS_AND_VIDEOS,
        PHOTOS,
        VIDEOS,
        ALL_FOLDERS,
        FOLDER
    }

    public CursorLoaderQuery(Uri uri, String[] projection, String selection,
                             String[] selectionArgs, String sortOrder) {
        mUri = uri;
        mProjection = projection;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mSortOrder = sortOrder;
    }

    public static CursorLoaderQuery getCursorLoaderQuery(CursorQuery cursorQuery) {
        switch (cursorQuery) {
            case PHOTOS_AND_VIDEOS:
                return new PhotosAndVideosQuery();
            case PHOTOS:
                return new PhotosQuery();
            case VIDEOS:
                return new VideosQuery();
            case FOLDER:
                return new FoldersQuery();
            case NONE:
                return null;
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
