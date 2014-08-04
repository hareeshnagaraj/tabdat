package thproject.test.com.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by hareeshnagaraj on 8/4/14.
 *
 * Used to display information to the user such as basic instructions, etc.
 */
public class InfoDialog extends DialogFragment {
    public String message;

    public interface InformationDialogInterface{
        public void showInfoDialog();
    }

    InformationDialogInterface mListener;
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            mListener = (InformationDialogInterface) activity;
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
        builder.setTitle("Information");
        String message = getString(R.string.info_dialog);
        builder.setMessage(message);
        return builder.create();
    }

}
