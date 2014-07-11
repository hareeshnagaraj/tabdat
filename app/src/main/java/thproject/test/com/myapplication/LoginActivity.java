package thproject.test.com.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.List;

import thproject.test.com.myapplication.R;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /*
        * Testing the parse api
        */
        Parse.initialize(this, "5jgmHwYOhlsKfKXpr2xfVXVgsnUqawFegZ2o2KI5", "92uou7grH5WO6LSk57CtYuQXi0QgviFBI6DYxjeC");
//        ParseObject testObject = new ParseObject("UserObject");
//        testObject.put("foo","bar");
//        testObject.saveInBackground();




        /*
        * Button to log the user in to facebook
        * */
        Button button = (Button) findViewById(R.id.fbloginbutton);
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
//                Intent i = new Intent(LoginActivity.this, MainActivity.class);
//                startActivity(i);
//                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
//                // close this activity
//                finish();
                fbLogin();

            }
        });

    }

    /*
    * FB Login Method
    * */

    private void fbLogin(){
        List<String> permissions = Arrays.asList("basic_info", "user_about_me",
                "user_relationships", "user_birthday", "user_location");
        ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {

                if (user == null) {
                    Log.d("LOGIN","Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    Log.d("LOGIN","NEW USER");

                } else {
                    Log.d("LOGIN","SUCCESS");
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
}
