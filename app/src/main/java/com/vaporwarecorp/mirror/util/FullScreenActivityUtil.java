package com.vaporwarecorp.mirror.util;

import android.app.Activity;
import android.view.View;

public class FullScreenActivityUtil {
// -------------------------- STATIC METHODS --------------------------

    private static void hideSystemUI(View view) {
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public static void onResume(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        hideSystemUI(decorView);
        decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                hideSystemUI(decorView);
            }
        });
    }
}