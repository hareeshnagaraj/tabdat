package thproject.test.com.myapplication;
import android.app.Activity;
import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

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
    private static final String guitareTabURLPrefix = "http://www.guitaretab.com/fetch/?type=tab&query=";
    private static final String guitarTabsCCPrefix = "http://www.guitartabs.cc/search.php?tabtype=any&band=&song=";
    private static final String echordsPrefix = "http://www.e-chords.com/search-all/";

    MySQLiteHelper db = getDB(this);
    private String callingActivity;
    private List<String> selectedTracks = new LinkedList<String>();
    int numTabs = 0; //used to count the tabs, exclusively in MainTabActivity

    public TabScraper(String artist, String songtitle){
        this.artist = artist;
        this.songtitle = songtitle;
    }

    public TabScraper(){    }

    /*
    * Setters for artist, title, calling activity, selected tracks
    * */
    public void setArtist(String artist){
        this.artist = capitalizeEachWord(artist);
    }
    public void setSongTitle(String title){
        this.songtitle = capitalizeEachWord(title);
    }
    public void setCallingActivity(String activity){
        this.callingActivity = activity;
//        Log.d("TabScraper calling activity",callingActivity);
    }
    public void setSelectedTracks(List<String> a){
        selectedTracks = a;
    }
    /*
    * Used to capitalize each word when setting the artist, to avoid multiple instances of same artist
    * */
    public String capitalizeEachWord(String a){
        String[] words = a.split(" ");
        StringBuilder sb = new StringBuilder();
        if (words[0].length() > 0) {
            sb.append(Character.toUpperCase(words[0].charAt(0)) + words[0].subSequence(1, words[0].length()).toString().toLowerCase());
            for (int i = 1; i < words.length; i++) {
                sb.append(" ");
                sb.append(Character.toUpperCase(words[i].charAt(0)) + words[i].subSequence(1, words[i].length()).toString().toLowerCase());
            }
        }
        String titleCaseValue = sb.toString();
        return titleCaseValue;
    }


    /*
    * Function to scrape the internet for tabs, different AsyncTask based on calling activity
    * Tests for internet connection
    * */
    public void scrape(){
        if(callingActivity.compareTo("SongsActivity") == 0) {
           new scrapeAsync().execute();
        }
        if(callingActivity.compareTo("MainTabActivity") == 0) {
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
//            Log.d("scrapeAsync","doInBackground");
            String ultimateGuitarURL = null;
            String guitareTabURL = null;

            if(!db.tabExists(songtitle,artist)){            //adding to our tab database
                Tab newTab = new Tab();
                newTab.setTitle(songtitle);
                newTab.setArtist(artist);
                db.addTab(newTab);
//                Log.d("scrapeAsync","adding artist");
            }
            try {
                ultimateGuitarURL = ultimateGuitarURL1 + URLEncoder.encode(songtitle, "UTF-8");
                guitareTabURL = guitareTabURLPrefix + URLEncoder.encode(songtitle, "UTF-8");
                String guitarCCURL = guitarTabsCCPrefix + URLEncoder.encode(songtitle, "UTF-8");
                String echordsURL = echordsPrefix + URLEncoder.encode(songtitle, "UTF-8");

//                Log.d("scrapeAsync URL",ultimateGuitarURL);
                guitarTabCCParse(guitarCCURL);
                guitareTabParse(guitareTabURL);
                ultimateGuitarParse(ultimateGuitarURL);
                echordScrape(echordsURL);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String v){
//           Log.d("scrapeAsync","onPostExecute" + v);
            /*
            * After execution, message sent to the SongsActivity or MainTabActivity to close progress dialog and show options
            * */
            if(callingActivity.compareTo("SongsActivity") == 0){
                SongsActivity.signalCompletion("complete",artist,songtitle,numTabs);
            }
            if(callingActivity.compareTo("MainTabActivity") == 0){
                MainTabActivity.scrapeCompleted(numTabs);   //sending the number of tabs back to the main activity
            }
        }
    }

    /*
    * AsyncTask to actually do the scraping off the main network thread from SongRecognitionActivity
    * Performs task in a loop
    * */
    public class scrapeAsyncArray extends AsyncTask <Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
//            Log.d("scrapeAsyncArray","artist : " + artist);
            SongRecognitionActivity.showProgress();
            for(int i = 0; i < selectedTracks.size(); i++){
                String track = selectedTracks.get(i);
                songtitle = track;
                SongRecognitionActivity.progressText(track);

                if(!db.tabExists(songtitle,artist)){            //adding to our tab database
                    Tab newTab = new Tab();
                    newTab.setTitle(songtitle);
                    newTab.setArtist(artist);
                    db.addTab(newTab);
                }

//                Log.d("scrapeAsyncArray","scraping: " + track);
                try {
                    String ultimateGuitarURL = ultimateGuitarURL1 + URLEncoder.encode(track, "UTF-8");
                    String guitareTabURL = guitareTabURLPrefix + URLEncoder.encode(songtitle, "UTF-8");
                    String guitarCCURL = guitarTabsCCPrefix + URLEncoder.encode(songtitle, "UTF-8");
                    String echordsURL = echordsPrefix + URLEncoder.encode(songtitle, "UTF-8");

                    guitarTabCCParse(guitarCCURL);
                    guitareTabParse(guitareTabURL);
                    ultimateGuitarParse(ultimateGuitarURL);
                    echordScrape(echordsURL);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            SongRecognitionActivity.stopProgress();
            return null;
        }
        @Override
        protected void onPostExecute(Void v){
//            Log.d("scrapeAsyncArray","completed");
            SongRecognitionActivity.exitSongRecognition();
        }
    }
    /*
    *
    * Function to parse UG page
    * 1. Execute query and get results for search page
    * 2. Store links to each tab
    * */
    public void ultimateGuitarParse(String url){
        Boolean currentartist = false;
        String printLink = "";
        Elements tableCells = null;

        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
            Elements table = doc.select(".tresults");
            tableCells = table.select("a");
            //iterating and getting links
            for( Element link : tableCells ){
                String linkClass = link.className();
//          Log.d("tabscraper link class", linkClass);
                if(linkClass.compareTo("song search_art") == 0){
                /*
                * Defining the current artist based on the above string comparison, which allows us to perform certain actions
                * */
                    String comparisonLocal = stripSpecialChars(this.artist);    //using a regular expression to compare values
                    String comparisonLink = stripSpecialChars(link.html());

//                    Log.d("Regex Comparison",comparisonLocal + " " + comparisonLink);
                    if(compareLocalToRemote(this.artist,link.html())){
                        currentartist = true;
//                   Log.d("tabscraper current artist", link.html());

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
                        Element parent = link.parent().nextElementSibling().nextElementSibling();   //getting the tab type by going over the sibling elements
                        Element typeWrapper = parent.child(0);
                        String linkType = typeWrapper.html();

                        //only adding bass and guitar tabs to our database, avoiding tab pro/power tab
                        if(linkType.contentEquals("tab") || linkType.contentEquals("bass") || linkType.contentEquals("chords")) {
//                            Log.d("Adding tab of type",linkType);
                            Link newLink = new Link();
                            newLink.setArtist(artist);
                            newLink.setTitle(songtitle);
                            newLink.setLink(href);
                            newLink.setSource("ultimate-guitar");
//                        Log.d("tabscraper href", href);
//                        Log.d("tabscraper addlink", newLink.toString());
                            db.addLink(newLink);                            //adding to our link database
                            numTabs++;
                        }
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }
    /*
    * Function to parse Guitaretab page
    * Issues resolved - Log.d not containing enough characters
    *
    * */
    public void guitareTabParse(String url){
//        Log.d("guitareTabParse","begin url " + url);
        Boolean currentartist = false;
        String printLink = "";

        Document doc = null;
        try {
            doc = Jsoup.connect(url)
                    .header("Accept-Encoding", "gzip, deflate")
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                    .maxBodySize(0)
                    .timeout(600000)
                    .get();

            Elements table = doc.select(".specrows");
            Elements tableInner = table.first().select("li");
//            Log.d("guitareTabParse table",table.toString());
//            Log.d("guitareTabParse tableInner",tableInner.toString());
            for(Element tableElement : tableInner){
                Elements links = tableElement.select("a");
                int numlinks = links.size();
                if(numlinks > 0){           //only getting links if they exist
                    Element artistLink = links.get(0);
                    Element songLink = links.get(1);
                    String artistString = artistLink.html();

                    String comparisonLink = stripSpecialChars(artistString);
                    String comparisonLocal = stripSpecialChars(this.artist);
                    if(compareLocalToRemote(this.artist,artistString)){
//                        Log.d("guitareTabParse adding " , artistLink.toString());

                        String href = songLink.attr("abs:href");
                        Link newLink = new Link();
                        newLink.setArtist(artist);
                        newLink.setTitle(songtitle);
                        newLink.setLink(href);
                        newLink.setSource("guitaretab");
                        db.addLink(newLink);                            //adding to our link database
                        numTabs++;

                    }
//                    Log.d("guitareTabParse artist" , artistLink.toString());
//                    Log.d("guitareTabParse link" , songLink.toString());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
//        Log.d("guitareTabParse","end");

    }

    /*
    * Function to parse Guitartabs.cc
    * */
    public void guitarTabCCParse(String url){
//        Log.d("guitarTabCCParse","begin url " + url);
        Document doc = null;
        try {
            doc = Jsoup.connect(url)
                    .header("Accept-Encoding", "gzip, deflate")
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                    .maxBodySize(0)
                    .timeout(600000)
                    .get();

            Elements outerTable = doc.select(".tabslist");
            Element outerTableBody = outerTable.get(0);
            Elements innerTable = outerTableBody.children().select("tr");

            String comparisonLocal = stripSpecialChars(this.artist);

            for( Element tableElement : innerTable ){
                Elements links = tableElement.select("a");
                int numLinks = links.size();
//                Log.d("guitarTabCCParse element",tableElement.toString());
//                Log.d("guitarTabCCParse numLinks",Integer.toString(numLinks));
                if(numLinks > 0){       //only performing actions if link is present
                    String artist = links.get(0).html();
                    Element tableLink = links.get(1);
                    String href = tableLink.attr("abs:href");
                    String comparisonLink = stripSpecialChars(artist);
//                    Log.d("guitarTabCCParse Link1",links.get(0).toString());
//                    Log.d("guitarTabCCParse Artist",artist);
//                    Log.d("guitarTabCCParse href",href);

                    if(compareLocalToRemote(this.artist,artist)){      //only adding valid tabs to our database
//                        Log.d("guitarTabCCParse href","adding tab");
                        Link newLink = new Link();
                        newLink.setArtist(artist);
                        newLink.setTitle(songtitle);
                        newLink.setLink(href);
                        newLink.setSource("guitartabs.cc");
                        db.addLink(newLink);                            //adding to our link database
//                        Log.d("guitarTabCCParse addlink", newLink.toString());
                        numTabs++;
                    }
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /*
    * Function to scrape the site echords
    * */
    public void echordScrape(String url){
        String testURL = url.replace("+","%20");
//        Log.d("echordScrape testURL",testURL);
        try {
            Document doc = Jsoup.connect(testURL)
                    .header("Accept-Encoding", "gzip, deflate")
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                    .maxBodySize(0)
                    .timeout(600000)
                    .get();
            Elements composersList = doc.select(".lista");
//            Log.d("echordScrape composersList num : ",Integer.toString(composersList.size()));
            for(Element composer : composersList){
//                Log.d("echordScrape composer block",composer.toString());
                Element artist = composer.select("h2").first().child(0);
                Element link = composer.select("h1").first().child(0);

                if(compareLocalToRemote(this.artist,artist.html())){                  //conditionally adding the tab if the artist matches
                    String href = "http://www.e-chords.com/"+link.attr("href");     //different than other functions, absolute value does not apply
                    Link newLink = new Link();
                    newLink.setArtist(this.artist);
                    newLink.setTitle(songtitle);
                    newLink.setLink(href);
                    newLink.setSource("echords");
                    db.addLink(newLink);
                    numTabs++;
                }

//                Log.d("echordScrape artist",artist.toString());
//                Log.d("echordScrape link",link.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    /*
    * Function to strip characters from two strings
    *
    * remove all spaces
    * remove all non alphabet characters
    * remove all prepositions such as the, is, and, etc.
    *
    * */
    public String stripSpecialChars(String a){
        String stripped = null;
        stripped = a.replaceAll("[^a-zA-Z0-9]","");
        stripped = stripped.replaceAll("\\s+","");
        stripped = stripped.toLowerCase();
        stripped = stripped.replaceAll("the","");
        stripped = stripped.replaceAll("and","");
        return stripped;
    }

    /*
    * Function to compare two strings
    * */
    public Boolean compareLocalToRemote(String a, String b){
        String rawA = stripSpecialChars(a);
        String rawB = stripSpecialChars(b);
//        Log.d("compareLocalToRemote string 1",rawA);
//        Log.d("compareLocalToRemote string 2",rawB);
        if(rawA.contentEquals(rawB)){
            return true;
        }
        else{
            return false;
        }
    }
 }
