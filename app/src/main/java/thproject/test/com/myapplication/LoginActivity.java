package thproject.test.com.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.HttpResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static thproject.test.com.myapplication.MySQLiteHelper.getDB;

public class LoginActivity extends Activity {
    Button loginButton;
    EditText email;
    EditText password;
    String user_email,user_password,default_email,default_password;
    private static int SPLASH_TIME_OUT = 3000;
    public MySQLiteHelper db;
    public SongGrabber grabsongs;
    Context context;
    static Handler handler;
    public ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //getting our DB
        db = getDB(this);
        grabsongs = new SongGrabber();

        //creating a handler to start the next activity
        handler = new Handler(){
            public void handleMessage(Message msg) {
                Bundle data = msg.getData();
                String artist = data.getString("artist");
                String title = data.getString("title");

                final TabScraper scraper = new TabScraper();
                scraper.setArtist(artist);
                scraper.setSongTitle(title);
                scraper.scrape();
                Log.d("LoginActivityHandler","past scraper");
            }
        };

        /*
        * This button is used to grab the user's email and password information, and prompt them to correctly enter the information if they have not
        * */
        loginButton = (Button) findViewById(R.id.loginButton);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        default_email = "Email";
        default_password = "password";

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_email = email.getText().toString();
                user_password = password.getText().toString();

                /*//Catch cases for people trying to continue without username or password
                if( user_email.equalsIgnoreCase(default_email) ){
                    Toast.makeText(getApplicationContext(),"Please Enter an Email",Toast.LENGTH_SHORT).show();
                }
                else if( !user_email.contains("@") || !user_email.contains(".")){
                    Toast.makeText(getApplicationContext(),"Please Enter a Valid Email",Toast.LENGTH_SHORT).show();
                }
                else if( user_password.equalsIgnoreCase(default_password) ){
                    Toast.makeText(getApplicationContext(),"Please Enter a Valid Password",Toast.LENGTH_SHORT).show();
                }
                else{

                    new loginAsync().execute();

                }*/
                //bypassing the login checks temporarily
                new loginAsync().execute();


            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
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

    //used to update the local sqlite database with songs from user's phone
    private void updateSongsInDb(){
       context = getApplicationContext();
       grabsongs.getUserSongs(context);
    }

    //used to start the main login activity
    private void startMain(){
        Log.d("startMain","links as of mainActivity");
        db.getAllLinks();
        Intent i = new Intent(LoginActivity.this, MainTabActivity.class);
        startActivity(i);
        // close this activity
        finish();
    }

    /*
    * Class to signal scrape
    * */

    public static void scrapeSong(String artist, String title){
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("artist",artist);
        data.putString("title",title);
        msg.setData(data);
        handler.sendMessage(msg);
    }

/*
    Class to handle asynchronous login
*/
    private class loginAsync extends AsyncTask<Void, Void, Void>{

    /*
    *     Server - side login is implemented as well commented out for time being
    * */
        @Override
        protected Void doInBackground(Void... voids) {

//            HttpClient httpclient = new DefaultHttpClient();
//            HttpPost httppost = new HttpPost("http://162.243.66.98:3000/users");
//
//            try {
//                // Add your data
//                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
//                nameValuePairs.add(new BasicNameValuePair("email", user_email));
//                nameValuePairs.add(new BasicNameValuePair("password", user_password));
//                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//                // Execute HTTP Post Request
//                HttpResponse response = httpclient.execute(httppost);
//
//            } catch (ClientProtocolException e) {
//                // TODO Auto-generated catch block
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//            }
            Log.d("loginAsync : ", "beginning update query");
            updateSongsInDb();
            Log.d("loginAsync : ", "end update query");
            return null;
        }
        @Override
        protected void onPostExecute(Void v) {
            startMain();
        }


    }
}

