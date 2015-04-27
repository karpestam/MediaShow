package se.karpestam.mediashow.Fullscreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import se.karpestam.mediashow.Media.RequestJob;
import se.karpestam.mediashow.Media.BitmapRequester;
import se.karpestam.mediashow.Media.RequestListener;
import se.karpestam.mediashow.Media.RequestResult;
import se.karpestam.mediashow.R;

public class FullscreenImageFragment extends Fragment implements RequestListener {

    private final String mListenerId = toString();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        BitmapRequester.getInstance(getActivity().getApplicationContext())
                .addListener(mListenerId, this);
        return inflater.inflate(R.layout.fullscreen_image_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /* Get values. */
        Bundle bundle = getArguments();
        final String data = bundle.getString(MediaStore.Files.FileColumns.DATA);
        final int orientation = bundle.getInt(MediaStore.Images.ImageColumns.ORIENTATION);
        final int mediaType = bundle.getInt(MediaStore.Files.FileColumns.MEDIA_TYPE);
        WindowManager windowManager = (WindowManager)getActivity()
                .getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        ImageView imageView = (ImageView)view.findViewById(R.id.fullscreen_image);
        imageView.setTag(data);
        Bitmap bitmap = BitmapRequester.getInstance(getActivity().getApplicationContext())
                .requestBitmap(
                        new RequestJob(data, orientation, imageView, mListenerId, true, point.x,
                                point.y, mediaType));
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public void onRequestResult(RequestResult requestResult) {
        String tag = (String)requestResult.mImageView.getTag();
        if (tag.equals(requestResult.mPath) && requestResult.mListenerId.equals(mListenerId)) {
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
        BitmapRequester.getInstance(getActivity().getApplicationContext())
                .removeListener(mListenerId);
    }
}
