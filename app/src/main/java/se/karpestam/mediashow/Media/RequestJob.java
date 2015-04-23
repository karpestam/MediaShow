package se.karpestam.mediashow.Media;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by Mats on 2015-04-16.
 */
public class RequestJob {
    public String mPath;
    public int mOrientation;
    public ImageView mImageView;
    public String mListenerId;
    public boolean mHighQuality;
    public int mWidth;
    public int mHeight;

    public RequestJob(String path, int orientation, ImageView imageView, String listenerId,
                      boolean highQuality, int width, int height) {
        mPath = path;
        mImageView = imageView;
        mOrientation = orientation;
        mListenerId = listenerId;
        mHighQuality = highQuality;
        mWidth = width;
        mHeight = height;
    }
}
