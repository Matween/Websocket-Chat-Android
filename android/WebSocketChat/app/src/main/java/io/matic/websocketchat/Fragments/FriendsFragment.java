package io.matic.websocketchat.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.matic.websocketchat.Entity.Friend;
import io.matic.websocketchat.MainActivity;
import io.matic.websocketchat.R;
import io.matic.websocketchat.Recycler.RecyclerViewAdapterFriends;
import io.matic.websocketchat.WebSocketChatClient;

/**
 * Created by Matic on 13/09/2017.
 */


// fragment is a view inside tabbed activity
public class FriendsFragment extends Fragment {

    private RecyclerView recyclerView;
    private static RecyclerViewAdapterFriends recyclerViewAdapter;
    private ArrayList<Friend> friends;

    // set adapter for recycler view
    public static RecyclerViewAdapterFriends getRecyclerViewAdapter() {
        return recyclerViewAdapter;
    }

    public FriendsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        // inflate view
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);
        // set empty list and fragment view
        friends = new ArrayList<>();
        recyclerView = rootView.findViewById(R.id.friendsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        recyclerViewAdapter = new RecyclerViewAdapterFriends(this.getActivity(), friends);
        recyclerView.setAdapter(recyclerViewAdapter);
        // return fragment view
        return rootView;
    }

    // after activity and views were created
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // prepare json object to request data of friendships and requests
        JSONObject requestFriendshipRequests = new JSONObject();
        JSONObject requestFriendships = new JSONObject();
        JSONObject userFrom = new JSONObject();
        try {
            requestFriendshipRequests.put("flag", "friendshipRequests");
            userFrom.put("id", MainActivity.getCurrentUser().getUserID());
            userFrom.put("email", MainActivity.getCurrentUser().getEmail());
            userFrom.put("fullname", MainActivity.getCurrentUser().getFullName());
            requestFriendshipRequests.put("userFrom", userFrom);
            requestFriendships.put("flag", "friendships");
            requestFriendships.put("userFrom", userFrom);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // send requests if connection is established
        if(WebSocketChatClient.getInstance() != null) {
            WebSocketChatClient.getInstance().send(requestFriendshipRequests.toString());
            WebSocketChatClient.getInstance().send(requestFriendships.toString());
        } else {
            // notify to reconnect
            Toast.makeText(MainActivity.getInstance().getApplicationContext(),
                    "You are disconnected. Please reconnect!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public String toString() {
        return "Friends";
    }

}
