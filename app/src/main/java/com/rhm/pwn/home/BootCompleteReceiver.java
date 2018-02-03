package com.rhm.pwn.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by sambo on 10/9/17.
 */

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SAMB", "BootCompleteReceiver event received. Starting PWN services");
        Intent service = new Intent(context.getApplicationContext(), URLCheckService.class);
        context.getApplicationContext().startService(service);
    }
}
