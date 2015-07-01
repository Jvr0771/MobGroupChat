package io.yxy.mobgroupchat;

import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

/**
 * Created by yxy on 15/7/1.
 */
public class BaseActivity extends ActionBarActivity {

    public void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

}
