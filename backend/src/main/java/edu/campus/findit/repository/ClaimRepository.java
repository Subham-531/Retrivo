package edu.campus.findit.repository;

import edu.campus.findit.model.Claim;
import edu.campus.findit.model.enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByClaimantUserIdOrderByCreatedAtDesc(Long claimantUserId);

    @Query("SELECT c FROM Claim c WHERE c.match.foundItem.user.id = :userId OR c.match.lostItem.user.id = :userId ORDER BY c.createdAt DESC")
    List<Claim> findClaimsInvolvingUser(@Param("userId") Long userId);

    @Query("SELECT c FROM Claim c WHERE c.match.id = :matchId AND c.claimantUser.id = :claimantId")
    Optional<Claim> findExistingClaim(@Param("matchId") Long matchId, @Param("claimantId") Long claimantId);

    @Query("SELECT c FROM Claim c WHERE c.match.foundItem.id = :foundItemId AND c.id != :excludedClaimId AND c.status = 'PENDING'")
    List<Claim> findCompetingPendingClaims(@Param("foundItemId") Long foundItemId, @Param("excludedClaimId") Long excludedClaimId);

    @Query("SELECT COUNT(c) FROM Claim c WHERE (c.match.foundItem.id = :itemId OR c.match.lostItem.id = :itemId) AND c.id != :excludedClaimId AND c.status IN ('PENDING', 'APPROVED')")
    long countActiveClaimsForItem(@Param("itemId") Long itemId, @Param("excludedClaimId") Long excludedClaimId);

    long countByStatus(ClaimStatus status);

    @Query("SELECT COUNT(c) FROM Claim c WHERE (c.match.foundItem.user.id = :userId OR c.match.lostItem.user.id = :userId) AND c.status = 'PENDING'")
    long countPendingClaimsForUser(@Param("userId") Long userId);
}

