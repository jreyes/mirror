package com.vaporwarecorp.mirror.vendor.houndify;

import android.app.Activity;
import android.content.Intent;
import com.hound.android.fd.Houndify;
import com.vaporwarecorp.mirror.util.FullScreenActivityUtil;

public class HoundifyVoiceSearchActivity
        extends com.hound.android.fd.HoundifyVoiceSearchActivity {
// ------------------------------ FIELDS ------------------------------

    private static final String SAFE_TOKEN = "you_must_start_with_me";

// -------------------------- STATIC METHODS --------------------------

    public static void newInstance(Activity context) {
        final Intent intent = new Intent(context, HoundifyVoiceSearchActivity.class);
        intent.putExtra(SAFE_TOKEN, true);
        context.startActivityForResult(intent, Houndify.REQUEST_CODE);
        context.overridePendingTransition(com.hound.android.voicesdk.R.anim.houndify_search_enter, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FullScreenActivityUtil.onResume(this);
    }
}
