package com.example.deusc.androidblog.Model;

import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class MessageId {

    @Exclude
    public String MessageId;

    //method that acts as a intermediate between classes
    public <T extends MessageId> T withId(@NonNull final String id){
        this.MessageId = id;
        return (T) this;
    }
}
