package se.karpestam.mediashow.Fullscreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import se.karpestam.mediashow.Media.BitmapCache;
import se.karpestam.mediashow.Media.RequestJob;
import se.karpestam.mediashow.Media.BitmapRequester;
import se.karpestam.mediashow.Media.RequestListener;
import se.karpestam.mediashow.Media.RequestResult;
import se.karpestam.mediashow.R;

public class FullscreenPageFragment extends Fragment implements RequestListener {

    private final String mListenerId = toString();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fullscreen_page_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /* Get values. */
        Bundle bundle = getArguments();
        final int id = bundle.getInt(MediaStore.MediaColumns._ID);
        final String data = bundle.getString(MediaStore.MediaColumns.DATA);
        final int orientation = bundle.getInt(MediaStore.Images.ImageColumns.ORIENTATION);

        WindowManager windowManager = (WindowManager)getActivity()
                .getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        ImageView imageView = (ImageView)view.findViewById(R.id.fullscreen_image);

        BitmapRequester.getInstance().addListener(mListenerId, this);
        /* See if there's any cached Bitmap. */
        Bitmap bitmap = BitmapCache.getInstance().get(id);
        if (bitmap != null) {
            /* Set the cached Bitmap, this is only low-res. */
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageBitmap(null);
        }
        /* Always request high-res, since they are not cached. */
        BitmapRequester.getInstance().requestBitmap(
                new RequestJob(id, data, orientation, imageView, mListenerId, true, point.x,
                        point.y));
    }

    @Override
    public void onRequestResult(RequestResult requestResult) {
        if ((int)requestResult.mImageView
                .getTag() == requestResult.mId && requestResult.mListenerId.equals(mListenerId)) {
            if (requestResult.mIsResultOk) {
                requestResult.mImageView.setImageBitmap(requestResult.mBitmap);
            } else {
                requestResult.mImageView.setBackgroundColor(Color.RED);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        BitmapRequester.getInstance().removeListener(mListenerId);
    }
}
