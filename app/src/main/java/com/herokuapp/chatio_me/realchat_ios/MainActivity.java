package com.herokuapp.chatio_me.realchat_ios;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsPagerAdapter myTabsPagerAdapter;
    FirebaseUser currentUser;

    private DatabaseReference UserRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //tao danh sach tabs
        myViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        myTabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsPagerAdapter);
        myTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //check user online hay offline

        if(currentUser != null){
            String online_user_id = mAuth.getCurrentUser().getUid();
            UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
        }


        mToolbar  = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("REAL-CHAT");
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
           LogOutUser();
        }
        //neu user online set value online-> true
        else if(currentUser!=null){
            UserRef.child("online").setValue("true");
        }
    }

    //neu user an app
    @Override
    protected void onStop() {
        super.onStop();

        if(currentUser!=null){
            UserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }

    }

    private void LogOutUser(){
        Intent startPageIntent = new Intent(MainActivity.this, StartPageActivity.class);
        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startPageIntent);
        Toast.makeText(getBaseContext(), "Bạn đã thoát khỏi hệ thống!", Toast.LENGTH_SHORT).show();
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.main_logout_button){

            if(currentUser != null){
                UserRef.child("online").setValue(ServerValue.TIMESTAMP);
            }
            mAuth.signOut();
            LogOutUser();
        }
        if(item.getItemId() == R.id.main_account_setting_button){
           Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(settingsIntent);
        }
        if(item.getItemId() == R.id.main_all_users_button){
            Intent settingsIntent = new Intent(MainActivity.this,AllUsersActivity.class);
            startActivity(settingsIntent);
        }
        return true;
    }
}
