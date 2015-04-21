package se.karpestam.mediashow.MediaDecoder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MediaItemDecoder {
    private static final int NUMBER_OF_WORKER_THREADS = 4;
    /* 64 MegaByte of cache memory.. */
    private static final int CACHE_SIZE = 64 * 1024 * 1024;
    private static MediaItemDecoder mMediaItemDecoder;
    private LruCache<Integer, Bitmap> mBitmapLruCache;
    private Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            MediaItem mediaItem = (MediaItem)msg.obj;
            if (mediaItem.mIsResultOk) {
                mBitmapLruCache.put(mediaItem.mId, mediaItem.mBitmap);
            }
            Collection<MediaItemListener> listeners = mListeners.values();
            for (MediaItemListener listener : listeners) {
                listener.onMediaItem(mediaItem);
            }
        }
    };
    private Map<String, MediaItemListener> mListeners;
    ThreadPoolExecutor mExecutor;

    private MediaItemDecoder() {
        mExecutor = new ThreadPoolExecutor(NUMBER_OF_WORKER_THREADS, NUMBER_OF_WORKER_THREADS, 0,
                TimeUnit.NANOSECONDS, new LinkedBlockingDeque<Runnable>(30) {
            @Override
            public boolean offer(Runnable runnable) {
                if (remainingCapacity() == 0) {
                    super.removeLast();
                }
                return super.offerFirst(runnable);
            }
        });

        mBitmapLruCache = new LruCache<Integer, Bitmap>(CACHE_SIZE) {
            @Override
            protected int sizeOf(Integer key, Bitmap value) {
                return value.getByteCount();
            }
        };
        mListeners = new HashMap<>();
    }

    public static MediaItemDecoder getInstance() {
        if (mMediaItemDecoder == null) {
            mMediaItemDecoder = new MediaItemDecoder();
        }
        return mMediaItemDecoder;
    }

    public interface MediaItemListener {
        void onMediaItem(MediaItem mediaItem);
    }

    public void addListener(String id, MediaItemListener mediaItemListener) {
        mListeners.put(id, mediaItemListener);
    }

    public void removeListener(String id) {
        mListeners.remove(id);
    }

    public Bitmap getBitmap(int id) {
        return mBitmapLruCache.get(id);
    }

    public void decode(final MediaItem mediaItem) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inJustDecodeBounds = false;
                options.inSampleSize = 5;
                Message message = mMainHandler.obtainMessage();
                mediaItem.mBitmap = BitmapFactory.decodeFile(mediaItem.mPath, options);
                mediaItem.mIsResultOk = mediaItem.mBitmap != null;
                message.obj = mediaItem;
                mMainHandler.sendMessage(message);
            }
        };
        mExecutor.execute(runnable);
    }

    private static class ListenerHandler extends Handler {

    }
}
