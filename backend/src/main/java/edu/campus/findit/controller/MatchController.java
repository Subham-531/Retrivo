package edu.campus.findit.controller;

import edu.campus.findit.dto.response.ApiResponse;
import edu.campus.findit.dto.response.MatchResponse;
import edu.campus.findit.model.User;
import edu.campus.findit.repository.MatchRepository;
import edu.campus.findit.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchRepository matchRepository;
    private final AuthService authService;

    @Value("${findit.matching.min-threshold:50}")
    private int minThreshold;

    public MatchController(MatchRepository matchRepository, AuthService authService) {
        this.matchRepository = matchRepository;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MatchResponse>>> getMyMatches() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        List<MatchResponse> list = matchRepository.findRelevantMatchesForUser(currentUser.getId(), minThreshold)
                .stream()
                .map(MatchResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> getMatchesForItem(@PathVariable Long itemId) {
        List<MatchResponse> list = matchRepository.findByItemId(itemId)
                .stream()
                .map(MatchResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> getAllMatches(
            @RequestParam(required = false) Integer minScore
    ) {
        int threshold = (minScore != null) ? minScore : minThreshold;
        List<MatchResponse> list = matchRepository.findAllAboveThreshold(threshold)
                .stream()
                .map(MatchResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }
}

