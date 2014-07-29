package thproject.test.com.myapplication;

/**
 * Created by hareeshnagaraj on 7/29/14.
 *
 * This class is used to manipulate and store links in our database
 *
 */
public class Link {
    private int id;
    private String artist;
    private String title;
    private String link;
    private String source;

    public Link(){};

    public Link(String artist, String title, String link, String source){
        this.artist = artist;
        this.title = title;
        this.link =link;
        this.source = source;
    }

    public void setArtist(String a){
        this.artist = a;
    }

    public void setID(int a){
        this.id = a;
    }

    public void setTitle(String a){
        this.title = a;
    }

    public void setLink(String a){
        this.link = a;
    }

    public void setSource(String a){
        this.source = a;
    }

    public String getArtist(){
        return this.artist;
    }

    public String getTitle(){
        return this.title;
    }

    public String getLink(){
        return this.link;
    }

    public String getSource(){
        return this.source;
    }

    public Integer getID(){
        return this.id;
    }

    @Override
    public String toString(){
        return "Link [id=" + id + ", title=" + title + ", artist=" + artist + " link="+link + " source="+source + "]";
    }

}
