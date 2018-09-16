package com.example.deusc.androidblog.BottomNavigationFragments;

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
import com.example.deusc.androidblog.Model.UserModel;
import com.example.deusc.androidblog.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


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
    public List<UserModel> usersModelList;
    public List<LikesModel> likesModelList;

    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        commentsModelList = new ArrayList<>();
        usersModelList = new ArrayList<>();

        //firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        //setting up adapter and recycler view
        notificationRecyclerView = view.findViewById(R.id.notification_list_view);
        notificationsRecyclerAdapter = new NotificationsRecyclerAdapter(commentsModelList);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        notificationRecyclerView.setLayoutManager(mLayoutManager);
        notificationRecyclerView.setAdapter(notificationsRecyclerAdapter);

        final String currentUser = firebaseAuth.getCurrentUser().getUid();

        if(currentUser != null) {
            firebaseFirestore.collection("Posts").addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if(documentSnapshots != null){
                            for(DocumentSnapshot query: documentSnapshots){
                                postUserId = query.getString("user_id");
                                postId = query.getId();
                                firebaseFirestore.collection("Posts/" + postId + "/Comments").addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                                        if(documentSnapshots != null){
                                            for(DocumentSnapshot doc: documentSnapshots.getDocuments()){
                                                CommentsModel commentsModel = doc.toObject(CommentsModel.class).withId(postId);
                                                commentsModelList.add(commentsModel);
                                                notificationsRecyclerAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    }
                                });
                            }
                        }

                }
            });
        }

        return view;
    }

}
