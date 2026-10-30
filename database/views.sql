-- ============================================================
-- FindIt: Smart Lost & Found Matcher for College Campuses
-- Database Views for Reporting, Security & Performance
-- ============================================================

USE findit_db;

-- ------------------------------------------------------------
-- 1. VIEW: vw_active_reports
-- Combines item details with reporter info and category name
-- Masks confidential identifying details for public browsing
-- ------------------------------------------------------------
CREATE OR REPLACE VIEW vw_active_reports AS
SELECT 
    i.id AS item_id,
    i.item_name,
    c.name AS category_name,
    i.colour,
    i.brand,
    i.description,
    i.location,
    i.event_date,
    i.report_type,
    i.status,
    i.image_path,
    u.id AS reporter_user_id,
    u.full_name AS reporter_name,
    u.email AS reporter_email,
    i.created_at
FROM items i
JOIN categories c ON i.category_id = c.id
JOIN users u ON i.user_id = u.id
WHERE i.status IN ('ACTIVE', 'CLAIM_PENDING');

-- ------------------------------------------------------------
-- 2. VIEW: vw_high_confidence_matches
-- Shows matches with score >= 80% along with lost & found item details
-- ------------------------------------------------------------
CREATE OR REPLACE VIEW vw_high_confidence_matches AS
SELECT 
    m.id AS match_id,
    m.compatibility_score,
    m.matching_reasons,
    m.status AS match_status,
    m.created_at AS matched_at,
    -- Lost Item Info
    l.id AS lost_item_id,
    l.item_name AS lost_item_name,
    u_lost.full_name AS lost_by_student,
    l.location AS lost_location,
    l.event_date AS date_lost,
    -- Found Item Info
    f.id AS found_item_id,
    f.item_name AS found_item_name,
    u_found.full_name AS found_by_student,
    f.location AS found_location,
    f.event_date AS date_found
FROM matches m
JOIN items l ON m.lost_item_id = l.id
JOIN users u_lost ON l.user_id = u_lost.id
JOIN items f ON m.found_item_id = f.id
JOIN users u_found ON f.user_id = u_found.id
WHERE m.compatibility_score >= 80;

-- ------------------------------------------------------------
-- 3. VIEW: vw_category_loss_statistics
-- Aggregates total reports, active count, and recovery rate per category
-- ------------------------------------------------------------
CREATE OR REPLACE VIEW vw_category_loss_statistics AS
SELECT 
    c.id AS category_id,
    c.name AS category_name,
    COUNT(i.id) AS total_reports,
    SUM(CASE WHEN i.report_type = 'LOST' THEN 1 ELSE 0 END) AS lost_count,
    SUM(CASE WHEN i.report_type = 'FOUND' THEN 1 ELSE 0 END) AS found_count,
    SUM(CASE WHEN i.status = 'RETURNED' THEN 1 ELSE 0 END) AS recovered_count,
    ROUND(
        (SUM(CASE WHEN i.status = 'RETURNED' THEN 1 ELSE 0 END) / NULLIF(COUNT(i.id), 0)) * 100, 
        2
    ) AS recovery_rate_percentage
FROM categories c
LEFT JOIN items i ON c.id = i.category_id
GROUP BY c.id, c.name;

-- ------------------------------------------------------------
-- 4. VIEW: vw_pending_claims_detail
-- Complete details of claims currently awaiting review
-- ------------------------------------------------------------
CREATE OR REPLACE VIEW vw_pending_claims_detail AS
SELECT 
    cl.id AS claim_id,
    cl.status AS claim_status,
    cl.message AS claim_message,
    cl.created_at AS claim_submitted_at,
    claimant.id AS claimant_id,
    claimant.full_name AS claimant_name,
    claimant.email AS claimant_email,
    m.id AS match_id,
    m.compatibility_score,
    lost_item.item_name AS lost_item_name,
    found_item.item_name AS found_item_name,
    found_item.location AS found_location,
    finder.full_name AS found_by_user
FROM claims cl
JOIN users claimant ON cl.claimant_user_id = claimant.id
JOIN matches m ON cl.match_id = m.id
JOIN items lost_item ON m.lost_item_id = lost_item.id
JOIN items found_item ON m.found_item_id = found_item.id
JOIN users finder ON found_item.user_id = finder.id
WHERE cl.status = 'PENDING';

