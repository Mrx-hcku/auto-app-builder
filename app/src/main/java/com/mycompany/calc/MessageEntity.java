package com.mycompany.calc;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "offline_messages")
public class MessageEntity {
    @PrimaryKey
    @NonNull
    private String messageId;
    private String chatId;
    private String sender;
    private String message;
    private String mediaUrl;
    private String replyToMessage;
    private boolean isForwarded;
    private long timestamp;

    public MessageEntity(@NonNull String messageId, String chatId, String sender, String message, String mediaUrl, String replyToMessage, boolean isForwarded, long timestamp) {
        this.messageId = messageId;
        this.chatId = chatId;
        this.sender = sender;
        this.message = message;
        this.mediaUrl = mediaUrl;
        this.replyToMessage = replyToMessage;
        this.isForwarded = isForwarded;
        this.timestamp = timestamp;
    }

    @NonNull public String getMessageId() { return messageId; }
    public String getChatId() { return chatId; }
    public String getSender() { return sender; }
    public String getMessage() { return message; }
    public String getMediaUrl() { return mediaUrl; }
    public String getReplyToMessage() { return replyToMessage; }
    public boolean isForwarded() { return isForwarded; }
    public long getTimestamp() { return timestamp; }
}
