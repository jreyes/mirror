package com.vaporwarecorp.mirror.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.vaporwarecorp.mirror.MirrorApp;
import com.vaporwarecorp.mirror.R;
import com.vaporwarecorp.mirror.ui.fragment.PictureFullscreenFragment;
import com.vaporwarecorp.mirror.util.FullScreenActivityUtil;

import java.util.Random;

public class SplashActivity extends Activity {
// ------------------------------ FIELDS ------------------------------

    private static final String[] SPLASH_COVERS = {
            "http://i.giphy.com/rR2AWZ3ip77r2.gif",
            "http://i.giphy.com/eebmNnxxtSNiw.gif",
            "http://i.giphy.com/2XXGmo4Q1yPjq.gif",
            "http://i.giphy.com/3Ow6njmLYdchW.gif",
            "http://i.giphy.com/AWqRqyyLYhZxS.gif",
            "http://i.giphy.com/UKIUEcSrcvNKM.gif"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        String pictureUri = SPLASH_COVERS[new Random().nextInt(SPLASH_COVERS.length)];

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, PictureFullscreenFragment.newInstance(pictureUri))
                .commit();

        displayMirror();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FullScreenActivityUtil.onResume(this);
    }

    private void displayMirror() {
        new Handler().postDelayed(() -> {
            if (MirrorApp.getApplication(this).initializeManagers()) {
                Intent i = new Intent(SplashActivity.this, MirrorActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);

                // close this activity
                finish();
            } else {
                displayMirror();
            }
        }, 10000);
    }
}
