package edu.campus.findit.repository;

import edu.campus.findit.model.ClaimMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimMessageRepository extends JpaRepository<ClaimMessage, Long> {

    List<ClaimMessage> findByClaimIdOrderByCreatedAtAsc(Long claimId);

    @Modifying
    @Query("UPDATE ClaimMessage m SET m.read = true WHERE m.claim.id = :claimId AND m.sender.id <> :userId AND m.read = false")
    int markMessagesAsReadForUser(@Param("claimId") Long claimId, @Param("userId") Long userId);

    @Query("SELECT COUNT(m) FROM ClaimMessage m WHERE m.claim.id = :claimId AND m.sender.id <> :userId AND m.read = false")
    long countUnreadMessages(@Param("claimId") Long claimId, @Param("userId") Long userId);
}

