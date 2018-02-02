package io.matic.websocketchat.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import io.matic.websocketchat.ChatActivity;
import io.matic.websocketchat.Entity.Message;
import io.matic.websocketchat.R;
import io.matic.websocketchat.StaticVariables;

/**
 * Created by Matic on 15/09/2017.
 */

public class MessageListAdapter extends BaseAdapter {
    Context context;
    private static ArrayList<Message> messages = new ArrayList<>();

    public static ArrayList<Message> getMessages() {
        return messages;
    }

    public static void setMessages(ArrayList<Message> messages) {
        MessageListAdapter.messages = messages;
    }

    public MessageListAdapter(Context context, ArrayList<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // set co-chat user profile pic
        String avatar = ChatActivity.getUserTo().getProfilePic();
        int imageResource = StaticVariables.getProfilePic(avatar);
        // set inflater
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        // if message by user
        if(messages.get(position).isSelf()) {
            // inflate view
            convertView = inflater.inflate(R.layout.message_sent, null);
            TextView textMessage = (TextView) convertView.findViewById(R.id.text_message_body);
            TextView textTime = (TextView) convertView.findViewById(R.id.text_message_time);
            textMessage.setText(messages.get(position).getMessage());
            textTime.setText(messages.get(position).getTime());
        // if message by co speaker
        } else {
            // inflate view
            convertView = inflater.inflate(R.layout.message_received, null);
            ImageView imageSender = (ImageView) convertView.findViewById(R.id.image_message_profile);
            TextView textSender = (TextView) convertView.findViewById(R.id.text_message_name);
            TextView textMessage = (TextView) convertView.findViewById(R.id.text_message_body);
            TextView textTime = (TextView) convertView.findViewById(R.id.text_message_time);
            textMessage.setText(messages.get(position).getMessage());
            imageSender.setImageResource(imageResource);
            textSender.setText(messages.get(position).getFrom());
            textTime.setText(messages.get(position).getTime());
        }
        return convertView;
    }
}
