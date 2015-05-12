package se.karpestam.mediashow.Fullscreen;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import se.karpestam.mediashow.Media.BitmapRequest;
import se.karpestam.mediashow.Media.BitmapRequester;
import se.karpestam.mediashow.Media.BitmapResultListener;
import se.karpestam.mediashow.Media.BitmapResult;
import se.karpestam.mediashow.R;

public class FullscreenImageFragment extends Fragment implements BitmapResultListener {

    private final String mListenerId = toString();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        BitmapRequester.getInstance(getActivity().getApplicationContext())
                .addListener(mListenerId, this);
        return inflater.inflate(R.layout.fullscreen_image_item, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /* Get values. */
        Bundle bundle = getArguments();
        final String data = bundle.getString(MediaStore.Files.FileColumns.DATA);
        final int orientation = bundle.getInt(MediaStore.Images.ImageColumns.ORIENTATION);
        final int mediaType = bundle.getInt(MediaStore.Files.FileColumns.MEDIA_TYPE);
        final ImageView imageView = (ImageView)view.findViewById(R.id.fullscreen_image);
        imageView.setTag(data);
        Point point = getScreenSize();
        Bitmap bitmap = BitmapRequester.getInstance(getActivity().getApplicationContext())
                .requestBitmap(
                        new BitmapRequest(data, orientation, imageView, mListenerId, true, point.x,
                                point.y, mediaType));
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public void onRequestResult(BitmapResult bitmapResult) {
        String tag = (String)bitmapResult.mImageView.getTag();
        if (tag.equals(bitmapResult.mPath) && bitmapResult.mListenerId.equals(mListenerId)) {
            bitmapResult.mImageView.setImageBitmap(bitmapResult.mBitmap);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        BitmapRequester.getInstance(getActivity().getApplicationContext())
                .removeListener(mListenerId);
    }

    private Point getScreenSize() {
        WindowManager windowManager = (WindowManager)getActivity()
                .getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        return point;
    }
}
