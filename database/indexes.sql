-- ============================================================
-- FindIt: Smart Lost & Found Matcher for College Campuses
-- Database Indexes & Performance Optimization Script
-- ============================================================

USE findit_db;

-- ------------------------------------------------------------
-- 1. INDEXES FOR SMART MATCHING & FILTERING
-- The matching engine frequently pairs LOST items with compatible FOUND items
-- by checking category_id, report_type, and status.
-- ------------------------------------------------------------
-- Composite index for fast matching candidate retrieval:
CREATE INDEX idx_items_matching 
ON items (report_type, status, category_id, event_date);

-- Index for searching items by location:
CREATE INDEX idx_items_location 
ON items (location);

-- Index for filtering items by date:
CREATE INDEX idx_items_event_date 
ON items (event_date);

-- Index for searching user reports:
CREATE INDEX idx_items_user_status 
ON items (user_id, status);

-- ------------------------------------------------------------
-- 2. INDEXES FOR MATCHES & CLAIMS
-- Optimizes lookup of matches for an item and pending claims
-- ------------------------------------------------------------
CREATE INDEX idx_matches_lost_score 
ON matches (lost_item_id, compatibility_score DESC);

CREATE INDEX idx_matches_found_score 
ON matches (found_item_id, compatibility_score DESC);

CREATE INDEX idx_claims_match_status 
ON claims (match_id, status);

CREATE INDEX idx_claims_claimant 
ON claims (claimant_user_id, status);

-- ------------------------------------------------------------
-- 3. INDEX FOR USER NOTIFICATIONS
-- Fast retrieval of unread notifications for student dashboard
-- ------------------------------------------------------------
CREATE INDEX idx_notifications_user_unread 
ON notifications (user_id, is_read, created_at DESC);

-- ------------------------------------------------------------
-- 4. EXPLAIN PLAN DEMONSTRATIONS FOR VIVA
-- ------------------------------------------------------------

-- Demo Query 1: Finding compatible items for matching (Uses idx_items_matching)
EXPLAIN SELECT id, item_name, colour, location, event_date 
FROM items 
WHERE report_type = 'FOUND' 
  AND status = 'ACTIVE' 
  AND category_id = 1;

-- Demo Query 2: Retrieving student unread notifications (Uses idx_notifications_user_unread)
EXPLAIN SELECT id, title, message, created_at 
FROM notifications 
WHERE user_id = 2 
  AND is_read = FALSE 
ORDER BY created_at DESC;

