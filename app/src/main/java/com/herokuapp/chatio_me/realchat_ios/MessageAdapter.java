package com.herokuapp.chatio_me.realchat_ios;

import android.graphics.Color;
import android.support.v4.view.LayoutInflaterFactory;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Tandv on 12/14/2017.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private EditText me;
    private View mView;

    public MessageAdapter(List<Messages> userMessagesList){
        this.userMessagesList = userMessagesList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View V = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_layout_of_users,parent,false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(V);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        String message_sender_id = mAuth.getCurrentUser().getUid();

        Messages messages = userMessagesList.get(position);
        String fromUserId = messages.getFrom();



        if(fromUserId.equals(message_sender_id)){
            holder.messagesText.setBackgroundResource(R.drawable.message_text_background_two);
            holder.messagesText.setTextColor(Color.WHITE);
           holder.messagesText.setGravity(Gravity.RIGHT);

        }

        else
        {
            holder.messagesText.setBackgroundResource(R.drawable.message_text_background);

            holder.messagesText.setTextColor(Color.WHITE);
            //
            holder.messagesText.setGravity(Gravity.LEFT);

        }
        holder.messagesText.setText(messages.getMessage());
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView messagesText;
       // public CircleImageView userProfileImage;

        public MessageViewHolder(View view){
            super(view);
            messagesText = (TextView) view.findViewById(R.id.messages_text);
           // userProfileImage = (CircleImageView) view.findViewById(R.id.message_profile_image);
        }
    }

}
