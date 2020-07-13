package com.example.deusc.androidblog.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.deusc.androidblog.CommentsActivity;
import com.example.deusc.androidblog.Model.MessageModel;
import com.example.deusc.androidblog.Model.UserModel;
import com.example.deusc.androidblog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageRecyclerAdapter extends RecyclerView.Adapter<MessageRecyclerAdapter.ViewHolder> {

    public List<MessageModel> messageList;
    public List<UserModel> userList;

    public Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public MessageRecyclerAdapter(List<MessageModel> messageList, List<UserModel> userList){
        this.messageList = messageList;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false );
        context = parent.getContext();
        ViewHolder viewHolder = new ViewHolder(view);
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        //retrieve data from extended Model object
        final String messageId = messageList.get(position).MessageId;
        final String currentUser = firebaseAuth.getCurrentUser().getUid();

        //setting the Data
        //get description String with position from List messageList with method getDescription from MessageModel
        String description = messageList.get(position).getDescription();
        String image_url = messageList.get(position).getImage_url();
        String user_id = messageList.get(position).getUser_id();
        String thumbnail = messageList.get(position).getThumbnail();

        //setting User thumbnail image and name
        final String userName = userList.get(position).getName();
        String userImage = userList.get(position).getImage();
        holder.setUserNameImage(userName, userImage);

        //setting date of post
        try {
            long millisecond = messageList.get(position).getTimestamp().getTime();
            String dateString = DateFormat.format("dd/mm/yyyy", new Date(millisecond)).toString();
            holder.setDate(dateString);
        } catch (Exception e) {
            Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        //Like counts feature
        firebaseFirestore.collection("Posts/" + messageId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null) {

                    if (!queryDocumentSnapshots.isEmpty()) {
                        //document not empty - there are likes
                        int count = queryDocumentSnapshots.size(); //gets total number of items in collection
                        holder.likeCountNumber(count);
                    } else {
                        //no likes
                        holder.likeCountNumber(0);
                    }
                }
            }
        });

        //Comments counts feature
        firebaseFirestore.collection("Posts/" + messageId + "/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null) {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        //document not empty - there are likes
                        int count = queryDocumentSnapshots.size(); //gets total number of items in collection
                        holder.commentCommentNumber(count);
                    } else {
                        //no likes
                        holder.commentCommentNumber(0);
                    }
                }
            }
        });

        //Like Features
        holder.likeAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get Likes - realtime changes
                firebaseFirestore.collection("Posts/" + messageId + "/Likes").document(currentUser)
                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        //change color of button on database change
                        if (documentSnapshot != null) {
                            if (documentSnapshot.exists()) {
                                holder.likeAction.setImageDrawable(context.getDrawable(R.mipmap.action_like));
                            } else {
                                holder.likeAction.setImageDrawable(context.getDrawable(R.mipmap.action_dislike));
                            }
                        }
                    }
                });

                //Likes database
                firebaseFirestore.collection("Posts/" + messageId + "/Likes").document(currentUser)
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        //check if data not exists then add data
                        if(!task.getResult().exists()){
                            Map<String, Object> ratings = new HashMap<>();
                            ratings.put("timestamp", FieldValue.serverTimestamp());
                            ratings.put("currentUser", currentUser);
                            ratings.put("user", userName);
                            //id of blog post
                            firebaseFirestore.collection("Posts/" + messageId + "/Likes").document(currentUser).set(ratings);
                            firebaseFirestore.collection("Posts/" + messageId + "/Notification").document(currentUser).set(ratings);
                        }else {
                            firebaseFirestore.collection("Posts/" + messageId + "/Likes").document(currentUser).delete();

                        }
                    }
                });
            }
        });

        holder.actionComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentIntent = new Intent(context, CommentsActivity.class);
                commentIntent.putExtra("message_id", messageId);
                context.startActivity(commentIntent);
            }
        });

        //inflating View with setDescriptionText
        holder.setBlogImage(image_url,thumbnail);
        holder.setDescriptionText(description);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        //home
        private TextView description;
        private ImageView blogImage;
        private TextView user_id;
        private TextView timestamp;
        private CircleImageView userImage;
        private ImageView likeAction;
        private TextView likeCount;
        //notifications
        private ImageView actionComment;


        public ViewHolder(View itemView) {
            super(itemView);
            //used to populate methods
            mView = itemView;

            likeAction = itemView.findViewById(R.id.action_like);
            actionComment = itemView.findViewById(R.id.action_comment);

        }
        public void setDescriptionText(String text){
            description = mView.findViewById(R.id.blog_description);
            description.setText(text);
        }
        public void setUserNameImage(String name, String image){
            user_id = mView.findViewById(R.id.blog_username);
            user_id.setText(name);

            userImage = mView.findViewById(R.id.blog_user_image);

            RequestOptions placeholderRequest = new RequestOptions();
            placeholderRequest.placeholder(R.drawable.ellipse);

            Glide.with(userImage.getContext()).applyDefaultRequestOptions(placeholderRequest)
                    .load(image)
                    .into(userImage);
        }

        public void setDate(String text){
            timestamp = mView.findViewById(R.id.blog_date);
            timestamp.setText(text);
        }

        public void setBlogImage(String imageDownloadUri, String thumbnail){
            blogImage = mView.findViewById(R.id.blog_image);

            RequestOptions placeholderRequest = new RequestOptions();
            placeholderRequest.placeholder(R.drawable.rectangle);

            Glide.with(blogImage.getContext()).applyDefaultRequestOptions(placeholderRequest).load(imageDownloadUri)
                    .thumbnail(Glide.with(blogImage.getContext()).load(thumbnail)).into(blogImage);
            }
        public void likeCountNumber(int count){
            likeCount = mView.findViewById(R.id.like_count);
            likeCount.setText(count + " Likes");
        }

        public void commentCommentNumber(int count){
            likeCount = mView.findViewById(R.id.comment_count);
            likeCount.setText(count + " Comments");
        }


    }
}
