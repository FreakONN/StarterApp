package com.example.deusc.androidblog.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {

    public List<CommentsModel> commentsModelList;
    public List<UserModel> usersModelList;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public Context context;

    public CommentsRecyclerAdapter(List<CommentsModel> commentsModelList, List<UserModel> userList) {
        this.commentsModelList = commentsModelList;
        this.usersModelList = userList;
    }

    @NonNull
    @Override
    public CommentsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reply_list_item, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        return new CommentsRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentsRecyclerAdapter.ViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        String commentMessage = commentsModelList.get(position).getComment_message();
        holder.setCommentMessage(commentMessage);

        String user_id = commentsModelList.get(position).getUser_id();
        //User Data will be retrieved here...
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String userName = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");
                    holder.setUserName(userName);
                    holder.setUserImage(userImage);
                } else {
                    //Firebase Exception
                }
            }
        });
    }
    @Override
    public int getItemCount() {
        if(commentsModelList != null) {
            return commentsModelList.size();
        } else {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private View mView;

        private TextView user_id;
        private TextView comment_message;
        private CircleImageView image;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setCommentMessage(String commentMessage) {
            comment_message = mView.findViewById(R.id.comments_message);
            comment_message.setText(commentMessage);
        }
        public void setUserName(String user) {
            user_id = mView.findViewById(R.id.comments_message);
            user_id.setText(user);
        }
        public void setUserImage(String profileImage){
            image = mView.findViewById(R.id.comment_profile_image);

            RequestOptions placeholderRequest = new RequestOptions();
            placeholderRequest.placeholder(R.drawable.rectangle);

            Glide.with(image.getContext()).applyDefaultRequestOptions(placeholderRequest)
                    .load(profileImage)
                   .into(image);
        }
    }
}