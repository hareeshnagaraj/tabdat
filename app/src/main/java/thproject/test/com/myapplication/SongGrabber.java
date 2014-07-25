package thproject.test.com.myapplication;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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



    public void getUserSongs(Context mContext, LinearLayout myFragmentView){
        ContentResolver musicResolver = mContext.getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

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
                Log.d("from songGrabber", thisTitle + " " + thisArtist);
                if(thisTitle == null || thisTitle == "<unknown>"){
                    thisTitle = "Untitled";
                }
                if(thisArtist == null || thisArtist == "<unknown>"){
                    thisArtist = "Unknown Artist";
                }
                String cardText = thisTitle + "\n" + thisArtist;
                TextView newCard = (TextView) inflater.inflate(R.layout.textviewcard, null);
                newCard.setText(cardText);
                if(myFragmentView != null){
                    myFragmentView.addView(newCard);
                }
                else{
                    Log.d("NULL IN SONGgrabber","null");
                }

                songMap.put(thisArtist,thisTitle);
                numsongs++;
            }
            while (musicCursor.moveToNext());
            Log.d("num songs from songGrabber", Integer.toString(numsongs));

        }
    }

    public HashMap<String,String> grabMap(){
        return songMap;
    }


}
