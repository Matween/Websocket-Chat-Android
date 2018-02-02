package io.matic.websocketchat;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import io.matic.websocketchat.Adapter.FriendRequestsAdapter;
import io.matic.websocketchat.Entity.FriendRequest;
import io.matic.websocketchat.Entity.User;
import io.matic.websocketchat.Fragments.ConversationsFragment;
import io.matic.websocketchat.Fragments.FriendsFragment;
import io.matic.websocketchat.Fragments.MyFragmentPagerAdapter;
import io.matic.websocketchat.Fragments.UsersFragment;

public class MainActivity extends AppCompatActivity {

    public static MainActivity instance;
    private int[] imageResId = {
            R.drawable.ic_action_user,
            R.drawable.ic_action_message,
            R.drawable.ic_action_favorite
    };

    public static MainActivity getInstance() {
        return instance;
    }

    public static void setInstance(MainActivity instance) {
        MainActivity.instance = instance;
    }

    private TabLayout tabLayout;
    private static ViewPager viewPager;
    private static MyFragmentPagerAdapter pagerAdapter;

    public static ViewPager getViewPager() {
        return viewPager;
    }

    public static MyFragmentPagerAdapter getPagerAdapter() {
        return pagerAdapter;
    }

    public static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        MainActivity.currentUser = currentUser;
    }

    private static Toolbar toolbar;
    private static FriendRequestsAdapter friendRequestsAdapter;
    private static ArrayList<FriendRequest> friendRequests = new ArrayList<>();

    public static FriendRequestsAdapter getFriendRequestsAdapter() {
        return friendRequestsAdapter;
    }

    public static void setFriendRequestsAdapter(FriendRequestsAdapter friendRequestsAdapter) {
        MainActivity.friendRequestsAdapter = friendRequestsAdapter;
    }

    public static ArrayList<FriendRequest> getFriendRequests() {
        return friendRequests;
    }

    public static void setFriendRequests(ArrayList<FriendRequest> friendRequests) {
        MainActivity.friendRequests = friendRequests;
    }


    public static Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setInstance(this);
        setContentView(R.layout.activity_main);

        friendRequestsAdapter = new FriendRequestsAdapter(friendRequests);
        FriendRequestsActivity.setFriendRequestsAdapter(friendRequestsAdapter);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(currentUser.getFullName());
        toolbar.setNavigationIcon(StaticVariables.getProfilePic(currentUser.getProfilePic()));
        setSupportActionBar(toolbar);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        viewPager = (ViewPager) findViewById(R.id.container);
        tabLayout.setupWithViewPager(viewPager);
        this.addPages();

        setupTabIcons();


    }


    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(imageResId[0]);
        tabLayout.getTabAt(1).setIcon(imageResId[1]);
        tabLayout.getTabAt(2).setIcon(imageResId[2]);

    }

    private void addPages() {
        pagerAdapter = new MyFragmentPagerAdapter(this.getSupportFragmentManager());
        pagerAdapter.addPage(new UsersFragment());
        pagerAdapter.addPage(new ConversationsFragment());
        pagerAdapter.addPage(new FriendsFragment());

        viewPager.setAdapter(pagerAdapter);

    }

    // notification for friend request
    public void sendNotificationFriend(String userFrom) {
        // create a notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_action_notification)
                        .setContentTitle("New friend request from:")
                        .setContentText(userFrom);

        // show notification
        NotificationManager mNotificationManager = (NotificationManager)
        getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(001, mBuilder.build());
        // play sound
        playNotification();

    }

    // notification for new message
    public void sendNotificationMessage(String content, String userFrom) {
        // create a notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_action_message)
                        .setContentTitle("Message from " + userFrom)
                        .setContentText(content);

        // show notification
        NotificationManager mNotificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(001, mBuilder.build());
        // play sound
        playNotification();
    }

    // play notification sound
    public void playNotification() {
        try {
            // find default notification sound
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play(); // play
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // clear data on exit
    public void clearOnExit() {
        if(ChatActivity.getInstance() != null) {
            ChatActivity.getInstance().clearMessages();
        }
        if(FriendsFragment.getRecyclerViewAdapter() == null ||
                ConversationsFragment.getRecyclerViewAdapter() == null ||
                UsersFragment.getRecyclerViewAdapter() == null ||
                FriendRequestsActivity.getFriendRequestsAdapter() == null) {
            return;
        }
        FriendsFragment.getRecyclerViewAdapter().clearData();
        ConversationsFragment.getRecyclerViewAdapter().clearData();
        UsersFragment.getRecyclerViewAdapter().clearData();
        FriendRequestsActivity.getFriendRequestsAdapter().clearData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // do something on menu item click
        if (id == R.id.action_settings) {
            // open settings activity
            Intent intent = new Intent(MainActivity.getInstance(), SettingsActivity.class);
            startActivity(intent);
        } else if(id == R.id.action_friend_requests) {
            // open friend request activity
            Intent intent = new Intent(MainActivity.getInstance(), FriendRequestsActivity.class);
            FriendRequestsActivity.setFriendRequests(getFriendRequests());
            startActivity(intent);
        } else if(id == R.id.action_logout) {
            // login activity
            Intent intent = new Intent(MainActivity.getInstance(), LoginActivity.class);
            if(WebSocketChatClient.getInstance() != null) {
                // close websocket connection
                WebSocketChatClient.getInstance().close();
            }
            // clear data
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            clearOnExit();
            // start login activity
            startActivity(intent);
            // finish main activity
            finish();
        } else if(id == R.id.action_reconnect) {
            // reconnect to websocket
            if(WebSocketChatClient.getInstance() == null) {
                clearOnExit();
                WebSocketChatClient.reconnect(StaticVariables.getEmail(),
                        StaticVariables.getPassword());
            }
        }

        return super.onOptionsItemSelected(item);
    }

    // don't allow to go back to login window
    @Override
    public void onBackPressed() {
        return;
    }


}
