package se.karpestam.mediashow.Media;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

class BitmapHelper {

    private BitmapHelper() {
        /* Prevent instantiation. */
    }

    public static Bitmap resize(String sourcePath, int width, int height, int orientation,
            Bitmap.Config config) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /* First decode with inJustDecodeBounds=true to check dimensions. */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(sourcePath, options);

        if (isNotSameAspect(width, height, options)) {
            int oldWidth = options.outHeight;
            int oldHeight = options.outWidth;
            options.outWidth = oldWidth;
            options.outHeight = oldHeight;
        }
        options.inSampleSize = calculateInSampleSize(options, width, height);
        /* Decode bitmap with inSampleSize set. */
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = config;
        Bitmap bitmap = BitmapFactory.decodeFile(sourcePath, options);
        if (orientation == 90 || orientation == 270) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);
            bitmap = Bitmap
                    .createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,
                            true);
        }
        return bitmap;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth,
            int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) >
                    reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize+1;
    }

    private static boolean isNotSameAspect(int width, int height, BitmapFactory.Options options) {
        if (width < height && options.outWidth > options.outHeight) {
            return true;
        }
        return false;
    }
}
