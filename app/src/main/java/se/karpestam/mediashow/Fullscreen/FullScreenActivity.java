package se.karpestam.mediashow.Fullscreen;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import se.karpestam.mediashow.R;

/**
 * Created by Mats on 2015-04-23.
 */
public class FullScreenActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().hide();
        setContentView(R.layout.fullscreen_activity);
    }
}
