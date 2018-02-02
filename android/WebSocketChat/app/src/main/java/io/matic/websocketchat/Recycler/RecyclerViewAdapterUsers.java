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
import io.matic.websocketchat.Entity.Message;
import io.matic.websocketchat.Entity.User;
import io.matic.websocketchat.MainActivity;
import io.matic.websocketchat.R;
import io.matic.websocketchat.StaticVariables;
import io.matic.websocketchat.WebSocketChatClient;

/**
 * Created by Wut on 18 Jul 2017.
 */

// adapter for users that are online
public class RecyclerViewAdapterUsers extends RecyclerView.Adapter<RecyclerViewAdapterUsers.ViewHolderUsers> {

    private static ArrayList<User> usersOnline;
    private Context context;
    private View view;
    private ViewHolderUsers viewHolder;


    public static ArrayList<User> getUsersOnline() {
        return usersOnline;
    }

    public static void setUsersOnline(ArrayList<User> usersOnline) {
        RecyclerViewAdapterUsers.usersOnline = usersOnline;
    }

    public RecyclerViewAdapterUsers(Context context, ArrayList<User> usersOnline){

        this.usersOnline = usersOnline;
        this.context = context;
    }

    public interface ItemClickListener {
        void onItemClick(View v, int position);
    }

    // view holder is one item inside recyler view
    public static class ViewHolderUsers extends RecyclerView.ViewHolder implements ItemClickListener{

        public TextView textViewFullName;
        public ImageView imageViewProfilePic;
        public ItemClickListener itemClickListener;

        // set view holer
        public ViewHolderUsers(View v){
            super(v);
            textViewFullName = (TextView) v.findViewById(R.id.textViewFullName);
            imageViewProfilePic = (ImageView) v.findViewById(R.id.imageProfilePicture);
        }

        // set item click listener for one item
        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        // do something on item click
        @Override
        public void onItemClick(View v, int position) {
            this.itemClickListener.onItemClick(v, getLayoutPosition());
        }
    }

    /*
    * ADD or REMOVE data
    * */

    public void addUser(User user) {
        usersOnline.add(user);
        notifyDataSetChanged();
    }

    public void updateData(ArrayList<User> newUsers) {
        usersOnline.clear();
        usersOnline.addAll(newUsers);
        setUsersOnline(newUsers);
        notifyDataSetChanged();
    }

    public void removeUser(User user) {
        Iterator<User> it = usersOnline.iterator();
        while(it.hasNext()) {
            if(it.next().getEmail().equals(user.getEmail())) {
                it.remove();
            }
        }
        notifyDataSetChanged();
    }

    public void clearData() {
        if(usersOnline != null) {
            usersOnline.clear();
            notifyDataSetChanged();
        }
    }

    // create view holder
    @Override
    public ViewHolderUsers onCreateViewHolder(ViewGroup parent, int viewType){
        // inflate view holder
        view = LayoutInflater.from(context).inflate(R.layout.fragment_model_users ,parent,false);
        viewHolder = new ViewHolderUsers(view);
        return viewHolder;
    }

    // after view holder has been created
    @Override
    public void onBindViewHolder(ViewHolderUsers holder, int position){
        // set items inside view holder
        String avatar = usersOnline.get(position).getProfilePic();
        int imageResource = StaticVariables.getProfilePic(avatar);
        holder.imageViewProfilePic.setImageResource(imageResource);
        holder.textViewFullName.setText(usersOnline.get(position).getFullName());
        // on item click open conversation
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get selected user
                User userTo = usersOnline.get(position);
                // check if conversation with that user already ecists
                String conversationId = checkExistingConversation(userTo);
                // if conversation does not exist
                if(conversationId.equals("") || conversationId.equals(null)) {
                    // create json object to request new conversation and send it
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
                // if conversation already exists
                } else {
                    // set params for chat activity and open it
                    ChatActivity.setUserTo(userTo);
                    ChatActivity.setConversationId(conversationId);
                    ChatActivity.setMessages(setMessageList(ChatActivity.getConversationId()));
                    Intent intent = new Intent(context, ChatActivity.class);
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersOnline.size();
    }

    // check if conversation already exists
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

    // set messages for conversation
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
