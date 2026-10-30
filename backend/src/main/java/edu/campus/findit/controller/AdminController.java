package edu.campus.findit.controller;

import edu.campus.findit.dto.response.AdminStatsResponse;
import edu.campus.findit.dto.response.ApiResponse;
import edu.campus.findit.dto.response.ItemResponse;
import edu.campus.findit.dto.response.UserResponse;
import edu.campus.findit.model.User;
import edu.campus.findit.model.enums.ItemStatus;
import edu.campus.findit.service.AdminService;
import edu.campus.findit.service.AuthService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final AuthService authService;

    public AdminController(AdminService adminService, AuthService authService) {
        this.adminService = adminService;
        this.authService = authService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getDashboardStats()));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllUsers()));
    }

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<Page<ItemResponse>>> getAllItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllItems(pageable)));
    }

    @PutMapping("/items/{id}/status")
    public ResponseEntity<ApiResponse<ItemResponse>> updateItemStatus(
            @PathVariable Long id,
            @RequestParam ItemStatus status
    ) {
        User admin = authService.getCurrentAuthenticatedUser();
        ItemResponse item = adminService.updateItemStatus(id, status, admin);
        return ResponseEntity.ok(ApiResponse.ok("Item status successfully updated", item));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
        adminService.deleteItem(id);
        return ResponseEntity.ok(ApiResponse.ok("Item removed by administrator", null));
    }
}

