package edu.campus.findit.service;

import edu.campus.findit.dto.request.ClaimCreateRequest;
import edu.campus.findit.dto.request.ClaimReviewRequest;
import edu.campus.findit.dto.response.ClaimMessageResponse;
import edu.campus.findit.dto.response.ClaimResponse;
import edu.campus.findit.exception.BadRequestException;
import edu.campus.findit.exception.ResourceNotFoundException;
import edu.campus.findit.exception.UnauthorizedException;
import edu.campus.findit.model.*;
import edu.campus.findit.model.enums.ClaimStatus;
import edu.campus.findit.model.enums.ItemStatus;
import edu.campus.findit.model.enums.MatchStatus;
import edu.campus.findit.model.enums.Role;
import edu.campus.findit.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final MatchRepository matchRepository;
    private final ItemRepository itemRepository;
    private final ItemHistoryRepository historyRepository;
    private final NotificationRepository notificationRepository;
    private final ClaimMessageRepository claimMessageRepository;

    public ClaimService(ClaimRepository claimRepository,
                        MatchRepository matchRepository,
                        ItemRepository itemRepository,
                        ItemHistoryRepository historyRepository,
                        NotificationRepository notificationRepository,
                        ClaimMessageRepository claimMessageRepository) {
        this.claimRepository = claimRepository;
        this.matchRepository = matchRepository;
        this.itemRepository = itemRepository;
        this.historyRepository = historyRepository;
        this.notificationRepository = notificationRepository;
        this.claimMessageRepository = claimMessageRepository;
    }

    @Transactional
    public ClaimResponse createClaim(ClaimCreateRequest req, User claimant) {
        Match match = matchRepository.findById(req.getMatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Match recommendation not found with ID: " + req.getMatchId()));

        Item lostItem = match.getLostItem();
        Item foundItem = match.getFoundItem();

        if (lostItem.getStatus() == ItemStatus.RETURNED || foundItem.getStatus() == ItemStatus.RETURNED) {
            throw new BadRequestException("This item has already been marked as RETURNED and resolved");
        }

        // Prevent finder from claiming their own found report against themselves
        if (foundItem.getUser().getId().equals(claimant.getId())) {
            throw new BadRequestException("You are the finder of this item; you cannot claim your own found report");
        }

        // Check for duplicate pending claim
        Optional<Claim> existingClaim = claimRepository.findExistingClaim(match.getId(), claimant.getId());
        if (existingClaim.isPresent() && existingClaim.get().getStatus() == ClaimStatus.PENDING) {
            throw new BadRequestException("You already have an active pending claim for this matched item");
        }

        Claim claim = new Claim(match, claimant, req.getMessage().trim());
        Claim savedClaim = claimRepository.save(claim);

        // Seed initial message into direct chat thread
        claimMessageRepository.save(new ClaimMessage(savedClaim, claimant, req.getMessage().trim()));

        // Update item statuses to CLAIM_PENDING
        lostItem.setStatus(ItemStatus.CLAIM_PENDING);
        foundItem.setStatus(ItemStatus.CLAIM_PENDING);
        itemRepository.save(lostItem);
        itemRepository.save(foundItem);

        match.setStatus(MatchStatus.CLAIMED);
        matchRepository.save(match);

        // Audit logs
        historyRepository.save(new ItemHistory(
                lostItem, claimant, "ACTIVE", "CLAIM_PENDING",
                "Claim #" + savedClaim.getId() + " submitted by " + claimant.getFullName()
        ));
        historyRepository.save(new ItemHistory(
                foundItem, claimant, "ACTIVE", "CLAIM_PENDING",
                "Claim #" + savedClaim.getId() + " submitted against found item"
        ));

        // In-app notification to the finder
        Notification notif = new Notification(
                foundItem.getUser(),
                "New Claim Submitted!",
                claimant.getFullName() + " submitted an ownership claim for \"" + foundItem.getItemName() + "\". Please review proof of ownership.",
                "/claims"
        );
        notificationRepository.save(notif);

        return ClaimResponse.fromEntity(savedClaim);
    }

    @Transactional
    public ClaimResponse reviewClaim(Long claimId, ClaimReviewRequest req, User reviewer) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + claimId));

        Item foundItem = claim.getMatch().getFoundItem();
        Item lostItem = claim.getMatch().getLostItem();
        Match match = claim.getMatch();

        boolean isFinder = foundItem.getUser().getId().equals(reviewer.getId());
        boolean isAdmin = reviewer.getRole() == Role.ADMIN;
        if (!isFinder && !isAdmin) {
            throw new UnauthorizedException("Only the finder or an administrator can review this claim");
        }

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new BadRequestException("This claim has already been " + claim.getStatus());
        }

        claim.setStatus(req.getStatus());
        claim.setReviewedAt(LocalDateTime.now());

        if (req.getStatus() == ClaimStatus.APPROVED) {
            // Note: Items remain CLAIM_PENDING until physical handover is confirmed (markCompleted)
            match.setStatus(MatchStatus.CLAIMED);

            historyRepository.save(new ItemHistory(
                    lostItem, reviewer, "CLAIM_PENDING", "CLAIM_PENDING",
                    "Claim #" + claim.getId() + " approved by " + reviewer.getFullName() + ". Awaiting physical handover."
            ));
            historyRepository.save(new ItemHistory(
                    foundItem, reviewer, "CLAIM_PENDING", "CLAIM_PENDING",
                    "Claim #" + claim.getId() + " approved. Awaiting physical handover to verified owner " + claim.getClaimantUser().getFullName()
            ));

            // Notify claimant to coordinate pickup/handover
            notificationRepository.save(new Notification(
                    claim.getClaimantUser(),
                    "Claim Approved — Awaiting Handover",
                    "Your claim for \"" + foundItem.getItemName() + "\" was APPROVED! Coordinate with " + foundItem.getUser().getFullName() + " to pick up your belongings at the campus Property Office.",
                    "/claims"
            ));
        } else if (req.getStatus() == ClaimStatus.REJECTED) {
            // Check if there are other active claims on these items before resetting status to ACTIVE
            long activeClaimsLost = claimRepository.countActiveClaimsForItem(lostItem.getId(), claim.getId());
            if (activeClaimsLost == 0) {
                lostItem.setStatus(ItemStatus.ACTIVE);
            }

            long activeClaimsFound = claimRepository.countActiveClaimsForItem(foundItem.getId(), claim.getId());
            if (activeClaimsFound == 0) {
                foundItem.setStatus(ItemStatus.ACTIVE);
                match.setStatus(MatchStatus.SUGGESTED);
            }

            historyRepository.save(new ItemHistory(
                    lostItem, reviewer, "CLAIM_PENDING", lostItem.getStatus().name(),
                    "Claim #" + claim.getId() + " rejected: " + (req.getReviewNote() != null ? req.getReviewNote() : "Ownership details did not match")
            ));
            historyRepository.save(new ItemHistory(
                    foundItem, reviewer, "CLAIM_PENDING", foundItem.getStatus().name(),
                    "Claim #" + claim.getId() + " rejected. Item remains in found pool."
            ));

            // Notify claimant
            notificationRepository.save(new Notification(
                    claim.getClaimantUser(),
                    "Claim Declined",
                    "Your claim for \"" + foundItem.getItemName() + "\" was declined. Reason: " +
                            (req.getReviewNote() != null ? req.getReviewNote() : "Identifying details did not match."),
                    "/claims"
            ));
        }

        itemRepository.save(lostItem);
        itemRepository.save(foundItem);
        matchRepository.save(match);
        Claim saved = claimRepository.save(claim);

        return ClaimResponse.fromEntity(saved);
    }

    @Transactional
    public ClaimResponse markCompleted(Long claimId, User user) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + claimId));

        if (claim.getStatus() != ClaimStatus.APPROVED) {
            throw new BadRequestException("Only approved claims can be marked as completed (current status: " + claim.getStatus() + ")");
        }

        Item foundItem = claim.getMatch().getFoundItem();
        Item lostItem = claim.getMatch().getLostItem();
        Match match = claim.getMatch();

        // Enforce authorization: only finder, claimant, or admin can confirm physical handover
        boolean isFinder = foundItem.getUser().getId().equals(user.getId());
        boolean isClaimant = claim.getClaimantUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isFinder && !isClaimant && !isAdmin) {
            throw new UnauthorizedException("Only the finder, claimant, or administrator can confirm item handover");
        }

        // Transition claim status to COMPLETED
        claim.setStatus(ClaimStatus.COMPLETED);
        Claim savedClaim = claimRepository.save(claim);

        // Transition items to RETURNED
        lostItem.setStatus(ItemStatus.RETURNED);
        foundItem.setStatus(ItemStatus.RETURNED);
        itemRepository.save(lostItem);
        itemRepository.save(foundItem);

        // Mark match as RESOLVED
        match.setStatus(MatchStatus.RESOLVED);
        matchRepository.save(match);

        // Invalidate competing pending claims on the same found item
        List<Claim> competing = claimRepository.findCompetingPendingClaims(foundItem.getId(), claim.getId());
        for (Claim compClaim : competing) {
            compClaim.setStatus(ClaimStatus.REJECTED);
            compClaim.setReviewedAt(LocalDateTime.now());
            claimRepository.save(compClaim);

            historyRepository.save(new ItemHistory(
                    foundItem, user, "CLAIM_PENDING", "RETURNED",
                    "Competing Claim #" + compClaim.getId() + " automatically closed because item was recovered by verified owner."
            ));

            notificationRepository.save(new Notification(
                    compClaim.getClaimantUser(),
                    "Claim Invalidated",
                    "The found item \"" + foundItem.getItemName() + "\" was recovered by another verified owner. Your pending claim has been closed.",
                    "/claims"
            ));
        }

        // Audit logs in ItemHistory
        historyRepository.save(new ItemHistory(
                lostItem, user, "CLAIM_PENDING", "RETURNED",
                "Handover verified and completed by " + user.getFullName() + ". Item recovered."
        ));
        historyRepository.save(new ItemHistory(
                foundItem, user, "CLAIM_PENDING", "RETURNED",
                "Physical handover completed. Item returned to verified owner " + claim.getClaimantUser().getFullName() + " (confirmed by " + user.getFullName() + ")."
        ));

        // Notifications
        if (isFinder) {
            notificationRepository.save(new Notification(
                    claim.getClaimantUser(),
                    "Handover Confirmed — Item Recovered!",
                    "The finder " + foundItem.getUser().getFullName() + " confirmed that your item \"" + lostItem.getItemName() + "\" was successfully handed over.",
                    "/claims"
            ));
        } else if (isClaimant) {
            notificationRepository.save(new Notification(
                    foundItem.getUser(),
                    "Handover Confirmed — Thank You!",
                    claim.getClaimantUser().getFullName() + " confirmed receiving \"" + foundItem.getItemName() + "\". Thank you for turning it in to the Property Office!",
                    "/claims"
            ));
        } else {
            // Admin confirmed
            notificationRepository.save(new Notification(
                    claim.getClaimantUser(),
                    "Handover Confirmed by Administrator",
                    "Campus Property Office administrator " + user.getFullName() + " verified the recovery of \"" + lostItem.getItemName() + "\".",
                    "/claims"
            ));
            notificationRepository.save(new Notification(
                    foundItem.getUser(),
                    "Handover Confirmed by Administrator",
                    "Campus Property Office administrator verified that \"" + foundItem.getItemName() + "\" was returned to its verified owner.",
                    "/claims"
            ));
        }

        return ClaimResponse.fromEntity(savedClaim);
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> getClaimsForUser(User user) {
        return claimRepository.findClaimsInvolvingUser(user.getId())
                .stream()
                .map(ClaimResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> getMySubmittedClaims(User user) {
        return claimRepository.findByClaimantUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(ClaimResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClaimResponse getClaimById(Long claimId, User user) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + claimId));
        checkClaimChatAuthorization(claim, user);
        return ClaimResponse.fromEntity(claim);
    }

    @Transactional
    public List<ClaimMessageResponse> getClaimMessages(Long claimId, User user) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + claimId));
        checkClaimChatAuthorization(claim, user);

        // Mark incoming messages from the counterpart as read
        claimMessageRepository.markMessagesAsReadForUser(claimId, user.getId());

        return claimMessageRepository.findByClaimIdOrderByCreatedAtAsc(claimId)
                .stream()
                .map(ClaimMessageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClaimMessageResponse sendClaimMessage(Long claimId, String content, User sender) {
        if (content == null || content.trim().isEmpty()) {
            throw new BadRequestException("Message content cannot be blank");
        }

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + claimId));
        checkClaimChatAuthorization(claim, sender);

        ClaimMessage message = new ClaimMessage(claim, sender, content.trim());
        ClaimMessage saved = claimMessageRepository.save(message);

        // Notify counterpart
        User recipient = null;
        if (sender.getId().equals(claim.getClaimantUser().getId())) {
            recipient = claim.getMatch().getFoundItem().getUser();
        } else {
            recipient = claim.getClaimantUser();
        }

        if (recipient != null && !recipient.getId().equals(sender.getId())) {
            String preview = content.trim();
            if (preview.length() > 60) {
                preview = preview.substring(0, 57) + "...";
            }
            notificationRepository.save(new Notification(
                    recipient,
                    "New Message from " + sender.getFullName(),
                    preview + " (Claim #" + claim.getId() + " - " + claim.getMatch().getFoundItem().getItemName() + ")",
                    "/claims"
            ));
        }

        return ClaimMessageResponse.fromEntity(saved);
    }

    private void checkClaimChatAuthorization(Claim claim, User user) {
        if (user.getRole() == Role.ADMIN) {
            return; // Admins have oversight over all claims
        }

        Long claimantId = claim.getClaimantUser().getId();
        Long finderId = claim.getMatch().getFoundItem().getUser().getId();
        Long lostUserId = claim.getMatch().getLostItem() != null ? claim.getMatch().getLostItem().getUser().getId() : null;

        boolean isAuthorized = user.getId().equals(claimantId)
                || user.getId().equals(finderId)
                || (lostUserId != null && user.getId().equals(lostUserId));

        if (!isAuthorized) {
            throw new UnauthorizedException("You are not authorized to access direct chat for this claim");
        }
    }
}

