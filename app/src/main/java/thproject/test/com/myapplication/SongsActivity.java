package thproject.test.com.myapplication;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
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
* */
public class SongsActivity extends FragmentActivity implements TabPickerDialog.TabPickerListener,SearchDialog.SearchListener{
    private String artist;
    Context context;
    private SongGrabber grabsongs = new SongGrabber();
    private static Handler handler;
    public thproject.test.com.myapplication.NowLayout nowSongLayout;
    public ProgressDialog progressDialog;
    SearchDialog searchDialog;
    MySQLiteHelper db = getDB(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs);
        Bundle extras = this.getIntent().getExtras();
        if ( extras != null ) {
            if ( extras.containsKey("artist") ) {
                this.artist = extras.getString("artist");
//                Log.d("SongsActivity", artist);
            }
        }

        context = getApplicationContext();
        //disable application icon from ActionBar, set up remaining attributes
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(artist);

        nowSongLayout = (thproject.test.com.myapplication.NowLayout) findViewById(R.id.mainSongsLayout);
        context = getApplicationContext();
        grabsongs.displaySongs(context,nowSongLayout,artist);

        //Handler to show loading progress
        handler = new Handler(){
            public void handleMessage(Message msg) {
                Bundle data = msg.getData();
                String artist = data.getString("artist");
                String title = data.getString("title");
                String action = data.getString("action");

                //First action occurs when no tabs are present, progress dialog shown
                if(action.compareTo("init") == 0 && checkInternetConnection()) {
                    if(searchDialog != null){
                        searchDialog.dismiss();
                    }
                    progressDialog = new ProgressDialog(SongsActivity.this);
                    progressDialog.setMessage("Loading tab " + title);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.show();
                    //begin scrape process
                    beginScraper(artist,title);
                }
                //Action when loading the tabs is complete
                if(action.compareTo("complete") == 0){
                    if(progressDialog != null) {
                        progressDialog.hide();
                        progressDialog.dismiss();
                    }
                    int numtabs = data.getInt("numtabs");
                    if(numtabs > 0){
                        Toast.makeText(getApplicationContext(),Integer.toString(numtabs)+" tabs added",Toast.LENGTH_SHORT).show();
                    }
                    if(!cardExists(title)){
//                        Log.d("cardExists is false: ", title);
                        stripCards();
                        grabsongs.displaySongs(context,nowSongLayout,artist);
                        showTabDialog(artist,title);
                    }
                    else{
                        showTabDialog(artist,title);
                    }
                }
                //Action if tab alreadye exists
                if(action.compareTo("exists") == 0){
                    if(progressDialog != null) {
                        progressDialog.hide();
                        progressDialog.dismiss();
                    }
                    showTabDialog(artist,title);

                }
                if(action.contentEquals("toast")){
                    String toast = data.getString("toast");
                    Toast.makeText(getApplicationContext(),toast,Toast.LENGTH_SHORT).show();
                }
                //Action to start the TabViewActivity
                if(action.compareTo("showtab") == 0){
                    String src = data.getString("link");
                    String srctype = data.getString("source");
                    Bundle extras = new Bundle();
                    extras.putString("link",src);
                    extras.putString("source",srctype);
                    extras.putString("title",data.getString("title"));
                    extras.putString("artist",data.getString("artist"));
                    startTabView(extras);
                }
            }
        };

    }

    /*
    * Function to conditionally add card to layout
    * */
    public Boolean cardExists(String title){
        int numChildren = nowSongLayout.getChildCount();
        Boolean childExists = false;
        for(int i = 0; i < numChildren; i ++){
            TextView currentView = (TextView) nowSongLayout.getChildAt(i);
            String currentText = currentView.getText().toString();
//            Log.d("getChildren",currentText);
            if(currentText.contentEquals(title)){
                childExists = true;
            }
        }
        return childExists;
    }

    /*
    * remove all cards from the layout
    * */
    public void stripCards(){
       nowSongLayout.removeAllViews();
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
    /*
    * Action to show toasts
    * */
    public static void toasty(String a){
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("toast",a);
        data.putString("action","toast");
        msg.setData(data);
        handler.sendMessage(msg);
    }
    /*
    * Used to signal the handler with the appropriate message
    * */
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
    public static void signalCompletion(String a, String b, String c, int d){
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("action",a);
        data.putString("artist",b);
        data.putString("title",c);
        data.putInt("numtabs",d);
        msg.setData(data);
        handler.sendMessage(msg);
    }

    /*
    * Used to begin the TabScraping process
    * */
    public void beginScraper(String artist, String title){
        final TabScraper scraper = new TabScraper();
        scraper.setArtist(artist);
        scraper.setSongTitle(title);
        scraper.setCallingActivity("SongsActivity");
        scraper.scrape();
    }

    /*
    * Used to signal handler to begin TabViewActivity
    * */
    public static void signalTabView(Link link){
        Message msg = new Message();
        Bundle data = new Bundle();

        data.putString("link",link.getLink());
        data.putString("source",link.getSource());
        data.putString("title",link.getTitle());
        data.putString("artist",link.getArtist());
        data.putString("action","showtab");

        msg.setData(data);
        handler.sendMessage(msg);
    }
    /*
    * Used to show the search dialog
    * */
    public void showSearchDialog(){
        searchDialog = new SearchDialog();
        searchDialog.setCallingActivity("SongsActivity");
        searchDialog.setArtist(artist);
        searchDialog.show(getFragmentManager(),"searchDialog");
    }

    /*
    * Used to check internet connectivity
    * */
    private boolean checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // test for connection
        if (cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            Log.v("checkInternetConnection", "Internet Connection Not Present");
            toasty("Connect mobile data or WiFi network");
            return false;
        }
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
        if ( id == R.id.searchInSongActivity ){
            showSearchDialog();
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
        int length = -1;

        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            String[] items = (String[]) pairs.getKey();
            List<Link> links = (List<Link>) pairs.getValue();
            length = links.size();
            dialog.setItems(items);
            dialog.setLinks(links);
            it.remove(); // avoids a ConcurrentModificationException
        }
//        Log.d("showTabDialog links length", Integer.toString(length));
        if(length > 0){
            dialog.show(getFragmentManager(),"TabPickerDialog");
        }
        else{
            Toast.makeText(getApplicationContext(),"No Tabs found :(",Toast.LENGTH_SHORT).show();
        }
    }

    //Method to create list of items that populate tabPickerdialog
    public HashMap<String[],List<Link>> tabDialogItems(String artist, String title){

        HashMap<String,Link> songHash = db.getLink(artist,title);   //gets the number of matching links from our DB
        List<Link> links = new LinkedList<Link>();

        Iterator it = songHash.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            Link currentLink = (Link) pairs.getValue();
            String currentSource = currentLink.getLink();
            if(currentSource != null && currentSource.contains("http")){  //adding the item to our list
//                Log.d("tabDialogItems",currentLink.toString());
                links.add(currentLink);
            }
            it.remove(); // avoids a ConcurrentModificationException
        }

        //copying our links to a static array

        String[] items = new String[links.size()];
        for(int i = 0; i < links.size(); i++) {
         String originalLink = links.get(i).getLink();
         String source = links.get(i).getSource();
         String displayItem = "Version " + Integer.toString(i+1) + " (" + source + ")";

         //Seeing if we have a bass tab or not
         if(originalLink.contains("btab.htm")){
            displayItem += " (Bass)";
         }

         items[i] = (displayItem);
        }

        //preparing the return hash
        HashMap<String[],List<Link>> returnHash = new HashMap<String[], List<Link>>();
        returnHash.put(items,links);
        return returnHash;
    }
    //Overrides the method from the fragment TabPickerDialog and searchDialog
    @Override
    public void onTabClick(DialogFragment dialog) {

    }

    /*
    * Method to start a new TabViewActivity
    *
    * Ultimate-Guitar links will open in browser, other links will ideally open within the app
    * */
    private void startTabView(Bundle extras){

        String source = (String) extras.get("source");
        String strUrl = (String) extras.get("link");
        if (!strUrl.startsWith("http://") && !strUrl.startsWith("https://")){
            strUrl= "http://" + strUrl;
        }

        if(source.compareTo("ultimate-guitar") == 0){       //only for ultimate-guitar
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(strUrl)));
        }
        else {
            Intent i;
            i = new Intent(SongsActivity.this, TabViewActivity.class);
            i.putExtras(extras);
            startActivity(i);
            return;
       }
        // close this activity
    }


}
