package edu.campus.findit.dto.response;

import edu.campus.findit.model.Claim;
import edu.campus.findit.model.enums.ClaimStatus;
import java.time.LocalDateTime;

public class ClaimResponse {
    private Long id;
    private Long matchId;
    private Integer compatibilityScore;
    private Long claimantId;
    private String claimantName;
    private String claimantEmail;
    private String claimantPhone;
    private ItemResponse lostItem;
    private ItemResponse foundItem;
    private String message;
    private ClaimStatus status;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;

    public ClaimResponse() {}

    public static ClaimResponse fromEntity(Claim claim) {
        ClaimResponse resp = new ClaimResponse();
        resp.setId(claim.getId());
        if (claim.getMatch() != null) {
            resp.setMatchId(claim.getMatch().getId());
            resp.setCompatibilityScore(claim.getMatch().getCompatibilityScore());
            if (claim.getMatch().getLostItem() != null) {
                resp.setLostItem(ItemResponse.fromEntity(claim.getMatch().getLostItem(), false));
            }
            if (claim.getMatch().getFoundItem() != null) {
                resp.setFoundItem(ItemResponse.fromEntity(claim.getMatch().getFoundItem(), false));
            }
        }
        if (claim.getClaimantUser() != null) {
            resp.setClaimantId(claim.getClaimantUser().getId());
            resp.setClaimantName(claim.getClaimantUser().getFullName());
            resp.setClaimantEmail(claim.getClaimantUser().getEmail());
            resp.setClaimantPhone(claim.getClaimantUser().getPhone());
        }
        resp.setMessage(claim.getMessage());
        resp.setStatus(claim.getStatus());
        resp.setReviewedAt(claim.getReviewedAt());
        resp.setCreatedAt(claim.getCreatedAt());
        return resp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }

    public Integer getCompatibilityScore() { return compatibilityScore; }
    public void setCompatibilityScore(Integer compatibilityScore) { this.compatibilityScore = compatibilityScore; }

    public Long getClaimantId() { return claimantId; }
    public void setClaimantId(Long claimantId) { this.claimantId = claimantId; }

    public String getClaimantName() { return claimantName; }
    public void setClaimantName(String claimantName) { this.claimantName = claimantName; }

    public String getClaimantEmail() { return claimantEmail; }
    public void setClaimantEmail(String claimantEmail) { this.claimantEmail = claimantEmail; }

    public String getClaimantPhone() { return claimantPhone; }
    public void setClaimantPhone(String claimantPhone) { this.claimantPhone = claimantPhone; }

    public ItemResponse getLostItem() { return lostItem; }
    public void setLostItem(ItemResponse lostItem) { this.lostItem = lostItem; }

    public ItemResponse getFoundItem() { return foundItem; }
    public void setFoundItem(ItemResponse foundItem) { this.foundItem = foundItem; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public ClaimStatus getStatus() { return status; }
    public void setStatus(ClaimStatus status) { this.status = status; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

