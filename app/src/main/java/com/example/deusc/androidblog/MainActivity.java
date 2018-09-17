package com.example.deusc.androidblog;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.deusc.androidblog.BottomNavigationFragments.AccountFragment;
import com.example.deusc.androidblog.BottomNavigationFragments.HomeFragment;
import com.example.deusc.androidblog.BottomNavigationFragments.NotificationFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private Toolbar mainToolbar;
    //firebaseAuth
    FirebaseAuth mAuth;
    private FloatingActionButton addPostBtn;
    private FirebaseFirestore firebaseFirestore;
    private String currentUserId;
    private BottomNavigationView mainBotttomNavigation;
    //fragments
    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        //toolbar
        mainToolbar = findViewById(R.id.main_toolbar);
        mainBotttomNavigation = findViewById(R.id.main_bottom_navigation);
        setSupportActionBar(mainToolbar);

        if(mAuth.getCurrentUser() != null){
            //fragments
            homeFragment = new HomeFragment();
            accountFragment = new AccountFragment();
            notificationFragment = new NotificationFragment();
            //set initial fragment
            //fragmentSwitcher(homeFragment);
            initializeFragment();
            //navigation to fragments
            mainBotttomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_containter_frame);

                    switch (item.getItemId()){
                        case R.id.nav_action_home:
                            fragmentSwitcher(homeFragment,currentFragment);
                            return true;
                        case R.id.nav_action_account:
                            fragmentSwitcher(accountFragment,currentFragment);
                            return true;
                        case R.id.nav_action_notificatin:
                            fragmentSwitcher(notificationFragment,currentFragment);
                            return true;
                        default:
                            return false;
                    }
                }
            });
            addPostBtn = findViewById(R.id.add_post_btn);
            addPostBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newPostIntent = new Intent(MainActivity.this, NewPostActivity.class);
                    startActivity(newPostIntent);
                }
            });
        }
    }

    private void initializeFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        //TODO zamijeniti četiri linije s petljom koja provjerava na kojem smo fragmentu i sakriva ostale po deafaulto smo na home-u stoga ostale skrivamo

        fragmentTransaction.add(R.id.main_containter_frame, homeFragment);
        //zamijeniti s petljom
        fragmentTransaction.add(R.id.main_containter_frame, accountFragment);
        fragmentTransaction.add(R.id.main_containter_frame, notificationFragment);

        fragmentTransaction.hide(notificationFragment);
        fragmentTransaction.hide(accountFragment);

        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_logout_btn:
                logOut();
                return true;
            case R.id.action_setting_btn:
                Intent settingsIntent = new Intent(MainActivity.this, SetupActivity.class);
                startActivity(settingsIntent);
            default:
                return false;
        }
    }

    private void logOut() {
        mAuth.signOut();
        sendToLogin();
    }

    private void sendToLogin() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }
    private void fragmentSwitcher(Fragment fragment, Fragment currentFragment){
        //replacing(switching) fragment with a new one
        //TODO riješiti s attach/detach listenerom
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_containter_frame,fragment);

        if(fragment == homeFragment){
            fragmentTransaction.hide(accountFragment);
            fragmentTransaction.hide(notificationFragment);
        }

        if(fragment == accountFragment){
            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(notificationFragment);
        }

        if(fragment == notificationFragment){
            fragmentTransaction.hide(accountFragment);
            fragmentTransaction.hide(homeFragment);
        }
        fragmentTransaction.show(fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            sendToLogin();
        }else{
            //logged
            currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
            firebaseFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        //user exists?
                        if(!task.getResult().exists()){
                            Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                            startActivity(setupIntent);
                            finish();
                        }
                    }else{
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "Error : " + errorMessage, Toast.LENGTH_SHORT ).show();
                    }
                }
            });

        }
    }
}
