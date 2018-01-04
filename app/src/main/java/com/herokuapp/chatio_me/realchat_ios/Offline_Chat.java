package com.herokuapp.chatio_me.realchat_ios;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by TanDv on 12/13/2017.
 */

public class Offline_Chat extends Application {
    private DatabaseReference UserRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    public void onCreate() {
        super.onCreate();
        //
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso build = builder.build();
        build.setIndicatorsEnabled(true);
        build.setLoggingEnabled(true);
        Picasso.setSingletonInstance(build);


        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //neu user online
        if(currentUser != null){
            String online_user_id = mAuth.getCurrentUser().getUid();

            UserRef = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child(online_user_id);
            UserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //neu user disconnect
                    UserRef.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
