package edu.campus.findit.dto.response;

import edu.campus.findit.model.Notification;
import java.time.LocalDateTime;

public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private Boolean isRead;
    private String link;
    private LocalDateTime createdAt;

    public NotificationResponse() {}

    public static NotificationResponse fromEntity(Notification notif) {
        NotificationResponse resp = new NotificationResponse();
        resp.setId(notif.getId());
        resp.setTitle(notif.getTitle());
        resp.setMessage(notif.getMessage());
        resp.setIsRead(notif.getIsRead());
        resp.setLink(notif.getLink());
        resp.setCreatedAt(notif.getCreatedAt());
        return resp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

