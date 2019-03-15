package com.example.ubicomp.checkinoutsystem;

import android.app.TimePickerDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ChildActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    private TextView date;
    private TextView name;
    private  TextView checkIn;
    private TextView checkOut;
    public  ToggleButton checkInTog;
    public ToggleButton checkOutTog;
    private Button showHistory;
    private String username;
    private Button notification;
    private Button logout;
    private Button changeCheckOut;
    private Button save;
    private ImageView photo;
    private EditText personPickUp;
    private int idChild;
    private SharedPreferences sp;
    private static DatabaseManager db;

    @Override
    protected void onRestart(){
        super.onRestart();
        showDateInfo(); //To update the info showed
    }

    @Override
    protected void onResume(){
        super.onResume();
        showDateInfo(); //To update the info showed
    }

    @Override
    public void onBackPressed() {
        ActivityCompat.finishAffinity(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);
        db = new DatabaseManager(this);
        date = findViewById(R.id.date);
        name = findViewById(R.id.nameText);
        photo = findViewById(R.id.childPhoto);
        checkIn = findViewById(R.id.checkIn);
        checkOut = findViewById(R.id.checkOut);
        checkInTog = findViewById(R.id.checkInToggle);
        checkOutTog = findViewById(R.id.checkOutToggle);
        showHistory = findViewById(R.id.showHistoryBtn);
        notification = findViewById(R.id.notificationBtn);
        changeCheckOut = findViewById(R.id.changeTime);
        personPickUp = findViewById(R.id.person);
        save = findViewById(R.id.saveBtn);
        logout = findViewById(R.id.logoutBtn);
        sp = getSharedPreferences("Login", MODE_PRIVATE);
        username = sp.getString("current-user","");
        idChild = sp.getInt("current-id-child",0);

        name.setText(db.getChildName(idChild));
        photo.setImageResource(R.drawable.child_boy);

        Calendar now = Calendar.getInstance();
        final SimpleDateFormat dateformatter = new SimpleDateFormat("dd-MM-yyyy");
        final SimpleDateFormat timeformatter = new SimpleDateFormat("HH:mm:ss");
        date.setText(dateformatter.format(now.getTime()));

        showDateInfo();

        checkInTog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String current_date = date.getText().toString();
                String checkoutT = db.getCheckOutTimeForDateChild(idChild,current_date);
                String person = db.getPickupPersonForDateChild(idChild,current_date);
                Calendar now = Calendar.getInstance();
                if(!person.equals("") && !checkoutT.equals("")){
                    String time = timeformatter.format(now.getTime());
                    boolean click = db.updateData(current_date, idChild, time, checkoutT, person, 0);
                    if (click) {
                        checkInTog.setEnabled(false);
                        checkIn.setText(("Check In: " + time));
                        personPickUp.setText(person);
                        Toast.makeText(ChildActivity.this, "Check in successful! ", Toast.LENGTH_LONG).show();

                    } else
                        Toast.makeText(ChildActivity.this, "Something went wrong! ", Toast.LENGTH_LONG).show();

                }else{
                    checkInTog.setChecked(false);
                    Toast.makeText(ChildActivity.this, "You have to insert pick up person and time before the check in!", Toast.LENGTH_LONG).show();
                }

            }

        });

        checkOutTog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String current_date = date.getText().toString();
                String checkinT = db.getCheckInForDateChild(idChild,current_date);
                String person = db.getPickupPersonForDateChild(idChild,current_date);
                Calendar now = Calendar.getInstance();
                String time = timeformatter.format(now.getTime());
                if (checkinT.equals("")) {
                    Toast.makeText(ChildActivity.this, "You have to check in first! ", Toast.LENGTH_LONG).show();
                    checkOutTog.setChecked(false);
                } else {
                    boolean click = db.updateData(current_date,idChild, checkinT, time,person, 1);
                    if (click) {
                        checkOutTog.setEnabled(false);
                        checkOut.setText(("Check Out: " + time));
                        personPickUp.setEnabled(false);
                        personPickUp.setText((person));
                        save.setEnabled(false);
                        changeCheckOut.setEnabled(false);
                        Toast.makeText(ChildActivity.this, "Check out successful! ", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ChildActivity.this, "Something went wrong! ", Toast.LENGTH_LONG).show();
                        checkOutTog.setChecked(false);
                    }
                }
            }

        });

        //TO MODIFY (if we want the history)
        showHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor data = db.showInfo(idChild);
                if (data.getCount() == 0) {
                    display("Histoty Data", "No data found");
                    return;
                }
                StringBuffer buffer = new StringBuffer();
                while (data.moveToNext()) {
                    buffer.append("Date: " + data.getString(0)+"\n");
                    buffer.append("Check In Time: " + data.getString(2)+ "\n");
                    if(data.getInt(5) == 0) {
                        buffer.append("Temporary Check Out: " + data.getString(3) + "\n");
                        buffer.append("Temporary Pick up Person: " + data.getString(4) + "\n"+"\n");
                    }
                    else{
                        buffer.append("Check Out Time: " + data.getString(3) + "\n");
                        buffer.append("Pick up Person: " + data.getString(4) + "\n"+"\n");
                    }

                    display("History Data: ", buffer.toString());
                }

            }
        });

        changeCheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogFragment timePicker = new TimePickerManager();
                timePicker.show(getSupportFragmentManager(),"time picker");
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String current_date = date.getText().toString();
                String checkinT = db.getCheckInForDateChild(idChild,current_date);
                String checkoutT = db.getCheckOutTimeForDateChild(idChild,current_date);
                String person = personPickUp.getText().toString();
                if(!person.equals("")) {
                    boolean click = db.updateData(current_date, idChild, checkinT, checkoutT, person, 0);
                    if (click) {
                        Toast.makeText(ChildActivity.this, "Update pick-up person! ", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ChildActivity.this, "Something went wrong! ", Toast.LENGTH_LONG).show();
                        checkOutTog.setChecked(false);
                    }
                }else{
                    String previous_person = db.getPickupPersonForDateChild(idChild,current_date);
                    personPickUp.setText(previous_person);
                }
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sp.edit().putBoolean("remember",false).apply();
                Intent intent = new Intent(ChildActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        //ONLY TO CHECK NOTIFICATION (TO CANCEL)
        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(checkInOutInfo()){
                    case "CheckIn":
                        checkInNotification();
                        break;
                    case "CheckOut":
                        checkOutNotification();
                        break;
                    default:
                }
            }
        });


    }

    @Override
    public void onTimeSet(TimePicker view, int hour, int minute) {
        Calendar c = Calendar.getInstance();
        int current_hour = c.get(Calendar.HOUR_OF_DAY);
        int current_minute = c.get(Calendar.MINUTE);

        if(current_hour>hour || (current_hour == hour && current_minute>minute)){
            Toast.makeText(ChildActivity.this, "Hour not available! Choose a future time. ", Toast.LENGTH_LONG).show();
            return;
        }

        String h = (new Integer(hour)).toString();
        if(hour < 10){
            h = "0" + h;
        }
        String m = (new Integer(minute)).toString();
        if(minute < 10){
            m = "0" + m;
        }
        String checkOutTxt = h+":"+m;
        checkOut.setText(("Check Out: " +checkOutTxt));
        String current_date = date.getText().toString();
        String checkinT = db.getCheckInForDateChild(idChild,current_date);
        String person = db.getPickupPersonForDateChild(idChild,current_date);
        db.updateData(current_date,idChild, checkinT, checkOutTxt, person, 0);

    }

    //It updates all elements of the view based on the checkInOut table content for the current date
    private void showDateInfo(){
        String current_date = date.getText().toString();
        String checkinT = db.getCheckInForDateChild(idChild,current_date);
        String checkoutT = db.getCheckOutTimeForDateChild(idChild,current_date);
        String person = db.getPickupPersonForDateChild(idChild,current_date);
        int checkout = db.getCheckOutForDateChild(idChild,date.getText().toString());

            if(checkinT !=null && !checkinT.equals("")) {
                checkIn.setText(("Check In: "+checkinT));
                checkInTog.setEnabled(false);
            }
        checkOut.setText(("Check Out: "+checkoutT));
        personPickUp.setText(person);
            if(checkout ==1){
                checkOutTog.setEnabled(false);
                changeCheckOut.setEnabled(false);
                personPickUp.setEnabled(false);
                save.setEnabled(false);
            }
    }

    // Shows a dialog with a message (USED ONLY FOR HISTORY BUTTON)
    private void display(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }


    /**
     * TO TEST NOTIFICATION ONLY
     */
    private static String DEFAULT_CHANNEL_ID = "default_channel";
    private static String DEFAULT_CHANNEL_NAME = "Default";

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


}

