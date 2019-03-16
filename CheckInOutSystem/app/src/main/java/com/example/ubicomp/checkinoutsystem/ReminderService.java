package com.example.ubicomp.checkinoutsystem;

import android.content.*;
import android.content.Intent;
import android.content.Context;



public class ReminderService extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Util.scheduleJob(context);
    }
}
