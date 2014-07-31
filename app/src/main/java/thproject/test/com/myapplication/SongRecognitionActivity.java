package thproject.test.com.myapplication;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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

public class SongRecognitionActivity extends Activity {
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

    Handler mHandler;
    AudioRecorder recorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_recognition);

        context = getApplicationContext();

        recordButton = (Button) findViewById(R.id.record);
        recordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                try {
                    startStreaming();
                } catch (GnException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });


        mHandler = new Handler();

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
            Toast.makeText(getApplicationContext(), "Ready to Listen", Toast.LENGTH_SHORT).show();

        }
    }

    /*
    *
    * Non-Streaming audio recognition of audio
    *
    * */
    public void startRecording() throws GnException{
        recorder = new AudioRecorder();
        recorder.startRecording();
        Runnable myTask = new Runnable() {
            @Override
            public void run() {
                //do work
                recorder.stopAndPlay();
                mHandler.postDelayed(this, 1000);
            }
        };

    }

    private class AudioRecorder{
        public void stopAndPlay()  {
            stopRecording();
            play();
        }

        public void startRecording(){
            mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFileName += "/tabdat.3gp";

            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile(mFileName);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            Log.d("startRecording directory", mFileName);
            try {
                mRecorder.prepare();
                mRecorder.start();
            } catch (IOException e) {
                Log.e("audiorecorder fail", "prepare() failed");
            } catch(IllegalStateException e){
                Log.e("IllegalStateException fail", e.toString());
            }
        }

        public void stopRecording(){
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }

        public void play(){
            Log.d("startRecording directory", "playing song");

            mPlayer = new MediaPlayer();
            try {
                mPlayer.setDataSource(mFileName);
                mPlayer.prepare();
                mPlayer.start();
            } catch (IOException e) {
                Log.e("IO", "prepare() failed");
            }
        }
        public void stop(){
            mPlayer.release();
            mPlayer = null;
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
                    Long samplerate = gnMicrophone.samplesPerSecond();
                    Long samplesize = gnMicrophone.sampleSizeInBits();
                    Log.d("startRecording samplerate samplesize", Long.toString(samplerate) + "  " + Long.toString(samplesize) );
                    Log.d("startRecording bytesRead", Long.toString(bytesRead) + isListening.toString());
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
//            String albumResponse = gnResponseAlbums.albums();
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

                Log.d("musicIdStreamAlbumResult ",  name.display()  + " " + title.display());
                Toast.makeText(getApplicationContext(),  name.display()  + " " + title.display(), Toast.LENGTH_SHORT).show();
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
