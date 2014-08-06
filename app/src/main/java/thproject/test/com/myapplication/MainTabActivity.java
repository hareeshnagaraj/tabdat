package thproject.test.com.myapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import thproject.test.com.myapplication.R;

public class MainTabActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,SearchDialog.SearchListener,InfoDialog.InformationDialogInterface {

    private static Handler handler;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    /*
    * Used to store the current frag
    * */
    private int currentActivity = 1;
    private Context context;
    public NowLayout nowLayout;
    MySQLiteHelper db;
    EditText artist;
    EditText title;

    public ProgressDialog progressDialog;       //dialog to show progress
    SearchDialog dialog;
    InfoDialog infoDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab);


        Bundle extras = this.getIntent().getExtras();
        if ( extras != null ) {
            if ( extras.containsKey("numUsers") ) {
                int numberOfUsers = extras.getInt("numUsers");
//                Log.d("numberOFUsers main",Integer.toString(numberOfUsers));
                if(numberOfUsers == 0){      //To be replaced with showcaseview, the introduction/explanation
                    showInfoDialog();
                }
            }
        }

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        //conditionally showing the info on the first login

        //creating our database and adding a single tab
        db = MySQLiteHelper.getDB(this);
        //disable application icon from ActionBar
        getActionBar().setDisplayShowHomeEnabled(false);

        //getting the user's songs
        context = getApplicationContext();
        nowLayout = (NowLayout) findViewById(R.id.mainTabLayout);

        //creating a handler to start the next activity
        handler = new Handler(){
            public void handleMessage(Message msg) {
//                Log.d("message", "hit");
                Bundle data = msg.getData();
                String action = data.getString("action");

                if(action.compareTo("songs") == 0) {        //showing the songs activity on swipe
                    Bundle extras = new Bundle();
                    extras.putString("artist", data.getString("artist"));
                    startSongActivity(extras);
                }
                if(action.compareTo("showprogress") == 0){  //show progress bar
                    dialog.dismiss();
                    String searchTitle = data.getString("title");
                    progressDialog = new ProgressDialog(MainTabActivity.this);
                    progressDialog.setMessage("Loading tab " + searchTitle);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.show();
                }
                if(action.compareTo("hideprogress") == 0){  //hide progress bar
                    progressDialog.hide();
                    progressDialog.dismiss();
                    restartActivity();
                }
                if(action.compareTo("toast") == 0){         //showing a toast for the dialog
                    String toastmessage = data.getString("toast");
                    Toast.makeText(getApplicationContext(),toastmessage,Toast.LENGTH_SHORT).show();
                }
            }
        };
        //adding listener for search function

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        PlaceholderFragment fragment = new PlaceholderFragment();
        fragment.setPosition(position);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section1);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

   /*
   *
   * Method to signal handler to start new activity
   *
   * */
    public static void signalHandlerDisplaySongs(String artist){
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("artist",artist);
        data.putString("action","songs");
        msg.setData(data);
        handler.sendMessage(msg);
    }
     /*
    * Show progress bar
    * */
    public static void showProgress(String title){
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("title",title);
        data.putString("action","showprogress");
        msg.setData(data);
        handler.sendMessage(msg);
    }
    /*
    * Hide progress bar
    * */
    public static void hideProgress(){
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("action","hideprogress");
        msg.setData(data);
        handler.sendMessage(msg);
    }
    public static void toasty(String a){
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("toast",a);
        data.putString("action","toast");
        msg.setData(data);
        handler.sendMessage(msg);
    }
    /*
    * Show results of search scrape
    * */
    public static void scrapeCompleted(int a){
        if(a == 0){                     //action if no tabs are found
            toasty("No tabs found");
        }
        else{
            toasty(Integer.toString(a) + " tabs added");
        }
        hideProgress();
    }

    /*
    * Method to start new activity
    * */
    private void startSongActivity(Bundle extras){
        Intent i;
        i = new Intent(MainTabActivity.this,SongsActivity.class);
        i.putExtras(extras);
        startActivity(i);
        // close this activity
    }
    /*
    * Restart this activity to show new cards
    * */
    private void restartActivity(){
        Intent i;
        i = new Intent(MainTabActivity.this,MainTabActivity.class);
        startActivity(i);
        finish();
    }

    /*
    * Start song recognition activity
    * */
    private void startSongRecognitionActivity(){
        if(checkInternetConnection()){
            Intent i;
            i = new Intent(MainTabActivity.this,SongRecognitionActivity.class);
            startActivity(i);
            finish();
        }
    }


    /*
    * Show search dialog
    * */
    private void searchDialog(){
//        Log.d("searchDialog","pressed");
        dialog = new SearchDialog();
        dialog.setCallingActivity("MainTabActivity");
        dialog.show(getFragmentManager(), "SearchDialog");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main_tab, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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
        if(id == R.id.song_recognition){       //Starting the song recognition activity from action bar
            startSongRecognitionActivity();
            return true;
        }
        if(id == R.id.search){                  //Showing the search dialog
            searchDialog();
            return true;
        }
        if(id == R.id.info_dialog){             //Showing the info dialog
            showInfoDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    * Used to show our dialog
    * */
    @Override
    public void onTabClick(DialogFragment dialog) {

    }

    /*
    * Used to show our info dialog
    * */
    @Override
    public void showInfoDialog() {
        infoDialog = new InfoDialog();
        infoDialog.show(getFragmentManager(),"infoDialog");
    }

    /*
    * Used to check internet connectivity
    * */
    public boolean checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // test for connection
        if (cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
//            Log.v("checkInternetConnection", "Internet Connection Not Present");
            toasty(getString(R.string.connect_to_network));
            return false;
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public class PlaceholderFragment extends Fragment {
        private SongGrabber grabsongs;
        private int position = 0;

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private String link = "https://github.com/hareeshnagaraj/tabdat";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            fragment.setPosition(sectionNumber);
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {

        }

        /*
        * Method to show the share app dialog
        * */
        public void showShareDialog(){
            Intent intent=new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Any tab, anywhere! Check out tab dat on Google Play :)");
            intent.putExtra(Intent.EXTRA_TEXT, "Link: " + this.link);
            startActivity(Intent.createChooser(intent, "Share"));
        }


        /*
        * Sets the position
        * */
        public void setPosition(int a){
            this.position = a;
        }
        /*
        * The fragment oncreate method starts our SongGrabber class which finds all the songs in the user's library and
        * displays them - the context from the fragment is passed to SongGrabber
        *
        * */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            NowLayout myFragmentView = null;
//            Log.d("section number",ARG_SECTION_NUMBER);
            View rootView = null;

            /*

            different layouts for each fragment
            1 = main tab layout
            2 = share tab layout
            3 = settings/other/might be removed

            */
            switch(position){
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_main_tab, container, false);
                    myFragmentView = (NowLayout) rootView.findViewById(R.id.mainTabLayout);
                    //update the UI with specific context here (myFragmentView)
                    //initially all artists will be displayed, user can then swipe to view songs
                    grabsongs = new SongGrabber();
                    grabsongs.displayArtists(context,myFragmentView);
                    break;
                case 2:
                    rootView = inflater.inflate(R.layout.fragment_main_tab, container, false);
                    myFragmentView = (NowLayout) rootView.findViewById(R.id.mainTabLayout);
                    grabsongs = new SongGrabber();
                    grabsongs.displayArtists(context,myFragmentView);
                    showShareDialog();
                    break;
                case 3:
                    rootView = inflater.inflate(R.layout.fragment_my, container, false);
                    break;
            }
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainTabActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
