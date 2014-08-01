package thproject.test.com.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.gracenote.gnsdk.GnAlbum;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by hareeshnagaraj on 7/31/14.
 *
 * This class is used to show the albums after they are recognized
 * Once an album is recognized, the user is shown a list of tracks and given the option to select one
 *
 * After the user selects an album, this same DialogFragment is used to display the list of tracks to the user
 *
 */
public class TabPickerSongRecognition extends DialogFragment {
    public String[] albumstrings = {"a","b","c"};
    public String[] trackstrings = {"a","b","c"};
    public String titleString = "Choose album";
    public String artist;
    ArrayList mSelectedItems = new ArrayList();    //tracking selected items


    public interface songRecognizedListener{
        public void onTabClick(DialogFragment dialog);
    }

    songRecognizedListener listener;
    int displayMode = 0;        // 0 is for albums, 1 is for tracks
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            listener = (songRecognizedListener) activity;
        }
        catch(ClassCastException e){
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement TabPickerListener");
        }
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(titleString);

        if(displayMode == 0) {  //displaying albums
            builder.setItems(albumstrings, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Log.d("TabPickerDialog item selected", albumstrings[i]);
                    SongRecognitionActivity.startTracksDialog(i);           //signaling the
                }
            });
        }
        if(displayMode == 1){   //displaying tracks, allowing user to select multiple
            builder.setMultiChoiceItems(trackstrings,null,new DialogInterface.OnMultiChoiceClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                    if (isChecked) {
                        // If the user checked the item, add it to the selected items
                        mSelectedItems.add(which);
                    } else if (mSelectedItems.contains(which)) {
                        // Else, if the item is already in the array, remove it
                        mSelectedItems.remove(Integer.valueOf(which));
                    }
                }
            });
            builder.setPositiveButton("Select",new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    scrapeSelectedTracks();
                }
            });
            builder.setNegativeButton("Exit",new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    SongRecognitionActivity.exitSongRecognition();
                }
            });
        }

        return builder.create();
    }

    public void setDisplayMode(int a){ displayMode = a; };
    //used to set the current albums
    public void setAlbums(String[] albums){ albumstrings = albums; }
    //used to set the album objects, for access when songs selected
    public void setTitle(String a){ titleString = a; }
    //used to set the tracks
    public void setTracks(String[] tracks){trackstrings = tracks;}
    //set the current artist
    public void setArtist(String a){artist = a;}

    //Used to scrape all the tracks selected by the user and add them to our database`
    private void scrapeSelectedTracks(){
        int length = mSelectedItems.size();
        List<String> selectedTracks = new LinkedList<String>();
        for(int i=0; i < length; i++){
            int index = (Integer) mSelectedItems.get(i);
            String trackname = trackstrings[index];
            Log.d("scrapeSelected",trackname);
            selectedTracks.add(trackname);
        }

        TabScraper scraper = new TabScraper();
        scraper.setArtist(artist);
        scraper.setCallingActivity("SongRecognitionActivity");
        scraper.setSelectedTracks(selectedTracks);
        scraper.scrape();
    }

}
