package io.diplomska.websocketchatapi.db;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@Entity
@Table(name = "Users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "UserID")
	private Integer userID;
	private String fullname;
	private String email;
	private String password;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created")
	private Calendar created;
	private String profilePic;
	@ManyToMany
	@JoinTable(
			 name = "UsersConversations",
		        joinColumns = @JoinColumn(
		                name = "UserID",
		                referencedColumnName = "UserID"
		        ),
		        inverseJoinColumns = @JoinColumn(
		                name = "ConversationID",
		                referencedColumnName = "ConversationID"
		        ))
	private List<Conversation> conversations;
	@OneToMany(mappedBy = "userDetails")
	private List<Message> messages;
	@OneToMany(mappedBy = "friend1")
    private Set<Friendship> friendRequests = new HashSet<Friendship>();
	@OneToMany(mappedBy = "friend2")
    private Set<Friendship> friendAccepts = new HashSet<Friendship>();
	
	public User() {
	}
	
	/*
	 * Getters and Setters
	 * @param - SET
	 * @return - GET
	 */
	public Integer getUserID() {
		return userID;
	}
	public String getFullname() {
		return fullname;
	}
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Calendar getCreated() {
		return created;
	}
	public void setCreated(Calendar created) {
		this.created = created;
	}
	public String getProfilePic() {
		return profilePic;
	}
	public void setProfilePic(String profilePic) {
		this.profilePic = profilePic;
	}
	public List<Conversation> getConversations() {
		return conversations;
	}
	public void setConversations(List<Conversation> conversations) {
		this.conversations = conversations;
	}
	public List<Message> getMessages() {
		return messages;
	}
	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}
	public Set<Friendship> getFriendRequests() {
		return friendRequests;
	}
	public void setFriendRequests(Set<Friendship> friendRequests) {
		this.friendRequests = friendRequests;
	}
	public Set<Friendship> getFriendAccepts() {
		return friendAccepts;
	}
	public void setFriendAccepts(Set<Friendship> friendAccepts) {
		this.friendAccepts = friendAccepts;
	}
	
	
}
