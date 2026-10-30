package edu.campus.findit.repository;

import edu.campus.findit.model.ItemHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemHistoryRepository extends JpaRepository<ItemHistory, Long> {

    List<ItemHistory> findByItemIdOrderByCreatedAtDesc(Long itemId);
}

