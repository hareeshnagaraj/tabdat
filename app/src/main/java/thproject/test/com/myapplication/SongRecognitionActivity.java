package thproject.test.com.myapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gracenote.gnsdk.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class SongRecognitionActivity extends FragmentActivity {
    Context context;
    Button recordButton;
    GnManager gnsdk;
    GnUser gnUser;
    GnMic gnMicrophone;
    GnMusicIdStream gnMusicIdStream;
    Boolean isListening = true;
    String mFileName = null;

    MediaRecorder mRecorder = null;
    MediaPlayer mPlayer = null;

    static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_recognition);

        context = getApplicationContext();


        //handler for events within activity
        handler = new Handler(){
            public void handleMessage(Message msg) {
                Bundle data = msg.getData();
                String artist = data.getString("artist");
                String album = data.getString("album");
                String action = data.getString("action");

                //First action occurs when no tabs are present, progress dialog shown
                if(action.compareTo("test") == 0) {
                    Toast.makeText(getApplicationContext(),artist + " " + album,Toast.LENGTH_SHORT).show();
                }
            }
        };
        //Executing asynchronous connection
        new gnSync().execute();

        //disable application icon from ActionBar, set up remaining attributes
        ActionBar actionBar = getActionBar();
        actionBar.hide();
    }

    /*
    *
    * Class to signal handler for toasts within loop
    *
    * */

    public static void loopToast(String a, String b){
        Message msg = new Message();
        Bundle data = new Bundle();

        data.putString("action","test");
        data.putString("artist",a);
        data.putString("album",b);

        msg.setData(data);
        handler.sendMessage(msg);
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

            } catch (GnException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void v){
            Log.d("songRecognitionActivity","connection finished");
            Toast.makeText(getApplicationContext(), "Listening", Toast.LENGTH_SHORT).show();
            //Begin identification
            try {
                startStreaming();
            } catch (GnException e) {
                e.printStackTrace();
            }

        }
    }


    /*
    * Thread to perform actual streaming of audio
    * */
     public void startStreaming() throws  GnException{
        isListening = true;
        gnMicrophone = new GnMic(44100, 16, 1);   //mic initialization
        gnMicrophone.sourceInit();
        //initialize music stream
        gnMusicIdStream = new GnMusicIdStream(gnUser, new GnMusicIdStreamEvents());
        gnMusicIdStream.audioProcessStart(gnMicrophone.samplesPerSecond(), gnMicrophone.sampleSizeInBits(), gnMicrophone.numberOfChannels());
        Thread audioProcessThread = new Thread(new Runnable(){
            @Override
            public void run() {
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024*4);
                long bytesRead = 0;
                Log.d("startRecording","begin loop");
                while(isListening) {
                    bytesRead = gnMicrophone.getData(byteBuffer, byteBuffer.capacity());
                    try {
                        gnMusicIdStream.audioProcess(byteBuffer.array(), bytesRead);
                    } catch (GnException e) {
                        e.printStackTrace();
                    }
                }
                Log.d("startRecording","end loop");
            }
        });
        audioProcessThread.start();
        gnMusicIdStream.identifyAlbumAsync();
    }

    /*
    * Class to identify streaming audio, overriding the IGnMusicIDStreamEvents
    * */
    private class GnMusicIdStreamEvents implements IGnMusicIdStreamEvents {

        @Override
        public void musicIdStreamProcessingStatusEvent(GnMusicIdStreamProcessingStatus gnMusicIdStreamProcessingStatus, IGnCancellable iGnCancellable) {
            Log.d("GnMusicIdStreamEvents","musicIdStreamProcessingStatusEvent");

        }


        @Override
        public void musicIdStreamIdentifyingStatusEvent(GnMusicIdStreamIdentifyingStatus gnMusicIdStreamIdentifyingStatus, IGnCancellable iGnCancellable) {
            Log.d("musicIdStreamIdentifyingStatusEvent name",gnMusicIdStreamIdentifyingStatus.name());
            //Closing the microphone at the appropriate time
            if(gnMusicIdStreamIdentifyingStatus.name().compareTo("kStatusIdentifyingEnded") == 0){
                gnMicrophone.sourceClose();
            }
        }
        /*
        *
        * Parsing the results from the API call
        *
        * */
        @Override
        public void musicIdStreamAlbumResult(GnResponseAlbums gnResponseAlbums, IGnCancellable iGnCancellable) {
            GnAlbum result = null;
            Log.d("GnMusicIdStreamEvents",gnResponseAlbums.toString());
            Log.d("GnMusicIdStreamEvents stop listening","isListening = false");
            isListening = false;

            GnResponseAlbums local = gnResponseAlbums;
            GnAlbumIterable results = local.albums();
            GnAlbumIterator it = results.getIterator();
            Long albumCount = results.count();
            Log.d("GnAlbumIterable count",Long.toString(albumCount));

            while(it.hasNext()){
                try {
                    result = it.next();
                } catch (GnException e) {
                    e.printStackTrace();
                }
                GnArtist artist = result.artist();
                GnTitle title = result.title();
                GnName name = artist.name();

                loopToast(name.display(),title.display());
                Log.d("musicIdStreamAlbumResult ", name.display() + " " + title.display());
           }

            //Action if no album found
            if(albumCount == 0){

            }
        }

        @Override
        public void musicIdStreamIdentifyCompletedWithError(GnError gnError) {
            Log.d("GnMusicIdStreamEvents",gnError.toString());
            isListening = false;
        }

        @Override
        public void statusEvent(GnStatus gnStatus, long l, long l2, long l3, IGnCancellable iGnCancellable) {
            Log.d("GnStatus",Long.toString(l) + Long.toString(l2) + Long.toString(l3) );

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
