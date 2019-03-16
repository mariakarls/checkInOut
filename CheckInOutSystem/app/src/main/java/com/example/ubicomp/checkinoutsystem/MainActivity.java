package com.example.ubicomp.checkinoutsystem;

import android.app.AlarmManager;
import android.content.Context;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import android.app.PendingIntent;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    /**
     * Variable for geofencing
     */
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    protected ArrayList<Geofence> geofenceList;
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;

    private LocationManager locationManager;


    private DatabaseManager db;
    private SharedPreferences sp;

    /**
     * Activity elements' variables
     */
    public EditText username;
    private EditText password;
    private Button login;
    private CheckBox remember;

    @Override
    public void onBackPressed() {
        ActivityCompat.finishAffinity(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(MainActivity.this, "Your GPS is OFF! Turn it on to use all app facilities! ", Toast.LENGTH_LONG).show();
        }
        //Initialize the geofencing client
        geofencingClient = LocationServices.getGeofencingClient(this);
        // Empty list for storing geofences
        geofenceList = new ArrayList<Geofence>();
        // Get the geofences used.
        populateGeofenceList();
        //Add the geofences
        addGeofences();


        username = findViewById(R.id.userText);
        password = findViewById(R.id.passwordText);
        login = findViewById(R.id.loginButton);
        remember = findViewById(R.id.rememberMe);
        db = new DatabaseManager(this);
        sp = getSharedPreferences("Login", MODE_PRIVATE);

        Util.startReminderService(this);



        /**
         * If it's the first access check/request for location permission
         */
        if(!(sp.getBoolean("first-access",false))){
            checkPermission();
            db.populateDatabase(); // For prototype only (populate the database to test the application)
            sp.edit().putBoolean("first-access",true).apply();
        }

        /**
         * If the user checked in the past the remember box (and there wasn't a logout action)
         */
         if(sp.getBoolean("remember",false)){
             validate_login(sp.getString("user",""),sp.getString("password",""));
         }else {
             /**
              * To pre insert username a password if saved in the past
              */
             if (!sp.getString("user", "").equals("")) {
                 username.setText(sp.getString("user", ""));
             }
             if (!sp.getString("password", "").equals("")) {
                 password.setText(sp.getString("password", ""));
             }
         }


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate_login(username.getText().toString(),password.getText().toString());
            }
        });

        remember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp.edit().putBoolean("remember",remember.isChecked()).apply();
            }
        });

    }

    /**
     * Method to populate the geofence list (from data in Constants class)
     */
    public void populateGeofenceList() {
        for (Map.Entry<String, LatLng> entry : Constants.LANDMARKS.entrySet()) {
            geofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(entry.getKey())
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());
        }
    }

    /**
     * This method return the geofences request to trigger in the geofencing service
     * @return GeofenceRequest
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        //When the geofence is activated start triggering even though the user is already in the geofence
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    /**
     * This method returns the pending intent to execute when a geofence event happen
     * @return PendingIntent
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionIntentService.class);
        geofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    /**
     * This methods adds to the geofencing service the geofences (GeofencingRequest) with the
     * pending intent to execute (ONLY IF the app has the location permission)
     */
    private void addGeofences(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this,"To can use all app facilities you have to grant the location permission for this app and turn on the GPS!", Toast.LENGTH_SHORT).show();
        }
        else
            geofencingClient.addGeofences(getGeofencingRequest(),getGeofencePendingIntent())
                    .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Geofences added
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to add geofences
                        }
                    });
    }


    /**
     * This method request the permission to access the location
     */
    private void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted (this ask for permission after the first access)
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                 new AlertDialog.Builder(this)
                 .setTitle("Permission needed")
                 .setMessage("You need to allow the geolocation permission to use all application facilities.")
                 .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                }
                })
                 .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                }
                })
                 .create().show();
            } else {
                //Permission request during the first access
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission to access location granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission to access location not granted!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    /**
     * Checks the correct username and password insert by the user and start the login.
     * @param username
     * @param password
     */
    private void validate_login(String username, String password){
        if(validate(username, password)){
            // Only if the username and password are correct, they are stored in SharedPreferences (if request)
            if(remember.isChecked()){
                sp.edit().putString("user",username).apply();
                sp.edit().putString("password",password).apply();
            }
            login(username);
        }
    }

    /**
     * Check username and password typed.
     *
     * @param username
     * @param password
     * @return the result of the validation (true: if username and password are correct)
     */
    private boolean validate(String username, String password){
        Cursor users = db.getUsers(); //From the database get all users
        if (users.getCount() > 0){
            String current_user = null;
            while(users.moveToNext()){
                current_user = users.getString(0); // Get the username (table Users)
                if(username != null && username.equals(current_user)){
                    if(users.getString(1).equals(password)) {
                        return true;
                    }
                    else
                        break;
                }
            }

            Toast.makeText(MainActivity.this, "Username or password wrong! ", Toast.LENGTH_LONG).show();
            return false;
        }
        Toast.makeText(MainActivity.this, "No users availables ", Toast.LENGTH_LONG).show();
        return false;

    }

    /**
     * Login the user
     * @param user
     */
    private void login(String user){
        sp.edit().putString("current-user",user).apply(); //Store in the SharedPreferences the current user
        Cursor data = db.getChildrenForUser(user);
        Intent intent;
        // Based on the number of children start a different activity
        if(data.getCount()==1) {
            data.moveToNext();
            int child = data.getInt(0);
            sp.edit().putInt("current-id-child",child).apply();
            intent = new Intent(MainActivity.this, ChildActivity.class);
            startActivity(intent);
        }
        else if(data.getCount()>1){
            // DIFFERENT INTENT TO DO
            Toast.makeText(MainActivity.this, "TODO: activity for more children", Toast.LENGTH_LONG).show();

        }
        else {
            Toast.makeText(MainActivity.this, "Error this user cannot access! ", Toast.LENGTH_LONG).show();
        }

    }

}
