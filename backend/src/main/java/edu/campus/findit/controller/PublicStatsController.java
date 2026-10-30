package edu.campus.findit.controller;

import edu.campus.findit.dto.response.ApiResponse;
import edu.campus.findit.model.enums.ItemStatus;
import edu.campus.findit.model.enums.ReportType;
import edu.campus.findit.repository.ItemRepository;
import edu.campus.findit.repository.MatchRepository;
import edu.campus.findit.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class PublicStatsController {

    private final ItemRepository itemRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

    public PublicStatsController(ItemRepository itemRepository,
                                 MatchRepository matchRepository,
                                 UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPublicStats() {
        Map<String, Object> stats = new HashMap<>();
        long totalLost = itemRepository.countByReportType(ReportType.LOST);
        long totalFound = itemRepository.countByReportType(ReportType.FOUND);
        long totalRecovered = itemRepository.countByStatus(ItemStatus.RETURNED);
        long totalMatches = matchRepository.count();
        long totalStudents = userRepository.count();

        stats.put("totalReports", totalLost + totalFound);
        stats.put("totalLost", totalLost);
        stats.put("totalFound", totalFound);
        stats.put("totalRecovered", totalRecovered);
        stats.put("totalMatches", totalMatches);
        stats.put("totalStudents", totalStudents);

        double rate = (totalLost + totalFound > 0) ? ((double) totalRecovered / (totalLost + totalFound)) * 100.0 : 0.0;
        stats.put("recoveryRatePercentage", Math.round(rate * 10.0) / 10.0);

        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}

