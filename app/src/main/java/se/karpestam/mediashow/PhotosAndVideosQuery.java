package se.karpestam.mediashow;

import android.provider.MediaStore;

public class PhotosAndVideosQuery extends CursorLoaderQuery {

    public PhotosAndVideosQuery() {
        super(MediaStore.Files.getContentUri("external"), null, MediaStore.Files.FileColumns.MEDIA_TYPE + " = ? OR " + MediaStore.Files
                .FileColumns.MEDIA_TYPE + " = ?", new String[]{String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE), String
                .valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)}, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
    }
}
