package io.matic.websocketchat.Recycler;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import io.matic.websocketchat.ChatActivity;
import io.matic.websocketchat.Entity.Conversation;
import io.matic.websocketchat.Entity.Friend;
import io.matic.websocketchat.Entity.Message;
import io.matic.websocketchat.Entity.User;
import io.matic.websocketchat.MainActivity;
import io.matic.websocketchat.R;
import io.matic.websocketchat.StaticVariables;
import io.matic.websocketchat.WebSocketChatClient;

/**
 * Created by Wut on 18 Jul 2017.
 */

// adpater for friends
public class RecyclerViewAdapterFriends extends RecyclerView.Adapter<RecyclerViewAdapterFriends.ViewHolderFriends> {

    private static ArrayList<Friend> friends;
    private Context context;
    private View view;
    private ViewHolderFriends viewHolder;

    // set adapter
    public RecyclerViewAdapterFriends(Context context, ArrayList<Friend> friends){
        this.friends = friends;
        this.context = context;
    }

    public static ArrayList<Friend> getFriends() {
        return friends;
    }

    public static void setFriends(ArrayList<Friend> friends) {
        RecyclerViewAdapterFriends.friends = friends;
    }

    public interface ItemClickListener {
        void onItemClick(View v, int position);
    }

    // view holder is one item in recycler view
    public static class ViewHolderFriends extends RecyclerView.ViewHolder implements ItemClickListener{

        public TextView textViewFullName;
        public ImageView imageViewProfilePic;
        public ItemClickListener itemClickListener;

        public ViewHolderFriends(View v){
            super(v);
            textViewFullName = (TextView) v.findViewById(R.id.textViewFullName);
            imageViewProfilePic = (ImageView) v.findViewById(R.id.imageProfilePicture);
        }

        // set item click listener for one item
        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        // do something when item clicked
        @Override
        public void onItemClick(View v, int position) {
            this.itemClickListener.onItemClick(v, getLayoutPosition());
        }
    }

    /*
    * ADD or REMOVE data
    * */

    public void addFriend(Friend friend) {
        friends.add(friend);
        notifyItemRangeChanged(friends.size() - 1, friends.size());
    }

    public void updateData(ArrayList<Friend> newUsers) {
        friends.clear();
        friends.addAll(newUsers);
        notifyDataSetChanged();
    }

    public void removeFriend(Friend user) {
        Iterator<Friend> it = friends.iterator();
        while(it.hasNext()) {
            if(it.next().getFriend().getEmail().equals(user.getFriend().getEmail())) {
                it.remove();
            }
        }
        notifyDataSetChanged();
    }

    public void clearData() {
        if(friends != null) {
            friends.clear();
            notifyDataSetChanged();
        }
    }

    @Override
    public ViewHolderFriends onCreateViewHolder(ViewGroup parent, int viewType){
        // inflate view holder
        view = LayoutInflater.from(context).inflate(R.layout.fragment_model_friends,parent,false);
        viewHolder = new ViewHolderFriends(view);
        return viewHolder;
    }

    // after view holder has been created
    @Override
    public void onBindViewHolder(ViewHolderFriends holder, int position){
        // set items inside view holder
        String avatar = friends.get(position).getFriend().getProfilePic();
        int imageResource = StaticVariables.getProfilePic(avatar);
        holder.imageViewProfilePic.setImageResource(imageResource);
        holder.textViewFullName.setText(friends.get(position).getFriend().getFullName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get current selected item
                User userTo = friends.get(position).getFriend();
                String conversationId = checkExistingConversation(userTo);
                // if conversation does not exist
                if(conversationId.equals("") || conversationId.equals(null)) {
                    // send json object to request a new conversation
                    JSONObject send = new JSONObject();
                    JSONObject userToJSON = new JSONObject();
                    JSONObject userFromJSON = new JSONObject();
                    try {
                        send.put("flag", "conversation");
                        userToJSON.put("id", userTo.getUserID());
                        userToJSON.put("email", userTo.getEmail());
                        userToJSON.put("fullname", userTo.getFullName());
                        userToJSON.put("profilePic", userTo.getProfilePic());
                        send.put("userTo", userToJSON);
                        userFromJSON.put("id", MainActivity.getCurrentUser().getUserID());
                        userFromJSON.put("email", MainActivity.getCurrentUser().getEmail());
                        userFromJSON.put("fullname", MainActivity.getCurrentUser().getFullName());
                        send.put("userFrom", userFromJSON);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(WebSocketChatClient.getInstance() != null) {
                        WebSocketChatClient.getInstance().send(send.toString());
                    }
                // if conversation exists
                } else {
                    // set params for ChatActivity, open chat and load data
                    ChatActivity.setUserTo(userTo);
                    ChatActivity.setConversationId(conversationId);
                    ChatActivity.setMessages(setMessageList(conversationId));
                    Intent intent = new Intent(context, ChatActivity.class);
                    context.startActivity(intent);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    // return conversation id if conversation already exists
    public String checkExistingConversation(User userTo) {
        Iterator<Conversation> it = RecyclerViewAdapterConversations.getConversations().iterator();
        String conversationId = "";
        while(it.hasNext()) {
            Conversation tmp = it.next();
            if(tmp.getUserTo().getEmail().equals(userTo.getEmail())) {
                conversationId = tmp.getConversationId();
            }
        }
        return conversationId;
    }

    // set message list for conversation id
    public ArrayList<Message> setMessageList(String conversationId) {
        Iterator<Conversation> it = RecyclerViewAdapterConversations.getConversations().iterator();
        ArrayList<Message> messageList = new ArrayList<>();
        while(it.hasNext()) {
            Conversation tmp = it.next();
            if(tmp.getConversationId().equals(conversationId)) {
                messageList = tmp.getMessages();
            }
        }
        return messageList;
    }
}
