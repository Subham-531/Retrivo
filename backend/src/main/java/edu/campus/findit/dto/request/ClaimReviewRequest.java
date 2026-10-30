package edu.campus.findit.dto.request;

import edu.campus.findit.model.enums.ClaimStatus;
import jakarta.validation.constraints.NotNull;

public class ClaimReviewRequest {

    @NotNull(message = "Review status (APPROVED or REJECTED) is required")
    private ClaimStatus status;

    private String reviewNote;

    public ClaimReviewRequest() {}

    public ClaimStatus getStatus() { return status; }
    public void setStatus(ClaimStatus status) { this.status = status; }

    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }
}

