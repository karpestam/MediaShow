package se.karpestam.mediashow.MediaDecoder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public class MediaItemDecoder {
    private static final int NUMBER_OF_BITMAP_HANDLERS = 6;
    private Handler mMainHandler;
    private MediaItemListener mListener;
    private BitmapHandler[] mBitmapHandlers;
    private HandlerThread[] mBitmapHandlerThreads;
    private int mCurrentHandler;

    public MediaItemDecoder(MediaItemListener gridItemCallback) {
        mListener = gridItemCallback;
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

                mListener.onMediaItem((MediaItem) msg.obj);
            }
        };
    }

    public interface MediaItemListener {
        void onMediaItem(MediaItem mediaItem);
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
            mediaItem.mBitmap = BitmapFactory.decodeFile(mediaItem.mPath, options);
            Message message = mMainHandler.obtainMessage();
            message.obj = mediaItem;
            mMainHandler.sendMessage(message);
        }
    }
}
