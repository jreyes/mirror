package com.vaporwarecorp.mirror.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.vaporwarecorp.mirror.feature.main.view.MirrorActivity;

public class BootReceiver extends BroadcastReceiver {
// -------------------------- OTHER METHODS --------------------------

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, MirrorActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}