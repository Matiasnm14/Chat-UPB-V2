package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.model.entities.enums.TypeMessage;

public class ChatMessageViewModel {
    private final String messageId;
    private final String content;
    private final boolean outgoing;
    private final String senderName;
    private final String time;
    private final boolean read;
    private final TypeMessage type;
    private final boolean pinned;

    public ChatMessageViewModel(String messageId, String content, boolean outgoing, String senderName, String time, boolean read, TypeMessage type, boolean pinned) {
        this.messageId = messageId;
        this.content = content;
        this.outgoing = outgoing;
        this.senderName = senderName;
        this.time = time;
        this.read = read;
        this.type = type;
        this.pinned = pinned;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getContent() {
        return content;
    }

    public boolean isOutgoing() {
        return outgoing;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getTime() {
        return time;
    }

    public boolean isRead() {
        return read;
    }

    public TypeMessage getType() {
        return type;
    }

    public boolean isPinned() {
        return pinned;
    }
}
