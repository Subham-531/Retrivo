package edu.campus.findit.service;

import edu.campus.findit.dto.request.ClaimCreateRequest;
import edu.campus.findit.dto.request.ClaimReviewRequest;
import edu.campus.findit.dto.response.ClaimResponse;
import edu.campus.findit.exception.BadRequestException;
import edu.campus.findit.exception.UnauthorizedException;
import edu.campus.findit.model.*;
import edu.campus.findit.model.enums.ClaimStatus;
import edu.campus.findit.model.enums.ItemStatus;
import edu.campus.findit.model.enums.MatchStatus;
import edu.campus.findit.model.enums.Role;
import edu.campus.findit.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemHistoryRepository historyRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ClaimMessageRepository claimMessageRepository;

    private ClaimService claimService;

    private User studentLost;
    private User studentFound;
    private User studentThirdParty;
    private User adminUser;

    private Item lostItem;
    private Item foundItem;
    private Match match;

    @BeforeEach
    void setUp() {
        claimService = new ClaimService(
                claimRepository,
                matchRepository,
                itemRepository,
                historyRepository,
                notificationRepository,
                claimMessageRepository
        );

        studentLost = new User();
        studentLost.setId(10L);
        studentLost.setFullName("Alice Walker");
        studentLost.setEmail("alice@campus.edu");
        studentLost.setRole(Role.STUDENT);

        studentFound = new User();
        studentFound.setId(20L);
        studentFound.setFullName("Bob Builder");
        studentFound.setEmail("bob@campus.edu");
        studentFound.setRole(Role.STUDENT);

        studentThirdParty = new User();
        studentThirdParty.setId(30L);
        studentThirdParty.setFullName("Charlie Chaplin");
        studentThirdParty.setEmail("charlie@campus.edu");
        studentThirdParty.setRole(Role.STUDENT);

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setFullName("Admin Clerk");
        adminUser.setEmail("clerk@campus.edu");
        adminUser.setRole(Role.ADMIN);

        lostItem = new Item();
        lostItem.setId(101L);
        lostItem.setItemName("Blue Water Bottle");
        lostItem.setUser(studentLost);
        lostItem.setStatus(ItemStatus.ACTIVE);

        foundItem = new Item();
        foundItem.setId(201L);
        foundItem.setItemName("Blue Hydro Flask");
        foundItem.setUser(studentFound);
        foundItem.setStatus(ItemStatus.ACTIVE);

        match = new Match(lostItem, foundItem, 85, "[\"High similarity\"]");
        match.setId(501L);
        match.setStatus(MatchStatus.SUGGESTED);
    }

    @Test
    @DisplayName("Claim creation sets items to CLAIM_PENDING and match to CLAIMED")
    void testCreateClaimSuccess() {
        ClaimCreateRequest req = new ClaimCreateRequest();
        req.setMatchId(match.getId());
        req.setMessage("Has an engineering sticker on the bottom.");

        when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));
        when(claimRepository.findExistingClaim(match.getId(), studentLost.getId())).thenReturn(Optional.empty());
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> {
            Claim c = invocation.getArgument(0);
            c.setId(901L);
            return c;
        });

        ClaimResponse response = claimService.createClaim(req, studentLost);

        assertNotNull(response);
        assertEquals(ClaimStatus.PENDING, response.getStatus());
        assertEquals(ItemStatus.CLAIM_PENDING, lostItem.getStatus());
        assertEquals(ItemStatus.CLAIM_PENDING, foundItem.getStatus());
        assertEquals(MatchStatus.CLAIMED, match.getStatus());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Finder cannot claim their own found item report")
    void testFinderCannotClaimOwnItem() {
        ClaimCreateRequest req = new ClaimCreateRequest();
        req.setMatchId(match.getId());
        req.setMessage("I found this but I want to claim it.");

        when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

        assertThrows(BadRequestException.class, () -> claimService.createClaim(req, studentFound));
    }

    @Test
    @DisplayName("Claim approval keeps items as CLAIM_PENDING awaiting physical handover")
    void testReviewClaimApprovalDecoupledFromReturn() {
        Claim claim = new Claim(match, studentLost, "Has scratch near logo");
        claim.setId(901L);
        claim.setStatus(ClaimStatus.PENDING);
        lostItem.setStatus(ItemStatus.CLAIM_PENDING);
        foundItem.setStatus(ItemStatus.CLAIM_PENDING);

        when(claimRepository.findById(claim.getId())).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(i -> i.getArgument(0));

        ClaimReviewRequest req = new ClaimReviewRequest();
        req.setStatus(ClaimStatus.APPROVED);

        ClaimResponse resp = claimService.reviewClaim(claim.getId(), req, studentFound);

        assertEquals(ClaimStatus.APPROVED, resp.getStatus());
        // CRITICAL: Items must remain CLAIM_PENDING, NOT RETURNED!
        assertEquals(ItemStatus.CLAIM_PENDING, lostItem.getStatus());
        assertEquals(ItemStatus.CLAIM_PENDING, foundItem.getStatus());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Unauthorized user cannot review a claim")
    void testUnauthorizedClaimReview() {
        Claim claim = new Claim(match, studentLost, "Verification details");
        claim.setId(901L);

        when(claimRepository.findById(claim.getId())).thenReturn(Optional.of(claim));

        ClaimReviewRequest req = new ClaimReviewRequest();
        req.setStatus(ClaimStatus.APPROVED);

        assertThrows(UnauthorizedException.class, () -> claimService.reviewClaim(claim.getId(), req, studentThirdParty));
    }

    @Test
    @DisplayName("Marking handover complete transitions claim to COMPLETED and items to RETURNED")
    void testMarkCompletedSuccess() {
        Claim claim = new Claim(match, studentLost, "Verification details");
        claim.setId(901L);
        claim.setStatus(ClaimStatus.APPROVED);
        lostItem.setStatus(ItemStatus.CLAIM_PENDING);
        foundItem.setStatus(ItemStatus.CLAIM_PENDING);

        when(claimRepository.findById(claim.getId())).thenReturn(Optional.of(claim));
        when(claimRepository.findCompetingPendingClaims(foundItem.getId(), claim.getId())).thenReturn(Collections.emptyList());
        when(claimRepository.save(any(Claim.class))).thenAnswer(i -> i.getArgument(0));

        ClaimResponse response = claimService.markCompleted(claim.getId(), studentFound);

        assertEquals(ClaimStatus.COMPLETED, response.getStatus());
        assertEquals(ItemStatus.RETURNED, lostItem.getStatus());
        assertEquals(ItemStatus.RETURNED, foundItem.getStatus());
        assertEquals(MatchStatus.RESOLVED, match.getStatus());
        verify(historyRepository, atLeastOnce()).save(any(ItemHistory.class));
    }

    @Test
    @DisplayName("Marking handover complete auto-rejects competing pending claims")
    void testMarkCompletedAutoRejectsCompetingClaims() {
        Claim approvedClaim = new Claim(match, studentLost, "Verification details");
        approvedClaim.setId(901L);
        approvedClaim.setStatus(ClaimStatus.APPROVED);

        Claim competingClaim = new Claim(match, studentThirdParty, "I also think it's mine");
        competingClaim.setId(902L);
        competingClaim.setStatus(ClaimStatus.PENDING);

        when(claimRepository.findById(approvedClaim.getId())).thenReturn(Optional.of(approvedClaim));
        when(claimRepository.findCompetingPendingClaims(foundItem.getId(), approvedClaim.getId()))
                .thenReturn(List.of(competingClaim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(i -> i.getArgument(0));

        claimService.markCompleted(approvedClaim.getId(), adminUser);

        assertEquals(ClaimStatus.REJECTED, competingClaim.getStatus(), "Competing claim must be auto-rejected");
        assertEquals(ClaimStatus.COMPLETED, approvedClaim.getStatus());
        assertEquals(ItemStatus.RETURNED, lostItem.getStatus());
        assertEquals(ItemStatus.RETURNED, foundItem.getStatus());
    }

    @Test
    @DisplayName("Unrelated student cannot mark handover completed")
    void testUnauthorizedHandoverCompletion() {
        Claim approvedClaim = new Claim(match, studentLost, "Verification details");
        approvedClaim.setId(901L);
        approvedClaim.setStatus(ClaimStatus.APPROVED);

        when(claimRepository.findById(approvedClaim.getId())).thenReturn(Optional.of(approvedClaim));

        assertThrows(UnauthorizedException.class, () -> claimService.markCompleted(approvedClaim.getId(), studentThirdParty));
    }
}
