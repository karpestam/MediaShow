package se.karpestam.mediashow.Media;

import android.widget.ImageView;

public class BitmapRequest {
    public String mPath;
    public int mOrientation;
    public ImageView mImageView;
    public String mListenerId;
    public boolean mHighQuality;
    public int mWidth;
    public int mHeight;
    public int mMediaType;

    public BitmapRequest(String path, int orientation, ImageView imageView, String listenerId,
            boolean highQuality, int width, int height, int mediaType) {
        mPath = path;
        mImageView = imageView;
        mOrientation = orientation;
        mListenerId = listenerId;
        mHighQuality = highQuality;
        mWidth = width;
        mHeight = height;
        mMediaType = mediaType;
    }
}
