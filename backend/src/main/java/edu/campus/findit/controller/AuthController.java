package edu.campus.findit.controller;

import edu.campus.findit.dto.request.LoginRequest;
import edu.campus.findit.dto.request.RegisterRequest;
import edu.campus.findit.dto.response.ApiResponse;
import edu.campus.findit.dto.response.AuthResponse;
import edu.campus.findit.dto.response.UserResponse;
import edu.campus.findit.model.User;
import edu.campus.findit.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest req) {
        AuthResponse resp = authService.register(req);
        return ResponseEntity.ok(ApiResponse.ok("Registration successful", resp));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        AuthResponse resp = authService.login(req);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", resp));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        User user = authService.getCurrentAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }
        return ResponseEntity.ok(ApiResponse.ok(UserResponse.fromEntity(user)));
    }
}

