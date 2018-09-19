package com.example.deusc.androidblog.BottomNavigationFragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.deusc.androidblog.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {

    private TextView userCommented;
    private TextView userLiked;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private String postUserId;
    private String postId;
    private int userInteractedWithPost = 0;

    int userComments = 0;
    int userLikes = 0;
    int messageCount = 0;
    int likeCount = 0;
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
        userCommented.setText(String.valueOf(messageCount));

        userLiked = view.findViewById(R.id.account_like_count_you);
        userLiked.setText(String.valueOf(likeCount));

        final String currentUser = firebaseAuth.getCurrentUser().getUid(); //trenutno ulogirani korisnik
        if (currentUser != null) {

           firebaseFirestore.collection("Posts").addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable final QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshots != null) {
                        if (!documentSnapshots.isEmpty()) {
                            for (DocumentChange query : documentSnapshots.getDocumentChanges()) {
                                if (query.getType() == DocumentChange.Type.ADDED) {
                                //postId =  query.getDocument().getString("user_id")
                                    postId = query.getDocument().getId();
                                    //check Comments
                                    firebaseFirestore.collection("Posts/" + postId + "/Comments")
                                            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot documentSnapshots) {
                                            String documents = documentSnapshots.getDocuments().toString();
                                            for(QueryDocumentSnapshot doc: documentSnapshots){
                                                Map<String, Object> comments = doc.getData();
                                                String key = comments.get("user_id").toString();
                                                if(key.equals(currentUser)){
                                                    messageCount += 1;
                                                }
                                                userComments = messageCount;
                                                //Log.d("ADADASD", " " + doc.getData() + " : " + documents);
                                            }
                                        }
                                    });
                                    messageCount = 0;
                            }
                        }
                    }
                }
                }
            });

            firebaseFirestore.collection("Posts").addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable final QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshots != null) {
                        if (!documentSnapshots.isEmpty()) {
                            for (DocumentChange query : documentSnapshots.getDocumentChanges()) {
                                if (query.getType() == DocumentChange.Type.ADDED) {
                                    postId = query.getDocument().getId();
                                    //check Comments
                                    firebaseFirestore.collection("Posts/" + postId + "/Likes")
                                            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot documentSnapshots) {
                                            String documents = documentSnapshots.getDocuments().toString();
                                            for(QueryDocumentSnapshot doc: documentSnapshots){
                                                Map<String, Object> likes = doc.getData();
                                                String key = likes.get("currentUser").toString();
                                                if(key.equals(currentUser)){
                                                    likeCount += 1;
                                                }
                                                userLikes = likeCount;
                                                //Log.d("ADADASD", " " + doc.getData() + " : " + documents);
                                            }
                                        }
                                    });
                                    likeCount = 0;
                                }
                            }
                        }
                    }
                }
            });

           }
        return view;
    }
}