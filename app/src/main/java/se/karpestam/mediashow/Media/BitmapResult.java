package se.karpestam.mediashow.Media;

import android.graphics.Bitmap;
import android.widget.ImageView;

public class BitmapResult {
    public String mPath;
    public ImageView mImageView;
    public String mListenerId;
    public Bitmap mBitmap;

    public BitmapResult(String path, Bitmap bitmap, ImageView imageView, String listenerId) {
        mPath = path;
        mBitmap = bitmap;
        mImageView = imageView;
        mListenerId = listenerId;
    }
}
