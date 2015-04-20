package se.karpestam.mediashow.MediaDecoder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MediaItemDecoder {
    private static MediaItemDecoder mMediaItemDecoder;
    private static final int NUMBER_OF_BITMAP_HANDLERS = 6;
    private Handler mMainHandler;
    private BitmapHandler[] mBitmapHandlers;
    private HandlerThread[] mBitmapHandlerThreads;
    private int mCurrentHandler;
    private Map<String, MediaItemListener> mListeners;

    private MediaItemDecoder() {
        mListeners = new HashMap<>();
        mBitmapHandlers = new BitmapHandler[NUMBER_OF_BITMAP_HANDLERS];
        mBitmapHandlerThreads = new HandlerThread[NUMBER_OF_BITMAP_HANDLERS];
        for (int i = 0; i < NUMBER_OF_BITMAP_HANDLERS; i++) {
            mBitmapHandlerThreads[i] = new HandlerThread("bitmap_handler " + i);
            mBitmapHandlerThreads[i].start();
            mBitmapHandlers[i] = new BitmapHandler(mBitmapHandlerThreads[i].getLooper());
        }
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

    public void decode(MediaItem mediaItem) {
        Message msg = mBitmapHandlers[mCurrentHandler].obtainMessage();
        msg.obj = mediaItem;
        mBitmapHandlers[mCurrentHandler].sendMessage(msg);

        mCurrentHandler++;
        if (mCurrentHandler > NUMBER_OF_BITMAP_HANDLERS - 1) {
            mCurrentHandler = 0;
        }
    }

    private class BitmapHandler extends Handler {

        public BitmapHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            MediaItem mediaItem = (MediaItem) msg.obj;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inJustDecodeBounds = false;
            options.inSampleSize = 5;
            Message message = mMainHandler.obtainMessage();
            mediaItem.mBitmap = BitmapFactory.decodeFile(mediaItem.mPath, options);
            message.obj = mediaItem;
            mMainHandler.sendMessage(message);
        }
    }
}
