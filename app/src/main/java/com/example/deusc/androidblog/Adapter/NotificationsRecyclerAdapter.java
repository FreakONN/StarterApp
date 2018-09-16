package com.example.deusc.androidblog.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.deusc.androidblog.Model.CommentsModel;
import com.example.deusc.androidblog.Model.UserModel;
import com.example.deusc.androidblog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationsRecyclerAdapter extends RecyclerView.Adapter<NotificationsRecyclerAdapter.ViewHolder> {

    private RecyclerView notificationRecyclerView;
    private NotificationsRecyclerAdapter notificationsRecyclerAdapter;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    public List<CommentsModel> commentsModelList;
    public List<UserModel> userList;
    public List<Object> likesList;


    public Context context;

    public NotificationsRecyclerAdapter(List<CommentsModel> commentsModelList) {
        this.commentsModelList = commentsModelList;
    }

    @NonNull
    @Override
    public NotificationsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_list_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        context = parent.getContext();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final NotificationsRecyclerAdapter.ViewHolder holder, int position) {

        final String currentUser = firebaseAuth.getCurrentUser().getUid();

        firebaseFirestore.collection("Posts").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(!documentSnapshots.isEmpty()){
                    if(documentSnapshots != null){
                        for(DocumentSnapshot query: documentSnapshots){
                            firebaseFirestore.collection("Posts").document(query.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        String postImage = task.getResult().getString("thumbnail");
                                        holder.setPostImage(postImage);
                                    } else {
                                        //Firebase Exception
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });

        String user_id = commentsModelList.get(position).getUser_id();
        //User Data will be retrieved here...
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String userName = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");
                    holder.setCommentMessage(userName);
                    holder.setProfileImage(userImage);
                } else {
                    //Firebase Exception
                }
            }
        });

        firebaseFirestore.collection("Posts").document().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String postImage = task.getResult().getString("image_url");
                    holder.setPostImage(postImage);
                } else {
                    //Firebase Exception
                }
            }
        });
        String commentMessage = commentsModelList.get(position).getComment_message();
        holder.setCommentMessage(commentMessage);

    }

    @Override
    public int getItemCount() {
        return commentsModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        //comments
        private CircleImageView profileImage;
        private ImageView postImage;

        private TextView commentUserId;
        private TextView likeUserId;

        private TextView comment;
        private TextView commentTimestamp;



        public ViewHolder(View itemView) {

            super(itemView);
            mView = itemView;
        }
        public void setCommentMessage(String name) {
            comment = mView.findViewById(R.id.notification_message);
            comment.setText(name + " liked your post");
        }
        public void setDate(String time) {
            commentTimestamp = mView.findViewById(R.id.notification_date);
            commentTimestamp.setText(time);
        }
        public void setPostImage(String image){
            postImage = mView.findViewById(R.id.notification_image_post);

            RequestOptions placeholderRequest = new RequestOptions();
            placeholderRequest.placeholder(R.drawable.cube);

            Glide.with(postImage.getContext()).applyDefaultRequestOptions(placeholderRequest)
                    .load(image)
                    .into(postImage);
        }
        public void setProfileImage(String image){
            profileImage = mView.findViewById(R.id.notification_user_image);

            RequestOptions placeholderRequest = new RequestOptions();
            placeholderRequest.placeholder(R.drawable.ellipse);

            Glide.with(profileImage.getContext()).applyDefaultRequestOptions(placeholderRequest)
                    .load(image)
                    .into(profileImage);
        }
    }
}
