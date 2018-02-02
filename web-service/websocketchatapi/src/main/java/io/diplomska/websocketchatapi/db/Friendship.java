package io.diplomska.websocketchatapi.db;

import java.util.Calendar;

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
@Table(name = "Friendships")
public class Friendship {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "FriendshipID")
	private Integer friendhsipID;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "started")
	private Calendar started;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "ended")
	private Calendar ended;
	@ManyToOne
    @JoinColumn(name = "FromUserID")
	private User friend1;
	@ManyToOne
    @JoinColumn(name = "ToUserID")
	private User friend2;
	
	public Friendship() {
	}
	
	/*
	 * Getters and Setters
	 * @param - SET
	 * @return - GET
	 */
	public Integer getFriendhsipID() {
		return friendhsipID;
	}
	public Calendar getStarted() {
		return started;
	}
	public void setStarted(Calendar started) {
		this.started = started;
	}
	public Calendar getEnded() {
		return ended;
	}
	public void setEnded(Calendar ended) {
		this.ended = ended;
	}
	public User getFriend1() {
		return friend1;
	}
	public void setFriend1(User friend1) {
		this.friend1 = friend1;
	}
	public User getFriend2() {
		return friend2;
	}
	public void setFriend2(User friend2) {
		this.friend2 = friend2;
	}
	
}
