package thproject.test.com.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by hareeshnagaraj on 8/1/14.
 */
public class SearchDialog extends DialogFragment {
    private String artist;
    private String title;
    EditText artistEdit;
    EditText titleEdit;
    String callingActivity;


    // The interface commmunicates back to the parent activity
    public interface SearchListener{
        public void onTabClick(DialogFragment dialog);
    }
    // Use this instance of the interface to deliver action events
    SearchListener mListener;
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            mListener = (SearchListener) activity;
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

        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View promptView = layoutInflater.inflate(R.layout.search_dialog, null);
        builder.setView(promptView);
        searchSubmit(promptView);

        return builder.create();
    }
    //Set the calling activity to distinguish between the main activity and songs activity
    public void setCallingActivity(String a){
        this.callingActivity = a;
    }
    //Set artist
    public void setArtist(String a){ this.artist = a; }


    /*
    * Search dialog listener
    * */
    public void searchSubmit(View v){
        TextView submit = (TextView) v.findViewById(R.id.submitSearch);
        artistEdit = (EditText) v.findViewById(R.id.search_artist);
        titleEdit = (EditText) v.findViewById(R.id.search_title);

        if(callingActivity.contentEquals("SongsActivity")){         //setting the artist field if searching within songsActivity
            artistEdit.setText(artist);
            artistEdit.setClickable(false);
            artistEdit.setFocusable(false);
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title = titleEdit.getText().toString();
                artist = artistEdit.getText().toString();
                int titleLength = title.length();
                int artistLength = artist.length();
//                Log.d("searchSubmit"," title " + title + " length" + Integer.toString(titleLength) + " artist " + artist + " length" + Integer.toString(artistLength) );

                if(callingActivity.contentEquals("MainTabActivity")){       //Action if searching in main activity
                    if(titleLength == 0){
                        MainTabActivity.toasty("Please enter a title");
                    }
                    else if(artistLength == 0){
                        MainTabActivity.toasty("Please enter an artist");
                    }
                    else{
                        MainTabActivity.showProgress(title);
                        TabScraper scraper = new TabScraper();
                        scraper.setArtist(artist);
                        scraper.setSongTitle(title);
                        scraper.setCallingActivity("MainTabActivity");
                        scraper.scrape();
                    }
                }
                if(callingActivity.contentEquals("SongsActivity")){     //Action if called in SongsActivity
                    if(titleLength == 0){
                        SongsActivity.toasty("Please enter a title");
                    }
                    else{
                        SongsActivity.showProgress(artist,title,"init");
                    }
                }
            }
        });
    }

}
