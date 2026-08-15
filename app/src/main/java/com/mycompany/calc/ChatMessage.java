package com.mycompany.calc;

public class ChatMessage {
    private String sender;
    private String message;
    private String mediaUrl;
    private boolean isSentByUser;

    public ChatMessage(String sender, String message, String mediaUrl, boolean isSentByUser) {
        this.sender = sender;
        this.message = message;
        this.mediaUrl = mediaUrl;
        this.isSentByUser = isSentByUser;
    }

    public String getSender() { return sender; }
    public String getMessage() { return message; }
    public String getMediaUrl() { return mediaUrl; }
    public boolean isSentByUser() { return isSentByUser; }
}
