package se.karpestam.mediashow.Media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BitmapRequester {
    /* This instance, single-ton. */
    private static BitmapRequester mBitmapRequester;
    private static final int NUMBER_OF_WORKER_THREADS = 2;
    private Map<String, BitmapResultListener> mListeners;
    private ThreadPoolExecutor mExecutor;
    private BitmapCache mBitmapCache;
    private BitmapDiskCache mBitmapDiskCache;
    /**
     * Handles all listener callbacks and puts the decoded Bitmap to the cache.
     */
    private Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            BitmapResult bitmapResult = (BitmapResult)msg.obj;
            if (bitmapResult.mBitmap != null) {
                mBitmapCache.put(bitmapResult.mPath, bitmapResult.mBitmap);
            }
            Collection<BitmapResultListener> listeners = mListeners.values();
            for (BitmapResultListener listener : listeners) {
                listener.onRequestResult(bitmapResult);
            }
        }
    };

    private BitmapRequester(Context context) {
        mBitmapCache = new BitmapCache();
        mBitmapDiskCache = new BitmapDiskCache(context);
        /* Create thread pool that executes Runnable. Execution order is LIFO(last in first out)
         and maximum queue length is 30.
          */
        mExecutor = new ThreadPoolExecutor(NUMBER_OF_WORKER_THREADS, NUMBER_OF_WORKER_THREADS, 0,
                TimeUnit.NANOSECONDS, new LinkedBlockingDeque<Runnable>(35) {
            @Override
            public boolean offer(Runnable runnable) {
                if (remainingCapacity() == 0) {
                    super.removeLast();
                }
                return super.offerFirst(runnable);
            }
        }, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setPriority(Thread.MIN_PRIORITY);
                return thread;
            }
        });
        mListeners = new HashMap<>();
    }

    /**
     * Returns an instance of {@link BitmapRequester}. This is a single-ton.
     *
     * @return an instance of {@link BitmapRequester}.
     */
    public static BitmapRequester getInstance(Context context) {
        if (mBitmapRequester == null) {
            mBitmapRequester = new BitmapRequester(context);
        }
        return mBitmapRequester;
    }

    public void addListener(String id, BitmapResultListener bitmapResultListener) {
        mListeners.put(id, bitmapResultListener);
    }

    public void removeListener(String id) {
        mListeners.remove(id);
    }

    public Bitmap requestBitmap(final BitmapRequest bitmapRequest) {
        Bitmap bitmap = mBitmapCache.get(bitmapRequest.mPath);
        if (bitmap == null || bitmapRequest.mHighQuality) {
            mExecutor.execute(requestRunnable(bitmapRequest));
        }
        return bitmap;
    }

    private Runnable requestRunnable(final BitmapRequest bitmapRequest) {
        return new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = null;
                if (!bitmapRequest.mHighQuality) {
                    bitmap = mBitmapDiskCache.get(bitmapRequest.mPath);
                }
                if (bitmap == null) {
                    if (bitmapRequest.mMediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                        bitmap = BitmapHelper
                                .resize(bitmapRequest.mPath, bitmapRequest.mWidth, bitmapRequest.mHeight,
                                        bitmapRequest.mOrientation,
                                        (bitmapRequest.mHighQuality ? Config.ARGB_8888 : Config
                                                .RGB_565));
                    } else {
                        bitmap = ThumbnailUtils.createVideoThumbnail(bitmapRequest.mPath,
                                bitmapRequest.mHighQuality ? MediaStore.Video.Thumbnails
                                        .FULL_SCREEN_KIND : MediaStore.Video.Thumbnails.MINI_KIND);
                    }
                    if (!bitmapRequest.mHighQuality) {
                        mBitmapDiskCache.add(bitmapRequest.mPath, bitmap);
                    }
                }
                Message message = mMainHandler.obtainMessage();
                message.obj = new BitmapResult(bitmapRequest.mPath, bitmap, bitmapRequest.mImageView,
                        bitmapRequest.mListenerId);
                message.arg1 = bitmapRequest.mHighQuality ? 1 : 0;
                mMainHandler.sendMessage(message);
            }
        };
    }

}