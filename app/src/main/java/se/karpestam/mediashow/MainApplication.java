package se.karpestam.mediashow;

import android.app.Application;

import se.karpestam.mediashow.CursorLoader.CursorLoaderQuery;
import se.karpestam.mediashow.CursorLoader.PhotosAndVideosQuery;

/**
 * Created by 23055395 on 2015-05-19.
 */
public class MainApplication extends Application {

    /**
     * The query used when changing activities. For example if a picture is clicked in the
     * {@link se.karpestam.mediashow.Grid.GridActivity} the same query must be made in {@link
     * se.karpestam.mediashow.Fullscreen.FullscreenActivity} to be able to show correct picture
     * according to the position.
     */
    private CursorLoaderQuery mCursorLoaderQuery;

    @Override
    public void onCreate() {
        super.onCreate();

        mCursorLoaderQuery = new PhotosAndVideosQuery();
    }

    /**
     * Set the current {@link CursorLoaderQuery} to use when going between activities.
     *
     * @param cursorLoaderQuery
     */
    public void setCursorLoaderQuery(CursorLoaderQuery cursorLoaderQuery) {
        mCursorLoaderQuery = cursorLoaderQuery;
    }

    /**
     * Get the current {@link CursorLoaderQuery}.
     *
     * @return Current {@link CursorLoaderQuery}.
     */
    public CursorLoaderQuery getCursorLoaderQuery() {
        return mCursorLoaderQuery;
    }

}
