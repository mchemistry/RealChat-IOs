package com.herokuapp.chatio_me.realchat_ios;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private Button sendfriendrequestbtn;
    private Button declinefriendrequestbtn;

    private TextView profileUsername;
    private TextView profileUserStatus;
    private ImageView profileImage;

    private DatabaseReference friendRequestRef;
    private DatabaseReference UserReference;
    private DatabaseReference friendsRef;
    private DatabaseReference NotificationRef;
    private FirebaseAuth mAuth;
    private String CURRENT_STATE;
    String sender_user_id;
    String receiver_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        UserReference = FirebaseDatabase.getInstance().getReference().child("Users");

        //them 1 thuoc tinh moi tren database
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");

        //lay thong tin offline
        friendRequestRef.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        sender_user_id = mAuth.getCurrentUser().getUid();
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        friendsRef.keepSynced(true);

        //cai dat Notification
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        NotificationRef.keepSynced(true);

        receiver_user_id = getIntent().getExtras().get("visit_user_id").toString();

        Toast.makeText(ProfileActivity.this, receiver_user_id, Toast.LENGTH_SHORT).show();

        profileUsername = (TextView) findViewById(R.id.profile_visit_username);
        profileImage = (ImageView) findViewById(R.id.profile_visit_user_image);
        profileUserStatus = (TextView) findViewById(R.id.profile_visit_user_status);
        sendfriendrequestbtn = (Button) findViewById(R.id.send_req_btn);
        declinefriendrequestbtn = (Button) findViewById(R.id.decline_req_btn);

        CURRENT_STATE = "not_friend";
        UserReference.child(receiver_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //lay thong tin tren databse va inner len man hinh
                String name  = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();
                profileUsername.setText(name);
                profileUserStatus.setText(status);
                Picasso.with(ProfileActivity.this).load(image).into(profileImage);

                //cap nhat button gui loi moi ket ban
                friendRequestRef.child(sender_user_id)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                   if(dataSnapshot.hasChild(receiver_user_id)){
                                        //cap nhat du lieu
                                       String req_type = dataSnapshot.child(receiver_user_id).child("request_type").getValue().toString();
                                       if(req_type.equals("sent")){
                                           CURRENT_STATE = "request_sent";
                                           sendfriendrequestbtn.setText("HỦY YÊU CẦU");


                                           //xoa nut tu choi ket ban
                                           declinefriendrequestbtn.setVisibility(View.INVISIBLE);
                                           declinefriendrequestbtn.setEnabled(false);
                                       }
                                       else if(req_type.equals(("received"))){
                                           CURRENT_STATE = "request_received";
                                           sendfriendrequestbtn.setText("CHẤP NHẬN LỜI MỜI");
                                           //them nut tu choi ket ban
                                           declinefriendrequestbtn.setVisibility(View.VISIBLE);
                                           declinefriendrequestbtn.setEnabled(true);

                                           declinefriendrequestbtn.setOnClickListener(new View.OnClickListener() {
                                               @Override
                                               public void onClick(View v) {
                                                   //tu choi ket ban
                                                   DeclineFriendRequest();
                                               }
                                           });
                                       }
                                   }

                               else{
                                   friendsRef.child(sender_user_id)
                                           .addListenerForSingleValueEvent(new ValueEventListener() {
                                               @Override
                                               public void onDataChange(DataSnapshot dataSnapshot) {
                                                   //neu ton tai thi xoa khoi database
                                                   if(dataSnapshot.hasChild(receiver_user_id)){
                                                       CURRENT_STATE = "friends";
                                                       sendfriendrequestbtn.setText("Hủy kết bạn");

                                                       //xoa nut tu choi ket ban
                                                       declinefriendrequestbtn.setVisibility(View.INVISIBLE);
                                                       declinefriendrequestbtn.setEnabled(false);
                                                   }
                                               }

                                               @Override
                                               public void onCancelled(DatabaseError databaseError) {

                                               }
                                           });
                               }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        declinefriendrequestbtn.setVisibility(View.INVISIBLE);
        declinefriendrequestbtn.setEnabled(false);


        //neu khong phai la mminh thi moi nhin thay nut gui,huy ket ban
        if(!sender_user_id.equals(receiver_user_id)){
            //gui loi moi ket ban toi user khac
            sendfriendrequestbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //ngan nguoi dung gui them request
                    sendfriendrequestbtn.setEnabled(false);

                    if(CURRENT_STATE.equals("not_friend")){
                        //gui loi moi ket  ban
                        makeNewFriend();

                    }
                    //huy yeu cau
                    if(CURRENT_STATE.equals("request_sent")){
                        CancelFriendRequest();
                    }
                    //chap nhan loi moi
                    if(CURRENT_STATE.equals("request_received")){
                        AcceptFriendRequest();
                    }
                    //huy ket ban
                    if(CURRENT_STATE.equals("friends")){
                        Unfriend();
                    }
                }
            });
        }
        //neu la minh thi enable nut gui yeu cau va chan ket ban
        else{
            declinefriendrequestbtn.setVisibility(View.INVISIBLE);
            sendfriendrequestbtn.setVisibility(View.INVISIBLE);
        }


    }

    //xu ly tu choi ket ban
    private void DeclineFriendRequest() {

        friendRequestRef.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendRequestRef.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendfriendrequestbtn.setEnabled(true);
                                                CURRENT_STATE = "not_friend";
                                                sendfriendrequestbtn.setText("KẾT BẠN");
                                                //xoa nut tu choi ket ban
                                                declinefriendrequestbtn.setVisibility(View.INVISIBLE);
                                                declinefriendrequestbtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    //xu ly huy ket ban

    private void Unfriend() {
        friendsRef.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            //xoa receive_id va sent_id tren databse
                            friendsRef.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendfriendrequestbtn.setEnabled(true);
                                                CURRENT_STATE = "not_friend";
                                                sendfriendrequestbtn.setText("KẾT BẠN");
                                                //xoa nut tu choi ket ban
                                                declinefriendrequestbtn.setVisibility(View.INVISIBLE);
                                                declinefriendrequestbtn.setEnabled(false);
                                            }
                                        }
                                    });

                        }
                    }
                });

    }

    //xu ly chap nhan loi moi ket ban
    private void AcceptFriendRequest() {
        //luu ngay gui request
        Calendar getDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        final String saveCurentDate = currentDate.format(getDate.getTime());

        friendsRef.child(sender_user_id).child(receiver_user_id).child("date").setValue(saveCurentDate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        friendsRef.child(receiver_user_id).child(sender_user_id).child("date").setValue(saveCurentDate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        friendRequestRef.child(sender_user_id).child(receiver_user_id).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            friendRequestRef.child(receiver_user_id).child(sender_user_id).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()){
                                                                                sendfriendrequestbtn.setEnabled(true);
                                                                                CURRENT_STATE = "friends";
                                                                                sendfriendrequestbtn.setText("Hủy kết bạn");
                                                                                //xoa nut tu choi ket ban
                                                                                declinefriendrequestbtn.setVisibility(View.INVISIBLE);
                                                                                declinefriendrequestbtn.setEnabled(false);
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

    //xu ly huy yeu cau
    private void CancelFriendRequest() {
        friendRequestRef.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendRequestRef.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendfriendrequestbtn.setEnabled(true);
                                                CURRENT_STATE = "not_friend";
                                                sendfriendrequestbtn.setText("KẾT BẠN");
                                                //xoa nut tu choi ket ban
                                                declinefriendrequestbtn.setVisibility(View.INVISIBLE);
                                                declinefriendrequestbtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    //xu ly loi moi ket ban
    private void makeNewFriend() {
        friendRequestRef
                .child(sender_user_id).child(receiver_user_id).child("request_type")
                .setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendRequestRef.
                                    child(receiver_user_id).child(sender_user_id).child("request_type")
                                    .setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendfriendrequestbtn.setEnabled(true);
                                                CURRENT_STATE = "request_sent";
                                                sendfriendrequestbtn.setText("HỦY YÊU CẦU");
                                                //xoa nut tu choi ket ban
                                                declinefriendrequestbtn.setVisibility(View.INVISIBLE);
                                                declinefriendrequestbtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
