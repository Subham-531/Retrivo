package edu.campus.findit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.campus.findit.model.Item;
import edu.campus.findit.model.Match;
import edu.campus.findit.model.Notification;
import edu.campus.findit.model.enums.ItemStatus;
import edu.campus.findit.model.enums.MatchStatus;
import edu.campus.findit.model.enums.ReportType;
import edu.campus.findit.repository.ItemRepository;
import edu.campus.findit.repository.MatchRepository;
import edu.campus.findit.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class MatchingService {

    private final MatchRepository matchRepository;
    private final ItemRepository itemRepository;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${findit.matching.min-threshold:50}")
    private int minMatchThreshold;

    @Value("${findit.matching.strong-threshold:80}")
    private int strongMatchThreshold;

    public MatchingService(MatchRepository matchRepository,
                           ItemRepository itemRepository,
                           NotificationRepository notificationRepository) {
        this.matchRepository = matchRepository;
        this.itemRepository = itemRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Executes smart matching for a newly created or updated item against all compatible active items.
     * LOST items are matched against FOUND items, and vice versa.
     */
    @Transactional
    public List<Match> matchItemAgainstOpposites(Item currentItem) {
        if (currentItem.getStatus() == ItemStatus.RETURNED || currentItem.getStatus() == ItemStatus.CLOSED) {
            return Collections.emptyList();
        }

        ReportType oppositeType = (currentItem.getReportType() == ReportType.LOST)
                ? ReportType.FOUND
                : ReportType.LOST;

        // Retrieve candidate items from database
        List<Item> candidates = itemRepository.findCandidateItemsForMatching(
                oppositeType, ItemStatus.ACTIVE, currentItem.getId()
        );

        List<Match> resultingMatches = new ArrayList<>();

        for (Item candidate : candidates) {
            // Guarantee lostItem is always the LOST report, foundItem is always the FOUND report
            Item lostItem = (currentItem.getReportType() == ReportType.LOST) ? currentItem : candidate;
            Item foundItem = (currentItem.getReportType() == ReportType.FOUND) ? currentItem : candidate;

            MatchResult result = calculateCompatibility(lostItem, foundItem);

            // Check if match meets minimum threshold
            if (result.score >= minMatchThreshold) {
                Optional<Match> existingOpt = matchRepository.findByLostItemIdAndFoundItemId(
                        lostItem.getId(), foundItem.getId()
                );

                Match match;
                if (existingOpt.isPresent()) {
                    match = existingOpt.get();
                    match.setCompatibilityScore(result.score);
                    match.setMatchingReasons(result.reasonsJson);
                } else {
                    match = new Match(lostItem, foundItem, result.score, result.reasonsJson);
                    match.setStatus(MatchStatus.SUGGESTED);
                }

                Match savedMatch = matchRepository.save(match);
                resultingMatches.add(savedMatch);

                // Notify both parties if this is a high-confidence match
                if (result.score >= strongMatchThreshold && existingOpt.isEmpty()) {
                    notifyUsersOfStrongMatch(lostItem, foundItem, result.score);
                }
            }
        }

        return resultingMatches;
    }

    /**
     * Transparent, rule-based 100-point matching model:
     * - Category Match: 30 points
     * - Colour Match: 20 points
     * - Location / Facility Proximity: 20 points
     * - Date Proximity: 16 points
     * - Brand & Keywords Similarity: 14 points
     * Total = 100 points
     */
    public MatchResult calculateCompatibility(Item lost, Item found) {
        int totalScore = 0;
        List<String> reasons = new ArrayList<>();

        // 1. Category Matching (Max 30 pts)
        if (lost.getCategory() != null && found.getCategory() != null) {
            if (lost.getCategory().getId().equals(found.getCategory().getId())) {
                totalScore += 30;
                reasons.add("Exact category match: " + lost.getCategory().getName() + " (+30 pts)");
            } else {
                reasons.add("Category mismatch: " + lost.getCategory().getName() + " vs " + found.getCategory().getName() + " (+0 pts)");
            }
        }

        // 2. Colour Matching (Max 20 pts)
        String lostColour = clean(lost.getColour());
        String foundColour = clean(found.getColour());
        if (!lostColour.isEmpty() && !foundColour.isEmpty()) {
            if (lostColour.equalsIgnoreCase(foundColour)) {
                totalScore += 20;
                reasons.add("Identical primary colour: " + lost.getColour() + " (+20 pts)");
            } else if (lostColour.contains(foundColour) || foundColour.contains(lostColour)) {
                totalScore += 12;
                reasons.add("Partial colour resemblance: " + lost.getColour() + " / " + found.getColour() + " (+12 pts)");
            }
        }

        // 3. Location Proximity (Max 20 pts)
        String lostLoc = clean(lost.getLocation());
        String foundLoc = clean(found.getLocation());
        if (!lostLoc.isEmpty() && !foundLoc.isEmpty()) {
            if (lostLoc.equalsIgnoreCase(foundLoc)) {
                totalScore += 20;
                reasons.add("Exact campus location match: " + lost.getLocation() + " (+20 pts)");
            } else {
                int locScore = evaluateLocationZone(lostLoc, foundLoc);
                if (locScore > 0) {
                    totalScore += locScore;
                    reasons.add("Same campus zone / facility (" + lost.getLocation() + " & " + found.getLocation() + ") (+" + locScore + " pts)");
                }
            }
        }

        // 4. Date Proximity (Max 16 pts)
        if (lost.getEventDate() != null && found.getEventDate() != null) {
            // An item cannot realistically be found more than 1 day before it was reportedly lost
            if (found.getEventDate().isBefore(lost.getEventDate().minusDays(1))) {
                reasons.add("Found date precedes loss date (+0 pts)");
            } else {
                long daysApart = Math.abs(ChronoUnit.DAYS.between(lost.getEventDate(), found.getEventDate()));
                int dateScore;
                if (daysApart == 0) {
                    dateScore = 16;
                    reasons.add("Reported on the exact same date (+16 pts)");
                } else if (daysApart <= 1) {
                    dateScore = 14;
                    reasons.add("Event dates within 24 hours (+14 pts)");
                } else if (daysApart <= 2) {
                    dateScore = 12;
                    reasons.add("Event dates within 48 hours (+12 pts)");
                } else if (daysApart <= 5) {
                    dateScore = 8;
                    reasons.add("Event dates within 5 days (+8 pts)");
                } else if (daysApart <= 10) {
                    dateScore = 4;
                    reasons.add("Event dates within 10 days (+4 pts)");
                } else {
                    dateScore = 0;
                }
                totalScore += dateScore;
            }
        }

        // 5. Brand & Keywords Similarity (Max 14 pts: Brand up to 6 pts, Keywords up to 8 pts)
        int brandScore = 0;
        String lostBrand = clean(lost.getBrand());
        String foundBrand = clean(found.getBrand());
        if (!lostBrand.isEmpty() && !foundBrand.isEmpty() && lostBrand.equalsIgnoreCase(foundBrand)) {
            brandScore = 6;
            reasons.add("Matching brand: " + lost.getBrand() + " (+6 pts)");
        }

        // Tokenized Jaccard overlap on titles & descriptions
        double similarity = computeTextSimilarity(
                lost.getItemName() + " " + lost.getDescription(),
                found.getItemName() + " " + found.getDescription()
        );
        int jaccardScore = (int) Math.round(similarity * 8);
        if (jaccardScore > 0) {
            reasons.add("Item keywords and description match (+" + jaccardScore + " pts)");
        }
        int textScore = Math.min(brandScore + jaccardScore, 14);
        totalScore += textScore;

        // Cap at 100
        int finalScore = Math.min(totalScore, 100);

        String jsonReasons;
        try {
            jsonReasons = objectMapper.writeValueAsString(reasons);
        } catch (Exception e) {
            jsonReasons = "[\"Compatible report characteristics\"]";
        }

        return new MatchResult(finalScore, jsonReasons);
    }

    private int evaluateLocationZone(String loc1, String loc2) {
        String[] zones = {"library", "lab", "cafeteria", "canteen", "hostel", "sports", "hall", "block", "auditorium", "sac"};
        for (String zone : zones) {
            if (loc1.contains(zone) && loc2.contains(zone)) {
                return 16;
            }
        }
        return 0;
    }

    private double computeTextSimilarity(String s1, String s2) {
        Set<String> set1 = tokenize(s1);
        Set<String> set2 = tokenize(s2);
        if (set1.isEmpty() || set2.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }

    private Set<String> tokenize(String text) {
        Set<String> tokens = new HashSet<>();
        if (text == null) return tokens;

        Set<String> stopWords = Set.of("the", "a", "an", "is", "in", "at", "of", "on", "and", "or", "to", "for", "with", "my", "it");
        String[] words = text.toLowerCase().replaceAll("[^a-z0-9 ]", "").split("\\s+");
        for (String w : words) {
            if (w.length() > 2 && !stopWords.contains(w)) {
                tokens.add(w);
            }
        }
        return tokens;
    }

    private String clean(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private void notifyUsersOfStrongMatch(Item lost, Item found, int score) {
        try {
            // Notify student who lost the item
            Notification n1 = new Notification(
                    lost.getUser(),
                    "High Confidence Match Found (" + score + "%)",
                    "Your lost item \"" + lost.getItemName() + "\" closely matches a found item at " + found.getLocation() + ".",
                    "/matches"
            );
            notificationRepository.save(n1);

            // Notify student who found the item
            Notification n2 = new Notification(
                    found.getUser(),
                    "Potential Owner Identified (" + score + "%)",
                    "A student reported losing \"" + lost.getItemName() + "\" matching your found report.",
                    "/matches"
            );
            notificationRepository.save(n2);
        } catch (Exception ignored) {
        }
    }

    public static class MatchResult {
        public final int score;
        public final String reasonsJson;

        public MatchResult(int score, String reasonsJson) {
            this.score = score;
            this.reasonsJson = reasonsJson;
        }
    }
}

