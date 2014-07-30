package thproject.test.com.myapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import thproject.test.com.myapplication.R;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static thproject.test.com.myapplication.MySQLiteHelper.getDB;

/*
*
* In this class we display all the songs of a particular artist, then allow a click to show the available tabs
*
*
* */
public class SongsActivity extends FragmentActivity implements TabPickerDialog.TabPickerListener{
    private String artist;
    Context context;
    private SongGrabber grabsongs = new SongGrabber();
    private static Handler handler;
    public thproject.test.com.myapplication.NowLayout nowSongLayout;
    public ProgressDialog progressDialog;
    MySQLiteHelper db = getDB(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs);
        Bundle extras = this.getIntent().getExtras();
        if ( extras != null ) {
            if ( extras.containsKey("artist") ) {
                this.artist = extras.getString("artist");
                Log.d("SongsActivity", artist);
            }
        }
        context = getApplicationContext();

        //Handler to show loading progress
        handler = new Handler(){
            public void handleMessage(Message msg) {
                Bundle data = msg.getData();
                String artist = data.getString("artist");
                String title = data.getString("title");
                String action = data.getString("action");

                //First action occurs when no tabs are present, progress dialog shown
                if(action.compareTo("init") == 0) {
                    progressDialog = new ProgressDialog(SongsActivity.this);
                    progressDialog.setMessage("Loading tab " + title);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.show();
                    //begin scrape process
                    beginScraper(artist,title);
                }
                //Action when loading the tabs is complete
                if(action.compareTo("complete") == 0){
                    progressDialog.hide();
                    Toast.makeText(getApplicationContext(),"Loading complete!",Toast.LENGTH_SHORT).show();
                    showTabDialog(artist,title);
                }
                //Action if tab alreadye exists
                if(action.compareTo("exists") == 0){
                    if(progressDialog != null) {
                        progressDialog.hide();
                    }
                    showTabDialog(artist,title);
                }

            }
        };



        //disable application icon from ActionBar, set up remaining attributes
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(artist);

        nowSongLayout = (thproject.test.com.myapplication.NowLayout) findViewById(R.id.mainSongsLayout);
        context = getApplicationContext();
        grabsongs.displaySongs(context,nowSongLayout,artist);

    }



    public static void showProgress(String artist, String title, String action){
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("artist",artist);
        data.putString("title",title);
        data.putString("action", action);
        msg.setData(data);
        handler.sendMessage(msg);
    }

    public static void signalCompletion(String a){
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("action",a);
        msg.setData(data);
        handler.sendMessage(msg);
    }

    public static void signalCompletion(String a, String b, String c){
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("action",a);
        data.putString("artist",b);
        data.putString("title",c);
        msg.setData(data);
        handler.sendMessage(msg);
    }

    public void beginScraper(String artist, String title){
        final TabScraper scraper = new TabScraper();
        scraper.setArtist(artist);
        scraper.setSongTitle(title);
        scraper.scrapeUltimateGuitar();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.songs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if(id == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //method to show our dialog
    public void showTabDialog(String artist,String title){
        TabPickerDialog dialog = new TabPickerDialog();
        HashMap<String[],List<Link>> popupItems = tabDialogItems(artist,title);
        Iterator it = popupItems.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            String[] items = (String[]) pairs.getKey();
            List<Link> links = (List<Link>) pairs.getValue();
//            dialog.setItems(items);
            it.remove(); // avoids a ConcurrentModificationException
        }
        dialog.show(getFragmentManager(),"TabPickerDialog");

    }

    //Method to create list of items that populate tabPickerdialog
    public HashMap<String[],List<Link>> tabDialogItems(String artist, String title){

        HashMap<String,Link> songHash = db.getLink(artist,title);   //gets the number of matching links from our DB
        List<Link> links = new LinkedList<Link>();

        Iterator it = songHash.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            Link currentLink = (Link) pairs.getValue();
            String currentSource = currentLink.getSource();
            Log.d("tabDialogItems",currentLink.toString());
            if(currentSource != null){  //adding the item to our list
                links.add(currentLink);
            }
            it.remove(); // avoids a ConcurrentModificationException
        }

        //copying our links to a static array
        String[] items = new String[links.size()];
        for(int i = 0; i < links.size(); i++) items[i] = ("Tab " + Integer.toString(i+1));

        //preparing the return hash
        HashMap<String[],List<Link>> returnHash = new HashMap<String[], List<Link>>();
        returnHash.put(items,links);
        return returnHash;
    }
    //Overrides the method from the fragment TabPickerDialog
    @Override
    public void onTabClick(DialogFragment dialog) {

    }


}
