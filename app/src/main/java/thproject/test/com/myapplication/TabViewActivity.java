package thproject.test.com.myapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import thproject.test.com.myapplication.R;

import static thproject.test.com.myapplication.MySQLiteHelper.getDB;

/*
*
* This class is used to actually display the tab saved in the database, using a WebView
*
* */
public class TabViewActivity extends Activity {
    private Link display;
    String link;
    String song = "";
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
                Log.d("TabViewActivity raw link",link);
                display = db.getLinkFromSource(link);
                Log.d("TabViewActivity queried link",display.toString());
                try{
                    song = display.getTitle();
                }
                catch(NullPointerException a){
                    song = "Unknown";
                }
            }
        }
        //disable application icon from ActionBar, set up remaining attributes
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(song);

        //load in the webview
        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        webView.loadUrl(link);


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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}