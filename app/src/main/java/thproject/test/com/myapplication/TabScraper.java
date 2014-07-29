package thproject.test.com.myapplication;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

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

    /*
    *
    * Function to scrape Ultimate-Guitar.com, one of the best tab sites!
    * Used them all the time growing up
    *
    * */
    public void scrapeUltimateGuitar(){
        String url = ultimateGuitarURL1 + songtitle;
        try {
            Document doc = Jsoup.connect("http://en.wikipedia.org/").get();
            Elements newsHeadlines = doc.select(".tresults");
            String test = newsHeadlines.toString();
            Log.d("tabscraper", test);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
