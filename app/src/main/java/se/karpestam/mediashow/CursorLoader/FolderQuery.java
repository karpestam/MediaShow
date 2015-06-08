package se.karpestam.mediashow.CursorLoader;

import android.provider.MediaStore;

public class FolderQuery extends CursorLoaderQuery {

    private final String mBucketId;
    public FolderQuery(final String bucketId, final int queryFilter) {
        super(MediaStore.Files.getContentUri("external"), null, MediaStore.Images.ImageColumns.BUCKET_ID + " = ? AND (" + MediaStore.Files.FileColumns.MEDIA_TYPE + " = ? OR " + MediaStore.Files
                .FileColumns.MEDIA_TYPE + " = ?)", new String[]{bucketId, String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE), String
                .valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)}, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC", queryFilter);
        mBucketId = bucketId;
    }

    @Override
    void setQueryFilter(int queryFilter) {
        mQueryFilter = queryFilter;
    }
}
