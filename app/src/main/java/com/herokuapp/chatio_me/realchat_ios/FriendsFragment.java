package com.herokuapp.chatio_me.realchat_ios;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    private RecyclerView myFriendsList;
    private DatabaseReference FriendsReference;
    private DatabaseReference friendsRef;
    private DatabaseReference UsersRef;
    private FirebaseAuth mAuth;
    String sender_user_id;
    String receiver_user_id;
    String online_user_id;

    private View myMainView;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        myFriendsList = (RecyclerView) myMainView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        FriendsReference  = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        FriendsReference.keepSynced(true);
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");

        myFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UsersRef.keepSynced(true);

        // Inflate the layout for this fragment
        return myMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>
                (
                        Friends.class,
                        R.layout.all_users_display_layout,
                        FriendsViewHolder.class,
                        FriendsReference
                ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, final int position) {
                viewHolder.setDate(model.getDate());

                final String list_users_id = getRef(position).getKey();
                //day thong tin ban be vao list
                UsersRef.child(list_users_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                        //String userStatus  = dataSnapshot.child("user_status").getValue().toString();
                        String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();

                        //set icon online va offline neu user online hay offline
                        if(dataSnapshot.hasChild("online")){
                            String online_status = (String) dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(online_status);
                        }

                        viewHolder.setUsername(userName);
                        viewHolder.setThumbImage(getContext(),thumbImage);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = new CharSequence[]{
                                        "Trang cá nhân",
                                        "Hủy kết bạn"
                                };
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("SELECT OPTIONS");
                                builder.setItems(options,new DialogInterface.OnClickListener(){
                                    @Override
                                            public void onClick(DialogInterface dialogInterface,int position){
                                                if(position == 0){
                                                    Intent profileIntent = new Intent(getContext(),ProfileActivity.class);
                                                    profileIntent.putExtra("visit_user_id", list_users_id);
                                                    startActivity(profileIntent);
                                                }
                                                if(position == 1){
                                                    friendsRef.child(online_user_id).child(list_users_id).removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        //xoa receive_id va sent_id tren databse
                                                                        friendsRef.child(list_users_id).child(online_user_id).removeValue()
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if(task.isSuccessful()){
                                                                                            Toast.makeText(getContext(), "Xóa bạn thành công!", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    }
                                                                                });

                                                                    }
                                                                }
                                                            });
                                                }


                                    }
                                });
                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

        };
        myFriendsList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setDate(String date){
            TextView sinceFriendsdate = (TextView) mView.findViewById(R.id.all_users_status);
            sinceFriendsdate.setText(Html.fromHtml("<b>Hai bạn đã là bạn của nhau hôm:</b>"+"\n" + date));

        }

        public void setUsername(String userName) {
            TextView userNameDisplay = (TextView) mView.findViewById(R.id.all_users_name);
            userNameDisplay.setText(userName);


        }
        public  void setThumbImage(final Context ctx,final String thumbImage){
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
    }
}
