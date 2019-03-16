package com.example.ubicomp.checkinoutsystem;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

public class Util {

    private static Util util = null;

    /**
     * It starts a background service only once (singleton)
     * @param context
     */
    public static void startReminderService(Context context){
        if(util == null){
            util = new Util();
            scheduleJob(context);
        }
    }

    public static void scheduleJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, ReminderJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(30 * 1000); // Wait at least 30s
        builder.setOverrideDeadline(60 * 1000); // Maximum delay 60s

        JobScheduler jobScheduler = (JobScheduler)context.getSystemService(context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }

}