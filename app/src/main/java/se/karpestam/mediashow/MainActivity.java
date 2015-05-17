package se.karpestam.mediashow;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.Window;
import android.widget.Toolbar;

import se.karpestam.mediashow.Grid.GridFragment;


public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(Constants.LOG_TAG, MainActivity.class.getSimpleName() + " onCreate() " +
                "savedInstanceState=" + savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, new GridFragment(), GridFragment.FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        int savedTheme = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS_FILE_NAME,
                Context.MODE_PRIVATE).getInt(Constants.THEME, 0);
        switch (savedTheme) {
            case 0:
                resid = R.style.AppThemeDefault;
                break;
            case 1:
                resid = R.style.AppThemeLight;
                break;
            case 2:
                resid = R.style.AppThemeDark;
                break;
        }
        super.onApplyThemeResource(theme, resid, first);

        Log.d("MATS", "onApplyThemeResource()");
    }
}