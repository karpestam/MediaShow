package se.karpestam.mediashow.CursorLoader;

import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;

public class PhotosQuery extends CursorLoaderQuery {

    public PhotosQuery(final int queryFilter) {
        super(MediaStore.Files.getContentUri("external"), null,
                MediaStore.Files.FileColumns.MEDIA_TYPE + " = ?",
                new String[]{String.valueOf(FileColumns.MEDIA_TYPE_IMAGE)},
                MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC", queryFilter);
    }
}
