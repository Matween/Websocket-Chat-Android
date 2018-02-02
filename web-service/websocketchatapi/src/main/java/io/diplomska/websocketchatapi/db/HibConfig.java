package io.diplomska.websocketchatapi.db;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.NativeQuery;
import org.hibernate.service.ServiceRegistry;
import org.json.JSONArray;

import com.google.common.base.Joiner;


public class HibConfig {

	// set hibernate configuration
	private static final Configuration cfg = new Configuration().addAnnotatedClass(User.class)
												.addAnnotatedClass(Conversation.class)
												.addAnnotatedClass(Friendship.class)
												.addAnnotatedClass(Message.class)
												.configure("hibernate.cfg.xml");;
	private static final ServiceRegistry reg = new StandardServiceRegistryBuilder()
										.applySettings(cfg.getProperties())
										.build();;
	private static final SessionFactory ssf  = cfg.buildSessionFactory(reg);
	
	public HibConfig() {
	}
	
	
	// check if user exists
	public User loginUser(String email, String password) {
		User user = null;
		Session ss = null;
		Transaction tx = null;
		try {
			ss = ssf.openSession();
			tx = ss.beginTransaction();
			user = (User) ss.createQuery("FROM User WHERE email = '" + email + 
					"' AND password = '" + password + "'").getSingleResult();
			tx.commit();
		} catch(HibernateException e) {
			user = null;
			tx.rollback();
			e.printStackTrace();
		} catch(NoResultException ex) {
			user = null;
			tx.rollback();
			ex.printStackTrace();
		} finally {
			ss.close();
		}
		return user;
	}
	
	// create user
	public boolean registerUser(String fullname, String email, String password) {
		boolean register = false;
		Session ss = null;
		Transaction tx = null;
		try {
			ss = ssf.openSession();
			tx = ss.beginTransaction();
			User user = new User();
			user.setFullname(fullname);
			user.setEmail(email);
			user.setPassword(password);
			user.setCreated(Calendar.getInstance());
			user.setProfilePic("default");
			ss.save(user);
			tx.commit();
			register = true;
		} catch(HibernateException e) {
			tx.rollback();
			e.printStackTrace();
			register = false;
		} finally {
			ss.close();
		}
		return register;
	}
	
	// find user by parameter email
	public User getUserByEmail(String email) {
		User user = null;
		Session ss = null;
		Transaction tx = null;
		try {
			ss = ssf.openSession();
			tx = ss.beginTransaction();
			user = (User) ss.createQuery("FROM User WHERE email = '" + email + "'")
					.getSingleResult();
			tx.commit();
		} catch(HibernateException e) {
			user = null;
			tx.rollback();
			e.printStackTrace();
		} finally {
			ss.close();
		}
		return user;
	}
	
	// get user by parameter userId
	public User getUserById(String id) {
		User user = null;
		Session ss = null;
		Transaction tx = null;
		try {
			ss = ssf.openSession();
			tx = ss.beginTransaction();
			user = (User) ss.createQuery("FROM User WHERE UserID = " + id)
					.getSingleResult();
			tx.commit();
		} catch(HibernateException e) {
			user = null;
			tx.rollback();
			e.printStackTrace();
		} finally {
			ss.close();
		}
		return user;
	}
	
	// save users profile picture
	public int setProfilePicture(String profilePic, String userId) {
		Session ss = null;
		Transaction tx = null;
		int insert = 0;
		try {
			ss = ssf.openSession();
			tx = ss.beginTransaction();
			String sql = "UPDATE Users SET profilePic = '" + profilePic + "'"
						+ " WHERE userId = " + userId;
			insert = ss.createNativeQuery(sql).executeUpdate();
			tx.commit();
		} catch(HibernateException e) {
			insert = 0;
			tx.rollback();
			e.printStackTrace();
		} finally {
			ss.close();
		}
		return insert;
	}
	
	// update password
	public int updatePassword(String userId, String password) {
		int update = 0;
		String sql = "UPDATE Users SET password = '" + password + "'" 
					+ " WHERE UserID = " + userId;
		Session ss = null;
		Transaction tx = null;
		try {
			ss = ssf.openSession();
			tx = ss.beginTransaction();
			update = ss.createNativeQuery(sql).executeUpdate();
			tx.commit();
		} catch(HibernateException e) {
			update = 0;
			tx.rollback();
			e.printStackTrace();
		} finally {
			ss.close();
		}
		return update;
	}
	
	// insert conversation
	public int insertConversation(String[] userIds) {
		Session ss = null;
		Transaction tx = null;
		Conversation conversation = null;
		int conversationId = 0;
		try {
			ss = ssf.openSession();
			tx = ss.beginTransaction();
			conversation = new Conversation();
			conversation.setCreated(Calendar.getInstance());
			ss.save(conversation);
			conversationId = conversation.getConversationID();
		} catch(HibernateException e) {
			conversationId = 0;
		}
		for(String userId : userIds) {
			String sql = "INSERT INTO UsersConversations(UserID, ConversationID) "
					+ "VALUES("+ userId +", " + conversationId + ")";
			if(conversationId != 0) {
				ss.createNativeQuery(sql).executeUpdate();
			}
		}
		try {
			tx.commit();
		} catch(HibernateException e) {
			tx.rollback();
			conversationId = 0;
			e.printStackTrace();
		} finally {
			ss.close();
		}
		return conversationId;
	}
	
	// get conversation by user email
	public List<Object[]> getConversationsByEmail(String email) {
		List<Object[]> resultConversations = new ArrayList<Object[]>();
		Session ss = null;
		Transaction tx = null;
		String sql = "SELECT uc.ConversationID, uc.UserID, u.fullname, u.email, u.profilePic "
				+ "FROM Users u INNER JOIN UsersConversations uc ON uc.UserID = u.UserID "
				+ "WHERE uc.ConversationID IN "
				+ "(SELECT uc.ConversationID "
				+ "FROM UsersConversations uc INNER JOIN Users u ON uc.UserID = u.UserID "
				+ "WHERE u.email = '" + email + "') ORDER BY u.fullname";
		try {
			ss = ssf.openSession();
			tx = ss.beginTransaction();
			resultConversations = ss.createNativeQuery(sql).getResultList();
			tx.commit();
		} catch(HibernateException e) {
			tx.rollback();
			e.printStackTrace();
		} finally {
			ss.close();
		}
		return resultConversations;
	}
	
	// insert message
	public boolean insertMessage(String content, Calendar sent, String conversationId, String userId) {
		boolean insert = false;
		Session ss = null;
		Transaction tx = null;
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
		String sql = "INSERT INTO Messages(message, sent, ConversationID, UserID) " +
				"VALUES(?1, ?2, ?3, ?4)";
		System.out.println("Message insert sql: " + sql);
		try {
			ss = ssf.openSession();
			tx = ss.beginTransaction();
			NativeQuery query = ss.createNativeQuery(sql);
			query.setParameter(1, content);
			query.setParameter(2, sdf.format(sent.getTime()).toString());
			query.setParameter(3, conversationId);
			query.setParameter(4, userId);
			query.executeUpdate();
			tx.commit();
			insert = true;
		} catch (HibernateException e) {
			tx.rollback();
			insert = false;
			e.printStackTrace();
		} finally {
			ss.close();
		}
		return insert;
	}
	
	// get messages for conversation
	public List<Object[]> getMessagesByConversationId(String conversationId) {
		Session ss = null;
		Transaction tx = null;
		List<Object[]> messages = new ArrayList<Object[]>();
		String sql = "SELECT m.UserID, u.email, u.fullname, m.message, m.sent "
				+ "FROM Messages m INNER JOIN Users u ON m.userID = u.UserID "
				+ "WHERE m.ConversationID = " + conversationId + " ORDER BY m.sent";
		try {
			ss = ssf.openSession();
			tx = ss.beginTransaction();
			messages = ss.createNativeQuery(sql).getResultList();
			tx.commit();
		} catch(HibernateException e) {
			tx.rollback();
			e.printStackTrace();
		} finally {
			ss.close();
		}
		return messages;
	}
	
	// insert friendship
	public String insertFriendship(String fromUserId, String toUserId) {
		Session ss = null;
		Transaction tx = null;
		String friendshipId = null;
		User userFrom = getUserById(fromUserId);
		User userTo = getUserById(toUserId);
		Friendship friendship = new Friendship();
		friendship.setFriend1(userFrom);
		friendship.setFriend2(userTo);
		boolean insert = false;
		try {
			ss = ssf.openSession();
			tx = ss.beginTransaction();
			ss.save(friendship);
			tx.commit();
			insert = true;
		} catch (HibernateException e) {
			tx.rollback();
			insert = false;
			e.printStackTrace();
		}
		if(insert) {
			friendshipId = String.valueOf(friendship.getFriendhsipID());
		} else {
			friendshipId = null;
		}
		return friendshipId;
	}
	
	// update friendship
	public int updateFriendshipById(String friendshipId, boolean started) {
		Session ss = null;
		Transaction tx = null;
		int update = 0;
		String sql = "";
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
		if(started) {
			sql = "UPDATE Friendships SET "
				+ "started = '" + 
				sdf.format(Calendar.getInstance().getTime()).toString() + "' "
				+ "WHERE FriendshipID= " + friendshipId;
		} else if(!started) {
			sql = "UPDATE Friendships SET "
					+ "ended = '" + 
					sdf.format(Calendar.getInstance().getTime()).toString() + "' "
					+ "WHERE FriendshipID= " + friendshipId;
		}
		try {
			ss = ssf.openSession();
			tx = ss.beginTransaction();
			update = ss.createNativeQuery(sql).executeUpdate();
			tx.commit();
		} catch(HibernateException e) {
			update = 0;
			tx.rollback();
			e.printStackTrace();
		} finally {
			ss.close();
		}
		return update;
	}
	
	// get friend list
	public List<Object[]> getFriendships(String fromUserId) {
		List<Object[]> friendships = new ArrayList<Object[]>();
		Session ss = null;
		Transaction tx = null;
		String sql = "SELECT f.FriendshipID, u.UserID, u.email, u.fullname, u.profilePic FROM Users u LEFT JOIN Friendships f "
				+ "ON u.UserID = f.ToUserID WHERE (f.FromUserID = " + fromUserId + 
				" AND f.started IS NOT NULL AND f.ended IS NULL) UNION " + 
				"SELECT f.FriendshipID, u.UserID, u.email, u.fullname, u.profilePic FROM Users u LEFT JOIN Friendships f "
				+ "ON u.UserID = f.FromUserID WHERE (f.ToUserID = " + fromUserId + 
				" AND f.started IS NOT NULL AND f.ended IS NULL)";
		System.out.println("SQL get friendships: "+ sql);
		try {
			ss = ssf.openSession();
			tx = ss.beginTransaction();
			friendships = ss.createNativeQuery(sql).getResultList();
			tx.commit();
		} catch(HibernateException e) {
			tx.rollback();
			e.printStackTrace();
		} finally {
			ss.close();
		}
		return friendships;
	}
	
	// get friend requests
	public List<Object[]> getFriendshipRequests(String fromUserId) {
		List<Object[]> friendshipRequests = new ArrayList<Object[]>();
		Session ss = null;
		Transaction tx = null;
		String sql = "SELECT f.FriendshipID, u.UserID, u.email, u.fullname, u.profilePic FROM Users u LEFT JOIN Friendships f "
				+ "ON u.UserID = f.FromUserID WHERE (f.ToUserID = " + fromUserId + 
				" AND f.started IS NULL)";
		System.out.println("SQL get friendship requests: "+ sql);
		try {
			ss = ssf.openSession();
			tx = ss.beginTransaction();
			friendshipRequests = ss.createNativeQuery(sql).getResultList();
			System.out.println("Number of requests: " + friendshipRequests.size());
			tx.commit();
		} catch(HibernateException e) {
			tx.rollback();
			e.printStackTrace();
		} finally {
			ss.close();
		}
		return friendshipRequests;
	}
	
	
}
