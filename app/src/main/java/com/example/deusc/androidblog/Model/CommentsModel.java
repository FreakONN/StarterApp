package com.example.deusc.androidblog.Model;

import java.util.Date;

public class CommentsModel extends com.example.deusc.androidblog.Model.MessageId{

    private String comment_message, user_id;
    private Date timestamp;

    public CommentsModel(){}

    public CommentsModel(String comment_message, String user_id, Date timestamp) {
        this.comment_message = comment_message;
        this.user_id = user_id;
        this.timestamp = timestamp;
    }

    public String getComment_message() {
        return comment_message;
    }

    public void setComment_message(String comment_message) {
        this.comment_message = comment_message;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
