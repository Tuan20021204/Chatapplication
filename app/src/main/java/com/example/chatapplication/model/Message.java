package com.example.chatapplication.model;

public class Message {
    private String messageText; // Nội dung tin nhắn
    private String senderId;    // ID của người gửi
    private long timestamp;     // Thời gian gửi tin nhắn

    // Constructor
    public Message(String messageText, String senderId, long timestamp) {
        this.messageText = messageText;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    // Default constructor (cần thiết cho Firebase)
    public Message() {
    }

    // Getter cho messageText
    public String getMessageText() {
        return messageText;
    }

    // Getter cho senderId
    public String getSenderId() {
        return senderId;
    }

    // Getter cho timestamp
    public long getTimestamp() {
        return timestamp;
    }
}
