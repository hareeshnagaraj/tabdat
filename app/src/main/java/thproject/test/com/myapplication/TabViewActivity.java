package thproject.test.com.myapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import java.util.HashMap;
import java.util.Map;

import thproject.test.com.myapplication.R;

import static thproject.test.com.myapplication.MySQLiteHelper.getDB;

/*
*
* This class is used to actually display the tab saved in the database, using a WebView
*
* */
public class TabViewActivity extends Activity {
    private Link display;
    private String link;
    String song = "";
    String source;
    String artist;
    MySQLiteHelper db = getDB(this);
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_view);


        //Grabbing our link itself
        Bundle extras = this.getIntent().getExtras();
        if ( extras != null ) {
            if ( extras.containsKey("link") ) {
                link = extras.getString("link");
            }
            if( extras.containsKey("source")){
                source = extras.getString("source");
//                Log.d("TabViewActivity source",source);
            }
            if(extras.containsKey("artist")){
                artist = extras.getString("artist");
            }
            if(extras.containsKey("title")){
                song = extras.getString("title");
            }
        }
        //disable application icon from ActionBar, set up remaining attributes
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(song);

        //load in the webview
        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(false);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setScrollbarFadingEnabled(false);
        webView.setInitialScale(50);

        //Passing headers into the webView to avoid ugly ads and such
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.125 Safari/537.36");

        if (source == "guitartabs.cc"){
            headers.put("Host","www.guitartabs.cc");
        }
        headers.put("Referrer",source);
        webView.loadUrl(link,headers);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tab_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.shareSong) {
            shareDialog();
            return true;
        }
        if(id == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    * Used to share the tab to different platforms
    * */
    public void shareDialog(){
        Intent intent=new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.putExtra(Intent.EXTRA_SUBJECT, this.song + " tab by " +   this.artist );
        intent.putExtra(Intent.EXTRA_TEXT, "Link: " + this.link + "\nFrom tab dat!");
        startActivity(Intent.createChooser(intent, "Share tab"));
    }

}