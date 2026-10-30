package edu.campus.findit.controller;

import edu.campus.findit.dto.request.ClaimCreateRequest;
import edu.campus.findit.dto.request.ClaimMessageCreateRequest;
import edu.campus.findit.dto.request.ClaimReviewRequest;
import edu.campus.findit.dto.response.ApiResponse;
import edu.campus.findit.dto.response.ClaimMessageResponse;
import edu.campus.findit.dto.response.ClaimResponse;
import edu.campus.findit.model.User;
import edu.campus.findit.service.AuthService;
import edu.campus.findit.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claimService;
    private final AuthService authService;

    public ClaimController(ClaimService claimService, AuthService authService) {
        this.claimService = claimService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ClaimResponse>> submitClaim(@Valid @RequestBody ClaimCreateRequest req) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        ClaimResponse claim = claimService.createClaim(req, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Claim submitted successfully. Awaiting finder review.", claim));
    }

    @GetMapping({"", "/my"})
    public ResponseEntity<ApiResponse<List<ClaimResponse>>> getMyClaims() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        List<ClaimResponse> list = claimService.getClaimsForUser(currentUser);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClaimResponse>> getClaimById(@PathVariable("id") Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        ClaimResponse claim = claimService.getClaimById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(claim));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<List<ClaimMessageResponse>>> getClaimMessages(@PathVariable("id") Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        List<ClaimMessageResponse> messages = claimService.getClaimMessages(id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(messages));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<ClaimMessageResponse>> sendClaimMessage(
            @PathVariable("id") Long id,
            @Valid @RequestBody ClaimMessageCreateRequest req
    ) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        ClaimMessageResponse message = claimService.sendClaimMessage(id, req.getContent(), currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Message sent", message));
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<ApiResponse<ClaimResponse>> reviewClaim(
            @PathVariable("id") Long id,
            @Valid @RequestBody ClaimReviewRequest req
    ) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        ClaimResponse reviewed = claimService.reviewClaim(id, req, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Claim reviewed successfully", reviewed));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<ClaimResponse>> markCompleted(@PathVariable("id") Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        ClaimResponse completed = claimService.markCompleted(id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Claim marked as completed", completed));
    }
}

