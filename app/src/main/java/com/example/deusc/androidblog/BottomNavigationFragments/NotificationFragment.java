package com.example.deusc.androidblog.BottomNavigationFragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.deusc.androidblog.Adapter.NotificationsRecyclerAdapter;
import com.example.deusc.androidblog.Model.CommentsModel;
import com.example.deusc.androidblog.Model.LikesModel;
import com.example.deusc.androidblog.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
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
public class NotificationFragment extends Fragment {

    private RecyclerView notificationRecyclerView;
    private NotificationsRecyclerAdapter notificationsRecyclerAdapter;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    String postUserId;
    String postId;

    public List<CommentsModel> commentsModelList;
    public List<LikesModel> likesModelList;
    Context context;

    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        likesModelList = new ArrayList<>();
        commentsModelList = new ArrayList<>();


        //firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        //setting up adapter and recycler view
        notificationRecyclerView = view.findViewById(R.id.notification_list_view);
        notificationsRecyclerAdapter = new NotificationsRecyclerAdapter(commentsModelList,likesModelList);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        notificationRecyclerView.setLayoutManager(mLayoutManager);
        notificationRecyclerView.setAdapter(notificationsRecyclerAdapter);

        final String currentUser = firebaseAuth.getCurrentUser().getUid();

        if(currentUser != null) {

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
                                            for(QueryDocumentSnapshot doc: documentSnapshots){
                                                CommentsModel commentsModel = doc.toObject(CommentsModel.class).withId(postId);
                                                Map<String, Object> map = doc.getData();
                                                commentsModelList.add(commentsModel);
                                                notificationsRecyclerAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
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
                                            for(QueryDocumentSnapshot doc: documentSnapshots){
                                                LikesModel likesModel = doc.toObject(LikesModel.class).withId(postId);
                                                likesModelList.add(likesModel);
                                                notificationsRecyclerAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
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
