package com.example.deusc.androidblog.BottomNavigationFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.deusc.androidblog.Adapter.MessageRecyclerAdapter;
import com.example.deusc.androidblog.Model.MessageModel;
import com.example.deusc.androidblog.Model.UserModel;
import com.example.deusc.androidblog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    //RecyclerView
    private RecyclerView messageListView;
    private MessageRecyclerAdapter messageRecyclerAdapter;
    //Models
    private List<MessageModel> messageList;
    private List<UserModel> userList;

    //Firebase
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    //vars
    private boolean firstPageLodaded = true;
    //Data
    private DocumentSnapshot lastVisible;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        firebaseAuth = FirebaseAuth.getInstance();

        //dataSet passing to RecyclerView
        messageList = new ArrayList<>();
        userList = new ArrayList<>();

        //Setting RecyclerViewAdapter
        messageListView = view.findViewById(R.id.home_blog_post_view);
        messageRecyclerAdapter = new MessageRecyclerAdapter(messageList,userList);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(container.getContext());
        messageListView.setLayoutManager(mLayoutManager);
        messageListView.setAdapter(messageRecyclerAdapter);
        messageListView.setHasFixedSize(true);

        if(firebaseAuth.getCurrentUser() != null){
            firebaseFirestore = FirebaseFirestore.getInstance();

            messageListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean reachedBottomLimit = !recyclerView.canScrollVertically(1);
                    if(reachedBottomLimit){
                        //getString has to match filed in FireStore
                        String lastIndex  = lastVisible.getString("description");
                        Toast.makeText(getContext(), " " + lastIndex, Toast.LENGTH_SHORT).show();
                        queryMoreMessages();
                    }
                }
            });

            Query firstQuery = firebaseFirestore
                    .collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(3);
            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                    if(queryDocumentSnapshots != null) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            //get last visible document
                            //data loaded in this query for the first time then we can change last visible
                            if(firstPageLodaded) {
                                lastVisible = queryDocumentSnapshots.getDocuments()
                                        .get(queryDocumentSnapshots.size() - 1);
                                //clear pages
                                messageList.clear();
                                userList.clear();
                            }
                            //document added or exists
                            for (DocumentChange query : queryDocumentSnapshots.getDocumentChanges()) {
                                if (query.getType() == DocumentChange.Type.ADDED) {
                                    String messageId = query.getDocument().getId();
                                    //storring data to Object Model MessageModel
                                    //extending Model class with generic class with withId method as intermediate
                                    final MessageModel messageModel = query.getDocument().toObject(MessageModel.class).withId(messageId);
                                    String messageUserId = query.getDocument().getString("user_id");

                                    firebaseFirestore.collection("Users").document(messageUserId).get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if(task.isSuccessful()){
                                                DocumentSnapshot res = task.getResult();
                                                //adding MessageModel,UserModel to List messageList,usersList
                                                //recyclerAdapter receives data and displays it in recycler view
                                                UserModel user = res.toObject(UserModel.class);
                                                if (firstPageLodaded) {
                                                    userList.add(user);
                                                    messageList.add(messageModel);
                                                } else {
                                                    //if the data is first loaded add it atop recycleView
                                                    userList.add(0, user);
                                                    messageList.add(0, messageModel);
                                                }
                                                messageRecyclerAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
                                }
                            }
                            //Data of first page is loaded only once
                            firstPageLodaded = false;
                        }
                    }
                }
            });
        }
        // Inflate the layout for this fragment
        return view;
    }

    public void queryMoreMessages(){
        if(firebaseAuth.getCurrentUser() != null) {

            Query nextQuery = firebaseFirestore.collection("Posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(3);
            nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                    if(queryDocumentSnapshots != null) {
                        //document added or exists
                        if (!queryDocumentSnapshots.isEmpty()) {
                            //get last visible document
                            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                            for (DocumentChange query : queryDocumentSnapshots.getDocumentChanges()) {
                                if (queryDocumentSnapshots != null) {
                                    if (query.getType() == DocumentChange.Type.ADDED) {
                                        //storring data to Object Model MessageModel
                                        String messageId = query.getDocument().getId();
                                        final MessageModel messageModel = query.getDocument().toObject(MessageModel.class).withId(messageId);
                                        String messageUserId = query.getDocument().getString("user_id");

                                        firebaseFirestore.collection("Users").document(messageUserId).get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            //adding MessageModel,UserModel to List messageList,usersList
                                                            //recyclerAdapter receives data and displays it in recycler view
                                                            UserModel user = task.getResult().toObject(UserModel.class);

                                                            userList.add(user);
                                                            messageList.add(messageModel);
                                                            //notifiy adapter for data changes
                                                            messageRecyclerAdapter.notifyDataSetChanged();
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }
    }

}
