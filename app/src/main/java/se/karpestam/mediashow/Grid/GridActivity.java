package se.karpestam.mediashow.Grid;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import se.karpestam.mediashow.R;

public class GridActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("MATS", "GridActivity onCreate");
        if (savedInstanceState == null) {
            setContentView(R.layout.grid_activity);
        }
    }
}
