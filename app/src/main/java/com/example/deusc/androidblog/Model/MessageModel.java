package com.example.deusc.androidblog.Model;

import java.util.Date;

public class MessageModel extends com.example.deusc.androidblog.Model.MessageId {

    //names need to match names in firebase Database
    public String user_id;
    public String image_url;
    public String description;
    public String thumbnail;
    public Date timestamp;

    public MessageModel(){}

    public MessageModel(String user_id, String image_url, String description, String thumbnail, Date timestamp) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.description = description;
        this.thumbnail = thumbnail;
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
