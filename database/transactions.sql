-- ============================================================
-- FindIt: Smart Lost & Found Matcher for College Campuses
-- Multi-Statement ACID Transactions (Commit & Rollback Scenarios)
-- Perfect for DBMS Viva demonstration of Atomicity & Isolation
-- ============================================================

USE findit_db;

-- ------------------------------------------------------------
-- TRANSACTION 1: SUCCESSFUL CLAIM APPROVAL & RECOVERY (COMMIT)
-- Atomically updates the claim, both items, match record,
-- logs the audit trail in item_history, and delivers a notification.
-- ------------------------------------------------------------
START TRANSACTION;

-- Step 1: Lock and verify the pending claim record
SELECT id, match_id, claimant_user_id, status 
FROM claims 
WHERE id = 1 AND status = 'PENDING' 
FOR UPDATE;

-- Step 2: Update claim status to APPROVED and set reviewed_at timestamp
UPDATE claims 
SET status = 'APPROVED', reviewed_at = CURRENT_TIMESTAMP 
WHERE id = 1;

-- Step 3: Update Match status to RESOLVED
UPDATE matches 
SET status = 'RESOLVED' 
WHERE id = 2;

-- Step 4: Update both the LOST item and FOUND item to RETURNED
UPDATE items 
SET status = 'RETURNED' 
WHERE id IN (2, 8);

-- Step 5: Insert audit history logs for both items
INSERT INTO item_history (item_id, changed_by_user_id, previous_status, new_status, change_note)
VALUES 
(2, 5, 'CLAIM_PENDING', 'RETURNED', 'Claim approved by finder Rohan Mehta. Handed over to Aarav Sharma.'),
(8, 5, 'CLAIM_PENDING', 'RETURNED', 'Claim approved. Handed over to verified owner Aarav Sharma.');

-- Step 6: Notify the claimant of approval
INSERT INTO notifications (user_id, title, message, is_read, link)
VALUES (3, 'Claim Approved!', 'Your claim for Fossil Wallet has been approved. Please coordinate pickup with the finder.', FALSE, '/claims');

-- Commit the transaction to persist all changes atomically
COMMIT;


-- ------------------------------------------------------------
-- TRANSACTION 2: CONFLICT DETECTION & ROLLBACK SCENARIO
-- Demonstrates how transactions protect data integrity when an
-- invalid operation or race condition occurs.
-- ------------------------------------------------------------
START TRANSACTION;

-- Scenario: Student tries to submit a claim for an already RETURNED item
-- Check item status
SET @target_lost_id = 4;
SET @current_status = (SELECT status FROM items WHERE id = @target_lost_id);

-- Simulate application validation check
-- If item is already RETURNED, we must abort the transaction
INSERT INTO item_history (item_id, changed_by_user_id, previous_status, new_status, change_note)
VALUES (@target_lost_id, 2, @current_status, 'CLAIM_PENDING', 'Invalid claim attempt on resolved item');

-- Validation check: detect conflict
-- (In SQL script viva demonstration, we deliberately rollback to show ACID atomicity)
ROLLBACK;

-- Verify that the history table has no orphan record from the aborted attempt
SELECT * FROM item_history WHERE item_id = 4 AND change_note LIKE '%Invalid claim%';
-- (Returns 0 rows, proving rollback succeeded!)

