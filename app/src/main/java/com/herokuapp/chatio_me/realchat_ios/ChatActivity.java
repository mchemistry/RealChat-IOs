package com.herokuapp.chatio_me.realchat_ios;

import android.content.Context;
import android.media.Image;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverId;
    private String messageReceiverName;

    private Toolbar ChatToolBar;
    private TextView userNameTitle;
    private TextView userLastSeen;
    private CircleImageView userChatProfileImage;

    private ImageButton sendMessageButton;
    private ImageButton selectImageButton;
    private EditText inputMessageText;

    private RecyclerView userMessagesList;



    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private String MessageSenderId;

    private List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        MessageSenderId = mAuth.getCurrentUser().getUid();

        messageReceiverId = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName= getIntent().getExtras().get("user_name").toString();
        rootRef = FirebaseDatabase.getInstance().getReference();
        ChatToolBar = (Toolbar) findViewById(R.id.chat_bar_layout);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar,null);

        actionBar.setCustomView(action_bar_view);
        //xu ly su kien buuton
        sendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        selectImageButton = (ImageButton) findViewById(R.id.select_image_button);
        inputMessageText = (EditText) findViewById(R.id.input_message);
        userMessagesList = (RecyclerView) findViewById(R.id.messageslist_of_users);


        userNameTitle = (TextView) findViewById(R.id.custom_user_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        userChatProfileImage = (CircleImageView) findViewById(R.id.custom_profile_image);
        userNameTitle.setText(messageReceiverName);

        messageAdapter = new MessageAdapter(messagesList);
       userMessagesList = (RecyclerView) findViewById(R.id.messageslist_of_users);

        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setHasFixedSize(true);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        FetchMessages();


        rootRef.child("Users").child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String online = dataSnapshot.child("online").getValue().toString();
                final String user_thumb = dataSnapshot.child("user_thumb_image").getValue().toString();

                //Inner anh khi user offline
                Picasso.with(ChatActivity.this).load(user_thumb).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.user).into(userChatProfileImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(ChatActivity.this).load(user_thumb).into(userChatProfileImage);
                    }
                });

                if(online.equals("true")){
                    userLastSeen.setText("Online now");
                }
                else{
                    TimeLeap getTime = new TimeLeap();

                    long last_seen = Long.parseLong(online);

                    String timeLeap = getTime.getTimeAgo(last_seen,getApplicationContext());

                    userLastSeen.setText(timeLeap);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });


    }

    private void FetchMessages() {

        rootRef.child("Messages").child(MessageSenderId).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void sendMessage() {

        String messageText = inputMessageText.getText().toString();

        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(ChatActivity.this, "Tin nhắn không được để trống !", Toast.LENGTH_LONG).show();
        }
        else{
            String message_sender_ref  = "Messages/"+ MessageSenderId + "/" + messageReceiverId;
            String message_receiver_ref  = "Messages/"+ messageReceiverId + "/" + MessageSenderId;

             DatabaseReference user_message_key = rootRef.child("Messages").child(MessageSenderId)
                                                            .child(messageReceiverId).push();

            String message_put_id = user_message_key.getKey();

            Map messageTextBody = new HashMap();

            messageTextBody.put("message",messageText);
            messageTextBody.put("seen",false);
            messageTextBody.put("type","text");
            messageTextBody.put("time", ServerValue.TIMESTAMP);
            messageTextBody.put("from",MessageSenderId);


            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(message_sender_ref + "/" + message_put_id, messageTextBody);

            messageBodyDetails.put(message_receiver_ref + "/" + message_put_id, messageTextBody);

            rootRef.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError!=null){
                        Log.d("Chat_Log",databaseError.getMessage().toString());
                    }

                    inputMessageText.setText(null);
                }
            });


        }
    }
}
