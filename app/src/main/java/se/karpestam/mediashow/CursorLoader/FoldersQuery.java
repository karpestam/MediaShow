package se.karpestam.mediashow.CursorLoader;

import android.provider.MediaStore;

public class FoldersQuery extends CursorLoaderQuery {

    public FoldersQuery() {
        super(MediaStore.Files.getContentUri("external"), new String[]{"DISTINCT " + MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.BUCKET_ID}, MediaStore.Files.FileColumns.MEDIA_TYPE + " = ? OR " + MediaStore.Files
                .FileColumns.MEDIA_TYPE + " = ?", new String[]{String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE), String
                .valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)}, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
    }
}
