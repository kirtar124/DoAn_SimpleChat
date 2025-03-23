package com.example.doanchat;

import java.sql.Timestamp;

public class Message {
    private int messageId, senderId, receiverId;
    private String messageText;
    private Timestamp sentTime;

    public Message(int messageId, int senderId, int receiverId, String messageText, Timestamp sentTime) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageText = messageText;
        this.sentTime = sentTime;
    }

    public int getMessageId() { return messageId; }
    public int getSenderId() { return senderId; }
    public int getReceiverId() { return receiverId; }
    public String getMessageText() { return messageText; }
    public Timestamp getSentTime() { return sentTime; }
}