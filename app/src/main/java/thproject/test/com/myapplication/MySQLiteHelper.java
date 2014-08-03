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
import java.util.HashMap;
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
    private static final String TABLE_LINKS = "links";

    // tabs Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_ARTIST = "artist";
    private static final String KEY_LINKS = "links";
    private static final String KEY_LINK = "link";
    private static final String KEY_SOURCE = "source";

    //users table column names
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";

    //keys for the tab DB
    private static final String[] COLUMNS = {KEY_ID,KEY_TITLE,KEY_ARTIST,KEY_LINKS};
    private static final String[] LINK_COLUMNS = {KEY_ID,KEY_ARTIST,KEY_TITLE,KEY_LINK,KEY_SOURCE};

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

    private static final String createLinksDB = "CREATE TABLE links ( " +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "artist TEXT, "+
            "title TEXT, "+
            "link TEXT, "+
            "source TEXT )";

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
        db.execSQL(createLinksDB);
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
    *
    * used to add a tab
    *
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
    *
    * Used to add a link to the DB
    *
    * */
    public void addLink(Link link){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM links WHERE title = ? AND link = ?";
        Cursor checkCursor = db.rawQuery(query,new String[]{link.getTitle(),link.getLink()});
        //Conditionally inserting the link if it doesn't already exist
        if(checkCursor.moveToFirst()){
            //Link exists
        }
        else{
            //Link doesn't exist
            ContentValues values = new ContentValues();
            values.put(KEY_TITLE,link.getTitle());
            values.put(KEY_ARTIST,link.getArtist());
            values.put(KEY_LINK,link.getLink());
            values.put(KEY_SOURCE,link.getSource());
            db.insert(TABLE_LINKS,null,values);
        }
        checkCursor.close();
    }

    /*
    * Get all the links of a specific tab
    * */
//    public List<Link> getLink(String title, String artist){
//        List<Link> tabLinks = new LinkedList<Link>();
//        SQLiteDatabase db = this.getReadableDatabase();
//        Link link;
//        int numlinks;
//        Cursor cursor =
//                db.query(TABLE_LINKS, // a. table
//                        LINK_COLUMNS, // b. column names
//                        " artist = ? AND title = ?", // c. selections
//                        new String[] { artist, title }, // d. selections args
//                        null, // e. group by
//                        null, // f. having
//                        null, // g. order by
//                        null); // h. limit
//        numlinks = cursor.getCount();
//        if (cursor.moveToNext()){
//            do{
//                link = new Link();
//                link.setID(Integer.parseInt(cursor.getString(0)));
//                link.setArtist(cursor.getString(1));
//                link.setTitle(cursor.getString(2));
//                link.setLink(cursor.getString(3));
//                link.setSource(cursor.getString(4));
////                tabLinks.add(link);
//                Log.d("getLink",link.toString());
//            }while(cursor.moveToNext());
//        }
//        return tabLinks;
//    }

    /*
    * Get number of links for a specific tab, store and return a HashMap
    *
    * */
    public HashMap<String,Link> getLink(String artist, String title){
        int numlinks = 0;
        HashMap<String,Link> linkHash = new HashMap();

        SQLiteDatabase db = this.getReadableDatabase();
        Link link;
        Link dummyLink = new Link();
        Cursor cursor =
                db.query(TABLE_LINKS, // a. table
                        LINK_COLUMNS, // b. column names
                        " artist = ? AND title = ?", // c. selections
                        new String[] { artist, title }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit
        numlinks = cursor.getCount();
        dummyLink.setTitle("numberOfLinks");
        dummyLink.setID(numlinks);
        linkHash.put("numlinks",dummyLink);

        if (cursor.moveToNext()){
            do{
                link = new Link();
                link.setID(Integer.parseInt(cursor.getString(0)));
                link.setArtist(cursor.getString(1));
                link.setTitle(cursor.getString(2));
                link.setLink(cursor.getString(3));
                link.setSource(cursor.getString(4));
                numlinks++;
//                Log.d("getLink",link.toString());
                linkHash.put(Integer.toString(numlinks),link);
            }while(cursor.moveToNext());
        }
//        Log.d("getLink",Integer.toString(numlinks));
        cursor.close();
        return linkHash;
    }

    /*
    * Get link from a specific source
    * */
    public Link getLinkFromSource(String source){
        Link link = new Link();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =
                db.query(TABLE_LINKS, // a. table
                        LINK_COLUMNS, // b. column names
                        " link = ?", // c. selections
                        new String[] { source }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit
        int numlinks = cursor.getCount();
        Log.d("getLinkFromSource num : ", Integer.toString(numlinks));
        if(cursor.moveToFirst()){
            link.setID(Integer.parseInt(cursor.getString(0)));
            link.setArtist(cursor.getString(1));
            link.setTitle(cursor.getString(2));
            link.setLink(cursor.getString(3));
            link.setSource(cursor.getString(4));
        }
        return link;
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
//                Log.d("getAllArtists : " , cursor.getString(0));
                artists.add(cursor.getString(0));
            }while(cursor.moveToNext());
        }
        cursor.close();
        return artists;
    }

    /*
    * Returning all the links we have stored
    * */
    public List<Link> getAllLinks(){
//        Log.d("getAllLinks","begin query");
        List<Link> links = new LinkedList<Link>();
        String query = "SELECT * FROM " + TABLE_LINKS;
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        Link link = null;
        if(cursor.moveToNext()){
            do{
               link = new Link();
               link.setID(Integer.parseInt(cursor.getString(0)));
               link.setArtist(cursor.getString(1));
               link.setTitle(cursor.getString(2));
               link.setLink(cursor.getString(3));
               link.setSource(cursor.getString(4));
               links.add(link);
//               Log.d("getAllLinks",link.toString());

            }while(cursor.moveToNext());
        }
        Log.d("getAllLinks","end query");
        return links;
    }

    /*
    *
    * Get number of tabs by an artist
    *
    * */
    public int getNumberOfTabsBy(String artist){
        int numtabs = 0;
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(true, TABLE_TABS, new String[] {"title"}, "artist=?", new String[] {artist}, null , null, "title", null);
        if(cursor.moveToNext()){
            do{
//                Log.d("getNumberOfTabsBy : " , cursor.getString(0));
                numtabs++;
            }while(cursor.moveToNext());
        }
        return numtabs;
    }
    /*
    *
    * Get all tabs by an artist
    *
    * */
    public List<String> getSongsBy(String artist){
        List<String> songs = new LinkedList<String>();
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.query(true, TABLE_TABS, new String[] {"title"}, "artist=?", new String[] {artist}, null , null, "title", null);
        if(cursor.moveToNext()){
            do{
//                Log.d("getSongsBy : " , cursor.getString(0));
                songs.add(cursor.getString(0));
            }while(cursor.moveToNext());
        }
        return songs;
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
//                Log.d("getAllTabs : " , tab.toString());

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
