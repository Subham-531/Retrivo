package edu.campus.findit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ClaimCreateRequest {

    @NotNull(message = "Match ID is required")
    private Long matchId;

    @NotBlank(message = "Claim message and proof of ownership description is required")
    private String message;

    public ClaimCreateRequest() {}

    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

