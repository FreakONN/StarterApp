package com.example.deusc.androidblog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.deusc.androidblog.Adapter.CommentsRecyclerAdapter;
import com.example.deusc.androidblog.Model.CommentsModel;
import com.example.deusc.androidblog.Model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {

    private Toolbar commentsToolbar;
    private RecyclerView comment_list;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;

    private List<CommentsModel> commentsList;
    private List<UserModel> userList;

    private ImageView commentPostBtn;
    private EditText commentField;
    private String message_id;
    private String currentUserId;
    private String  username;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String TAG = "Document";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        commentsToolbar = findViewById(R.id.comment_toolbar);
        setSupportActionBar(commentsToolbar);
        getSupportActionBar().setTitle("Comments");

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        currentUserId = firebaseAuth.getCurrentUser().getUid();
        //pasted string has to be of the same name
        message_id = getIntent().getStringExtra("message_id");

        commentPostBtn =  findViewById(R.id.comment_post_btn);
        commentField = findViewById(R.id.comment_field);
        comment_list = findViewById(R.id.comments_list);


        //RecyclerView list
        commentsList = new ArrayList<>();
        userList = new ArrayList<>();

        commentsRecyclerAdapter = new CommentsRecyclerAdapter(commentsList, userList);
        comment_list.setHasFixedSize(true);
        comment_list.setLayoutManager(new LinearLayoutManager(this));
        comment_list.setAdapter(commentsRecyclerAdapter);

        //retrieving comments in real-time
        firebaseFirestore.collection("Posts/" + message_id + "/Comments")
                .addSnapshotListener(CommentsActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent( QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    //document added or exists
                    for (DocumentChange query : queryDocumentSnapshots.getDocumentChanges()) {
                        if (query.getType() == DocumentChange.Type.ADDED) {
                            CommentsModel commentsModel= query.getDocument().toObject(CommentsModel.class);
                            commentsList.add(commentsModel);
                            commentsRecyclerAdapter.notifyDataSetChanged();

                        }
                    }
                }
            }
        });

        commentPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String comment_field = commentField.getText().toString();

                    //current comment that com.example.deusc.androidblog.Model.UserModel posted
                    Map<String, Object> commentsMap = new HashMap<>();
                    commentsMap.put("comment_message", comment_field);
                    commentsMap.put("user_id", currentUserId);
                    commentsMap.put("user", username);
                    commentsMap.put("timestamp", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("Posts/" + message_id + "/Comments").add(commentsMap)
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {

                                    if(!task.isSuccessful()){
                                        Toast.makeText(CommentsActivity.this, "Comment not posted: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }else{
                                        commentField.setText("");
                                    }
                                }
                            });
                firebaseFirestore.collection("Posts/" + message_id + "/Notification").add(commentsMap)
                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                if(!task.isSuccessful()){
                                    Toast.makeText(CommentsActivity.this, "Comment not posted: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }else{
                                    commentField.setText("");
                                }
                            }
                        });
            }
        });
    }
}
