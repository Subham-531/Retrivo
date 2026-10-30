package edu.campus.findit.dto.response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.campus.findit.model.Match;
import edu.campus.findit.model.enums.MatchStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MatchResponse {
    private Long id;
    private ItemResponse lostItem;
    private ItemResponse foundItem;
    private Integer compatibilityScore;
    private String compatibilityLabel;
    private List<String> matchingReasons = new ArrayList<>();
    private MatchStatus status;
    private LocalDateTime createdAt;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public MatchResponse() {}

    public static MatchResponse fromEntity(Match match) {
        MatchResponse resp = new MatchResponse();
        resp.setId(match.getId());
        if (match.getLostItem() != null) {
            resp.setLostItem(ItemResponse.fromEntity(match.getLostItem(), false));
        }
        if (match.getFoundItem() != null) {
            resp.setFoundItem(ItemResponse.fromEntity(match.getFoundItem(), false));
        }
        resp.setCompatibilityScore(match.getCompatibilityScore());
        
        // Dynamic Label based on score
        int score = match.getCompatibilityScore() != null ? match.getCompatibilityScore() : 0;
        if (score >= 80) {
            resp.setCompatibilityLabel("Strong Match");
        } else if (score >= 50) {
            resp.setCompatibilityLabel("Possible Match");
        } else {
            resp.setCompatibilityLabel("Low Compatibility");
        }

        // Parse JSON matching reasons
        try {
            if (match.getMatchingReasons() != null && match.getMatchingReasons().trim().startsWith("[")) {
                List<String> reasons = objectMapper.readValue(match.getMatchingReasons(), new TypeReference<List<String>>() {});
                resp.setMatchingReasons(reasons);
            } else if (match.getMatchingReasons() != null) {
                resp.getMatchingReasons().add(match.getMatchingReasons());
            }
        } catch (Exception e) {
            resp.getMatchingReasons().add(match.getMatchingReasons());
        }

        resp.setStatus(match.getStatus());
        resp.setCreatedAt(match.getCreatedAt());
        return resp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ItemResponse getLostItem() { return lostItem; }
    public void setLostItem(ItemResponse lostItem) { this.lostItem = lostItem; }

    public ItemResponse getFoundItem() { return foundItem; }
    public void setFoundItem(ItemResponse foundItem) { this.foundItem = foundItem; }

    public Integer getCompatibilityScore() { return compatibilityScore; }
    public void setCompatibilityScore(Integer compatibilityScore) { this.compatibilityScore = compatibilityScore; }

    public String getCompatibilityLabel() { return compatibilityLabel; }
    public void setCompatibilityLabel(String compatibilityLabel) { this.compatibilityLabel = compatibilityLabel; }

    public List<String> getMatchingReasons() { return matchingReasons; }
    public void setMatchingReasons(List<String> matchingReasons) { this.matchingReasons = matchingReasons; }

    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

