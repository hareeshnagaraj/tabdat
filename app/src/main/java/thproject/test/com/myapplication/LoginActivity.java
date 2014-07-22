package thproject.test.com.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

public class LoginActivity extends Activity {
    Button loginButton;
    EditText email;
    EditText password;
    String user_email,user_password,default_email,default_password;
    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //purely for testing, I will be bypassing the login system
        new Handler().postDelayed(new Runnable() {

//             * Showing splash screen with a timer. This will be useful when you
//             * want to show case your app logo / company

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(LoginActivity.this, MainTabActivity.class);
                startActivity(i);
                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
        //end of testing bypass


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
                    /*
                    * Here we authenticate with the database, inserting/grabbing the relevant info
                    * Using the AsyncTask method
                    *
                    *
                    * Still need to implement the following:
                    *
                    * SQLite or some local database
                    * check if user is logged in / has id or not
                    * */
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

    private class loginAsync extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
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

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }
            return null;
        }
        protected void onPostExecute(Void... voids) {
            Toast.makeText(getApplicationContext(),"Executed!",Toast.LENGTH_SHORT).show();
        }


    }
}

