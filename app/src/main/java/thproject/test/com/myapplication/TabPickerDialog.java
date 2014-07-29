package thproject.test.com.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by hareeshnagaraj on 7/29/14.
 *
 * This is the dialog fragment used to actually show tabs in the SongsActivity
 *
 */
public class TabPickerDialog extends DialogFragment {
    private String title = "Select a tab";
    private CharSequence[] items;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setItems(items, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d("TabPickerDialog item selected", Integer.toString(i));
            }
        });

        return builder.create();
    }

    public void setItems(CharSequence[] newitems){
        this.items = newitems;
    }
}
