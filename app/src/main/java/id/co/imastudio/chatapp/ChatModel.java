package id.co.imastudio.chatapp;

/**
 * Created by idn on 4/14/2017.
 */

public class ChatModel {
    private String id;
    private String text;
    private String name;
    private String photoUrl;
    private Long timestamp;

    public ChatModel() {
        //Needed for Firebase
    }

    public ChatModel(String text, String name, String photoUrl, Long timestamp ) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
