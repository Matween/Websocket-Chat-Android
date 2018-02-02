package io.matic.websocketchat.Entity;

/**
 * Created by Matic on 15/09/2017.
 */

public class Message {
    private String conversationId;
    private String message;
    private String from;
    private boolean self;
    private String time;

    public Message() {
    }

    public Message(String conversationId, String message, String from, boolean self, String time) {
        this.conversationId = conversationId;
        this.message = message;
        this.from = from;
        this.self = self;
        this.time = time;
    }

    public Message(String message, String from, boolean self, String time) {
        this.message = message;
        this.from = from;
        this.self = self;
        this.time = time;
    }


    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public boolean isSelf() {
        return self;
    }

    public void setSelf(boolean self) {
        this.self = self;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
