package thproject.test.com.myapplication;

/**
 * Created by hareeshnagaraj on 7/27/14.
 * the class used to manage users within the app itself
 */
public class User {
    private int id;
    private String email;
    private String password;

    public void User(){};

    public void User(String email, String password){
        this.email = email;
        this.password = password;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public void setPassword(String password){
        this.password = password;
    }
    public void setId(int id){
        this.id = id;
    }

    public String getPassword(){
        return this.password;
    }

    public String getEmail(){
        return this.email;
    }
    public String getId(){
        return Integer.toString(this.id);
    }

    @Override
    public String toString(){
        return this.email + " " + this.password;
    }

}
