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
import android.widget.Toast;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import static thproject.test.com.myapplication.MySQLiteHelper.getDB;


/**
 *
 *
 * Created by hareeshnagaraj on 7/21/14.
 * Intended to grab the user's songs, add them to the local database if not already present
 *
 *
 */
public class SongGrabber extends Activity {

    private int currentActivity = 1;
    private HashMap songMap = new HashMap<String,String>();
    private String cardText;
    MySQLiteHelper db = getDB(this);


    public void getUserSongs(final Context mContext){
        ContentResolver musicResolver = mContext.getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        Tab newtab = null;

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
                String thisArtist = musicCursor.getString(artistColumn);
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
                }

//                cardText = thisTitle + "\n" + thisArtist;
//                TextView newCard = (TextView) inflater.inflate(R.layout.textviewcard, null);
//                newCard.setText(cardText);
//                newCard.setId(numsongs);
//
//                newCard.setOnTouchListener(new OnSwipeTouchListener(mContext,newCard) {
//                    @Override
//                    public void onSwipeLeft(View view) {
//                        Toast.makeText(mContext,Integer.toString(view.getId()),Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//                if(myFragmentView != null){
//                    myFragmentView.addView(newCard);
//                }
//                else{
//                    Log.d("NULL IN SONGgrabber","null");
//                }

                songMap.put(thisArtist,thisTitle);
                numsongs++;
            }
            while (musicCursor.moveToNext());
            Log.d("num songs from songGrabber", Integer.toString(numsongs));

        }
    }

    /*
    *
    * Used to display all of the artist cards
    *
    * */
    public void displayArtists(final Context mContext,LinearLayout myFragmentView){
        List<String> artistlist = db.getAllArtists();
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        for (int i=0; i<artistlist.size(); i++) {
            Log.d("displayArtist :",artistlist.get(i));
            String currentArtist = artistlist.get(i);
            TextView newCard = (TextView) inflater.inflate(R.layout.textviewcard, null);
            newCard.setText(cardText);
            newCard.setId(i);

            cardText = currentArtist;
            if(myFragmentView != null && cardText != ""){
                myFragmentView.addView(newCard);
            }
            else{
                Log.d("NULL IN SONGgrabber","null");
            }

        }
    }

    public HashMap<String,String> grabMap(){
        return songMap;
    }


}
