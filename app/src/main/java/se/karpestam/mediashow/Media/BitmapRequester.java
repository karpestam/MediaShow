package se.karpestam.mediashow.Media;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BitmapRequester {
    /* This instance, single-ton. */
    private static BitmapRequester mBitmapRequester;
    private static final int NUMBER_OF_WORKER_THREADS = 3;
    private Map<String, RequestListener> mListeners;
    private ThreadPoolExecutor mExecutor;

    /**
     * Handles all listener callbacks and puts the decoded Bitmap to the cache.
     */
    private Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            RequestResult requestResult = (RequestResult)msg.obj;
            if (requestResult.mIsResultOk && msg.arg1 != 1) {
                BitmapCache.getInstance().put(requestResult.mId, requestResult.mBitmap);
            }
            Collection<RequestListener> listeners = mListeners.values();
            for (RequestListener listener : listeners) {
                listener.onRequestResult(requestResult);
            }
        }
    };

    private BitmapRequester() {
        /* Create thread pool that executes Runnable. Execution order is LIFO(last in first out)
         and maximum queue length is 30.
          */
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
        mListeners = new HashMap<>();
    }

    /**
     * Returns an instance of {@link BitmapRequester}. This is a single-ton.
     *
     * @return an instance of {@link BitmapRequester}.
     */
    public static BitmapRequester getInstance() {
        if (mBitmapRequester == null) {
            mBitmapRequester = new BitmapRequester();
        }
        return mBitmapRequester;
    }

    public void addListener(String id, RequestListener requestListener) {
        mListeners.put(id, requestListener);
    }

    public void removeListener(String id) {
        mListeners.remove(id);
    }

    public void requestBitmap(final RequestJob requestJob) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // First decode with inJustDecodeBounds=true to check dimensions
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(requestJob.mPath, options);

                if (isNotSameAspect(requestJob, options)) {
                    int width = options.outHeight;
                    int height = options.outWidth;
                    options.outWidth = width;
                    options.outHeight = height;
                }
                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, requestJob.mWidth,
                        requestJob.mHeight);
                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = requestJob.mHighQuality ? Config.ARGB_8888 : Config
                        .RGB_565;
                Bitmap bitmap = BitmapFactory.decodeFile(requestJob.mPath, options);
                if (requestJob.mOrientation == 90 || requestJob.mOrientation == 270) {
                    // create a matrix object
                    Matrix matrix = new Matrix();
                    matrix.postRotate(requestJob.mOrientation); // anti-clockwise by 90 degrees

// create a new bitmap from the original using the matrix to transform the result
                    bitmap = Bitmap
                            .createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                                    matrix, true);
                }
                Message message = mMainHandler.obtainMessage();
                message.obj = new RequestResult(requestJob.mId, bitmap,
                        requestJob.mImageView, requestJob.mListenerId,
                        bitmap != null ? true : false);
                message.arg1 = requestJob.mHighQuality ? 1 : 0;
                mMainHandler.sendMessage(message);
            }
        };
        mExecutor.execute(runnable);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth,
            int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) >
                    reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private boolean isNotSameAspect(RequestJob requestJob, Options options) {
        if (requestJob.mWidth < requestJob.mHeight && options.outWidth > options.outHeight) {
            return true;
        }
        return false;
    }
}
