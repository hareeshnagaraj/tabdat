package thproject.test.com.myapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.HashMap;

import static thproject.test.com.myapplication.MySQLiteHelper.getDB;

public class MainTabActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

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
    public thproject.test.com.myapplication.NowLayout nowLayout;
    MySQLiteHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        //creating our database and adding a single tab
        db = getDB(this);
        //disable application icon from ActionBar
        getActionBar().setDisplayShowHomeEnabled(false);

        //getting the user's songs
        context = getApplicationContext();
        nowLayout = (thproject.test.com.myapplication.NowLayout) findViewById(R.id.mainTabLayout);

        //creating a handler to start the next activity
        handler = new Handler(){
            public void handleMessage(Message msg) {
                Log.d("message", "hit");
                Bundle data = msg.getData();
                Bundle extras = new Bundle();
                extras.putString("artist", data.getString("artist"));
                startSongActivity(extras);
            }
        };

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
                mTitle = getString(R.string.title_section2);
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
        msg.setData(data);
        handler.sendMessage(msg);
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
    * Start song recognition activity
    * */
    private void startSongRecognitionActivity(){
        Intent i;
        i = new Intent(MainTabActivity.this,SongRecognitionActivity.class);
        startActivity(i);
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
        return super.onOptionsItemSelected(item);
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

            thproject.test.com.myapplication.NowLayout myFragmentView = null;
            Log.d("section number",ARG_SECTION_NUMBER);
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
                    rootView = inflater.inflate(R.layout.fragment_my, container, false);
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
