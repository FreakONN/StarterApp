package com.example.deusc.androidblog.BottomNavigationFragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.deusc.androidblog.Model.AccountModel;
import com.example.deusc.androidblog.Model.CommentsModel;
import com.example.deusc.androidblog.Model.LikesModel;
import com.example.deusc.androidblog.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
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

    private ListenerRegistration listener;

    private String postUserId;
    private String postId;
    private int userInteractedWithPost = 0;
    String currentUser;
    int userComments,  userLikes;

    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);



        userCommented = view.findViewById(R.id.account_comment_count_you);
        userCommented.setText(String.valueOf(userComments));

        userLiked = view.findViewById(R.id.account_like_count_you);
        userLiked.setText(String.valueOf(userLikes));

        //firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        dbComments = firebaseFirestore.collection("Posts/" + postId + "/Comments");
        dbLikes = firebaseFirestore.collection("Posts/" + postId + "/Comments");


        currentUser = firebaseAuth.getCurrentUser().getUid(); //Ioq6jVGkaSc19rGEwle2ocosCks2
        if (currentUser != null) {

            Map<String, Integer> map = new HashMap<>();
            AccountModel stats = new AccountModel();

            firebaseFirestore.collection("Posts").addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (!documentSnapshots.isEmpty()) {
                        if (documentSnapshots != null) {
                            for (DocumentSnapshot query : documentSnapshots) {
                                postUserId = query.getString("user_id"); //Ioq6jVGkaSc19rGEwle2ocosCks2
                                postId = query.getId();  //nfzZm1yp5G6Bk2xrsCu6
                            }
                        }
                    }
                }
            });

            //Comment count
            dbComments.get().addOnSuccessListener(getActivity(),new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (queryDocumentSnapshots != null) {
                        if(queryDocumentSnapshots.isEmpty()){
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                CommentsModel commentsModel = documentSnapshot.toObject(CommentsModel.class);
                                Map<String, Object> comments = documentSnapshot.getData();
                                comments.get("user_id");
                                //ako je currentUser == Comment->user_id
                                userComments = compareValues(comments, currentUser);
                            }
                        }
                    }
                }
            });

            //Like count
            dbLikes.get().addOnSuccessListener(getActivity(),new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if(queryDocumentSnapshots != null){
                        if(queryDocumentSnapshots.isEmpty()){
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                LikesModel likesModel = documentSnapshot.toObject(LikesModel.class);
                                Map<String, Object> likes = documentSnapshot.getData();
                                likes.get("user_id");
                                //ako je currentUser == Comment->user_id
                                userLikes = compareValues(likes, currentUser);
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

    @Override
    public void onStop() {
        super.onStop();
        listener.remove();
    }

    @Override
    public void onStart() {
        super.onStart();
        listener = dbComments.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot documentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Toast.makeText(getActivity(),"Error: " + e.getMessage() ,Toast.LENGTH_SHORT ).show();
                }
                    if(documentSnapshots != null){
                        if(documentSnapshots.isEmpty()){
                            for (QueryDocumentSnapshot documentSnapshot : documentSnapshots) {
                                LikesModel likesModel = documentSnapshot.toObject(LikesModel.class);
                                Map<String, Object> likes = documentSnapshot.getData();
                                likes.get("user_id");
                                //ako je currentUser == Comment->user_id
                                userInteractedWithPost = compareValues(likes, currentUser);
                            }
                        }
                    }

            }
        });

        //Like count
        dbComments.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot documentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Toast.makeText(getActivity(),"Error: " + e.getMessage() ,Toast.LENGTH_SHORT ).show();
                }
                if(documentSnapshots != null){
                    if(documentSnapshots.isEmpty()){
                        for (QueryDocumentSnapshot documentSnapshot : documentSnapshots) {
                            LikesModel likesModel = documentSnapshot.toObject(LikesModel.class);
                            Map<String, Object> likes = documentSnapshot.getData();
                            likes.get("user_id");
                            //ako je currentUser == Comment->user_id
                            userInteractedWithPost = compareValues(likes, currentUser);
                        }
                    }
                }
            }
        });




    }
}
