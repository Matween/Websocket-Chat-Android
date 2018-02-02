package io.matic.websocketchat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import io.matic.websocketchat.Adapter.MessageListAdapter;
import io.matic.websocketchat.Entity.Friend;
import io.matic.websocketchat.Entity.Message;
import io.matic.websocketchat.Entity.User;
import io.matic.websocketchat.Recycler.RecyclerViewAdapterFriends;

public class ChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private static User userTo = new User();
    private static String conversationId = "";
    private static ChatActivity instance;
    private static ArrayList<Message> messages;
    private static MessageListAdapter messageAdapter;
    private ImageButton buttonSend;
    private EditText editTextMessage;
    private static boolean visible = false;

    public static User getUserTo() {
        return userTo;
    }

    public static void setUserTo(User userTo) {
        ChatActivity.userTo = userTo;
    }

    public static String getConversationId() {
        return conversationId;
    }

    public static void setConversationId(String conversationId) {
        ChatActivity.conversationId = conversationId;
    }

    public static ChatActivity getInstance() {
        return instance;
    }

    public static void setInstance(ChatActivity instance) {
        ChatActivity.instance = instance;
    }

    public static ArrayList<Message> getMessages() {
        return messages;
    }

    public static void setMessages(ArrayList<Message> messages) {
        ChatActivity.messages = messages;
    }

    public static MessageListAdapter getMessageAdapter() {
        return messageAdapter;
    }

    public static void setMessageAdapter(MessageListAdapter messageAdapter) {
        ChatActivity.messageAdapter = messageAdapter;
    }

    public static boolean isVisible() {
        return visible;
    }



    /*
    *check if chat activity is visible
     */

    @Override
    protected void onResume() {
        super.onResume();
        visible = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        visible = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        visible = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setInstance(this);
        setVisible(true);
        // set toolbar title
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(userTo.getFullName());
        setSupportActionBar(toolbar);

        // remove add friend icon if already friends
        if(checkIfFriend(getUserTo())) {
            toolbar.getMenu().removeItem(R.id.action_friend_add);
        }

        // display messsages
        ListViewCompat viewMessages = (ListViewCompat) findViewById(R.id.message_list);
        if(messages == null) {
            // show empty message list if no messages
            messages = new ArrayList<Message>();
        }
        messageAdapter = new MessageListAdapter(this, messages);
        viewMessages.setAdapter(messageAdapter);

        editTextMessage = (EditText) findViewById(R.id.editTextMessage);
        buttonSend = (ImageButton) findViewById(R.id.buttonSend);
        // send message on button click
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // prepare json object with user info and message
                JSONObject send = new JSONObject();
                JSONObject userFrom = new JSONObject();
                JSONObject userTo = new JSONObject();
                String time = timeToString(Calendar.getInstance());
                try {
                    userFrom.put("id", MainActivity.getCurrentUser().getUserID())
                            .put("email", MainActivity.getCurrentUser().getEmail())
                            .put("fullname", MainActivity.getCurrentUser().getFullName());
                    userTo.put("id", getUserTo().getUserID())
                            .put("email", getUserTo().getEmail())
                            .put("fullname", getUserTo().getFullName());
                    send.put("flag", "message")
                            .put("content", editTextMessage.getText().toString())
                            .put("userTo", userTo)
                            .put("userFrom", userFrom)
                            .put("convId", getConversationId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // add message to list
                Message newMessage = new Message(getConversationId(), editTextMessage.getText().toString(),
                        MainActivity.getCurrentUser().getFullName(), true, time);
                // send message if connection established
                if(WebSocketChatClient.getInstance() != null) {
                    WebSocketChatClient.getInstance().send(send.toString());
                    messages.add(newMessage);
                    getMessageAdapter().notifyDataSetChanged();
                } else {
                    // notfy to reconnect
                    Toast.makeText(getApplicationContext(),
                            "You are disconnected. Please reconnect!", Toast.LENGTH_SHORT).show();
                }
                editTextMessage.setText(""); // clear message box text
            }
        });

    }

    // convert calendar instance to string format
    private String timeToString(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(calendar.getTime());
        return time;
    }

    // clear messages from list
    public void clearMessages() {
        if(messages != null){
            messages.clear();
            getMessageAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        if(checkIfFriend(getUserTo())) {
            menu.removeItem(R.id.action_friend_add);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // send friend request on menu item selection
        if (id == R.id.action_friend_add) {
            JSONObject send = new JSONObject();
            JSONObject userFrom = new JSONObject();
            JSONObject userTo = new JSONObject();
            try {
                send.put("flag", "friendshipRequest");
                userFrom.put("id", MainActivity.getCurrentUser().getUserID());
                userFrom.put("email", MainActivity.getCurrentUser().getEmail());
                userFrom.put("fullname", MainActivity.getCurrentUser().getFullName());
                send.put("userFrom", userFrom);
                userTo.put("id", getUserTo().getUserID());
                userTo.put("email", getUserTo().getEmail());
                userTo.put("fullname", getUserTo().getFullName());
                send.put("userTo", userTo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            WebSocketChatClient.getInstance().send(send.toString());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // check if person on friend list
    private boolean checkIfFriend(User friend) {
        boolean exists = false;
        ArrayList<Friend> friends = RecyclerViewAdapterFriends.getFriends();
        if(friends == null) {
            return false;
        }
        Iterator<Friend> iterator = friends.iterator();
        if(iterator == null) {
            return false;
        }
        if(iterator.hasNext()) {
            if(iterator.next().getFriend().getUserID().equals(friend.getUserID())) {
                exists = true;
            }
        }
        return exists;
    }
}
