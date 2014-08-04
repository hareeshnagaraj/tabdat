package thproject.test.com.myapplication;

import android.app.ActionBar;
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
import android.view.MotionEvent;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static thproject.test.com.myapplication.MySQLiteHelper.getDB;

public class LoginActivity extends Activity {
    Button loginButton;
    EditText email;
    EditText password;
    String user_email,user_password,default_email,default_password,existingEmail,existingPass;
    private static int SPLASH_TIME_OUT = 3000;
    public MySQLiteHelper db;
    public SongGrabber grabsongs;
    Context context;
    static Handler handler;
    public ProgressDialog progressDialog;
    public List<User> users;
    int numberOfUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //disable ActionBar, set up remaining attributes
        ActionBar actionBar = getActionBar();
        actionBar.hide();

        //getting our DB
        db = getDB(this);
        grabsongs = new SongGrabber();


        loginButton = (Button) findViewById(R.id.loginButton);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        default_email = "Email";
        default_password = "password";

        email.setHintTextColor(getResources().getColor(R.color.straight_white));
        password.setHintTextColor(getResources().getColor(R.color.straight_white));
        password.setText(default_password);

        users = db.getAllUsers();
        numberOfUsers = users.size();
        Log.d("LoginActivity","num users : " + Integer.toString(numberOfUsers));
        if(numberOfUsers > 0){
            existingEmail = users.get(0).getEmail();
            existingPass = users.get(0).getPassword();
            Log.d("LoginActivity","user " + existingEmail + " pass " + existingPass);
            email.setText(existingEmail);
            password.setText(existingPass);
        }

        //Listening to replace the password field on click
        password.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String val = password.getText().toString();
                if (val.contentEquals(default_password)) {
                    password.setText("");
                }
            }
        });

        //creating a handler to start the next activity
        handler = new Handler(){
            public void handleMessage(Message msg) {
                Bundle data = msg.getData();
                String artist = data.getString("artist");
                String title = data.getString("title");
                String text = data.getString("text");
                progressDialog.setMessage(text);
                Log.d("LoginActivityHandler","past scraper");
            }
        };


        /*
        * This button is used to grab the user's email and password information, and prompt them to correctly enter the information if they have not
        * */

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_email = email.getText().toString();
                user_password = password.getText().toString();

                //Catch cases for people trying to continue without username or password
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
                    //bypassing the login checks temporarily
                    progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setMessage("Logging In");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setCancelable(false);
                    if(numberOfUsers == 0){         //we only need to show if the user's songs are being catalogued
                        progressDialog.show();
                    }
                    new loginAsync().execute();
                }
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
        if(progressDialog != null) {
            progressDialog.hide();
            progressDialog.dismiss();
        }
        Intent i = new Intent(LoginActivity.this, MainTabActivity.class);
        startActivity(i);
        // close this activity
        finish();
    }

    /*
    * Static method to signal handler for toasts within loop
    */
    public static void loginDialogText(String a){
        Message msg = new Message();
        Bundle data = new Bundle();

        data.putString("action","test");
        data.putString("text",a);

        msg.setData(data);
        handler.sendMessage(msg);
    }

    /*
    * Add user, then list all the available users
    * */
    public void addUserToDB(String email, String pass){
        db.addUser(email,pass);
        db.getAllUsers();
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
              String sEmail = email.getText().toString();
              String sPass = password.getText().toString();
              if(numberOfUsers == 0) {
                updateSongsInDb();
                addUserToDB(sEmail,sPass);                    //Adding the user to our local database
              }

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://162.243.66.98:3000/users");

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("email", user_email));
                nameValuePairs.add(new BasicNameValuePair("password", user_password));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                InputStream stream = response.getEntity().getContent();
                Log.d("loginAsync response" , getStringFromInputStream(stream));
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }
            Log.d("loginAsync : ", "beginning update query");
            Log.d("loginAsync : ", "end update query");
            return null;
        }
        @Override
        protected void onPostExecute(Void v) {
            startMain();
        }

    }

    /*
    * used to parse API responses
    * */

    // convert InputStream to String
    private static String getStringFromInputStream(InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

}

