package edu.campus.findit.dto.request;

import edu.campus.findit.model.enums.ItemStatus;
import java.time.LocalDate;

public class ItemUpdateRequest {
    private String itemName;
    private Integer categoryId;
    private String colour;
    private String brand;
    private String description;
    private String location;
    private LocalDate eventDate;
    private ItemStatus status;
    private String imagePath;
    private String identifyingDetails;

    public ItemUpdateRequest() {}

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

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

    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getIdentifyingDetails() { return identifyingDetails; }
    public void setIdentifyingDetails(String identifyingDetails) { this.identifyingDetails = identifyingDetails; }
}

