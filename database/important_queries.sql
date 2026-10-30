-- ============================================================
-- FindIt: Smart Lost & Found Matcher for College Campuses
-- Important SQL Queries for DBMS Viva & Analytical Demonstrations
-- ============================================================

USE findit_db;

-- ============================================================
-- SECTION 1: REQUIRED SPECIFIC VIVA QUERIES
-- ============================================================

-- Query 1: Most commonly lost categories
-- Demonstrates: INNER JOIN, GROUP BY, Aggregate COUNT, ORDER BY DESC
SELECT 
    c.id AS category_id,
    c.name AS category_name,
    COUNT(i.id) AS total_lost_items
FROM categories c
JOIN items i ON c.id = i.category_id
WHERE i.report_type = 'LOST'
GROUP BY c.id, c.name
ORDER BY total_lost_items DESC;

-- Query 2: Reports by campus location
-- Demonstrates: Conditional Aggregation (SUM with CASE), GROUP BY, Pivot by location
SELECT 
    location,
    COUNT(*) AS total_reports,
    SUM(CASE WHEN report_type = 'LOST' THEN 1 ELSE 0 END) AS lost_items,
    SUM(CASE WHEN report_type = 'FOUND' THEN 1 ELSE 0 END) AS found_items,
    SUM(CASE WHEN status = 'RETURNED' THEN 1 ELSE 0 END) AS recovered_items
FROM items
GROUP BY location
ORDER BY total_reports DESC;

-- Query 3: Monthly recovery rates
-- Demonstrates: DATE_FORMAT, Aggregate calculations, NULLIF to prevent division by zero
SELECT 
    DATE_FORMAT(event_date, '%Y-%m') AS report_month,
    COUNT(*) AS total_items_reported,
    SUM(CASE WHEN status = 'RETURNED' THEN 1 ELSE 0 END) AS items_recovered,
    ROUND(
        (SUM(CASE WHEN status = 'RETURNED' THEN 1 ELSE 0 END) * 100.0) / NULLIF(COUNT(*), 0), 
        2
    ) AS recovery_rate_percentage
FROM items
GROUP BY DATE_FORMAT(event_date, '%Y-%m')
ORDER BY report_month ASC;

-- Query 4: Pending claims awaiting review
-- Demonstrates: Multi-table INNER JOIN across 5 tables
SELECT 
    cl.id AS claim_id,
    cl.created_at AS claim_date,
    cl.message AS claim_message,
    claimant.full_name AS claimant_name,
    claimant.email AS claimant_email,
    lost_i.item_name AS lost_item_name,
    found_i.item_name AS found_item_name,
    finder.full_name AS finder_name,
    m.compatibility_score
FROM claims cl
JOIN users claimant ON cl.claimant_user_id = claimant.id
JOIN matches m ON cl.match_id = m.id
JOIN items lost_i ON m.lost_item_id = lost_i.id
JOIN items found_i ON m.found_item_id = found_i.id
JOIN users finder ON found_i.user_id = finder.id
WHERE cl.status = 'PENDING';

-- Query 5: Users with the most resolved reports
-- Demonstrates: INNER JOIN, WHERE, GROUP BY, HAVING clause, ORDER BY
SELECT 
    u.id AS user_id,
    u.full_name,
    u.email,
    COUNT(i.id) AS recovered_count
FROM users u
JOIN items i ON u.id = i.user_id
WHERE i.status = 'RETURNED'
GROUP BY u.id, u.full_name, u.email
HAVING COUNT(i.id) >= 1
ORDER BY recovered_count DESC;

-- Query 6: Lost reports without any suitable matches
-- Demonstrates: LEFT JOIN with IS NULL filter (anti-join pattern)
SELECT 
    i.id AS lost_item_id,
    i.item_name,
    c.name AS category,
    i.location,
    i.event_date,
    u.full_name AS student_name
FROM items i
JOIN categories c ON i.category_id = c.id
JOIN users u ON i.user_id = u.id
LEFT JOIN matches m ON i.id = m.lost_item_id
WHERE i.report_type = 'LOST' 
  AND i.status = 'ACTIVE'
  AND m.id IS NULL;

-- Query 7: Average time between reporting and recovery
-- Demonstrates: DATEDIFF, AVG aggregate function, subquery join with item history
SELECT 
    ROUND(AVG(DATEDIFF(h.created_at, i.created_at)), 2) AS avg_recovery_days,
    MIN(DATEDIFF(h.created_at, i.created_at)) AS min_recovery_days,
    MAX(DATEDIFF(h.created_at, i.created_at)) AS max_recovery_days,
    COUNT(*) AS total_recovered_items_measured
FROM items i
JOIN item_history h ON i.id = h.item_id
WHERE h.new_status = 'RETURNED';


-- ============================================================
-- SECTION 2: ADDITIONAL ADVANCED SQL CONCEPTS FOR DBMS VIVA
-- ============================================================

-- Concept 1: Correlated Subquery
-- Finds items whose compatibility score is higher than the average score of all matches
SELECT 
    m.id AS match_id,
    l.item_name AS lost_item,
    f.item_name AS found_item,
    m.compatibility_score
FROM matches m
JOIN items l ON m.lost_item_id = l.id
JOIN items f ON m.found_item_id = f.id
WHERE m.compatibility_score > (
    SELECT AVG(compatibility_score) FROM matches
);

-- Concept 2: Subquery with IN
-- Lists categories that have at least one active lost item currently unresolved
SELECT name, description 
FROM categories 
WHERE id IN (
    SELECT DISTINCT category_id 
    FROM items 
    WHERE report_type = 'LOST' AND status = 'ACTIVE'
);

-- Concept 3: Window Function (MySQL 8.0)
-- Ranks matches for each lost item by compatibility score descending
SELECT 
    lost_item_id,
    found_item_id,
    compatibility_score,
    DENSE_RANK() OVER (PARTITION BY lost_item_id ORDER BY compatibility_score DESC) as match_rank
FROM matches;

