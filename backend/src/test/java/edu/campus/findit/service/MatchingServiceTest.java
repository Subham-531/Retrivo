package edu.campus.findit.service;

import edu.campus.findit.model.Category;
import edu.campus.findit.model.Item;
import edu.campus.findit.repository.ItemRepository;
import edu.campus.findit.repository.MatchRepository;
import edu.campus.findit.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class MatchingServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private NotificationRepository notificationRepository;

    private MatchingService matchingService;

    private Category electronicsCategory;
    private Category clothingCategory;

    @BeforeEach
    void setUp() {
        matchingService = new MatchingService(matchRepository, itemRepository, notificationRepository);

        electronicsCategory = new Category();
        electronicsCategory.setId(1);
        electronicsCategory.setName("Electronics");

        clothingCategory = new Category();
        clothingCategory.setId(2);
        clothingCategory.setName("Clothing & Accessories");
    }

    @Test
    @DisplayName("Exact match across all 5 criteria yields maximum 100 score")
    void testExactMatchAllCriteria() {
        LocalDate today = LocalDate.now();

        Item lost = new Item();
        lost.setId(101L);
        lost.setItemName("Apple MacBook Air M2 Space Gray");
        lost.setCategory(electronicsCategory);
        lost.setColour("Space Gray");
        lost.setLocation("Main Library Reading Room 3F");
        lost.setEventDate(today);
        lost.setBrand("Apple");
        lost.setDescription("Space gray MacBook Air laptop with sticker on lid");

        Item found = new Item();
        found.setId(201L);
        found.setItemName("Apple MacBook Air M2 Space Gray");
        found.setCategory(electronicsCategory);
        found.setColour("Space Gray");
        found.setLocation("Main Library Reading Room 3F");
        found.setEventDate(today);
        found.setBrand("Apple");
        found.setDescription("Space gray MacBook Air laptop with sticker on lid");

        MatchingService.MatchResult result = matchingService.calculateCompatibility(lost, found);

        assertEquals(100, result.score, "Perfect match across all 5 criteria must equal 100 points");
        assertTrue(result.reasonsJson.contains("Exact category match"));
        assertTrue(result.reasonsJson.contains("Identical primary colour"));
        assertTrue(result.reasonsJson.contains("Exact campus location match"));
        assertTrue(result.reasonsJson.contains("Reported on the exact same date"));
        assertTrue(result.reasonsJson.contains("Matching brand"));
    }

    @Test
    @DisplayName("Category mismatch awards 0 category points")
    void testCategoryMismatch() {
        LocalDate today = LocalDate.now();

        Item lost = new Item();
        lost.setCategory(electronicsCategory);
        lost.setColour("Black");
        lost.setLocation("Library");
        lost.setEventDate(today);
        lost.setItemName("Noise Cancelling Headphones");
        lost.setDescription("Black over-ear headphones");

        Item found = new Item();
        found.setCategory(clothingCategory);
        found.setColour("Black");
        found.setLocation("Library");
        found.setEventDate(today);
        found.setItemName("Winter Wool Scarf");
        found.setDescription("Black winter knit scarf");

        MatchingService.MatchResult result = matchingService.calculateCompatibility(lost, found);

        assertTrue(result.reasonsJson.contains("Category mismatch"));
        // Score should not include the 30 category points
        assertTrue(result.score <= 70, "Score without category match must be <= 70");
    }

    @Test
    @DisplayName("Date proximity scoring degrades properly with elapsed days")
    void testDateProximityDegradation() {
        LocalDate baseDate = LocalDate.of(2026, 3, 10);

        Item lost = new Item();
        lost.setCategory(electronicsCategory);
        lost.setEventDate(baseDate);
        lost.setItemName("USB Drive");
        lost.setDescription("Flash drive");

        Item foundSameDay = new Item();
        foundSameDay.setCategory(electronicsCategory);
        foundSameDay.setEventDate(baseDate);
        foundSameDay.setItemName("USB Drive");
        foundSameDay.setDescription("Flash drive");

        Item found1DayLater = new Item();
        found1DayLater.setCategory(electronicsCategory);
        found1DayLater.setEventDate(baseDate.plusDays(1));
        found1DayLater.setItemName("USB Drive");
        found1DayLater.setDescription("Flash drive");

        Item found4DaysLater = new Item();
        found4DaysLater.setCategory(electronicsCategory);
        found4DaysLater.setEventDate(baseDate.plusDays(4));
        found4DaysLater.setItemName("USB Drive");
        found4DaysLater.setDescription("Flash drive");

        Item found15DaysLater = new Item();
        found15DaysLater.setCategory(electronicsCategory);
        found15DaysLater.setEventDate(baseDate.plusDays(15));
        found15DaysLater.setItemName("USB Drive");
        found15DaysLater.setDescription("Flash drive");

        int scoreSameDay = matchingService.calculateCompatibility(lost, foundSameDay).score;
        int score1Day = matchingService.calculateCompatibility(lost, found1DayLater).score;
        int score4Days = matchingService.calculateCompatibility(lost, found4DaysLater).score;
        int score15Days = matchingService.calculateCompatibility(lost, found15DaysLater).score;

        assertTrue(scoreSameDay > score1Day, "Same day (+16) should score higher than 1 day (+14)");
        assertTrue(score1Day > score4Days, "1 day (+14) should score higher than 4 days (+8)");
        assertTrue(score4Days > score15Days, "4 days (+8) should score higher than 15 days (+0)");
    }

    @Test
    @DisplayName("Found item event date preceding lost date by more than 1 day receives 0 date points")
    void testFoundBeforeLostDate() {
        LocalDate lostDate = LocalDate.of(2026, 3, 15);
        LocalDate foundDate = LocalDate.of(2026, 3, 10); // 5 days before lost

        Item lost = new Item();
        lost.setCategory(electronicsCategory);
        lost.setEventDate(lostDate);
        lost.setItemName("Calculator");
        lost.setDescription("Scientific calculator");

        Item found = new Item();
        found.setCategory(electronicsCategory);
        found.setEventDate(foundDate);
        found.setItemName("Calculator");
        found.setDescription("Scientific calculator");

        MatchingService.MatchResult result = matchingService.calculateCompatibility(lost, found);
        assertTrue(result.reasonsJson.contains("Found date precedes loss date (+0 pts)"));
    }

    @Test
    @DisplayName("Campus facility zone matching awards partial 16 points")
    void testCampusZoneMatching() {
        Item lost = new Item();
        lost.setCategory(electronicsCategory);
        lost.setLocation("Central Library 2nd Floor");
        lost.setItemName("Notebook");
        lost.setDescription("Spiral notebook");

        Item found = new Item();
        found.setCategory(electronicsCategory);
        found.setLocation("Library Basement Storage");
        found.setItemName("Notebook");
        found.setDescription("Spiral notebook");

        MatchingService.MatchResult result = matchingService.calculateCompatibility(lost, found);
        assertTrue(result.reasonsJson.contains("Same campus zone / facility"));
    }
}
