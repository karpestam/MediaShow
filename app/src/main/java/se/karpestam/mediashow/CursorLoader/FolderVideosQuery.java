package se.karpestam.mediashow.CursorLoader;

import android.provider.MediaStore;

public class FolderVideosQuery extends CursorLoaderQuery {

    public FolderVideosQuery(final String bucketId) {
        super(MediaStore.Files.getContentUri("external"), null, MediaStore.Files
                .FileColumns.MEDIA_TYPE + " = ?)", new String[]{bucketId, String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)}, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
    }
}
