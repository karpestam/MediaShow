package se.karpestam.mediashow.CursorLoader;

import android.provider.MediaStore;

public class FolderPhotosQuery extends CursorLoaderQuery {

    public FolderPhotosQuery(final String bucketId) {
        super(MediaStore.Files.getContentUri("external"), null, MediaStore.Files
                .FileColumns.MEDIA_TYPE + " = ?)", new String[]{bucketId, String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)}, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
    }
}
