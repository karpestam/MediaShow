package se.karpestam.mediashow.MediaDecoder;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import java.io.Serializable;
import java.lang.ref.WeakReference;

/**
 * Created by Mats on 2015-04-16.
 */
public class MediaItem {
    public int mId;
    public String mPath;
    public int mOrientation;
    public ImageView mImageView;
    public String mListenerId;
    public Bitmap mBitmap;
    public boolean mIsResultOk;
    public MediaItem(int id, String path, int orientation) {
        mId = id;
        mPath = path;
        mOrientation = orientation;
    }
}
