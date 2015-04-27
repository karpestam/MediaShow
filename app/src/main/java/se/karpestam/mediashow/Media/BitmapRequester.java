package se.karpestam.mediashow.Media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

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
    private static final int NUMBER_OF_WORKER_THREADS = 3;
    private Map<String, RequestListener> mListeners;
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

            RequestResult requestResult = (RequestResult)msg.obj;
            if (requestResult.mIsResultOk && msg.arg1 != 1) {
                mBitmapCache.put(requestResult.mPath, requestResult.mBitmap);
            }
            Collection<RequestListener> listeners = mListeners.values();
            for (RequestListener listener : listeners) {
                listener.onRequestResult(requestResult);
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

    public void addListener(String id, RequestListener requestListener) {
        mListeners.put(id, requestListener);
    }

    public void removeListener(String id) {
        mListeners.remove(id);
    }

    public Bitmap requestBitmap(final RequestJob requestJob) {
        Bitmap bitmap = mBitmapCache.get(requestJob.mPath);
        if (bitmap == null || requestJob.mHighQuality) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = null;
                    if (!requestJob.mHighQuality) {
                        bitmap = mBitmapDiskCache.get(requestJob.mPath);
                    }
                    if (bitmap == null) {
                        if (requestJob.mMediaType == MediaStore.Files.FileColumns
                                .MEDIA_TYPE_IMAGE) {
                            bitmap = BitmapHelper.resize(requestJob.mPath, requestJob.mWidth,
                                    requestJob.mHeight, requestJob.mOrientation,
                                    (requestJob.mHighQuality ? Config.ARGB_8888 : Config.RGB_565));
                        } else {
                            bitmap = ThumbnailUtils.createVideoThumbnail(requestJob.mPath,
                                    requestJob.mHighQuality ? MediaStore.Video.Thumbnails
                                            .FULL_SCREEN_KIND : MediaStore.Video.Thumbnails
                                            .MINI_KIND);
                            Log.d("MATS",
                                    "bitmap " + bitmap.getWidth() + " " + bitmap.getHeight());
                        }
                        if (!requestJob.mHighQuality) {
                            mBitmapDiskCache.add(requestJob.mPath, bitmap);
                        }
                    }
                    Message message = mMainHandler.obtainMessage();
                    message.obj = new RequestResult(requestJob.mPath, bitmap,
                            requestJob.mImageView, requestJob.mListenerId,
                            bitmap != null ? true : false);
                    message.arg1 = requestJob.mHighQuality ? 1 : 0;
                    mMainHandler.sendMessage(message);
                }
            };
            mExecutor.execute(runnable);
        }
        return bitmap;
    }
}