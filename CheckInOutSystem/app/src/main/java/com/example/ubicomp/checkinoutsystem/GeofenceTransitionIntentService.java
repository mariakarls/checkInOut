package com.example.ubicomp.checkinoutsystem;


import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;



public class GeofenceTransitionIntentService extends IntentService {

    protected static final String TAG = GeofenceTransitionIntentService.class.getSimpleName();
    private static String DEFAULT_CHANNEL_ID = "default_channel";
    private static String DEFAULT_CHANNEL_NAME = "Default";

    public GeofenceTransitionIntentService() {
        super(GeofenceTransitionIntentService.class.getSimpleName());  // use TAG to name the IntentService worker thread
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = "Error: "+
                    geofencingEvent.getErrorCode();
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            //String geofenceTransitionDetails = getGeofenceTransitionDetails(geofencingEvent);

            // Send notification
            switch(checkInOutInfo()){
                case "CheckIn":
                    checkInNotification();
                    break;
                case "CheckOut":
                    checkOutNotification();
                    break;
                default:

            }
        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.common_google_play_services_unknown_issue,
                    geofenceTransition));
        }
    }


    /**
     // This gets all details of the geofence transition
     private static String getGeofenceTransitionDetails(GeofencingEvent event) {
     String transitionString =
     GeofenceStatusCodes.getStatusCodeString(event.getGeofenceTransition());
     ArrayList triggeringIDs = new ArrayList();
     for (Geofence geofence : event.getTriggeringGeofences()) {
     triggeringIDs.add(geofence.getRequestId());
     }
     return String.format("%s: %s", transitionString, TextUtils.join(", ", triggeringIDs));
     }
     **/

    /*
     * Create NotificationChannel (required from Android 8.0)
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

    /**
     * Method to invoke a checkin notification
     */
    public void checkInNotification(){
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel(manager);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("CheckIn Notification")
                .setContentText("Do you want to check in your child?")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Intent notificationIntent = new Intent(this, NotificationService.class).setAction("check-in");
        PendingIntent contentIntent = PendingIntent.getService(this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
        builder.addAction(R.drawable.ic_launcher_foreground,"Check-In",contentIntent);
        manager.notify(1,builder.build());
    }

    /**
     * Method to invoke a checkout notification
     */
    public void checkOutNotification(){
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel(manager);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("CheckOut Notification")
                .setContentText("Do you want to check out your child?")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Intent notificationIntent = new Intent(this, NotificationService.class).setAction("check-out");
        PendingIntent contentIntent = PendingIntent.getService(this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
        builder.addAction(R.drawable.ic_launcher_foreground,"Check-Out",contentIntent);
        manager.notify(1,builder.build());
    }

    /**
     * Method to choose the correct notification to invoke (based on the data stored in the database)
     * @return name of the notification to invoke
     */
    public String checkInOutInfo() {

        DatabaseManager db = new DatabaseManager(this);
        final Calendar now = Calendar.getInstance();
        final SimpleDateFormat dateformatter = new SimpleDateFormat("dd-MM-yyyy");
        final SimpleDateFormat timeformatter = new SimpleDateFormat("HH:mm:ss");
        SharedPreferences sp = getSharedPreferences("Login",MODE_PRIVATE);
        Cursor children = db.getChildrenForUser(sp.getString("user",""));
        if(children.getCount()==1) {
            children.moveToNext();
            int idChild = children.getInt(0);
            String date = dateformatter.format(now.getTime());
            Cursor data = db.showInfoForDate(idChild, date);
            if (data.getCount() != 0) {
                data.moveToNext();
                if (data.getString(2).equals("")) {
                    return "CheckIn";
                } else if (data.getInt(5) == 0) {
                    return "CheckOut";
                } else
                    return "";
            } else
                return "CheckIn";
        }
        else return ""; // TO DO

    }

}




