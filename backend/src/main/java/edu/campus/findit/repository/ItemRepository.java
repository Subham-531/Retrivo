package edu.campus.findit.repository;

import edu.campus.findit.model.Item;
import edu.campus.findit.model.enums.ItemStatus;
import edu.campus.findit.model.enums.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    List<Item> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndReportTypeAndStatus(Long userId, ReportType reportType, ItemStatus status);

    long countByReportType(ReportType reportType);

    long countByStatus(ItemStatus status);

    @Query("SELECT i FROM Item i WHERE i.reportType = :reportType AND i.status = :status AND i.id <> :excludeId")
    List<Item> findCandidateItemsForMatching(
            @Param("reportType") ReportType reportType,
            @Param("status") ItemStatus status,
            @Param("excludeId") Long excludeId
    );

    @Query("SELECT i FROM Item i WHERE " +
           "(:query IS NULL OR LOWER(i.itemName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(i.description) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(i.location) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:reportType IS NULL OR i.reportType = :reportType) AND " +
           "(:categoryId IS NULL OR i.category.id = :categoryId) AND " +
           "(:colour IS NULL OR LOWER(i.colour) = LOWER(:colour)) AND " +
           "(:location IS NULL OR LOWER(i.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:status IS NULL OR i.status = :status)")
    Page<Item> searchAndFilter(
            @Param("query") String query,
            @Param("reportType") ReportType reportType,
            @Param("categoryId") Integer categoryId,
            @Param("colour") String colour,
            @Param("location") String location,
            @Param("status") ItemStatus status,
            Pageable pageable
    );

    List<Item> findTop6ByStatusOrderByCreatedAtDesc(ItemStatus status);

    @Query("SELECT c.name, COUNT(i.id) FROM Category c LEFT JOIN Item i ON c.id = i.category.id GROUP BY c.id, c.name")
    List<Object[]> getCategoryItemCounts();

    @Query("SELECT FUNCTION('DATE_FORMAT', i.eventDate, '%Y-%m'), COUNT(i.id), SUM(CASE WHEN i.status = 'RETURNED' THEN 1 ELSE 0 END) " +
           "FROM Item i GROUP BY FUNCTION('DATE_FORMAT', i.eventDate, '%Y-%m') ORDER BY FUNCTION('DATE_FORMAT', i.eventDate, '%Y-%m') ASC")
    List<Object[]> getMonthlyItemStats();
}

