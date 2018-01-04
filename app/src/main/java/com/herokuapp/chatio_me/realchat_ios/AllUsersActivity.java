package com.herokuapp.chatio_me.realchat_ios;

import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity {
    private RecyclerView allUsersList;
    private Toolbar mToolbar;
    private DatabaseReference allUsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        mToolbar = (Toolbar) findViewById(R.id.all_users_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Phòng Chat");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        allUsersList = (RecyclerView) findViewById(R.id.all_users_list);
        allUsersList.setHasFixedSize(true);
        allUsersList.setLayoutManager(new LinearLayoutManager(this));

        allUsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        allUsersRef.keepSynced(true);

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<AllUsers, AllUsersViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<AllUsers, AllUsersViewHolder>(
                        AllUsers.class,R.layout.all_users_display_layout,
                        AllUsersViewHolder.class,
                        allUsersRef

        ) {
            @Override
            protected void populateViewHolder(AllUsersViewHolder viewHolder, AllUsers model, final int position) {
                viewHolder.setUser_name(model.getUser_name());
                viewHolder.setUser_status(model.getUser_status());
                viewHolder.setUser_thumb_image(getApplicationContext(),model.getUser_thumb_image());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(position).getKey();
                        Intent mProfileIntent = new Intent(AllUsersActivity.this, ProfileActivity.class);
                        mProfileIntent.putExtra("visit_user_id", visit_user_id);
                        startActivity(mProfileIntent);
                    }
                });
            }
        };

        allUsersList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class AllUsersViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public AllUsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        //Lay ten tai khoan dat vao list view
        public void setUser_name(String user_name){
            TextView name = (TextView)  mView.findViewById(R.id.all_users_name);
            name.setText(user_name);
        }

        //Lay ten tai khoan dat vao list view
        public void setUser_status(String user_status){
            TextView status = (TextView)  mView.findViewById(R.id.all_users_status);
            status.setText(user_status);
        }

        //lay anh chuyen vao list user
        public void setUser_thumb_image(final Context ctx,final String user_thumb_image){
            final CircleImageView thumb_image = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);

            //Inner anh khi user offline
            Picasso.with(ctx).load(user_thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.user).into(thumb_image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(user_thumb_image).into(thumb_image);
                }
            });
        }
    }
}
