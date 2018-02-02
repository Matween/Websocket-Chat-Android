package io.matic.websocketchat.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import io.matic.websocketchat.Entity.Friend;
import io.matic.websocketchat.Entity.FriendRequest;
import io.matic.websocketchat.Entity.User;
import io.matic.websocketchat.Fragments.FriendsFragment;
import io.matic.websocketchat.FriendRequestsActivity;
import io.matic.websocketchat.MainActivity;
import io.matic.websocketchat.R;
import io.matic.websocketchat.StaticVariables;
import io.matic.websocketchat.WebSocketChatClient;

/**
 * Created by Matic on 19/09/2017.
 */

public class FriendRequestsAdapter extends RecyclerView.Adapter<FriendRequestsAdapter.MyViewHolder> {

    private static ArrayList<FriendRequest> friendRequests;

    // view holder is one item in recycler view
    public class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageViewProfilePic;
        public TextView textViewFullName;
        public Button buttonAccept;
        public Button buttonRefuse;

        // set view holder
        public MyViewHolder(View itemView) {
            super(itemView);
            imageViewProfilePic = (ImageView) itemView.findViewById(R.id.imageViewProfilePic);
            textViewFullName = (TextView) itemView.findViewById(R.id.textViewFullName);
            buttonAccept = (Button) itemView.findViewById(R.id.buttonAccept);
            buttonRefuse = (Button) itemView.findViewById(R.id.buttonRefuse);

        }
    }

    // set list for adapter
    public FriendRequestsAdapter(ArrayList<FriendRequest> friendRequests) {
        this.friendRequests = friendRequests;
    }

    /*
    * ADD or REMOVE data
    * */

    public void updateData(ArrayList<FriendRequest> newFriendRequests) {
        friendRequests.clear();
        friendRequests.addAll(newFriendRequests);
        notifyDataSetChanged();
    }

    public void addItem(FriendRequest newFriendRequest) {
        friendRequests.add(newFriendRequest);
        notifyDataSetChanged();
    }

    public void clearData() {
        friendRequests.clear();
        notifyDataSetChanged();
    }

    public void removeItem(FriendRequest oldFriendRequest) {
        Iterator<FriendRequest> iterator = friendRequests.iterator();
        while(iterator.hasNext()) {
            if(iterator.next().getFriend().getUserID()
                    .equals(oldFriendRequest.getFriend().getUserID())) {
                iterator.remove();
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public FriendRequestsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate view holder
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.model_friend_requests, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FriendRequestsAdapter.MyViewHolder holder, int position) {
        // get current item and set values
        FriendRequest friendRequest = friendRequests.get(position);
        String avatar = friendRequest.getFriend().getProfilePic();
        int imageResource = StaticVariables.getProfilePic(avatar);
        holder.imageViewProfilePic.setImageResource(imageResource);
        holder.textViewFullName.setText(friendRequest.getFriend().getFullName());
        // accept friend request
        holder.buttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // prepare json object
                JSONObject send = new JSONObject();
                JSONObject userTo = new JSONObject();
                JSONObject userFrom = new JSONObject();
                User userTemp = friendRequest.getFriend();
                try {
                    send.put("flag", "friendshipAccept");
                    userTo.put("id", MainActivity.getCurrentUser().getUserID());
                    userTo.put("email", MainActivity.getCurrentUser().getEmail());
                    userTo.put("fullname", MainActivity.getCurrentUser().getFullName());
                    send.put("userTo", userTo);
                    userFrom.put("id", userTemp.getUserID());
                    userFrom.put("email", userTemp.getEmail());
                    userFrom.put("fullname", userTemp.getFullName());
                    send.put("userFrom", userFrom);
                    send.put("friendshipId", friendRequest.getFriendshipID());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // send json object if connection is established
                if(WebSocketChatClient.getInstance() != null) {
                    WebSocketChatClient.getInstance().send(send.toString());
                } else {
                    Toast.makeText(FriendRequestsActivity.getInstance(),
                            "You are disconnected. Please reconnect!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // remove from friend requests and add to friend list
                removeItem(friendRequest);
                String friendshipId = friendRequest.getFriendshipID();
                FriendsFragment.getRecyclerViewAdapter().addFriend(new Friend(friendshipId, userTemp));
            }
        });
        // refuse friend request
        holder.buttonRefuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // prepare json object
                JSONObject send = new JSONObject();
                JSONObject userTo = new JSONObject();
                JSONObject userFrom = new JSONObject();
                User userTemp = friendRequest.getFriend();
                try {
                    send.put("flag", "friendshipStop");
                    userTo.put("id", MainActivity.getCurrentUser().getUserID());
                    userTo.put("email", MainActivity.getCurrentUser().getEmail());
                    userTo.put("fullname", MainActivity.getCurrentUser().getFullName());
                    send.put("userTo", userTo);
                    userFrom.put("id", userTemp.getUserID());
                    userFrom.put("email", userTemp.getEmail());
                    userFrom.put("fullname", userTemp.getFullName());
                    send.put("userFrom", userFrom);
                    send.put("friendshipId", friendRequest.getFriendshipID());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // send json object if connection is established
                if(WebSocketChatClient.getInstance() != null) {
                    WebSocketChatClient.getInstance().send(send.toString());
                } else {
                    Toast.makeText(FriendRequestsActivity.getInstance(),
                            "You are disconnected. Please reconnect!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // remove from friend requests
                removeItem(friendRequest);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendRequests.size();
    }
}
