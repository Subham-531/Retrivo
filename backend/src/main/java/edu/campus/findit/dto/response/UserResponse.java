package edu.campus.findit.dto.response;

import edu.campus.findit.model.User;
import edu.campus.findit.model.enums.Role;
import java.time.LocalDateTime;

public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private String phone;
    private LocalDateTime createdAt;

    public UserResponse() {}

    public static UserResponse fromEntity(User user) {
        UserResponse resp = new UserResponse();
        resp.setId(user.getId());
        resp.setFullName(user.getFullName());
        resp.setEmail(user.getEmail());
        resp.setRole(user.getRole());
        resp.setPhone(user.getPhone());
        resp.setCreatedAt(user.getCreatedAt());
        return resp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

