package io.matic.websocketchat;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import io.matic.websocketchat.Entity.Conversation;
import io.matic.websocketchat.Entity.Friend;
import io.matic.websocketchat.Entity.FriendRequest;
import io.matic.websocketchat.Entity.Message;
import io.matic.websocketchat.Entity.User;
import io.matic.websocketchat.Fragments.ConversationsFragment;
import io.matic.websocketchat.Fragments.FriendsFragment;
import io.matic.websocketchat.Fragments.UsersFragment;

/**
 * Created by Matic on 07/09/2017.
 */

public class WebSocketChatClient extends WebSocketClient {

    private static WebSocketChatClient ourInstance = new WebSocketChatClient(StaticVariables.getURI(), new Draft_17());

    public WebSocketChatClient(URI serverUri, Draft draft) {
        super(serverUri, draft);
    }

    public static WebSocketChatClient getInstance() {
        return ourInstance;
    }

    public static void setInstance(WebSocketChatClient webSocketChatClient) {
        ourInstance = webSocketChatClient;
    }

    // reconnect
    public static void reconnect(String email, String password) {
        // set server uri
        URI serverUri = null;
        try {
            serverUri = new URI("ws://192.168.1.3:8080/websocketchatapi/chat?login=true&email=" + email +
                    "&pw=" + password);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if(serverUri != null) {
            // set new websocket instance and connect
            setInstance(new WebSocketChatClient(serverUri, new Draft_17()));
            ourInstance.connect();
        }
    }

    /*
    * TO CHECK if user logged in from register or login
    * */
    private boolean regActive;
    private boolean logActive;

    public void setRegActive(boolean regActive) {
        this.regActive = regActive;
    }

    public void setLogActive(boolean logActive) {
        this.logActive = logActive;
    }

    /*
    * called when connection is opened
    * */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.i("Websocket", "open");
    }

    /*
    * called when receiving a message
    * */
    @Override
    public void onMessage(String message) {
        Log.i("Websocket", message);

        // create a json object from message json string
        JSONObject msg = null;
        String flag = "";
        try {
            msg = new JSONObject(message);
            flag = msg.get("flag").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (flag.equals("successLogin")) {
            // set strings from json message
            String id = "", email = "", fullname = "", profilePic = "";
            try {
                id = msg.getString("id");
                email = msg.getString("email");
                fullname = msg.getString("fullname");
                profilePic = msg.getString("profilePic");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // set current logged in user
            User currentUser = new User();
            currentUser.setUserID(id);
            currentUser.setEmail(email);
            currentUser.setFullName(fullname);
            currentUser.setProfilePic(profilePic);
            MainActivity.setCurrentUser(currentUser);
            // open main activity
            Intent intent = new Intent(LoginActivity.getInstance(), MainActivity.class);
            LoginActivity.getInstance().startActivity(intent);
            Log.d("Current user", MainActivity.getCurrentUser().getFullName());
            logActive = false; // set login active to false on new activity

        } else if (flag.equals("failLogin")) {
            LoginActivity.getInstance().runOnUiThread(() -> {
                // show toast -> user passed in wrong credentials
                LoginActivity.getInstance().getProgressBar().setVisibility(View.GONE);
                LoginActivity.getInstance().enableControls();
                Toast.makeText(LoginActivity.getInstance().getApplicationContext(), "Wrong credentials!", Toast.LENGTH_SHORT).show();
            });
        } else if (flag.equals("successRegister")) {
            String id = "", email = "", fullname = "", profilePic = "";
            try {
                id = msg.getString("id");
                email = msg.getString("email");
                fullname = msg.getString("fullname");
                profilePic = msg.getString("profilePic");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // set current logged in user
            User currentUser = new User();
            currentUser.setUserID(id);
            currentUser.setEmail(email);
            currentUser.setFullName(fullname);
            currentUser.setProfilePic(profilePic);
            MainActivity.setCurrentUser(currentUser);
            // start main activity
            Intent intent = new Intent(RegisterActivity.getInstance(), MainActivity.class);
            RegisterActivity.getInstance().startActivity(intent);
            Log.i("Current user", MainActivity.getCurrentUser().getFullName());
            regActive = false; // set register activity to false
        } else if(flag.equals("failRegister")) {
            // error on server side -> notify via toast
            RegisterActivity.getInstance().runOnUiThread(() -> {
                RegisterActivity.getInstance().getProgressBar().setVisibility(View.INVISIBLE);
                RegisterActivity.getInstance().enableControls();
                Toast.makeText(RegisterActivity.getInstance().getApplicationContext(),
                        "Ooops, something went wrong. Try again later!", Toast.LENGTH_SHORT).show();
            });
        } else if (flag.equals("onlineUser")) {
            // new user logged in -> get that user's info
            JSONObject userOnline = null;
            String id = null, email = null, fullname = null, profilePic = null;
            try {
                userOnline = msg.getJSONObject("user");
                id = userOnline.getString("id");
                email = userOnline.getString("email");
                fullname = userOnline.getString("fullname");
                profilePic = userOnline.getString("profilePic");
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
            // define new user
            User online = new User(id,fullname, email, profilePic);

            // get a handler that can be used to post to the main thread
            Handler mainHandler = new Handler(MainActivity.getInstance().getApplicationContext().getMainLooper());
            Runnable myRunnable = () -> {
                // add user to online list
                if(!online.getUserID().equals(MainActivity.getCurrentUser().getUserID())) {
                    UsersFragment.getRecyclerViewAdapter().addUser(online);
                }
            };
            mainHandler.post(myRunnable); // post to main thread

        } else if (flag.equals("message")) {
            // set message and sender info
            String content = null, userFromName = null, time = null,
                    convId = null,  userFromId = null;
            JSONObject userFrom = null;
            try {
                content = msg.getString("content");
                userFrom = msg.getJSONObject("userFrom");
                userFromName = userFrom.getString("fullname");
                time = msg.getString("time");
                convId = msg.getString("convId");
                userFromId = userFrom.getString("id");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // define new message
            Message newMessage = new Message(convId, content, userFromName, false, time);
            User chatUser = ChatActivity.getUserTo();
            final String conversationId = convId;
            // check if the right chat window is opened
            if (ChatActivity.isVisible() && chatUser.getUserID().equals(userFromId)) {
                Log.i("ChatActivity name", ChatActivity.getInstance().getTitle().toString());
                // run on UI thread
                ChatActivity.getInstance().runOnUiThread(() -> {
                    // add message to message list
                    ChatActivity.setConversationId(conversationId);
                    ChatActivity.getMessages().add(newMessage);
                    ChatActivity.getMessageAdapter().notifyDataSetChanged();
                });
            } else {
                // if the right chat windows is not active
                // get main thread
                Handler mainHandler = new Handler(MainActivity.getInstance().getApplicationContext().getMainLooper());
                Runnable mainRunnable = () -> {
                    // send notification and add message to conversation
                    MainActivity.getInstance().sendNotificationMessage(newMessage.getMessage(), newMessage.getFrom());
                    ConversationsFragment.getRecyclerViewAdapter().checkExistingConversation(conversationId, newMessage);
                };
                mainHandler.post(mainRunnable); // post to main thread
            }
        } else if (flag.equals("conversation")) {
            // parse json to get data for conversation
            String convId = "", userToId = "", userToEmail = "", userToName = "",
                    profilePic = "";
            JSONObject userFrom = null, userTo = null;
            try {
                convId = msg.getString("convId");
                userFrom = msg.getJSONObject("userFrom");
                userTo = msg.getJSONObject("userTo");
                userToId = userTo.getString("id");
                userToEmail = userTo.getString("email");
                userToName = userTo.getString("fullname");
                profilePic = userTo.getString("profilePic");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // set conversation details
            ChatActivity.setConversationId(convId);
            User userToC = new User(userToId, userToName, userToEmail, profilePic);
            final String convIdC = convId;
            ChatActivity.setUserTo(userToC);
            // find main thread
            Handler mainHandler = new Handler(MainActivity.getInstance().getApplicationContext().getMainLooper());
            Runnable mainRunnable = () ->
                    // add conversation to conversations list
                    ConversationsFragment.getRecyclerViewAdapter().addItem(new Conversation(convIdC, userToC, new ArrayList<Message>()));
            mainHandler.post(mainRunnable); // post runnable to main thread
            // start chat activity
            Intent intent = new Intent(MainActivity.getInstance(), ChatActivity.class);
            MainActivity.getInstance().startActivity(intent);
        } else if (flag.equals("offline")) {
            // get the info of offline user
            JSONObject userLeft = null;
            String id = "", email = "", fullname = "";
            try {
                userLeft = msg.getJSONObject("userLeft");
                id = userLeft.getString("id");
                email = userLeft.getString("email");
                fullname = userLeft.getString("fullname");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            User offlineNow = new User();
            offlineNow.setUserID(id);
            offlineNow.setEmail(email);
            offlineNow.setFullName(fullname);
            // get main thread
            Handler mainHandler = new Handler(MainActivity.getInstance().getApplicationContext().getMainLooper());
            // remove user from online list
            Runnable myRunnable = () -> UsersFragment.getRecyclerViewAdapter().removeUser(offlineNow);
            mainHandler.post(myRunnable); // post to main thread
        } else if (flag.equals("requestedConversation")) {
            // get info of conversation
            String convId = null, userId = null, fullname = null,
                    email = null, profilePic = null;
            try {
                convId = msg.getString("convId");
                userId = msg.getString("userId");
                fullname = msg.getString("fullname");
                email = msg.getString("email");
                profilePic = msg.getString("profilePic");
            } catch(JSONException e) {
                e.printStackTrace();
                return;
            }
            // set conversation
            Conversation conversation = new Conversation(convId, new User(userId, fullname, email, profilePic));
            conversation.setMessages(new ArrayList<Message>());
            // get main thread
            Handler mainHandler = new Handler(MainActivity.getInstance().getApplicationContext().getMainLooper());
            // add conversation to conversations list
            Runnable myRunnable = () -> ConversationsFragment.getRecyclerViewAdapter().addItem(conversation);
            mainHandler.post(myRunnable); // post to main thread

        } else if(flag.equals("requestedMessage")) {
            // get message info
            String id = null, fullname = null, email = null,
                    convId = null, content = null, sent = null;
            JSONObject userFrom = null;
            try {
                userFrom = msg.getJSONObject("userFrom");
                convId = msg.getString("convId");
                id = userFrom.getString("id");
                fullname = userFrom.getString("fullname");
                email = userFrom.getString("email");
                content = msg.getString("message");
                sent = msg.getString("sent");
            } catch(JSONException e) {
                e.printStackTrace();
                return;
            }
            boolean self = false;
            // if user sent the message
            if(email.equals(MainActivity.getCurrentUser().getEmail())) {
                self = true;
            }
            // define message
            Message message1 = new Message(convId, content, fullname, self, sent);
            // get main thread
            Handler mainHandler = new Handler(MainActivity.getInstance().getApplicationContext().getMainLooper());
            // check to which conversation the message is from and add it
            Runnable myRunnable = () ->
                    ConversationsFragment.getRecyclerViewAdapter()
                            .checkExistingConversation(message1.getConversationId(), message1);
            mainHandler.post(myRunnable); // post to main thread
        } else if (flag.equals("friendship")) {
            // parse the friendship info
            JSONObject jsonFriend = null;
            String id = null, email = null, fullname = null,
                    friendshipId = null, profilePic = null;
            try {
                jsonFriend = msg.getJSONObject("friend");
                friendshipId = msg.getString("friendshipId");
                id = jsonFriend.getString("id");
                fullname = jsonFriend.getString("fullname");
                email = jsonFriend.getString("email");
                profilePic = jsonFriend.getString("profilePic");
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
            // define new friend
            Friend friend = new Friend(friendshipId, new User(id, fullname, email, profilePic));
            Log.d("Friend", friend.getFriend().getFullName());

            // get main thread
            Handler mainHandler = new Handler(MainActivity.getInstance().getApplicationContext().getMainLooper());
            // add friend to friend list
            Runnable myRunnable = () -> FriendsFragment.getRecyclerViewAdapter().addFriend(friend);
            mainHandler.post(myRunnable); // post to main thread
        } else if (flag.equals("newFriendRequest")) {
            // get friend request info
            JSONObject userFrom = null;
            String friendshipId = "";
            String id = "", email = "", fullname = "", profilePic = "";
            try {
                friendshipId = msg.getString("friendshipId");
                userFrom = msg.getJSONObject("userFrom");
                id = userFrom.getString("id");
                email = userFrom.getString("email");
                fullname = userFrom.getString("fullname");
                profilePic = userFrom.getString("profilePic");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // define new friend request
            User user = new User(id, fullname, email, profilePic);
            FriendRequest friendRequest = new FriendRequest(friendshipId, user);
            MainActivity.getFriendRequests().add(friendRequest);
            // get main thread
            Handler handler = new Handler(MainActivity.getInstance().getApplicationContext().getMainLooper());
            // send notification of friend request
            Runnable mainRunnable = () -> MainActivity.getInstance().sendNotificationFriend(user.getFullName());
            handler.post(mainRunnable); // post to main thread
        } else if (flag.equals("friendshipRequest")) {
            // parse already seen friend requests
            JSONObject jsonRequest = null;
            String friendshipId = null, userId = null,
                    email = null, fullname = null, profilePic = null;
            try {
                jsonRequest = msg.getJSONObject("friend1");
                friendshipId = msg.getString("friendshipId");
                userId = jsonRequest.getString("id");
                email = jsonRequest.getString("email");
                fullname = jsonRequest.getString("fullname");
                profilePic = jsonRequest.getString("profilePic");
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
            // define friend request and add it to the list
            FriendRequest friendRequest = new FriendRequest(friendshipId, new User(userId, fullname, email, profilePic));
            MainActivity.getFriendRequests().add(friendRequest);

        } else if(flag.equals("failChangePassword")) {
            // get reason behind change password failure
            String reason = null;
            try {
                reason = msg.getString("reason");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // act accordingly
            // run on UI thread to show toast
            if(reason.equals("serverError")) {
                SettingsActivity.getInstance().runOnUiThread(() -> {
                    SettingsActivity.getInstance().getProgressBar().setVisibility(View.INVISIBLE);
                    SettingsActivity.getInstance().enableControls();
                    Toast.makeText(SettingsActivity.getInstance().getApplicationContext(), "There was an error!", Toast.LENGTH_SHORT).show();
                });
            } else if(reason.equals("wrongCredentials")) {
                SettingsActivity.getInstance().runOnUiThread(() -> {
                    SettingsActivity.getInstance().getProgressBar().setVisibility(View.INVISIBLE);
                    SettingsActivity.getInstance().enableControls();
                    Toast.makeText(SettingsActivity.getInstance().getApplicationContext(), "Wrong credentials!", Toast.LENGTH_SHORT).show();
                });
            }
        } else if(flag.equals("successChangePassword")) {
            SettingsActivity.getInstance().runOnUiThread(() -> {
                SettingsActivity.getInstance().getProgressBar().setVisibility(View.INVISIBLE);
                SettingsActivity.getInstance().enableControls();
                Toast.makeText(SettingsActivity.getInstance().getApplicationContext(), "Password changed!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    /*
     * when websocket connection is closed
    */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.i("Websocket", "close " + reason);
        // set instance of websocket to null
        setInstance(null);
        // failed to connect from login activity
        if(logActive) {
            // ron on UI thread to show toast
            LoginActivity.getInstance().runOnUiThread(() -> {
                LoginActivity.getInstance().getProgressBar().setVisibility(View.INVISIBLE);
                Toast.makeText(LoginActivity.getInstance().getApplicationContext(), "Failed to connect!", Toast.LENGTH_SHORT).show();
                // enable buttons and text boxes
                LoginActivity.getInstance().enableControls();
                logActive = false;
            });
        }
        // failed to connect from register activity
        if(regActive) {
            // run on UI thread to show toast
            RegisterActivity.getInstance().runOnUiThread(() -> {
                RegisterActivity.getInstance().getProgressBar().setVisibility(View.INVISIBLE);
                Toast.makeText(RegisterActivity.getInstance().getApplicationContext(), "Failed to connect!", Toast.LENGTH_SHORT).show();
                // enable buttons and text boxes
                RegisterActivity.getInstance().enableControls();
                regActive = false;
            });
        }

    }

    /*
    * when an error occurs
    * */
    @Override
    public void onError(Exception ex) {
        Log.i("Websocket error", ex.getMessage());
    }
}
