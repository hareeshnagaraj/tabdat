package thproject.test.com.myapplication;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.gracenote.gnsdk.*;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import thproject.test.com.myapplication.R;

public class SongRecognitionActivity extends FragmentActivity implements TabPickerSongRecognition.songRecognizedListener{
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

    static Handler handler;         //handler

    String[] globalAlbums = null;   //global list of albums
    List<GnAlbum> albumObjects = new LinkedList<GnAlbum>();

    //Once selected, we instantiate a global object for the artist
    String artist;
    private static List<String> selectedTracks = new LinkedList<String>();
    public ProgressDialog progressDialog;       //dialog to show progress


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
                //Action to show our list of albums
                if(action.compareTo("albums") == 0){
                    showAlbumDialog();
                }
                //Action to show our list of tracks
                if(action.compareTo("tracks") == 0){
                    int index = data.getInt("albumIndex");
                    GnAlbum selectedAlbum = albumObjects.get(index);
                    showTracksDialog(selectedAlbum);
                }
                //Actino to begin scraping
                if(action.compareTo("scrape") == 0){
                    beginScraping();
                }
                //show progress
                if(action.compareTo("showprogress") == 0){
                    progressDialog = new ProgressDialog(SongRecognitionActivity.this);
                    progressDialog.setMessage("Loading tab ");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.show();
                }

                //hide progress
                if(action.compareTo("stopprogress") == 0){
                    progressDialog.hide();
                }

                //update progress
                if(action.compareTo("progresstext") == 0){
                    String text = data.getString("text");
                    progressDialog.setMessage("Loading tab " + text);
                }

                //Action to exit this activity
                if(action.compareTo("exit") == 0){
                    Intent i;
                    i = new Intent(SongRecognitionActivity.this,MainTabActivity.class);
                    startActivity(i);
                    finish();
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
    * Static method to signal handler for toasts within loop
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
    * Static method to show progress bar, hide progress bar, update text
    * */
    public static void showProgress(){
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("action","showprogress");
        msg.setData(data);
        handler.sendMessage(msg);
    }
    public static void stopProgress(){
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("action","stopprogress");
        msg.setData(data);
        handler.sendMessage(msg);

    }
    public static void progressText(String a ){
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("action","progresstext");
        data.putString("text",a);
        msg.setData(data);
        handler.sendMessage(msg);
    }

    /*
    *
    * Static method to signal handler to show dialog with albums
    *
    * */
    public static void startAlbumDialog(){
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("action","albums");
        msg.setData(data);
        handler.sendMessage(msg);
    }
    /*
    *Static method signaling handler to show tracks
    * */
    public static void startTracksDialog(int a){
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("action","tracks");
        data.putInt("albumIndex",a);
        msg.setData(data);
        handler.sendMessage(msg);
    }
    /*
    * Static method to end activity
    * */
    public static void exitSongRecognition(){
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("action", "exit");
        msg.setData(data);
        handler.sendMessage(msg);
    }
    /*
    * Static method to signal handler to begin scraping
    * */
    public static void signalTrackScraping(List<String> a){
        selectedTracks = a;
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("action", "scrape");
        msg.setData(data);
        handler.sendMessage(msg);
    }

    /*
    * Method to begin scraping appropriately
    * */
    public void beginScraping(){

    }


    /*
    * method to show album dialog
    *
    * sets the list of albums, as well as passes the album objects
    *
    * */
    public void showAlbumDialog(){
        TabPickerSongRecognition dialog = new TabPickerSongRecognition();
        dialog.setDisplayMode(0);           //specifying the display mode as albums
        dialog.setAlbums(globalAlbums);
        dialog.show(getFragmentManager(),"TabPickerSongRecognition");
    }

    /*
    * Method to show tracks dialog
    * Sets global artist
    * Gets the album object, accordingly parses the tracks and displays them
    * */
    public void showTracksDialog(GnAlbum album){
        GnArtist selectedArtist = album.artist();
        artist = selectedArtist.name().display();

        GnTrackIterable trackIterable = album.tracks();
        GnTrackIterator trackIterator = trackIterable.getIterator();
        GnTrack track = null;
        List<String> trackListObject = new LinkedList<String>();
        while(trackIterator.hasNext()){
            try {
                track = trackIterator.next();
                GnTitle title = track.title();
//                Log.d("showTracksDialog track : " , title.display());
                trackListObject.add(title.display());
            } catch (GnException e) {
                e.printStackTrace();
            }
        }
        String[] finalTrackList = listToArray(trackListObject);
        TabPickerSongRecognition dialog = new TabPickerSongRecognition();
        dialog.setDisplayMode(1);   //setting our dialog to display tracks
        dialog.setTracks(finalTrackList);
        dialog.setTitle("Select Tracks to Tab");
        dialog.setArtist(artist);
        dialog.show(getFragmentManager(),"TabPickerSongRecognition");
    }

    /*
    * Method to convert List objects to arrays for convenience
    * */
    public String[] listToArray(List<String> list){
        String[] returnList = new String[list.size()];
        for(int i = 0; i < list.size(); i++){
            returnList[i] = list.get(i);
        }
        return returnList;
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
//            Log.d("songRecognitionActivity","connection finished");
            Toast.makeText(getApplicationContext(), "Listening", Toast.LENGTH_LONG).show();
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
     public void startStreaming() throws GnException{
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
//                Log.d("startRecording","begin loop");
                while(isListening) {
                    bytesRead = gnMicrophone.getData(byteBuffer, byteBuffer.capacity());
                    try {
                        gnMusicIdStream.audioProcess(byteBuffer.array(), bytesRead);
                    } catch (GnException e) {
                        e.printStackTrace();
                    }
                }

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
//            Log.d("GnMusicIdStreamEvents","musicIdStreamProcessingStatusEvent");
        }


        @Override
        public void musicIdStreamIdentifyingStatusEvent(GnMusicIdStreamIdentifyingStatus gnMusicIdStreamIdentifyingStatus, IGnCancellable iGnCancellable) {
//            Log.d("musicIdStreamIdentifyingStatusEvent name",gnMusicIdStreamIdentifyingStatus.name());
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
//            Log.d("GnMusicIdStreamEvents",gnResponseAlbums.toString());
//            Log.d("GnMusicIdStreamEvents stop listening","isListening = false");
            isListening = false;

            List<String> displayList = new LinkedList<String>();

            GnResponseAlbums local = gnResponseAlbums;
            GnAlbumIterable results = local.albums();
            GnAlbumIterator it = results.getIterator();
            Long albumCount = results.count();
//            Log.d("GnAlbumIterable count",Long.toString(albumCount));

            //Action if no album found
            if(albumCount == 0){
                loopToast("No albums found :(","");
                exitSongRecognition();   //returning to the previous activity
            }
            else{
            //if album/s are found we display the appropriate dialog to the user
                while(it.hasNext()){
                    try {
                        result = it.next();
                        albumObjects.add(result);
                    } catch (GnException e) {
                        e.printStackTrace();
                    }
                    GnArtist artist = result.artist();
                    GnTitle title = result.title();
                    GnName name = artist.name();
                    displayList.add(title.display() + " by " + name.display());
//                    Log.d("musicIdStreamAlbumResult ", name.display() + " " + title.display());
               }
               String[] returnList = listToArray(displayList);
               globalAlbums = returnList;       //setting our globalalbums list to this returnList
               //Signaling our handler
               startAlbumDialog();
           }

        }


        @Override
        public void musicIdStreamIdentifyCompletedWithError(GnError gnError) {
//            Log.d("GnMusicIdStreamEvents",gnError.toString());
            isListening = false;
        }

        @Override
        public void statusEvent(GnStatus gnStatus, long l, long l2, long l3, IGnCancellable iGnCancellable) {
//            Log.d("GnStatus",Long.toString(l) + Long.toString(l2) + Long.toString(l3) );
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


    /*
    * Handling the events for the dialog fragment
    * */

    @Override
    public void onTabClick(DialogFragment dialog) {

    }
 }
