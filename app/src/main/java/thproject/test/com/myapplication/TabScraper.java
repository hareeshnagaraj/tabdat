package thproject.test.com.myapplication;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
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
            /*
            * Scrape functionality if string is ultimate-guitar.com, multiple steps
            * */
            if(string[0] == "ultimate-guitar") {
                String url = ultimateGuitarURL1 + songtitle;
                ultimateGuitarParse(url);
            }
            return null;
        }


        @Override
        protected void onPostExecute(String v){
           Log.d("scrapeAsync","onPostExecute" + v);
            /*
            * After execution, message sent to the SongsActivity to close progress dialog and show options
            * */
            SongsActivity.signalCompletion();
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
               // Log.d("tabscraper link iteration data", link.html());
                if(this.artist.compareTo(link.html()) == 0){
                   currentartist = true;
                }
                else{
                    currentartist = false;
                }
            }
            else{
                /*
                * Storing the link for the tab
                * */
                if(currentartist && (linkClass.compareTo("song") == 0)){
                    String href = link.attr("href");
                    int hrefLength = href.length();
                    String tabIdentifier = href.substring(hrefLength - 8, hrefLength);

                    //getting the print link
                    try {
                        Document tabPage = Jsoup.connect(href).get();
                        Elements printLinkSearch = tabPage.select("a#print_link");
                        printLink = printLinkSearch.attr("abs:href");

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Link newTab = new Link();
                    newTab.setArtist(artist);
                    newTab.setTitle(songtitle);
                    newTab.setLink(printLink);
                    newTab.setSource("ultimate-guitar");

                    //Guitar Tab
                    if(tabIdentifier.compareTo("_tab.htm") == 0 && printLink != null){
//                        Log.d("tabscraper tab type ", "guitar");
//                        Log.d("tabscraper  href ", href);
//                        Log.d("tab scraper print link ",printLink);
                          db.addLink(newTab);
                    }
                    //Bass Tab
                    else if(tabIdentifier.compareTo("btab.htm") == 0 && printLink != null){
//                        Log.d("tabscraper tab type ", "bass");
//                        Log.d("tabscraper  href ", href);
//                        Log.d("tab scraper print link ",printLink);
                          db.addLink(newTab);
                    }

                }

             }
        }
        db.getAllLinks();
    }
}
