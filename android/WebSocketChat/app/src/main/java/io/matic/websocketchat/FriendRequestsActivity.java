package io.matic.websocketchat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;

import io.matic.websocketchat.Adapter.FriendRequestsAdapter;
import io.matic.websocketchat.Entity.FriendRequest;

public class FriendRequestsActivity extends AppCompatActivity {

    private static ArrayList<FriendRequest> friendRequests;
    private RecyclerView recyclerView;
    private static FriendRequestsAdapter friendRequestsAdapter;
    private static FriendRequestsActivity instance;
    private static Toolbar toolbar;

    public static void setFriendRequests(ArrayList<FriendRequest> friendRequests) {
        FriendRequestsActivity.friendRequests = friendRequests;
    }

    public static FriendRequestsAdapter getFriendRequestsAdapter() {
        return friendRequestsAdapter;
    }
    public static void setFriendRequestsAdapter(FriendRequestsAdapter friendRequestsAdapter) {
        FriendRequestsActivity.friendRequestsAdapter = friendRequestsAdapter;
    }

    public static FriendRequestsActivity getInstance() {
        return instance;
    }
    public static void setInstance(FriendRequestsActivity instance) {
        FriendRequestsActivity.instance = instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);
        setInstance(this);
        // make an empty friend reqests list if there are no requests
        if(friendRequests == null) {
            friendRequests = new ArrayList<>();
        }
        // set toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Friend requests");
        setSupportActionBar(toolbar);

        // show friend requests on screen
        recyclerView = (RecyclerView) findViewById(R.id.friendRequestsRecyclerView);
        friendRequestsAdapter = new FriendRequestsAdapter(friendRequests);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(friendRequestsAdapter);

    }


}
