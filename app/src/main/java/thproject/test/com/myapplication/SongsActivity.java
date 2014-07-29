package thproject.test.com.myapplication;

import android.app.ActionBar;
import android.app.Activity;
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

/*
*
* In this class we display all the songs of a particular artist, then allow a click to show the available tabs
*
*
* */
public class SongsActivity extends FragmentActivity{
    private String artist;
    Context context;
    private SongGrabber grabsongs;
    private static Handler handler;
    public thproject.test.com.myapplication.NowLayout nowSongLayout;
    public ProgressDialog progressDialog;

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
        grabsongs = new SongGrabber();
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

    public static void signalCompletion(){
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("action","complete");
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
}
