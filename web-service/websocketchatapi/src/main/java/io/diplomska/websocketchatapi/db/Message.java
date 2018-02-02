package io.diplomska.websocketchatapi.db;

import java.util.Calendar;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@Entity
@Table(name = "Messages")
public class Message {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "MessageID")
	private Integer messageID;
	private String message;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "sent")
	private Calendar sent;
	@ManyToOne
	@JoinColumn(name = "MessageID", referencedColumnName = "UserID", insertable= false, updatable = false)
	private User userDetails;
	@ManyToOne
	@JoinColumn(name = "MessageID", referencedColumnName = "ConversationID", insertable= false, updatable = false)
	private Conversation conversationDetails;
	
	public Message() {
	}
	
	/*
	 * Getters and Setters
	 * @param - SET
	 * @return - GET
	 */
	public Integer getMessageID() {
		return messageID;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public Calendar getSent() {
		return sent;
	}
	
	public void setSent(Calendar sent) {
		this.sent = sent;
	}
	
	public User getUserDetails() {
		return userDetails;
	}
	
	public void setUserDetails(User userDetails) {
		this.userDetails = userDetails;
	}
	
	public Conversation getConversationDetails() {
		return conversationDetails;
	}
	
	public void setConversationDetails(Conversation conversationDetails) {
		this.conversationDetails = conversationDetails;
	}

	
	
	
}
