package se.karpestam.mediashow.Media;

import android.graphics.Bitmap;
import android.util.LruCache;

public class BitmapCache {
    /* 64 MegaByte of cache memory. */
    private static final int CACHE_SIZE = 64 * 1024 * 1024;
    private LruCache<String, Bitmap> mBitmapLruCache;

    public BitmapCache() {
        mBitmapLruCache = new LruCache<String, Bitmap>(CACHE_SIZE) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    public void put(String id, Bitmap bitmap) {
        mBitmapLruCache.put(id, bitmap);
    }

    public Bitmap get(String path) {
        return mBitmapLruCache.get(path);
    }
}
