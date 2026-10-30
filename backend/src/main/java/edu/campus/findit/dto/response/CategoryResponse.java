package edu.campus.findit.dto.response;

import edu.campus.findit.model.Category;

public class CategoryResponse {
    private Integer id;
    private String name;
    private String description;
    private String icon;

    public CategoryResponse() {}

    public static CategoryResponse fromEntity(Category category) {
        CategoryResponse resp = new CategoryResponse();
        resp.setId(category.getId());
        resp.setName(category.getName());
        resp.setDescription(category.getDescription());
        resp.setIcon(category.getIcon());
        return resp;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}

