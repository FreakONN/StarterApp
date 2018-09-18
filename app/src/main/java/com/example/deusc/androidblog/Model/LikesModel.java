package com.example.deusc.androidblog.Model;

import java.util.Date;

public class LikesModel extends MessageId {

    String currentUser;
    Date timestamp;

    public LikesModel(){}

    public LikesModel(String currentUser, Date timestamp) {
        this.currentUser = currentUser;
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return currentUser;
    }

    public void setUser_id(String user_id) {
        this.currentUser = user_id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
