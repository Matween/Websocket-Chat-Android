package io.diplomska.websocketchatapi.db;

import java.util.Calendar;
import java.util.List;

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
@Table(name = "Conversations")
public class Conversation {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ConversationID")
	private Integer conversationID;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created")
	private Calendar created;
	@ManyToMany
	@JoinTable(
			 name = "UsersConversations",
		        joinColumns = @JoinColumn(
		                name = "ConversationID",
		                referencedColumnName = "ConversationID"
		        ),
		        inverseJoinColumns = @JoinColumn(
		                name = "UserID",
		                referencedColumnName = "UserID"
		        ))
	private List<User> users;
	@OneToMany(mappedBy = "conversationDetails")
	private List<Message> messages;
	
	public Conversation() {
	}
	
	/*
	 * Getters and Setters
	 * @param - SET
	 * @return - GET
	 */
	public Integer getConversationID() {
		return conversationID;
	}
	public Calendar getCreated() {
		return created;
	}
	public void setCreated(Calendar created) {
		this.created = created;
	}
	public List<User> getUsers() {
		return users;
	}
	public void setUsers(List<User> users) {
		this.users = users;
	}
	public List<Message> getMessages() {
		return messages;
	}
	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}
	
	
}
