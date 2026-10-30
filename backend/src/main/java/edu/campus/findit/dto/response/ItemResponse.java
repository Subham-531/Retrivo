package edu.campus.findit.dto.response;

import edu.campus.findit.model.Item;
import edu.campus.findit.model.enums.ItemStatus;
import edu.campus.findit.model.enums.ReportType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ItemResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private Integer categoryId;
    private String categoryName;
    private String categoryIcon;
    private String itemName;
    private String colour;
    private String brand;
    private String description;
    private String location;
    private LocalDate eventDate;
    private ReportType reportType;
    private ItemStatus status;
    private String imagePath;
    private String identifyingDetails;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ItemResponse() {}

    public static ItemResponse fromEntity(Item item, boolean includeSensitiveDetails) {
        ItemResponse resp = new ItemResponse();
        resp.setId(item.getId());
        if (item.getUser() != null) {
            resp.setUserId(item.getUser().getId());
            resp.setUserName(item.getUser().getFullName());
            resp.setUserEmail(item.getUser().getEmail());
            resp.setUserPhone(item.getUser().getPhone());
        }
        if (item.getCategory() != null) {
            resp.setCategoryId(item.getCategory().getId());
            resp.setCategoryName(item.getCategory().getName());
            resp.setCategoryIcon(item.getCategory().getIcon());
        }
        resp.setItemName(item.getItemName());
        resp.setColour(item.getColour());
        resp.setBrand(item.getBrand());
        resp.setDescription(item.getDescription());
        resp.setLocation(item.getLocation());
        resp.setEventDate(item.getEventDate());
        resp.setReportType(item.getReportType());
        resp.setStatus(item.getStatus());
        resp.setImagePath(item.getImagePath());
        if (includeSensitiveDetails) {
            resp.setIdentifyingDetails(item.getIdentifyingDetails());
        } else {
            resp.setIdentifyingDetails(null);
        }
        resp.setCreatedAt(item.getCreatedAt());
        resp.setUpdatedAt(item.getUpdatedAt());
        return resp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getCategoryIcon() { return categoryIcon; }
    public void setCategoryIcon(String categoryIcon) { this.categoryIcon = categoryIcon; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getColour() { return colour; }
    public void setColour(String colour) { this.colour = colour; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public ReportType getReportType() { return reportType; }
    public void setReportType(ReportType reportType) { this.reportType = reportType; }

    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getIdentifyingDetails() { return identifyingDetails; }
    public void setIdentifyingDetails(String identifyingDetails) { this.identifyingDetails = identifyingDetails; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

