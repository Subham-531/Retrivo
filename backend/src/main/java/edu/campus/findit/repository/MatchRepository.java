package edu.campus.findit.repository;

import edu.campus.findit.model.Match;
import edu.campus.findit.model.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    Optional<Match> findByLostItemIdAndFoundItemId(Long lostItemId, Long foundItemId);

    @Query("SELECT m FROM Match m WHERE m.lostItem.id = :itemId OR m.foundItem.id = :itemId ORDER BY m.compatibilityScore DESC")
    List<Match> findByItemId(@Param("itemId") Long itemId);

    @Query("SELECT m FROM Match m WHERE " +
           "(m.lostItem.user.id = :userId OR m.foundItem.user.id = :userId) " +
           "AND m.compatibilityScore >= :minScore " +
           "ORDER BY m.compatibilityScore DESC")
    List<Match> findRelevantMatchesForUser(@Param("userId") Long userId, @Param("minScore") Integer minScore);

    @Query("SELECT m FROM Match m WHERE m.compatibilityScore >= :minScore ORDER BY m.compatibilityScore DESC")
    List<Match> findAllAboveThreshold(@Param("minScore") Integer minScore);

    long countByStatus(MatchStatus status);
}

