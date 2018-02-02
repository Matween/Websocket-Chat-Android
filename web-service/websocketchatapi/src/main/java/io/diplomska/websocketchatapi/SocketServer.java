package io.diplomska.websocketchatapi;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import io.diplomska.websocketchatapi.db.HibConfig;
import io.diplomska.websocketchatapi.db.User;

@ServerEndpoint("/chat")
public class SocketServer {

	// all live sessions
	private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());

	// Mapping between session and unique person email
	private static final BiMap<String, Session> emailSessionPair = Maps.synchronizedBiMap(HashBiMap.<String, Session>create());

	// working with database -> Hibernate
	private HibConfig hibConfig = new HibConfig();
    
	// Getting query params
	public static Map<String, String> getQueryMap(String query) {
		Map<String, String> map = new HashMap<String, String>();
		if (query != null) {
			String[] params = query.split("&");
			for (String param : params) {
				String[] keyvalue = param.split("=");
				map.put(keyvalue[0], keyvalue[1]);
			}
		}
		return map;
	}

	/**
	 * Called when a socket connection is opened
	 */
	@OnOpen
	public void onOpen(Session session) {

		System.out.println(session.getId() + " has opened a connection");
	
		// get query params
		Map<String, String> queryParams = getQueryMap(session.getQueryString());
		
		// login
		if (queryParams.containsKey("login") 
				&& queryParams.containsKey("email") 
				&& queryParams.containsKey("pw")) {
			String email = null;
			String pw = null;
			try {
				// get the param value and decode it
				email = URLDecoder.decode(queryParams.get("email"), "UTF-8");
				pw = URLDecoder.decode(queryParams.get("pw"), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			if(email == null || pw == null) {
				JSONObject jsonFail = new JSONObject();
				jsonFail.put("flag", "serverError");
				sendMessageToSession(session, jsonFail.toString());
				return;
			}
			User user = hibConfig.loginUser(email, pw);
			if(user != null) {
				// Mapping client name and session id
				emailSessionPair.put(email, session);
				// Adding session to session list
				sessions.add(session);
				JSONObject jsonUser = new JSONObject();
				jsonUser.put("flag", "successLogin");
				jsonUser.put("id", user.getUserID());
				jsonUser.put("email", user.getEmail());
				jsonUser.put("fullname", user.getFullname());
				jsonUser.put("profilePic", user.getProfilePic());
				sendMessageToSession(session, jsonUser.toString());
				// notify all clients of new login
				sendOnlineList(null);
			} else {
				JSONObject jsonFail = new JSONObject();
				jsonFail.put("flag", "failLogin");
				// send message of error and close the session
				sendMessageToSession(session, jsonFail.toString());
				closeSession(session);
			}
		// register
		} else if(queryParams.containsKey("register") 
				&& queryParams.containsKey("fullname")
				&& queryParams.containsKey("email") 
				&& queryParams.containsKey("pw")) {
			String fullname = null;
			String email = null;
			String pw = null;
			try {
				// get the param value and decode it
				fullname = URLDecoder.decode(queryParams.get("fullname"), "UTF-8");
				email = URLDecoder.decode(queryParams.get("email"), "UTF-8");
				pw = URLDecoder.decode(queryParams.get("pw"), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			if(email == null || pw == null || fullname == null) {
				JSONObject jsonFail = new JSONObject();
				jsonFail.put("flag", "serverError");
				sendMessageToSession(session, jsonFail.toString());
				return;
			}
			boolean success = hibConfig.registerUser(fullname, email, pw);
			if(success) {
				// Mapping client name and session id
				emailSessionPair.put(email, session);
				// Adding session to session list
				sessions.add(session);
				// login after successful register 
				User registered = hibConfig.getUserByEmail(email);
				JSONObject jsonRegistered = new JSONObject();
				try {
					jsonRegistered.put("flag", "successRegister");
					jsonRegistered.put("id", registered.getUserID());
					jsonRegistered.put("email", registered.getEmail());
					jsonRegistered.put("fullname", registered.getFullname());
					jsonRegistered.put("profilePic", registered.getProfilePic());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				// notify successful register to client that just connected
				sendMessageToSession(session, jsonRegistered.toString());
				// notify other users of new login
				sendOnlineList(null);
			} else {
				JSONObject jsonFail = new JSONObject();
				jsonFail.put("flag", "failRegister");
				// notify failed register and close the session
				sendMessageToSession(session, jsonFail.toString());
				closeSession(session);
			}
		} else {
			// insufficient data
			JSONObject jsonFail = new JSONObject();
			jsonFail.put("flag", "insufficent");
			// send message of insufficient data and close the session
			sendMessageToSession(session, jsonFail.toString());
			closeSession(session);
		}

	}

	/**
	 * method called when new message received from any client
	 * 
	 * @param message -> JSON message from client
	 */
	@OnMessage
	public void onMessage(String message, Session session) {

		System.out.println("Message from " + session.getId() + ": " + message);

		String flag = null;
		JSONObject jObj = null;
		// Parsing the json and getting flag
		try {
			jObj = new JSONObject(message);
			flag = jObj.getString("flag");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if(flag.equals("requestOnlineUsers")) {
			// send the list of all online users
			sendOnlineList(session);
		} else if(flag.equals("message")) {
			// parse message and users for the message
			JSONObject userTo = null, userFrom = null;
			String msg = null, convId = null;
			try {
				userTo = jObj.getJSONObject("userTo");
				userFrom = jObj.getJSONObject("userFrom");
				msg = jObj.getString("content");
				convId = jObj.getString("convId");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			// prepare to send message
			JSONObject sendTo = new JSONObject();
			sendTo.put("userTo", userTo);
			sendTo.put("userFrom", userFrom);
			sendTo.put("content", msg);
			if(Strings.isNullOrEmpty(convId) || Strings.isNullOrEmpty(msg)
					|| userTo == null || userFrom == null) {
				sendTo.put("flag", "messageError");
				sendMessageToSession(session, sendTo.toString());
				return;
			}
			Calendar now = Calendar.getInstance();
			// insert message to database
			boolean insertMsg = hibConfig.insertMessage(msg, now, convId, userFrom.getString("id"));
			if(insertMsg) {
				Session sessionTo = emailSessionPair.get(userTo.getString("email"));
				sendTo.put("flag", "message");
				sendTo.put("time", parseTime(now));
				sendTo.put("convId", convId);
				if(sessionTo != null) {
					// send message to intended user if available
					sendMessageToSession(sessionTo, sendTo.toString());
				}
			} else {
				sendTo.put("flag", "messageError");
				sendMessageToSession(session, sendTo.toString());
			}
		} else if(flag.equals("conversation")) {
			// parse message and users for the conversation
			JSONObject userTo = null, userFrom = null;
			String userToId = null, userFromId = null;
			try {
				userTo = jObj.getJSONObject("userTo");
				userFrom = jObj.getJSONObject("userFrom");
				userToId = userTo.getString("id");
				userFromId = userFrom.getString("id");
			} catch(JSONException e) {
				e.printStackTrace();
			}
			// prepare to send conversation info
			JSONObject sendTo = new JSONObject();
			sendTo.put("userTo", userTo);
			sendTo.put("userFrom", userFrom);
			// set users in conversations
			String[] userIds = {userToId, userFromId};
			// insert conversation for intended users
			int insertConv = hibConfig.insertConversation(userIds);
			if(insertConv != 0) {
				// on successful insert send conversation info to session
				sendTo.put("flag", "conversation");
				sendTo.put("convId", insertConv);
				sendMessageToSession(session, sendTo.toString());
				if(emailSessionPair.containsKey(userTo.getString("email"))) {
					sendTo = new JSONObject();
					User intialUser = hibConfig.getUserByEmail(userFrom.getString("email"));
					if(intialUser == null)
						return;
					sendTo.put("flag", "requestedConversation");
					sendTo.put("convId", insertConv);
					sendTo.put("userId", intialUser.getUserID());
					sendTo.put("fullname", intialUser.getFullname());
					sendTo.put("email", intialUser.getEmail());
					sendTo.put("profilePic", intialUser.getProfilePic());
					sendMessageToSession(emailSessionPair.get(userTo.getString("email")), sendTo.toString());
				}
			} else {
				// on not successful insert send error message
				sendTo.put("flag", "conversationFail");
			}
		} else if(flag.equals("requestActiveConversations")) {
			String emailFrom = emailSessionPair.inverse().get(session);
			List<Object[]> requestedConversations = hibConfig.getConversationsByEmail(emailFrom);
			for(Object[] requestedConversation : requestedConversations) {
				String email = requestedConversation[3].toString();
				// email from the request must be different from the conversation
				// otherwise it returns two of the same conversations for each person
				if(!email.equals(emailFrom)) {
					// prepare json object to send
					JSONObject conversation = new JSONObject();
					String convId = requestedConversation[0].toString();
					conversation.put("flag", "requestedConversation");
					conversation.put("convId", convId);
					conversation.put("userId", requestedConversation[1]);
					conversation.put("fullname", requestedConversation[2]);
					conversation.put("email", email);
					conversation.put("profilePic", requestedConversation[4]);
					// send conversation info and wait a bit
					sendMessageToSession(session, conversation.toString());
					sleep();
					// get messages for conversation and send them one by one
					List<Object[]> messages = hibConfig.getMessagesByConversationId(convId);
					for(Object[] messag : messages) {
						JSONObject usrFrom = new JSONObject();
						usrFrom.put("id", messag[0]);
						usrFrom.put("email", messag[1]);
						usrFrom.put("fullname", messag[2]);
						JSONObject mesg = new JSONObject();
						mesg.put("flag", "requestedMessage");
						mesg.put("convId", convId);
						mesg.put("userFrom", usrFrom);
						mesg.put("message", messag[3]);
						mesg.put("sent", messag[4]);
						// send message from the conversation and wait a bit
						sendMessageToSession(session, mesg.toString());
						sleep();
					}
				}
			}
		} else if(flag.equals("friendshipRequest")) {
			JSONObject userFrom = null, userTo = null;
			String userFromId = null, userToId = null, userToEmail = null;
			try {
				userFrom = jObj.getJSONObject("userFrom");
				userTo = jObj.getJSONObject("userTo");
				userFromId = userFrom.getString("id");
				userToId = userTo.getString("id");
				userToEmail = userTo.getString("email");
			} catch(JSONException e) {
				e.printStackTrace();
			}
			if(userFromId != null && userToId != null) {
				// insert friendship and return friendshipID
				String friendshipId = hibConfig.insertFriendship(userFromId, userToId);
				if(friendshipId != null) {
					// prepare friendship json object
					JSONObject sendTo = new JSONObject();
					sendTo.put("flag", "newFriendRequest");
					sendTo.put("userFrom", userFrom);
					sendTo.put("friendshipId", friendshipId);
					if(emailSessionPair.containsKey(userToEmail)) {
						// get session info if it contains the user
						Session sessionTo = emailSessionPair.get(userToEmail);
						// send friend request to intended user
						sendMessageToSession(sessionTo, sendTo.toString());
					}
				}
			}
		} else if(flag.equals("friendshipAccept")) {
			JSONObject userFrom = null, userTo = null;
			String userToEmail = null, friendshipId = null;
			try {
				userFrom = jObj.getJSONObject("userFrom");
				userTo = jObj.getJSONObject("userTo");
				userToEmail = userTo.getString("email");
				friendshipId = jObj.getString("friendshipId");
			} catch(JSONException e) {
				e.printStackTrace();
				return;
			}
			
			// update friendship and set started to true
			int update = hibConfig.updateFriendshipById(friendshipId, true);
			if(update == 0) {
				return;
			}
			
			// prepare JSON object to notify of a new friend
			JSONObject sendTo = new JSONObject();
			sendTo.put("flag", "friendship");
			sendTo.put("friendshipId", friendshipId);
			sendTo.put("friend", userFrom);
			sendMessageToSession(session, sendTo.toString());
			if(emailSessionPair.containsKey(userToEmail)) {
				Session sessionTo = emailSessionPair.get(userToEmail);
				sendMessageToSession(sessionTo, sendTo.toString());
			}
			
		} else if(flag.equals("friendshipStop")) {
			String friendshipId = null;
			try {
				friendshipId = jObj.getString("friendshipId");
			} catch(JSONException e) {
				e.printStackTrace();
				return;
			}
			// update friendship and set started to false -> friendship ended
			hibConfig.updateFriendshipById(friendshipId, false);
			
		} else if(flag.equals("friendshipRequests")) {
			JSONObject userFrom = null;
			String userFromId = null;
			try {
				userFrom = jObj.getJSONObject("userFrom");
				userFromId = userFrom.getString("id");
			} catch(JSONException e) {
				e.printStackTrace();
			}
			// get friend requests intended for userFrom
			List<Object[]> friendshipsByFrom = hibConfig.getFriendshipRequests(userFromId);
			for(Object[] friend : friendshipsByFrom) {
				// prepare json object for friend request
				JSONObject jsonFriendship = new JSONObject();
				JSONObject jsonFriend = new JSONObject();
				jsonFriendship.put("flag", "friendshipRequest");
				jsonFriendship.put("friendshipId", friend[0]);
				jsonFriend.put("id", friend[1]);
				jsonFriend.put("email", friend[2]);
				jsonFriend.put("fullname", friend[3]);
				jsonFriend.put("profilePic", friend[4]);
				jsonFriendship.put("friend1", jsonFriend);
				jsonFriendship.put("friend2", userFrom);
				// send pending friend request
				sendMessageToSession(session, jsonFriendship.toString());
			}
		} else if(flag.equals("friendships")) {
			JSONObject userFrom = null;
			String userFromId = null;
			try {
				userFrom = jObj.getJSONObject("userFrom");
				userFromId = userFrom.getString("id");
			} catch(JSONException e) {
				e.printStackTrace();
				return;
			}
			// get friends
			List<Object[]> friends = hibConfig.getFriendships(userFromId);
			// do nothing if friends list is empty
			if(friends.isEmpty())
				return;
			// for each friend in friends (list of friends)
			for(Object[] friend : friends) {
				JSONObject sendTo = new JSONObject();
				JSONObject jsonFriend = new JSONObject();
				sendTo.put("flag", "friendship");
				sendTo.put("friendshipId", friend[0]);
				jsonFriend.put("id", friend[1]);
				jsonFriend.put("email", friend[2]);
				jsonFriend.put("fullname", friend[3]);
				jsonFriend.put("profilePic", friend[4]);
				sendTo.put("friend", jsonFriend);
				// send friend info
				sendMessageToSession(session, sendTo.toString());
				sleep();
			}
		} else if(flag.equals("changePassword")) {
			JSONObject user = null;
			String newPass = null, userId = null, oldPass = null;
			try {
				user = jObj.getJSONObject("user");
				userId = user.getString("id");
				newPass = user.getString("newPassword");
				oldPass = user.getString("oldPassword");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			// checkk if passwords are null or empty
			if(Strings.isNullOrEmpty(userId) ||
					Strings.isNullOrEmpty(newPass) || 
					Strings.isNullOrEmpty(oldPass)) {
				JSONObject sendTo = new JSONObject();
				// form error json object and send it
				sendTo.put("flag", "failChangePassword");
				sendTo.put("reason", "serverError");
				sendMessageToSession(session, sendTo.toString());
				return;
			}
			
			// get user
			User requestUser = hibConfig.getUserById(userId);
			// check if old passwords match
			String requestPassword = requestUser.getPassword();
			if(!requestPassword.equals(oldPass)) {
				JSONObject sendTo = new JSONObject();
				// form error json object and send it
				sendTo.put("flag", "failChangePassword");
				sendTo.put("reason", "wrongCredentials");
				sendMessageToSession(session, sendTo.toString());
				return;
			}
			
			// update password
			int update = hibConfig.updatePassword(userId, newPass);
			if(update == 1) {
				// on success send message of success
				JSONObject sendTo = new JSONObject();
				sendTo.put("flag", "successChangePassword");
				sendMessageToSession(session, sendTo.toString());
			} else {
				// on failure send failure message
				JSONObject sendTo = new JSONObject();
				sendTo.put("flag", "failChangePassword");
				sendTo.put("reason", "serverError");
				sendMessageToSession(session, sendTo.toString());
			}
		} else if(flag.equals("newImage")) {
			JSONObject userFrom = null;
			String image = null, userId = null;
			try {
				userFrom = jObj.getJSONObject("userFrom");
				userId = userFrom.getString("id");
				image = jObj.getString("image");
			} catch(JSONException e) {
				e.printStackTrace();
			}
			if(Strings.isNullOrEmpty(userId) || Strings.isNullOrEmpty(image)) {
				// if userId or image or null or empty send failure message
				JSONObject sendTo = new JSONObject();
				sendTo.put("flag", "imageSetFail");
				sendMessageToSession(session, sendTo.toString());
				System.out.println("Image was not set");
				return; // stop
			}
			
			JSONObject sendTo = new JSONObject();
			// set image
			int insert = hibConfig.setProfilePicture(image, userId);			
			if(insert == 0) {
				// on error
				sendTo.put("flag", "imageSetFail");
			} else {
				// on success
				sendTo.put("flag", "imageSetSuccess");
			}
			sendMessageToSession(session, sendTo.toString()); // notify if image was set
		}
		
	}
	

	/**
	 * Method called when a connection is closed
	 */
	@OnClose
	public void onClose(Session session) {

		System.out.println("Session " + session.getId() + " has ended");

		// Getting the client email that exited
		String email = emailSessionPair.inverse().get(session);

		// removing the session from sessions list
		sessions.remove(session);
		emailSessionPair.remove(email);
		
		JSONObject leftNow = new JSONObject();
		JSONObject jsonOffline = new JSONObject();
		User offline = hibConfig.getUserByEmail(email);
		jsonOffline.put("id", offline.getUserID());
		jsonOffline.put("email", offline.getEmail());
		jsonOffline.put("fullname", offline.getFullname());
		leftNow.put("flag", "offline");
		leftNow.put("userLeft", jsonOffline);

		// Notifying all the clients about person exit
		for(Session onlineNow : sessions) {
			sendMessageToSession(onlineNow, leftNow.toString());
		}

	}
	
	// slep for 100 ms
	private void sleep() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// convert calendar instance to string format
	private String parseTime(Calendar date) {
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
		String time = sdf.format(date.getTime());
		return time;
	}
	
	// close session
	private void closeSession(Session session) {
		try {
			session.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// send message to session
	private void sendMessageToSession(Session session, String message) {
		try {
			System.out.println("Sending message to: " + session.getId() + " : " + message);
			session.getBasicRemote().sendText(message);
		} catch (IOException e) {
			System.out.println("Error sending message: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	// send online users list
	private void sendOnlineList(Session session) {
		List<JSONObject> usersOnline = new ArrayList<JSONObject>();
		for(Session online : sessions) {
			String email = emailSessionPair.inverse().get(online);
			User user = hibConfig.getUserByEmail(email);
			JSONObject sendTo = new JSONObject();
			JSONObject jsonUser = new JSONObject();
			jsonUser.put("id", user.getUserID());
			jsonUser.put("fullname", user.getFullname());
			jsonUser.put("email", user.getEmail());
			jsonUser.put("profilePic", user.getProfilePic());
			sendTo.put("flag", "onlineUser");
			sendTo.put("user", jsonUser);
			usersOnline.add(sendTo);
		}
		// if you want to send to all users
		if(session == null) {
			for(Session active : sessions) {
				for(JSONObject userOnline : usersOnline) {
					sendMessageToSession(active, userOnline.toString());
				}
			}
		// if you want to send to one user
		} else {
			for(JSONObject userOnline : usersOnline) {
				sendMessageToSession(session, userOnline.toString());
			}
		}
		
	 }

}