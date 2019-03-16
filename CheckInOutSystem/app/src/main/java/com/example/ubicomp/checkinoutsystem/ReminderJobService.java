package com.example.ubicomp.checkinoutsystem;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ReminderJobService extends JobService {


    private static String DEFAULT_CHANNEL_ID = "default_channel";
    private static String DEFAULT_CHANNEL_NAME = "Default";
    private int NOTIFICATION_TIME = 10;

    /*
     * Create NotificationChannel as required from Android 8.0 (Oreo)
     * */
    public static void createNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Create channel only if it is not already created
            if (notificationManager.getNotificationChannel(DEFAULT_CHANNEL_ID) == null) {
                notificationManager.createNotificationChannel(new NotificationChannel(
                        DEFAULT_CHANNEL_ID, DEFAULT_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
                ));
            }
        }
    }


    @Override
    public boolean onStartJob(JobParameters params) {
        Calendar cal = Calendar.getInstance();
        SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);
        DatabaseManager db = new DatabaseManager(getApplicationContext());
        SimpleDateFormat dateformatter = new SimpleDateFormat("dd-MM-yyyy");
        String current_date = dateformatter.format(cal.getTime());
        int current_hour = cal.get(Calendar.HOUR_OF_DAY);
        int current_minute = cal.get(Calendar.MINUTE);


        if(!sp.getString("user","").equals("")){
            Cursor children = db.getChildrenForUser(sp.getString("user",""));
            if(children.getCount()==1){
                children.moveToNext();
                int idChild = children.getInt(0);


                String checkOutTime = db.getCheckOutTimeForDateChild(idChild,current_date);
                if(checkOutTime!=null && !checkOutTime.equals("")) {

                    String[] time = checkOutTime.split(":");
                    int interval = (Integer.parseInt((time[0])) - current_hour)*60 - current_minute + Integer.parseInt(time[1]);
                    if (interval<=0) {
                        sp.edit().putBoolean("remind", false).apply();
                    }
                    else if (interval<NOTIFICATION_TIME && !sp.getBoolean("remind",false)) {
                        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        createNotificationChannel(manager);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), DEFAULT_CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_launcher_background)
                                .setContentTitle("Reminder!")
                                .setContentText("Remember to pick up your child at "+checkOutTime)
                                .setAutoCancel(true);
                        builder.build().flags |= Notification.FLAG_AUTO_CANCEL;
                        manager.notify(0, builder.build());
                        sp.edit().putBoolean("remind",true).apply();
                    }


                }
            }else{
                // TO DO
            }


        }
        //To trigger again the ReminderJob
        Util.scheduleJob(getApplicationContext());
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
