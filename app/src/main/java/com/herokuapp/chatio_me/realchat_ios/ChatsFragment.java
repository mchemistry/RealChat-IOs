package com.herokuapp.chatio_me.realchat_ios;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {
    private View mView;
    private RecyclerView myChatList;
    private DatabaseReference FriendsReference;
    private DatabaseReference UsersRef;
    private FirebaseAuth mAuth;
    String online_user_id;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_chats, container, false);
        myChatList = (RecyclerView) mView.findViewById(R.id.chats_list);
        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();

        FriendsReference  = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        FriendsReference.keepSynced(true);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        myChatList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        myChatList.setLayoutManager(linearLayoutManager);

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Chats,ChatsFragment.ChatsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Chats, ChatsViewHolder>
                (
                        Chats.class,
                        R.layout.all_users_display_layout,
                        ChatsFragment.ChatsViewHolder.class,
                        FriendsReference
                ) {
            @Override
            protected void populateViewHolder(final ChatsFragment.ChatsViewHolder viewHolder, Chats model, final int position) {


                final String list_users_id = getRef(position).getKey();
                //day thong tin ban be vao list
                UsersRef.child(list_users_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                        String userStatus  = dataSnapshot.child("user_status").getValue().toString();
                        String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();

                        //set icon online va offline neu user online hay offline
                        if(dataSnapshot.hasChild("online")){
                            String online_status = (String) dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(online_status);
                        }

                        viewHolder.setUsername(userName);
                        viewHolder.setThumbImage(getContext(),thumbImage);
                        viewHolder.setStatus(userStatus);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(dataSnapshot.child("online").exists()){
                                    Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                    chatIntent.putExtra("user_name",userName);
                                    chatIntent.putExtra("visit_user_id", list_users_id);
                                    startActivity(chatIntent);
                                }
                                else{
                                    //lay thoi gian da qua cua user da offline
                                    UsersRef.child(list_users_id).child("online")
                                            .setValue(ServerValue.TIMESTAMP).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("user_name",userName);
                                            chatIntent.putExtra("visit_user_id", list_users_id);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

        };
        myChatList.setAdapter(firebaseRecyclerAdapter);
    }
    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public ChatsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }



        public void setUsername(String userName) {
            TextView userNameDisplay = (TextView) mView.findViewById(R.id.all_users_name);
            userNameDisplay.setText(userName);


        }
        public  void setThumbImage(final Context ctx, final String thumbImage){
            final CircleImageView thumb_image = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);

            //Inner anh khi user offline
            Picasso.with(ctx).load(thumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.user).into(thumb_image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(thumbImage).into(thumb_image);
                }
            });
        }

        public void setUserOnline(String online_status) {

            ImageView online = (ImageView) mView.findViewById(R.id.online_status);

            if(online_status.equals("true")){
                online.setVisibility(View.VISIBLE);
            }
            else {
                online.setVisibility(View.INVISIBLE);
            }
        }

        public void setStatus(String userStatus) {
            TextView userNameDisplay = (TextView) mView.findViewById(R.id.all_users_status);
            userNameDisplay.setText(userStatus);

        }
    }
}
