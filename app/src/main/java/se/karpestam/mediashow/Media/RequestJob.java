package se.karpestam.mediashow.Media;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by Mats on 2015-04-16.
 */
public class RequestJob {
    public int mId;
    public String mPath;
    public int mOrientation;
    public ImageView mImageView;
    public String mListenerId;
    public boolean mHighQuality;
    public int mWidth;
    public int mHeight;

    public RequestJob(int id, String path, int orientation, ImageView imageView, String listenerId,
            boolean highQuality, int width, int height) {
        mId = id;
        mPath = path;
        mImageView = imageView;
        mImageView.setTag(id);
        mOrientation = orientation;
        mListenerId = listenerId;
        mHighQuality = highQuality;
        mWidth = width;
        mHeight = height;
    }
}
