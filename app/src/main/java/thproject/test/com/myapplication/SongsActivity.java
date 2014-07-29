package thproject.test.com.myapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
public class SongsActivity extends Activity {
    private String artist;
    private Context context;
    private SongGrabber grabsongs;
    public thproject.test.com.myapplication.NowLayout nowSongLayout;


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
