package thproject.test.com.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.gracenote.gnsdk.GnAlbum;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by hareeshnagaraj on 7/31/14.
 */
public class TabPickerSongRecognition extends DialogFragment {
    List<GnAlbum> albums = new LinkedList<GnAlbum>();
    public String[] albumstrings = {"a","b","c"};
    public String[] trackstring = {"a","b","c"};
    public String titleString = "Choose album";

    public interface songRecognizedListener{
        public void onTabClick(DialogFragment dialog);
    }
    songRecognizedListener listener;
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
        builder.setItems(albumstrings, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d("TabPickerDialog item selected", albumstrings[i]);

            }
        });

        return builder.create();
    }

    //used to set the current albums
    public void setAlbums(String[] albums){ albumstrings = albums; }
}
