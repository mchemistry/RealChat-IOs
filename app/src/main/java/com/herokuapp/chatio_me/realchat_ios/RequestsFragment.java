package com.herokuapp.chatio_me.realchat_ios;


import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {
    private RecyclerView myRequestlist;
    private View mMainView;
    private DatabaseReference FriendsRequestRef;
    private FirebaseAuth mAuth;
    String online_user_id;
    private DatabaseReference UsersDatabaseRef;

    private DatabaseReference FriendsDatabaseRef;
    private DatabaseReference FriendReqRef;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        myRequestlist = (RecyclerView) mMainView.findViewById(R.id.request_list);
        myRequestlist.setHasFixedSize(true);
        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        UsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendsRequestRef  = FirebaseDatabase.getInstance().getReference().child("Friend_Requests").child(online_user_id);


        FriendsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friends");

        FriendReqRef = FirebaseDatabase.getInstance().getReference("Friend_Requests");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myRequestlist.setLayoutManager(linearLayoutManager);

        // Inflate the layout for this fragment
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Requests,RequestViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Requests, RequestViewHolder>(
                Requests.class,
                R.layout.friends_request_all_user_layout,
                RequestsFragment.RequestViewHolder.class,
                FriendsRequestRef
                ) {
            @Override
            protected void populateViewHolder(final RequestViewHolder viewHolder, Requests model, int position) {
                //lay request tu database
                final String listUserId = getRef(position).getKey();
                DatabaseReference get_type_req =  getRef(position).child("request_type").getRef();

                get_type_req.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String type  = dataSnapshot.getValue().toString();
                            if (type.equals("received")){
                                UsersDatabaseRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                                        final String userStatus  = dataSnapshot.child("user_status").getValue().toString();
                                        final String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();

                                        viewHolder.setUsername(userName);
                                        viewHolder.setUserstatus(userStatus);
                                        viewHolder.setThumbImage(thumbImage,getContext());

                                        Button accept = (Button) viewHolder.mView.findViewById(R.id.request_accept_btn);
                                        Button decline = (Button) viewHolder.mView.findViewById(R.id.request_decline_btn);
                                        accept.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Calendar getDate = Calendar.getInstance();
                                                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                                                final String saveCurentDate = currentDate.format(getDate.getTime());

                                                FriendsDatabaseRef.child(online_user_id).child(listUserId).child("date").setValue(saveCurentDate)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                FriendsDatabaseRef.child(listUserId).child(online_user_id).child("date").setValue(saveCurentDate)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                FriendReqRef.child(online_user_id).child(listUserId).removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if(task.isSuccessful()){
                                                                                                    FriendReqRef.child(listUserId).child(online_user_id).removeValue()
                                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                @Override
                                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                                    if(task.isSuccessful()){
                                                                                                                        Toast.makeText(getContext(), "Thêm bạn thành công!", Toast.LENGTH_SHORT).show();
                                                                                                                    }
                                                                                                                }
                                                                                                            });
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        });
                                                            }
                                                        });
                                            }
                                        });

                                        //tu choi loi moi
                                        decline.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                FriendReqRef.child(listUserId).child(online_user_id).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    FriendReqRef.child(online_user_id).child(listUserId).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        Toast.makeText(getContext(), "Bạn đã từ chối lời mời!", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });

                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            else if(type.equals("sent")){


                                UsersDatabaseRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                                        final String userStatus  = dataSnapshot.child("user_status").getValue().toString();
                                        final String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();

                                        viewHolder.setUsername(userName);
                                        viewHolder.setUserstatus(userStatus);
                                        viewHolder.setThumbImage(thumbImage,getContext());
                                        Button req_sent = (Button) viewHolder.mView.findViewById(R.id.request_accept_btn);
                                        req_sent.setText("Hủy yêu cầu");
                                        viewHolder.mView.findViewById(R.id.request_decline_btn).setVisibility(View.INVISIBLE);
                                        req_sent.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                FriendReqRef.child(listUserId).child(online_user_id).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    FriendReqRef.child(online_user_id).child(listUserId).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        Toast.makeText(getContext(), "Hủy yêu cầu kết bạn thành công!", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        };

        myRequestlist.setAdapter(firebaseRecyclerAdapter);
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        public RequestViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setUsername(String userName) {

            TextView userDisplayName = (TextView) mView.findViewById(R.id.request_profile_name);
            userDisplayName.setText(userName);
        }

        public void setThumbImage(final String thumbImage,final Context ctx) {

            final CircleImageView thumb_image = (CircleImageView) mView.findViewById(R.id.request_profile_image);
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

        public void setUserstatus(String userStatus) {

            TextView status = (TextView) mView.findViewById(R.id.request_profile_status);
            status.setText(userStatus);
        }
    }

}
