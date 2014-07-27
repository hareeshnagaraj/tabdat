package thproject.test.com.myapplication;

/**
 * Created by hareeshnagaraj on 7/27/14.
 */
public class Tab {
    private int id;
    private String title;
    private String artist;
    private String links;

    public Tab(){}

    public Tab(String title, String artist, String links){
        this.title = title;
        this.artist = artist;
        this.links = links;
    }

    @Override
    public String toString(){
        return "Tab [id=" + id + ", title=" + title + ", artist=" + artist + " links="+links
                + "]";
    }
}
