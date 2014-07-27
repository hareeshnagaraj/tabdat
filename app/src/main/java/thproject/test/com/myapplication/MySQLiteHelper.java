package thproject.test.com.myapplication;

import android.app.ActionBar;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
    private static final String KEY_AUTHOR = "artist";
    private static final String KEY_LINKS = "links";

    private static final String[] COLUMNS = {KEY_ID,KEY_TITLE,KEY_AUTHOR,KEY_LINKS};

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

    }
 }
