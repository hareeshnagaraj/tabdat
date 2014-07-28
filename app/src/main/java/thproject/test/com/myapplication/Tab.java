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

    public String getTitle(){
        return this.title;
    }

    public String getId(){
        return Integer.toString(this.id);
    }

    public String getArtist(){
        return this.artist;
    }

    public String getLinks(){
        return this.links;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setArtist(String artist){
        this.artist = artist;
    }

    public void setLinks(String links){
        this.links = links;
    }

    public void setId(int id){
        this.id = id;
    }

    @Override
    public String toString(){
        return "Tab [id=" + id + ", title=" + title + ", artist=" + artist + " links="+links
                + "]";
    }
}
