package thproject.test.com.myapplication;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

/**
 * Created by hareeshnagaraj on 7/28/14.
 *
 * Class that will actually scrape the web for tabs and store them in the database
 */

public class TabScraper {
    private String artist;
    private String songtitle;
    private static final String ultimateGuitarURL1 = "http://www.ultimate-guitar.com/search.php?search_type=title&value=";

    public TabScraper(String artist, String songtitle){
        this.artist = artist;
        this.songtitle = songtitle;
    }

    public TabScraper(){    }

    /*
    * Setter for artist and title
    * */
    public void setArtist(String artist){
        this.artist = artist;
    }
    public void setSongTitle(String title){
        this.songtitle = title;
    }

    /*
    *
    * Function to scrape Ultimate-Guitar.com, one of the best tab sites!
    * Used them all the time growing up
    *
    * */
    public void scrapeUltimateGuitar(){
        new scrapeAsync().execute("ultimate-guitar");

    }

    /*
    * AsyncTask to actually do the scraping off the main network thread
    * */
    public class scrapeAsync extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... string) {
            try {

                /*
                * Scrape functionality if string is ultimate-guitar.com
                * */
                if(string[0] == "ultimate-guitar") {
                    String url = ultimateGuitarURL1 + songtitle;
                    Document doc = Jsoup.connect(url).get();
                    Elements newsHeadlines = doc.select(".tresults");
                    String test = newsHeadlines.toString();
                    Log.d("tabscraper", test);
                    Elements artistTest = doc.select(".song.search_art");
                    Log.d("tabscraper artist ", artistTest.toString());

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
