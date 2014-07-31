package thproject.test.com.myapplication;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.gracenote.gnsdk.*;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class SongRecognitionActivity extends Activity {
    Context context;
    Button recordButton;
    GnManager gnsdk;
    GnUser gnUser;
    GnMic gnMicrophone;
    GnMusicIdStream gnMusicIdStream;
    Boolean isListening = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_recognition);

        context = getApplicationContext();

        recordButton = (Button) findViewById(R.id.record);
        recordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                recordingTest();
                return false;
            }
        });

        //Executing asynchronous connection
        new gnSync().execute();


    }

    /*
    * Class to establish GnManager, etc.
    * */
    private class gnSync extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            // Initialize GNSDK manager, user, locale
            String gnsdkLicense = getString(R.string.gracenote_license_string);
            String clientId = getString(R.string.gracenote_client_id);
            String clientTag = getString(R.string.gracenote_client_tag);

            try {
                gnsdk = new GnManager(context, gnsdkLicense, GnLicenseInputMode.kLicenseInputModeString);
                gnUser = new GnUser( new GnUserStore(context), clientId, clientTag, "1" );
//                GnLocale locale = new GnLocale(GnLocaleGroup.kLocaleGroupMusic, GnLanguage.kLanguageEnglish, GnRegion.kRegionNorthAmerica, GnDescriptor.kDescriptorDefault, gnUser);
//                GnMusicId musicId = new GnMusicId(gnUser);
//                GnMusicIdFile musicIdFile = new GnMusicIdFile(gnUser);
                // Initialize mic



            } catch (GnException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void v){
            Log.d("songRecognitionActivity","connection finished");
            startRecording();
        }
    }

    public void recordingTest(){
        Log.d("recordingTest", "begin");
    }

    /*
    * Thread to perform actual streaming of audio
    * */
    public void startRecording(){
        Log.d("startRecording","begin");
        gnMicrophone = new GnMic(44100, 16, 1);   //mic initialization
        gnMicrophone.sourceInit();
        //initialize music stream
        try {
            gnMusicIdStream = new GnMusicIdStream(gnUser, new GnMusicIdStreamEvents());
            gnMusicIdStream.audioProcessStart(gnMicrophone.samplesPerSecond(), gnMicrophone.sampleSizeInBits(), gnMicrophone.numberOfChannels());
            Thread audioProcessThread = new Thread(new Runnable(){
                @Override
                public void run() {
                    try {
                        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024*4);
                        long bytesRead = 0;
                        while(isListening) {
                            bytesRead = gnMicrophone.getData(byteBuffer, byteBuffer.capacity());
                            gnMusicIdStream.audioProcess(byteBuffer.array(), bytesRead);
                        }
                    }
                    catch (GnException e) {
                        e.printStackTrace();
                    }
                }
            });
            audioProcessThread.start();
            gnMusicIdStream.identifyAlbumAsync();

        } catch (GnException e) {
            e.printStackTrace();
            Log.d("startRecording","GnException");
        }
        Log.d("startRecording","end");
    }


    /*
    * Class to identify streaming audio
    * */
    private class GnMusicIdStreamEvents implements IGnMusicIdStreamEvents {

        @Override
        public void musicIdStreamProcessingStatusEvent(GnMusicIdStreamProcessingStatus gnMusicIdStreamProcessingStatus, IGnCancellable iGnCancellable) {
            Log.d("GnMusicIdStreamEvents","musicIdStreamProcessingStatusEvent");

        }

        @Override
        public void musicIdStreamIdentifyingStatusEvent(GnMusicIdStreamIdentifyingStatus gnMusicIdStreamIdentifyingStatus, IGnCancellable iGnCancellable) {
            Log.d("musicIdStreamIdentifyingStatusEvent",gnMusicIdStreamIdentifyingStatus.toString());

        }

        @Override
        public void musicIdStreamAlbumResult(GnResponseAlbums gnResponseAlbums, IGnCancellable iGnCancellable) {
            Log.d("GnMusicIdStreamEvents",gnResponseAlbums.toString());

            isListening = false;

        }

        @Override
        public void musicIdStreamIdentifyCompletedWithError(GnError gnError) {
            Log.d("GnMusicIdStreamEvents",gnError.toString());
            isListening = false;
        }

        @Override
        public void statusEvent(GnStatus gnStatus, long l, long l2, long l3, IGnCancellable iGnCancellable) {

        }
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

    /**
     * GNSDK MusicID-File event delegate
     */
//    private class MusicIDFileEvents extends IGnMusicIdFileEvents {
//        HashMap<String, String> gnStatus_to_displayStatus;
//        public MusicIDFileEvents(){
//        gnStatus_to_displayStatus = new HashMap<String,String>(); gnStatus_to_displayStatus.put("kMusicIdFileCallbackStatusProcessingBegin", "Begin processing file");
//        gnStatus_to_displayStatus.put("kMusicIdFileCallbackStatusFileInfoQuery", "Querying file info");
//        gnStatus_to_displayStatus.put("kMusicIdFileCallbackStatusProcessingComplete", "Identificationcomplete"); }
//    }
}
