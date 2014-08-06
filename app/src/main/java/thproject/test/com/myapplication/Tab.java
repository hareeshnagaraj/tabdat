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
        this.title = capitalizeEachWord(title);
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
    /*
    * Used to capitalize each word when setting the artist, to avoid multiple instances of same artist
    * */
    public String capitalizeEachWord(String a){
        String titleCaseValue;
        try{
            String[] words = a.split(" ");
            StringBuilder sb = new StringBuilder();
            if (words[0].length() > 0) {
                sb.append(Character.toUpperCase(words[0].charAt(0)) + words[0].subSequence(1, words[0].length()).toString().toLowerCase());
                for (int i = 1; i < words.length; i++) {
                    sb.append(" ");
                    sb.append(Character.toUpperCase(words[i].charAt(0)) + words[i].subSequence(1, words[i].length()).toString().toLowerCase());
                }
            }
             titleCaseValue = sb.toString();
        }
        catch(StringIndexOutOfBoundsException e){
             titleCaseValue = a;
        }
        return titleCaseValue;
    }
}
