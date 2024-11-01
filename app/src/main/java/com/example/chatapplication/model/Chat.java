package com.example.chatapplication.model;

public class Chat {
    private String chatId;
    private String chatName;
    private String lastMessage;
    private String profileImageUrl; // URL của ảnh đại diện


    public Chat() {}

    public Chat(String chatId, String chatName, String lastMessage, String profileImageUrl) {
        this.chatId = chatId;
        this.chatName = chatName;
        this.lastMessage = lastMessage;
        this.profileImageUrl = profileImageUrl;
    }


    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public String getChatName() { return chatName; }
    public void setChatName(String chatName) { this.chatName = chatName; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
}
