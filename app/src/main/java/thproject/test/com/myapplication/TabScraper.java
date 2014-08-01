package thproject.test.com.myapplication;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static thproject.test.com.myapplication.MySQLiteHelper.getDB;

/**
 * Created by hareeshnagaraj on 7/28/14.
 *
 * Class that will actually scrape the web for tabs and store them in the database
 */

public class TabScraper extends Activity{
    private String artist;
    private String songtitle;
    private static final String ultimateGuitarURL1 = "http://www.ultimate-guitar.com/search.php?search_type=title&value=";
    MySQLiteHelper db = getDB(this);
    private String callingActivity;
    private List<String> selectedTracks = new LinkedList<String>();

    public TabScraper(String artist, String songtitle){
        this.artist = artist;
        this.songtitle = songtitle;
    }

    public TabScraper(){    }

    /*
    * Setters for artist, title, calling activity, selected tracks
    * */
    public void setArtist(String artist){
        this.artist = artist;
    }
    public void setSongTitle(String title){
        this.songtitle = title;
    }
    public void setCallingActivity(String activity){
        this.callingActivity = activity;
        Log.d("TabScraper calling activity",callingActivity);
    }
    public void setSelectedTracks(List<String> a){
        selectedTracks = a;
    }


    /*
    *   Function to scrape the internet for tabs, different AsyncTask based on calling activity
    * */
    public void scrape(){
        if(callingActivity.compareTo("SongsActivity") == 0) {
            new scrapeAsync().execute();
        }
        if(callingActivity.compareTo("SongRecognitionActivity") == 0){
            new scrapeAsyncArray().execute();
        }
    }

    /*
    * AsyncTask to actually do the scraping off the main network thread from SongsActivity
    * */
    public class scrapeAsync extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... string) {
            /*
            * Scrape functionality if string is ultimate-guitar.com, multiple steps
            * */
            Log.d("scrapeAsync","doInBackground");
            String ultimateGuitarURL = null;
            try {
                ultimateGuitarURL = ultimateGuitarURL1 + URLEncoder.encode(songtitle, "UTF-8");
                Log.d("scrapeAsync URL",ultimateGuitarURL);
                ultimateGuitarParse(ultimateGuitarURL);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String v){
           Log.d("scrapeAsync","onPostExecute" + v);
            /*
            * After execution, message sent to the SongsActivity to close progress dialog and show options
            * */
            SongsActivity.signalCompletion("complete",artist,songtitle);
        }
    }

    /*
    * AsyncTask to actually do the scraping off the main network thread from SongRecognitionActivity
    * Performs task in a loop
    * */
    public class scrapeAsyncArray extends AsyncTask <Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d("scrapeAsyncArray","artist : " + artist);
            for(int i = 0; i < selectedTracks.size(); i++){
                String track = selectedTracks.get(i);
                songtitle = track;
                Log.d("scrapeAsyncArray","scraping: " + track);
                try {
                    String ultimateGuitarURL = ultimateGuitarURL1 + URLEncoder.encode(track, "UTF-8");
                    ultimateGuitarParse(ultimateGuitarURL);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
            return null;
        }
        @Override
        protected void onPostExecute(Void v){
            Log.d("scrapeAsyncArray","completed");

        }
    }




    /*
    *
    * Function to parse UG page
    * 1. Execute query and get results for search page
    * 2. Open links to each tab and store print version
    * */
    public void ultimateGuitarParse(String url){
        Boolean currentartist = false;
        String printLink = "";

        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements table = doc.select(".tresults");
        Elements tableCells = table.select("a");

        //iterating and getting links
        for( Element link : tableCells ){
            String linkClass = link.className();
//          Log.d("tabscraper link class", linkClass);

            if(linkClass.compareTo("song search_art") == 0){
                /*
                * Defining the current artist based on the above string comparison, which allows us to perform certain actions
                * */
                if(this.artist.compareTo(link.html()) == 0){
                   currentartist = true;
                   Log.d("tabscraper current artist", link.html());

                }
                else{
                    currentartist = false;
                }
            }
            else{
                /*
                * Storing the link for the tab  -- need to store bass/guitar as well as ratings
                * */
                if(currentartist && (linkClass.compareTo("song") == 0)){
                    String href = link.attr("href");
                    int hrefLength = href.length();
                    String tabIdentifier = href.substring(hrefLength - 8, hrefLength);


                    Link newTab = new Link();
                    newTab.setArtist(artist);
                    newTab.setTitle(songtitle);
                    newTab.setLink(href);
                    newTab.setSource("ultimate-guitar");
                    Log.d("tabscraper href",href);
                    Log.d("tabscraper addlink",newTab.toString());

                    db.addLink(newTab);

                }

             }
        }
        db.getAllLinks();
    }
}
