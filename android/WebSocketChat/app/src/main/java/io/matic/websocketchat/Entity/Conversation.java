package io.matic.websocketchat.Entity;

import java.util.ArrayList;

/**
 * Created by Matic on 16/09/2017.
 */

public class Conversation {
    private String conversationId;
    private User userTo;
    private ArrayList<Message> messages;

    public Conversation() {
    }

    public Conversation(String conversationId, User userTo) {
        this.conversationId = conversationId;
        this.userTo = userTo;
    }

    public Conversation(String conversationId, User userTo, ArrayList<Message> messages) {
        this.conversationId = conversationId;
        this.userTo = userTo;
        this.messages = messages;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }


    public User getUserTo() {
        return userTo;
    }

    public void setUserTo(User userTo) {
        this.userTo = userTo;
    }


    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

}