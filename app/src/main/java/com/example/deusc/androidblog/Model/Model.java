package com.example.deusc.androidblog.Model;

import java.util.Date;

public class Model {
    public static final int COMMENT = 0;
    public static final int LIKE = 1;

    public int type;
    public int data;
    public String comment;
    public Date timestamp;

    public Model(int type, int data, String comment, Date timestamp) {
        this.type = type;
        this.data = data;
        this.comment = comment;
        this.timestamp = timestamp;
    }


}
