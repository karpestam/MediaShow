package se.karpestam.mediashow.MediaDecoder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MediaItemDecoder {
    private static final int NUMBER_OF_WORKER_THREADS = 8;
    private static final int SIZE_OF_WORKER_QUEUE = 30;
    private static MediaItemDecoder mMediaItemDecoder;
    private Handler mMainHandler;
    private Map<String, MediaItemListener> mListeners;
    private BlockingDeque<Runnable> mWorkQueue;
    ThreadPoolExecutor threadPoolExecutor;

    private MediaItemDecoder() {
        mWorkQueue = new LinkedBlockingDeque<>(SIZE_OF_WORKER_QUEUE);
        threadPoolExecutor = new ThreadPoolExecutor(NUMBER_OF_WORKER_THREADS, NUMBER_OF_WORKER_THREADS, Long.MAX_VALUE, TimeUnit.NANOSECONDS, mWorkQueue);
        mListeners = new HashMap<>();
        mMainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                MediaItem mediaItem = (MediaItem) msg.obj;
                Collection<MediaItemListener> listeners = mListeners.values();
                for (MediaItemListener listener : listeners) {
                    listener.onMediaItem(mediaItem);
                }
            }
        };
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

    public void decode(final MediaItem mediaItem) {
        if (mWorkQueue.size() == SIZE_OF_WORKER_QUEUE) {
            mWorkQueue.removeFirst();
        }
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inJustDecodeBounds = false;
                options.inSampleSize = 5;
                Message message = mMainHandler.obtainMessage();
                mediaItem.mBitmap = new WeakReference<>(BitmapFactory.decodeFile(mediaItem.mPath, options));
                message.obj = mediaItem;
                mMainHandler.sendMessage(message);
            }
        });
//        Message msg = mBitmapHandlers[mCurrentHandler].obtainMessage();
//        msg.obj = mediaItem;
//        mBitmapHandlers[mCurrentHandler].sendMessage(msg);

//        mCurrentHandler++;
//        if (mCurrentHandler > NUMBER_OF_BITMAP_HANDLERS - 1) {
//            mCurrentHandler = 0;
//        }
    }

//    private class BitmapHandler extends Handler {
//
//        public BitmapHandler(Looper looper) {
//            super(looper);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//
//            MediaItem mediaItem = (MediaItem) msg.obj;
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inPreferredConfig = Bitmap.Config.RGB_565;
//            options.inJustDecodeBounds = false;
//            options.inSampleSize = 5;
//            Message message = mMainHandler.obtainMessage();
//            mediaItem.mBitmap = BitmapFactory.decodeFile(mediaItem.mPath, options);
//            message.obj = mediaItem;
//            mMainHandler.sendMessage(message);
//        }
//    }
}
