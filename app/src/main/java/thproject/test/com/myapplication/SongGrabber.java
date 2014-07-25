package thproject.test.com.myapplication;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
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

    public void getUserSongs(Context mContext, LinearLayout nowLayout){
        ContentResolver musicResolver = mContext.getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

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
                if(thisTitle == null){
                    thisTitle = "Untitled";
                }
                if(thisArtist == null){
                    thisArtist = "Unknown Artist";
                }
//                TextView newCard = new TextView(mContext);
//                newCard.setTextAppearance(mContext,R.style.nowCardStyle);
//                if(nowLayout != null){
//                    nowLayout.addView(newCard);
//                }
                songMap.put(thisArtist,thisTitle);
                numsongs++;
            }
            while (musicCursor.moveToNext());
            Log.d("num songs from songGrabber", Integer.toString(numsongs));

        }
    }

    public void setCurrentActivity(int a){
        currentActivity = a;
    }
    public HashMap<String,String> grabMap(){
        return songMap;
    }
    public void addTabChildren(LinearLayout nowLayout){
        int numsongs2 = 0;
        Iterator myVeryOwnIterator = songMap.keySet().iterator();
        while(myVeryOwnIterator.hasNext()) {
            String key=(String)myVeryOwnIterator.next();
            String value=(String)songMap.get(key);
            Log.d("from addTabChildren",key + " " + value);
            numsongs2++;
        }
        Log.d("num songs from songGrabber2", Integer.toString(numsongs2));

    }

}
