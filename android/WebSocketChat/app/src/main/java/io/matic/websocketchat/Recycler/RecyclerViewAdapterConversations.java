package io.matic.websocketchat.Recycler;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.matic.websocketchat.ChatActivity;
import io.matic.websocketchat.Entity.Conversation;
import io.matic.websocketchat.Entity.Message;
import io.matic.websocketchat.Entity.User;
import io.matic.websocketchat.R;
import io.matic.websocketchat.StaticVariables;

/**
 * Created by Wut on 18 Jul 2017.
 */

// adapter for conversations
public class RecyclerViewAdapterConversations extends RecyclerView.Adapter<RecyclerViewAdapterConversations.ViewHolderConversations> {

    private static ArrayList<Conversation> conversations;
    private Context context;
    private View view;
    private ViewHolderConversations viewHolder;

    public static ArrayList<Conversation> getConversations() {
        return conversations;
    }

    public static void setConversations(ArrayList<Conversation> conversations) {
        RecyclerViewAdapterConversations.conversations = conversations;
    }

    public RecyclerViewAdapterConversations(Context context, ArrayList<Conversation> conversations){

        this.conversations = conversations;
        this.context = context;
    }

    public interface ItemClickListener {
        void onItemClick(View v, int position);
    }

    // view holder is one item in recycler view
    public static class ViewHolderConversations extends RecyclerView.ViewHolder implements ItemClickListener{

        public TextView textViewFullName, textViewLastMessage;
        public ImageView imageViewProfilePic;
        public ItemClickListener itemClickListener;

        // set view holder
        public ViewHolderConversations(View v){
            super(v);
            textViewFullName = (TextView) v.findViewById(R.id.textViewFullName);
            textViewLastMessage = (TextView) v.findViewById(R.id.textViewLastMessage);
            imageViewProfilePic = (ImageView) v.findViewById(R.id.imageProfilePicture);
        }

        // set item click listener
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

    public void addItem(Conversation conversation) {
        conversations.add(conversation);
        notifyDataSetChanged();
    }

    public void updateData(ArrayList<Conversation> newConversations) {
        conversations.clear();
        conversations.addAll(newConversations);
        setConversations(newConversations);
        notifyDataSetChanged();
    }

    public void clearData() {
        if(conversations != null) {
            conversations.clear();
            notifyDataSetChanged();
        }
    }

    @Override
    public ViewHolderConversations onCreateViewHolder(ViewGroup parent, int viewType){
        // inflate view holder
        view = LayoutInflater.from(context).inflate(R.layout.fragment_model_conversations,parent,false);
        viewHolder = new ViewHolderConversations(view);
        return viewHolder;
    }

    // after view holder has been created
    @Override
    public void onBindViewHolder(ViewHolderConversations holder, int position){
        // get current item
        Conversation conversation = conversations.get(position);
        List<Message> messages = conversation.getMessages();
        // set values for view holder items
        holder.textViewFullName.setText(conversation.getUserTo().getFullName());
        if(!messages.isEmpty()){
            holder.textViewLastMessage.setText(messages.get(messages.size() - 1).getMessage());
        } else {
            holder.textViewLastMessage.setText("");
        }
        // set image
        String avatar = conversation.getUserTo().getProfilePic();
        int imageResource = StaticVariables.getProfilePic(avatar);
        holder.imageViewProfilePic.setImageResource(imageResource);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // open chat activity for selected conversation
                User userTo = conversations.get(position).getUserTo();
                ChatActivity.setUserTo(userTo);
                ChatActivity.setConversationId(conversations.get(position).getConversationId());
                ChatActivity.setMessages(conversations.get(position).getMessages());
                Intent intent = new Intent(context, ChatActivity.class);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    // add message if conversation ids match
    public void checkExistingConversation(String conversationId, Message message) {
        Iterator<Conversation> it = RecyclerViewAdapterConversations.getConversations().iterator();
        Conversation tmp = null;
        while(it.hasNext()) {
            tmp = it.next();
            if(tmp.getConversationId().equals(conversationId)) {
                tmp.getMessages().add(message);
                notifyDataSetChanged();
            }
        }
    }
}
