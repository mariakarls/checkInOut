package com.example.ubicomp.checkinoutsystem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Class to manage a SQLite database (Only for prototype)
 */
public class DatabaseManager extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Kindergarten.db";
    private static final String USER_TABLE = "Users";
    private static final String USER_COL0="User";
    private static final String USER_COL1="Password";
    private static final String CHILDREN_TABLE = "Children";
    private static final String CHILDREN_COL0="IdChild";
    private static final String CHILDREN_COL1="Name";
    private static final String CHILDUSER_TABLE = "ChildUser";
    private static final String CHILDUSER_COL0="IdChild";
    private static final String CHILDUSER_COL1="User";
    private static final String CHECKINOUT_TABLE = "CheckInOut";
    private static final String CHECKINOUT_COL0="Date";
    private static final String CHECKINOUT_COL1="IdChild";
    private static final String CHECKINOUT_COL2="CheckInTime";
    private static final String CHECKINOUT_COL3="CheckOutTime";
    private static final String CHECKINOUT_COL4="PersonCheckOut";
    private static final String CHECKINOUT_COL5="CheckOut";

    public DatabaseManager(Context context) {
        super(context,DATABASE_NAME, null,1);
    }

    // Create all table of the database
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUserTable = "CREATE  TABLE " + USER_TABLE + "(User TEXT PRIMARY KEY, Password TEXT NOT NULL)";
        db.execSQL(createUserTable);
        String createChildrenTable = "CREATE TABLE " + CHILDREN_TABLE + "(IdChild INT PRIMARY KEY, Name TEXT NOT NULL)";
        db.execSQL(createChildrenTable);
        String createChildUserTable = "CREATE TABLE " + CHILDUSER_TABLE + "(IdChild INT NOT NULL, User TEXT NOT NULL, PRIMARY KEY (IdChild,User))";
        db.execSQL(createChildUserTable);
        String createCheckInOutTable = "CREATE TABLE " + CHECKINOUT_TABLE + "(Date TEXT NOT NULL, IdChild INT NOT NULL, CheckInTime TEXT, CheckOutTime TEXT, PersonCheckOut TEXT, CheckOut INT, PRIMARY KEY (Date, IdChild))";
        db.execSQL(createCheckInOutTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CHILDREN_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CHILDUSER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CHECKINOUT_TABLE);
        onCreate(db);
    }

    // Populate the database
    public void populateDatabase(){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues users = new ContentValues();
        users.put(USER_COL0, "Admin");
        users.put(USER_COL1, "1234");
        db.insert(USER_TABLE, null, users);
        users.put(USER_COL0, "Admin1");
        users.put(USER_COL1, "12345");
        db.insert(USER_TABLE, null, users);

        ContentValues children = new ContentValues();
        children.put(CHILDREN_COL0, 1);
        children.put(CHILDREN_COL1, "Albert");
        db.insert(CHILDREN_TABLE, null, children);
        children.put(CHILDREN_COL0, 2);
        children.put(CHILDREN_COL1, "John");
        db.insert(CHILDREN_TABLE, null, children);


        ContentValues childusers = new ContentValues();
        childusers.put(CHILDUSER_COL0, 1);
        childusers.put(CHILDUSER_COL1, "Admin");
        db.insert(CHILDUSER_TABLE, null, childusers);
        childusers.put(CHILDUSER_COL0, 2);
        childusers.put(CHILDUSER_COL1, "Admin1");
        db.insert(CHILDUSER_TABLE, null, childusers);

    }


    /**
     *  Add a new entry in the CheckInOut table
     * @param date actual date
     * @param idchild id child to check in
     * @param checkin time of check in
     * @param checkouttime time of check out
     * @param person person that checked out/will check out
     * @param checkout boolean (it's true after the check out)
     * @return
     */
    public boolean addData(String date, int idchild, String checkin, String checkouttime, String person, int checkout){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CHECKINOUT_COL0, date);
        values.put(CHECKINOUT_COL1, idchild);
        values.put(CHECKINOUT_COL2, checkin);
        values.put(CHECKINOUT_COL3, checkouttime);
        values.put(CHECKINOUT_COL4, person);
        values.put(CHECKINOUT_COL5, checkout);

        long result = db.insert(CHECKINOUT_TABLE, null, values);
        if(result == -1)
            return false;
        else
            return true;

    }

    /**
     * Update a specific CheckInOut entry
     */
    public boolean updateData(String date,int idchild, String checkin, String checkouttime, String person, int checkout){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CHECKINOUT_COL0, date);
        values.put(CHECKINOUT_COL1, idchild);
        values.put(CHECKINOUT_COL2, checkin);
        values.put(CHECKINOUT_COL3, checkouttime);
        values.put(CHECKINOUT_COL4, person);
        values.put(CHECKINOUT_COL5, checkout);
        Cursor data = showInfoForDate(idchild,date);
        if(data.getCount()!=0) {
            db.update(CHECKINOUT_TABLE, values, "IdChild LIKE " + "'" + idchild + "' AND Date LIKE '" + date + "'", null);
        }else{
            return addData(date,idchild,checkin,checkouttime,person,checkout);
        }
        return true;
    }

    /**
     * Get all the children of a specific user
     * @param user
     * @return
     */
    public Cursor getChildrenForUser(String user){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT *"+" FROM "+ CHILDUSER_TABLE+" WHERE User LIKE '"+ user + "'",null);
        return data;
    }

    /**
     * Get the name of a child (identified by id)
     * @param idchild
     * @return
     */
    public String getChildName(int idchild){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT *"+" FROM "+ CHILDREN_TABLE+" WHERE IdChild IS '"+ idchild + "'",null);
        data.moveToNext();
        return data.getString(1);
    }

    /**
     * Get all the users stored in the db
     * @return
     */
    public Cursor getUsers(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT *"+" FROM "+ USER_TABLE,null);
        return data;
    }

    /**
     * Return the CheckInOut's entry for a specific child and in a specific date
     * @param idchild
     * @param date
     * @return
     */
    public Cursor showInfoForDate(int idchild, String date){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT *"+" FROM "+ CHECKINOUT_TABLE+" WHERE IdChild LIKE '"+ idchild + "' AND Date LIKE '"+date+"'",null);
        return data;
    }

    /**
     * Returns the checkIn time for a specific child and date
     * @param idchild
     * @param date
     * @return
     */
    public String getCheckInForDateChild(int idchild, String date){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT *"+" FROM "+ CHECKINOUT_TABLE+" WHERE IdChild LIKE '"+ idchild + "' AND Date LIKE '"+date+"'",null);
        if(data.getCount()!=0){
            data.moveToNext();
            return data.getString(2);
        }
        return "";
    }

    /**
     * Returns the checkOut time for a specific child and date
     * @param idchild
     * @param date
     * @return
     */
    public String getCheckOutTimeForDateChild(int idchild, String date){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT *"+" FROM "+ CHECKINOUT_TABLE+" WHERE IdChild LIKE '"+ idchild + "' AND Date LIKE '"+date+"'",null);
        if(data.getCount()!=0){
            data.moveToNext();
            return data.getString(3);
        }
        return "";
    }

    /**
     * Returns the pickupPerson time for a specific child and date
     * @param idchild
     * @param date
     * @return
     */
    public String getPickupPersonForDateChild(int idchild, String date){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT *"+" FROM "+ CHECKINOUT_TABLE+" WHERE IdChild LIKE '"+ idchild + "' AND Date LIKE '"+date+"'",null);
        if(data.getCount()!=0){
            data.moveToNext();
            return data.getString(4);
        }
        return "";
    }

    /**
     * Returns the checkOut (boolean true after the check-out) time for a specific child and date
     * @param idchild
     * @param date
     * @return
     */
    public int getCheckOutForDateChild(int idchild, String date){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT *"+" FROM "+ CHECKINOUT_TABLE+" WHERE IdChild LIKE '"+ idchild + "' AND Date LIKE '"+date+"'",null);
        if(data.getCount()!=0){
            data.moveToNext();
            return data.getInt(5);
        }
        return 0;
    }

    /**
     * Return all CheckInOut's entries for a specific child
     * @param idchild
     * @return
     */
    public Cursor showInfo(int idchild){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT *"+" FROM "+ CHECKINOUT_TABLE+" WHERE IdChild LIKE '"+ idchild + "'",null);
        return data;
    }
}

