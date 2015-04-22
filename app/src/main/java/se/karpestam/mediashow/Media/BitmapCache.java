package se.karpestam.mediashow.Media;

import android.graphics.Bitmap;
import android.util.LruCache;

public class BitmapCache {
    private static BitmapCache mBitmapCache;
    /* 64 MegaByte of cache memory. */
    private static final int CACHE_SIZE = 32 * 1024 * 1024;
    private LruCache<Integer, Bitmap> mBitmapLruCache;

    private BitmapCache() {
        mBitmapLruCache = new LruCache<Integer, Bitmap>(CACHE_SIZE) {
            @Override
            protected int sizeOf(Integer key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    public static BitmapCache getInstance() {
        if (mBitmapCache == null) {
            mBitmapCache = new BitmapCache();
        }
        return mBitmapCache;
    }

    public void put(int id, Bitmap bitmap) {
        mBitmapLruCache.put(id, bitmap);
    }

    public Bitmap get(int id) {
        return mBitmapLruCache.get(id);
    }
}
