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

import io.matic.websocketchat.Entity.User;
import io.matic.websocketchat.MainActivity;
import io.matic.websocketchat.R;
import io.matic.websocketchat.Recycler.RecyclerViewAdapterUsers;
import io.matic.websocketchat.WebSocketChatClient;

/**
 * Created by Matic on 13/09/2017.
 */

// fragment is a view inside tabbed activity
public class UsersFragment extends Fragment {
    private static RecyclerView recyclerView;
    private static RecyclerViewAdapterUsers recyclerViewAdapter;
    private static ArrayList<User> users;

    // set adapter for recycler view
    public static RecyclerViewAdapterUsers getRecyclerViewAdapter() {
        return recyclerViewAdapter;
    }

    public UsersFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        // inflate view
        View rootView = inflater.inflate(R.layout.fragment_users, container, false);
        // set empty list and fragment view
        users = new ArrayList<>();
        recyclerView = (RecyclerView) rootView.findViewById(R.id.onlineRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        recyclerViewAdapter = new RecyclerViewAdapterUsers(this.getActivity(), users);
        recyclerView.setAdapter(recyclerViewAdapter);
        // return fragment view
        return rootView;
    }

    // after activity and view were created
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // prepare json object to request online users
        JSONObject requestOnlineUsers = new JSONObject();
        JSONObject userFrom = new JSONObject();
        try {
            requestOnlineUsers.put("flag", "requestOnlineUsers");
            userFrom.put("id", MainActivity.getCurrentUser().getUserID());
            userFrom.put("email", MainActivity.getCurrentUser().getEmail());
            userFrom.put("fullname", MainActivity.getCurrentUser().getFullName());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // send request if connection is established
        if(WebSocketChatClient.getInstance() != null) {
            WebSocketChatClient.getInstance().send(requestOnlineUsers.toString());
        } else {
            // notify to reconnect
            Toast.makeText(MainActivity.getInstance().getApplicationContext(),
                    "You are disconnected. Please reconnect!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public String toString() {
        return "Online";
    }



}
