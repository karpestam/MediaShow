package se.karpestam.mediashow.Media;

import android.graphics.Bitmap;
import android.widget.ImageView;

public class RequestResult {
    public int mId;
    public ImageView mImageView;
    public String mListenerId;
    public Bitmap mBitmap;
    public boolean mIsResultOk;

    public RequestResult(int id, Bitmap bitmap, ImageView imageView, String listenerId,
            boolean isResultOk) {
        mId = id;
        mBitmap = bitmap;
        mImageView = imageView;
        mListenerId = listenerId;
        mIsResultOk = isResultOk;
    }
}
