package thproject.test.com.myapplication;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.gracenote.gnsdk.*;

public class SongRecognitionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_recognition);
        String gnsdkLicense = getString(R.string.gracenote_license_string);
        String clientId = getString(R.string.gracenote_client_id);
        String clientTag = getString(R.string.gracenote_client_tag);
        Context context = this.getApplicationContext();

        // Initialize GNSDK manager, user, locale
        try {
            GnManager gnsdk = new GnManager(context, gnsdkLicense, GnLicenseInputMode.kLicenseInputModeString);
            GnUser gnUser = new GnUser( new GnUserStore(context), clientId, clientTag, "1" );
            GnLocale locale = new GnLocale(GnLocaleGroup.kLocaleGroupMusic, GnLanguage.kLanguageEnglish, GnRegion.kRegionNorthAmerica, GnDescriptor.kDescriptorDefault, gnUser);

        } catch (GnException e) {
            e.printStackTrace();
        }

        Button recordButton = (Button) findViewById(R.id.record);
        recordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                recordingTest();
                return false;
            }
        });


    }

    public void recordingTest(){
        Log.d("recordingTest", "begin");
    }

    public void startRecording(){

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.song_recognition, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
