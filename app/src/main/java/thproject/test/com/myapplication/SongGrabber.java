package thproject.test.com.myapplication;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import thproject.test.com.myapplication.R;


/**
 *
 *
 * Created by hareeshnagaraj on 7/21/14.
 * Intended to grab the user's songs, add them to the local database if not already present
 *
 *
 */
public class SongGrabber extends Activity {

    private String cardText;
    private String artist = "";
    MySQLiteHelper db = MySQLiteHelper.getDB(this);


    public void getUserSongs(final Context mContext){
        ContentResolver musicResolver = mContext.getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        Tab newtab = null;

        //Changing the login dialog text
        LoginActivity.loginDialogText("Updating Database");

        int numsongs = 0;
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = capitalizeEachWord(musicCursor.getString(artistColumn));
                newtab = new Tab();

                if(thisTitle == null || thisTitle == "<unknown>"){
                    thisTitle = "Untitled";
                }
                if(thisArtist == null || thisArtist.contains("<unknown>")){
                    thisArtist = "Unknown Artist";
                }


                //Querying database to see if tab record exists, if not, add it
                if(!db.tabExists(thisTitle,thisArtist)){
                    newtab.setTitle(thisTitle);
                    newtab.setArtist(thisArtist);
                    db.addTab(newtab);
                    //Scraping the web for this song via handler, and adding that to our DB
                }

                numsongs++;
            }
            while (musicCursor.moveToNext());
//            Log.d("num songs from songGrabber", Integer.toString(numsongs));

        }
    }

    /*
    *
    * Used to display all of the artist cards
    *
    * */
    public void displayArtists(final Context mContext,LinearLayout myFragmentView){
        final List<String> artistlist = db.getAllArtists();
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        for (int i=0; i<artistlist.size(); i++) {
//            Log.d("displayArtist :",artistlist.get(i));
            String displaytext;
            String currentArtist = artistlist.get(i);
            int numberOfSongs = db.getNumberOfTabsBy(currentArtist);
            if(numberOfSongs > 1){
                displaytext = currentArtist + "\n" + Integer.toString(numberOfSongs) + " Songs";
            }
            else{
                displaytext = currentArtist + "\n" + Integer.toString(numberOfSongs) +" Song";
            }
            cardText = displaytext;

            TextView newCard = (TextView) inflater.inflate(R.layout.textviewcard, null);
            newCard.setText(cardText);
            newCard.setId(i);
            addArtistCard(mContext, newCard, myFragmentView, artistlist);
        }
    }
    /*
    *
    * Method to display all of an artist's songs
    * Scraping is done on the fly
    *
    * */
    public void displaySongs(final Context mContext, LinearLayout myFragmentView,String artist){
        final List<String> songs = db.getSongsBy(artist);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        this.artist = artist;
        for(int j=0; j<songs.size(); j++){
            String songname = songs.get(j);
            TextView newCard = (TextView) inflater.inflate(R.layout.songcard, null);
            newCard.setText(songname);
            newCard.setId(j);
            addSongCard(mContext,newCard,myFragmentView,songs);
        }
    }

    /*
    * Method to show loading progress in SongsActivity, and scrape tabs as necessary
    * */
    public void showProgressInSongsActivity(String artist, String songname){

        HashMap<String,Link> songHash;

        songHash = db.getLink(artist, songname);
        Link dummyLink = songHash.get("numlinks");
        int numTabs = dummyLink.getID();

        if(numTabs == 0){   // here we need to scrape for tabs
            SongsActivity.showProgress(artist,songname,"init");
//            Log.d("showProgressInSongsActivity",songname + " has " + Integer.toString(numTabs) + " tabs"); //logs number of songs for this tab
        }
        else{       //tabs already exist in database
//            Log.d("showProgressInSongsActivity",songname + " has " + Integer.toString(numTabs) + " tabs"); //logs number of songs for this tab
            SongsActivity.signalCompletion("exists",artist,songname);
        }

    }


    /*
    * Method to add card to list of artists displayed in our main activity fragment
    * Left swipe on each card takes us to an activity that contains the list of songs for that particular artist
    * Long click to do the same action
    * */
    public void addArtistCard(final Context acontext, View view, LinearLayout layout,final List<String> artistlist){
        Log.d("artistCard", "added");

        view.setOnTouchListener(new OnSwipeTouchListener(acontext,view) {
            public void onSwipeLeft(View view) {
                int cardnum = view.getId();
                String cardname = artistlist.get(cardnum);
                MainTabActivity.signalHandlerDisplaySongs(cardname);
            }
            public void longPress(View view){
                int cardnum = view.getId();
                String cardname = artistlist.get(cardnum);
                MainTabActivity.signalHandlerDisplaySongs(cardname);
            }
            @Override
            public void onTouch(View view) { }

        });
        layout.addView(view);
    }

    /*
    *
    * Method to add card that displays the list of a single artist's songs
    *
    * */
    public void addSongCard(final Context acontext, View view, LinearLayout layout,final List<String> songlist){

        final String searchArtist = this.artist;
        final TabScraper scraper = new TabScraper();
        scraper.setArtist(searchArtist);

        view.setOnTouchListener(new OnSwipeTouchListener(acontext,view) {

            public void onSwipeLeft(View view) {
                int songid = view.getId();
                String songname = songlist.get(songid);
                showProgressInSongsActivity(searchArtist,songname);
            }
            public void longPress(View view){
                Log.d("songCard", "longPress");
                int songid = view.getId();
                String songname = songlist.get(songid);
                showProgressInSongsActivity(searchArtist,songname);
            }
            @Override
            public void onTouch(View view) { }
        });
        layout.addView(view);
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



}
