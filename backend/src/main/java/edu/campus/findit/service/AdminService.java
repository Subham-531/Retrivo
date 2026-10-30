package edu.campus.findit.service;

import edu.campus.findit.dto.response.AdminStatsResponse;
import edu.campus.findit.dto.response.ItemResponse;
import edu.campus.findit.dto.response.UserResponse;
import edu.campus.findit.exception.ResourceNotFoundException;
import edu.campus.findit.model.Item;
import edu.campus.findit.model.ItemHistory;
import edu.campus.findit.model.User;
import edu.campus.findit.model.enums.ClaimStatus;
import edu.campus.findit.model.enums.ItemStatus;
import edu.campus.findit.model.enums.ReportType;
import edu.campus.findit.model.enums.Role;
import edu.campus.findit.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final MatchRepository matchRepository;
    private final ClaimRepository claimRepository;
    private final ItemHistoryRepository historyRepository;

    public AdminService(UserRepository userRepository,
                        ItemRepository itemRepository,
                        MatchRepository matchRepository,
                        ClaimRepository claimRepository,
                        ItemHistoryRepository historyRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.matchRepository = matchRepository;
        this.claimRepository = claimRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getDashboardStats() {
        AdminStatsResponse stats = new AdminStatsResponse();

        long usersCount = userRepository.count();
        long studentsCount = userRepository.countByRole(Role.STUDENT);
        long lostCount = itemRepository.countByReportType(ReportType.LOST);
        long foundCount = itemRepository.countByReportType(ReportType.FOUND);
        long recoveredCount = itemRepository.countByStatus(ItemStatus.RETURNED);
        long matchesCount = matchRepository.count();
        long pendingClaimsCount = claimRepository.countByStatus(ClaimStatus.PENDING);

        stats.setTotalUsers(usersCount);
        stats.setTotalStudents(studentsCount);
        stats.setTotalLost(lostCount);
        stats.setTotalFound(foundCount);
        stats.setTotalRecovered(recoveredCount);
        stats.setTotalMatches(matchesCount);
        stats.setPendingClaims(pendingClaimsCount);

        long totalReports = lostCount + foundCount;
        double recoveryRate = totalReports > 0 ? ((double) recoveredCount / totalReports) * 100.0 : 0.0;
        stats.setRecoveryRatePercentage(Math.round(recoveryRate * 10.0) / 10.0);

        // Category breakdown from DB
        List<Object[]> catRows = itemRepository.getCategoryItemCounts();
        Map<String, Long> catDist = new HashMap<>();
        for (Object[] row : catRows) {
            String catName = (String) row[0];
            Number count = (Number) row[1];
            catDist.put(catName, count != null ? count.longValue() : 0L);
        }
        stats.setCategoryDistribution(catDist);

        // Monthly trends from DB
        List<Object[]> monthRows = itemRepository.getMonthlyItemStats();
        List<AdminStatsResponse.MonthlyTrend> trends = monthRows.stream().map(row -> {
            String month = (String) row[0];
            Number reported = (Number) row[1];
            Number recovered = (Number) row[2];
            return new AdminStatsResponse.MonthlyTrend(
                    month,
                    reported != null ? reported.longValue() : 0L,
                    recovered != null ? recovered.longValue() : 0L
            );
        }).collect(Collectors.toList());
        stats.setMonthlyTrends(trends);

        return stats;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ItemResponse> getAllItems(Pageable pageable) {
        return itemRepository.findAll(pageable)
                .map(item -> ItemResponse.fromEntity(item, true));
    }

    @Transactional
    public ItemResponse updateItemStatus(Long id, ItemStatus newStatus, User admin) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + id));

        ItemStatus oldStatus = item.getStatus();
        item.setStatus(newStatus);

        historyRepository.save(new ItemHistory(
                item, admin, oldStatus.name(), newStatus.name(),
                "Status moderated by administrator"
        ));

        Item saved = itemRepository.save(item);
        return ItemResponse.fromEntity(saved, true);
    }

    @Transactional
    public void deleteItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + id));
        itemRepository.delete(item);
    }
}

