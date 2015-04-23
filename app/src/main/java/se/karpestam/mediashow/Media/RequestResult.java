package se.karpestam.mediashow.Media;

import android.graphics.Bitmap;
import android.widget.ImageView;

public class RequestResult {
    public String mPath;
    public ImageView mImageView;
    public String mListenerId;
    public Bitmap mBitmap;
    public boolean mIsResultOk;

    public RequestResult(String path, Bitmap bitmap, ImageView imageView, String listenerId,
                         boolean isResultOk) {
        mPath = path;
        mBitmap = bitmap;
        mImageView = imageView;
        mListenerId = listenerId;
        mIsResultOk = isResultOk;
    }
}
