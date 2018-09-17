package com.example.deusc.androidblog.BottomNavigationFragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.deusc.androidblog.Model.CommentsModel;
import com.example.deusc.androidblog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {

    private TextView userCommented;
    private TextView userLiked;


    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private CollectionReference dbComments;
    private CollectionReference dbLikes;

    private String postUserId;
    private String postId;
    private int userInteractedWithPost = 0;
    String currentUser;
    int userComments,  userLikes;
    public List<CommentsModel> commentsModelList;

    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        //firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        userCommented = view.findViewById(R.id.account_comment_count_you);
        userCommented.setText(String.valueOf(userComments));

        userLiked = view.findViewById(R.id.account_like_count_you);
        userLiked.setText(String.valueOf(userLikes));
        commentsModelList = new ArrayList<>();

        if (currentUser != null) {

            firebaseFirestore.collection("Posts").addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshots != null) {
                        if (!documentSnapshots.isEmpty()) {
                            for (DocumentSnapshot query : documentSnapshots) {
                                postUserId = query.getString("user_id"); //Ioq6jVGkaSc19rGEwle2ocosCks2
                                postId = query.getId();  //nfzZm1yp5G6Bk2xrsCu6
                            }
                        }
                    }
                }
            });

            firebaseFirestore.collection("Posts/" + postId + "/Comments").addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable QuerySnapshot documentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                    if (documentSnapshots != null) {
                        //document added or exists
                        if(!documentSnapshots.isEmpty()){
                            for (QueryDocumentSnapshot query : documentSnapshots) {
                                currentUser = firebaseAuth.getCurrentUser().getUid(); //Ioq6jVGkaSc19rGEwle2ocosCks2

                                    Map<String, Object> comments = query.getData();
                                    Object userId = comments.get("user_id");
                                    if(currentUser != null){
                                        userComments = compareValues(comments, currentUser);

                                    }


                                firebaseFirestore.collection("Posts/" +postId + "/Comments").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if(task.isSuccessful()){
                                            QuerySnapshot comments1 = task.getResult();
                                            for(DocumentSnapshot doc: comments1){
                                                Log.d("DOC: ", " " + doc);
                                            }
                                        }

                                    }
                                });

                            }
                        }
                    }
                }
            });

            //Comment count
            firebaseFirestore.collection("Posts/" + postId + "/Comments").get().addOnSuccessListener(getActivity(),new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (queryDocumentSnapshots != null) {
                        if(!queryDocumentSnapshots.isEmpty()){
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                String messageId = documentSnapshot.getId();
                                CommentsModel commentsModel = documentSnapshot.toObject(CommentsModel.class).withId(messageId);
                                commentsModelList.add(commentsModel);
                                Map<String, Object> comments = documentSnapshot.getData();
                                commentsModel.getUser_id();
                                //ako je currentUser == Comment->user_id
                                userComments = compareValues(comments, currentUser);
                            }
                        }
                    }
                }
            });



        }
        return view;
    }

    public int compareValues(Map<String,Object> map, String currentUser){
        int count = 0;
        for(Map.Entry<String, Object> data: map.entrySet()) {

            if (data.getValue().equals(currentUser)) {
                count++;
            }
        }
        return count;
    }
}
