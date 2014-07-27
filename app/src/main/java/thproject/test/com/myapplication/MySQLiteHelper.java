package thproject.test.com.myapplication;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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

    // tabs Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_ARTIST = "artist";
    private static final String KEY_LINKS = "links";

    private static final String[] COLUMNS = {KEY_ID,KEY_TITLE,KEY_ARTIST,KEY_LINKS};

    public MySQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String tabtable = "CREATE TABLE tabs ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, "+
                "artist TEXT,"+
                "links TEXT )";

        // create books table
        db.execSQL(tabtable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }

    /*
    * used to add a tab
    * */
    public void addTab(Tab newtab){
        Log.d("new tab", newtab.toString());

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
    * Used to retrieve a tab
    * */
    public Tab getTab(int id){
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
        if (cursor != null)
            cursor.moveToFirst();

        /*
        * Build our new tab object
        * */

        Tab tab = new Tab();
        tab.setId(Integer.parseInt(cursor.getString(0)));
        tab.setTitle(cursor.getString(1));
        tab.setArtist(cursor.getString(2));
        tab.setLinks(cursor.getString(3));

        Log.d("getTab",tab.toString());
        return tab;
    }

    /*
    * Returning all the tabs that we currently have stored
    * */
    public List<Tab> getAllTabs(){

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
            }while(cursor.moveToNext());
        }
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
