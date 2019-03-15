package com.example.ubicomp.checkinoutsystem;


import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class NotificationService extends IntentService {


    public NotificationService() {
        super(NotificationService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        DatabaseManager database = new DatabaseManager(this);

        SharedPreferences sp = getSharedPreferences("Login",MODE_PRIVATE);
        String action = intent.getAction();
        final SimpleDateFormat timeformatter = new SimpleDateFormat("HH:mm:ss");
        final Calendar now = Calendar.getInstance();
        final SimpleDateFormat dateformatter = new SimpleDateFormat("dd-MM-yyyy");

        String time = timeformatter.format(now.getTime());
        Cursor children = database.getChildrenForUser(sp.getString("user",""));


        /**
         * Based on the action parameter of the notification executes different tasks
         */
        if(action!=null && action.equals("check-in")) {
            if(children.getCount()==1){
                children.moveToNext();
                sp.edit().putInt("current-id-child",children.getInt(0)).apply();
                Intent openActivity = new Intent(this, ChildActivity.class).setAction("check-in");
                startActivity(openActivity);
            }else{
                //TO DO: open list selection activity
            }

        }
        else if (action!=null && action.equals("check-out")){
            String current_date = dateformatter.format(now.getTime());
            if (children.getCount() == 1) {
                children.moveToNext();
                int idChild = children.getInt(0);
                int checkout = database.getCheckOutForDateChild(idChild,current_date);

                if(checkout == 0){
                    String checkin = database.getCheckInForDateChild(idChild, current_date);
                    String person = database.getPickupPersonForDateChild(idChild, current_date);
                    database.updateData(current_date, idChild, checkin, time, person, 1);
                    Toast.makeText(this, "Check-out successful!", Toast.LENGTH_SHORT).show();
                }
            }else {
                //TO DO: open list selection activity
            }


        }
        NotificationManagerCompat.from(this).cancel(1); //Cancel the notification after the user click


    }

}