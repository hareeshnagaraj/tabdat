package thproject.test.com.myapplication;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by hareeshnagaraj on 7/27/14.
 *
 * this is the class to manage our local database of tabs, etc.
 * will have some backup system in the future
 *
 * based on SQLite
 *
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "tabDB";
    // tabs table name
    private static final String TABLE_TABS = "tabs";
    // tabs table name
    private static final String TABLE_USERS = "users";

    // tabs Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_ARTIST = "artist";
    private static final String KEY_LINKS = "links";

    //users table column names
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";

    //keys for the tab DB
    private static final String[] COLUMNS = {KEY_ID,KEY_TITLE,KEY_ARTIST,KEY_LINKS};

    //keys for the column DB
    private static final String[] USER_COLUMNS = {KEY_ID,KEY_EMAIL,KEY_PASSWORD};

    //creating a static class that is accessed between activities
    private static  MySQLiteHelper globaldb;


    private static final String createTabsDB = "CREATE TABLE tabs ( " +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "title TEXT, "+
            "artist TEXT,"+
            "links TEXT )";

    private static final String createUsersDB = "CREATE TABLE users ( " +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "email TEXT, "+
            "password TEXT )";

    public MySQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    //accessing our static class
    public static synchronized MySQLiteHelper getDB(Context context) {
        if (globaldb == null) {
            globaldb = new MySQLiteHelper(context);
        }
        return globaldb;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTabsDB);
        db.execSQL(createUsersDB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }

    /*
    * Used to add a user to our DB, so that multiple users can be present on same device
    * */
    public void addUser(String email, String password){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_EMAIL,email);
        values.put(KEY_PASSWORD,password);
        db.insert(TABLE_USERS,null,values);
    }

    /*
    * getting the list of all users
    * */
   public List<User> getAllUsers(){
       List<User> users = new LinkedList<User>();
       String query = "SELECT  * FROM " + TABLE_USERS;
       SQLiteDatabase db = getWritableDatabase();
       Cursor cursor = db.rawQuery(query, null);
       User current = null;
       if(cursor.moveToNext()){
           do{
               current = new User();
               current.setId(Integer.parseInt(cursor.getString(0)));
               current.setEmail(cursor.getString(1));
               current.setPassword(cursor.getString(2));
               users.add(current);
               Log.d("getAllUsers ", current.toString());
           }while(cursor.moveToNext());
       }
       return users;
   }

    /*
    * used to add a tab
    * */
    public void addTab(Tab newtab){
//        Log.d("addTab", newtab.toString());
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, newtab.getTitle()); // get title
        values.put(KEY_ARTIST, newtab.getArtist()); // get author
        values.put(KEY_LINKS,newtab.getLinks()); //get links to tabs
        db.insert(TABLE_TABS,null,values);
    }

    /*
    * Simple boolean check of Tab in our DB
    * */
    public boolean tabExists(String title, String artist){
        boolean exists;
        String query = "SELECT * FROM tabs WHERE title = ? AND artist = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor checkCursor = db.rawQuery(query,new String[]{title,artist});

        try {
            if (checkCursor.moveToFirst()) {
                exists = true;
//                Log.d("tabExists" + title, "found tab in DB");
            } else {
                exists = false;
//                Log.d("tabExists" + title, "no tab in DB");
            }
        }
        finally{
            checkCursor.close();
        }
        return exists;
    }

    /*
    * Used to retrieve a tab
    * */
    public Tab getTab(int id){
        Tab tab;
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        // 2. build query
        Cursor cursor =
                db.query(TABLE_TABS, // a. table
                        COLUMNS, // b. column names
                        " id = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        if (cursor != null){
            cursor.moveToFirst();

            /*
            * Build our new tab object
            * */
            tab = new Tab();
            tab.setId(Integer.parseInt(cursor.getString(0)));
            tab.setTitle(cursor.getString(1));
            tab.setArtist(cursor.getString(2));
            tab.setLinks(cursor.getString(3));
        }
        else{
            tab = new Tab();
            tab.setId(-1);
            tab.setTitle("NO_TAB");
        }

        Log.d("getTab",tab.toString());
        return tab;
    }

    /*
    * Returning all the artists
    * */
    public List<String>  getAllArtists(){
        List<String> artists = new LinkedList<String>();

        String query = "SELECT artist FROM " + TABLE_TABS;

        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.query(true, TABLE_TABS, new String[] {"artist"}, null, null, "artist", null, null, null);
        if(cursor.moveToNext()){
            do{
                Log.d("getAllArtists : " , cursor.getString(0));
                artists.add(cursor.getString(0));
            }while(cursor.moveToNext());
        }
        cursor.close();
        return artists;
    }

    /*
    * Returning all the tabs that we currently have stored
    * */
    public List<Tab> getAllTabs(){
        Log.d("getAllTabs : " , "beginning query");

        List<Tab> tabs = new LinkedList<Tab>();
        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_TABS;

        // 2. get reference to writable DB
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build tab and add it to list
        Tab tab = null;
        if(cursor.moveToNext()){
            do{
                tab = new Tab();
                tab.setId(Integer.parseInt(cursor.getString(0)));
                tab.setTitle(cursor.getString(1));
                tab.setArtist(cursor.getString(2));
                tab.setLinks(cursor.getString(3));
                tabs.add(tab);
                Log.d("getAllTabs : " , tab.toString());

            }while(cursor.moveToNext());
        }
        Log.d("getAllTabs : " , "end query");
        return tabs;
    }

    /*
    * Function to delete a tab from our database
    * */

    public void deleteTab(Tab tab){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_TABS, //table name
                KEY_ID+" = ?",  // selections
                new String[] { String.valueOf(tab.getId()) });
        // 3. close
        db.close();
        Log.d("deleteTab", tab.toString());
    }
}
