package thproject.test.com.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by hareeshnagaraj on 7/29/14.
 *
 * This is the dialog fragment used to actually show tabs in the SongsActivity
 *
 */
public class TabPickerDialog extends DialogFragment {
    public String title = "Select a tab";
    public String[] items = {"a","b","c"};
    private List<Link> links = new LinkedList<Link>();


    // The interface commmunicates back to the parent activity
    public interface TabPickerListener{
        public void onTabClick(DialogFragment dialog);
    }
    // Use this instance of the interface to deliver action events
    TabPickerListener mListener;

    @Override
    public void onAttach(Activity activity){
       super.onAttach(activity);
       try{
           mListener = (TabPickerListener) activity;
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
        builder.setTitle(title);
        builder.setItems(items, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d("TabPickerDialog item selected", Integer.toString(i));
//                Link selectedLink = links.get(i);
//                Log.d("TabPickerDialog link selected", selectedLink.toString());
            }
        });

        return builder.create();
    }

    public void setItems(String[] newitems){
        this.items = newitems;
    }
    public void setLinks(List<Link> links){
        this.links = links;
    }
}
