package se.karpestam.mediashow;

import android.provider.MediaStore;

public class VideosQuery extends CursorLoaderQuery {

    public VideosQuery() {
        super(MediaStore.Files.getContentUri("external"), null,
                MediaStore.Files.FileColumns.MEDIA_TYPE + " = ?",
                new String[]{String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)},
                MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
    }
}
