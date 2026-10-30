package edu.campus.findit.model;

import edu.campus.findit.model.enums.MatchStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches", uniqueConstraints = {
    @UniqueConstraint(name = "uk_lost_found_pair", columnNames = {"lost_item_id", "found_item_id"})
})
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "lost_item_id", nullable = false)
    private Item lostItem;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "found_item_id", nullable = false)
    private Item foundItem;

    @Column(name = "compatibility_score", nullable = false)
    private Integer compatibilityScore;

    @Column(name = "matching_reasons", nullable = false, columnDefinition = "TEXT")
    private String matchingReasons;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchStatus status = MatchStatus.SUGGESTED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Match() {}

    public Match(Item lostItem, Item foundItem, Integer compatibilityScore, String matchingReasons) {
        this.lostItem = lostItem;
        this.foundItem = foundItem;
        this.compatibilityScore = compatibilityScore;
        this.matchingReasons = matchingReasons;
        this.status = MatchStatus.SUGGESTED;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Item getLostItem() { return lostItem; }
    public void setLostItem(Item lostItem) { this.lostItem = lostItem; }

    public Item getFoundItem() { return foundItem; }
    public void setFoundItem(Item foundItem) { this.foundItem = foundItem; }

    public Integer getCompatibilityScore() { return compatibilityScore; }
    public void setCompatibilityScore(Integer compatibilityScore) { this.compatibilityScore = compatibilityScore; }

    public String getMatchingReasons() { return matchingReasons; }
    public void setMatchingReasons(String matchingReasons) { this.matchingReasons = matchingReasons; }

    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

