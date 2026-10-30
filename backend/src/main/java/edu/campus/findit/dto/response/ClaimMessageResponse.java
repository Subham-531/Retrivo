package edu.campus.findit.dto.response;

import edu.campus.findit.model.ClaimMessage;
import java.time.LocalDateTime;

public class ClaimMessageResponse {
    private Long id;
    private Long claimId;
    private Long senderId;
    private String senderName;
    private String senderRole;
    private String content;
    private boolean read;
    private LocalDateTime createdAt;

    public ClaimMessageResponse() {}

    public static ClaimMessageResponse fromEntity(ClaimMessage msg) {
        ClaimMessageResponse resp = new ClaimMessageResponse();
        resp.setId(msg.getId());
        if (msg.getClaim() != null) {
            resp.setClaimId(msg.getClaim().getId());
        }
        if (msg.getSender() != null) {
            resp.setSenderId(msg.getSender().getId());
            resp.setSenderName(msg.getSender().getFullName());
            resp.setSenderRole(msg.getSender().getRole() != null ? msg.getSender().getRole().name() : "STUDENT");
        }
        resp.setContent(msg.getContent());
        resp.setRead(msg.isRead());
        resp.setCreatedAt(msg.getCreatedAt());
        return resp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClaimId() { return claimId; }
    public void setClaimId(Long claimId) { this.claimId = claimId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

