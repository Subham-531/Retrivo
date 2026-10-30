-- ============================================================
-- Retrivo: Smart Campus Lost & Found Matcher
-- Relational Database Schema (MySQL 8.0)
-- Fully Normalized (3NF) with Constraints, Indexes & Foreign Keys
-- ============================================================

CREATE DATABASE IF NOT EXISTS findit_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE findit_db;

-- Drop tables in reverse dependency order for clean reinstalls if needed
DROP TABLE IF EXISTS item_history;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS claim_messages;
DROP TABLE IF EXISTS claims;
DROP TABLE IF EXISTS matches;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

-- ------------------------------------------------------------
-- 1. USERS TABLE
-- Stores student and administrator accounts with BCrypt-hashed passwords.
-- ------------------------------------------------------------
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('STUDENT', 'ADMIN') NOT NULL DEFAULT 'STUDENT',
    phone VARCHAR(20) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_email CHECK (email LIKE '%@%._%')
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 2. CATEGORIES TABLE
-- Master catalog for item categories (electronics, stationery, etc.)
-- ------------------------------------------------------------
CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    icon VARCHAR(50) NULL
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 3. ITEMS TABLE
-- Stores both LOST and FOUND item reports submitted by students.
-- Sensitive identifying details are stored separately from description.
-- ------------------------------------------------------------
CREATE TABLE items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id INT NOT NULL,
    item_name VARCHAR(150) NOT NULL,
    colour VARCHAR(50) NOT NULL,
    brand VARCHAR(80) NULL,
    description TEXT NOT NULL,
    location VARCHAR(150) NOT NULL,
    event_date DATE NOT NULL,
    report_type ENUM('LOST', 'FOUND') NOT NULL,
    status ENUM('ACTIVE', 'CLAIM_PENDING', 'RETURNED', 'CLOSED') NOT NULL DEFAULT 'ACTIVE',
    image_path VARCHAR(255) NULL,
    identifying_details TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_items_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_items_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 4. MATCHES TABLE
-- Stores computed compatibility scores between LOST and FOUND items.
-- Enforces uniqueness so identical item pairs are never duplicated.
-- ------------------------------------------------------------
CREATE TABLE matches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lost_item_id BIGINT NOT NULL,
    found_item_id BIGINT NOT NULL,
    compatibility_score INT NOT NULL,
    matching_reasons TEXT NOT NULL,
    status ENUM('SUGGESTED', 'CLAIMED', 'DISMISSED', 'RESOLVED') NOT NULL DEFAULT 'SUGGESTED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_matches_lost FOREIGN KEY (lost_item_id) REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT fk_matches_found FOREIGN KEY (found_item_id) REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT uk_lost_found_pair UNIQUE (lost_item_id, found_item_id),
    CONSTRAINT chk_score_range CHECK (compatibility_score BETWEEN 0 AND 100),
    CONSTRAINT chk_different_items CHECK (lost_item_id <> found_item_id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 5. CLAIMS TABLE
-- Records ownership claims initiated by students against matched reports.
-- ------------------------------------------------------------
CREATE TABLE claims (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    match_id BIGINT NOT NULL,
    claimant_user_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED') NOT NULL DEFAULT 'PENDING',
    reviewed_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_claims_match FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE,
    CONSTRAINT fk_claims_claimant FOREIGN KEY (claimant_user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 5B. CLAIM MESSAGES TABLE (DIRECT IN-APP CHAT)
-- Real-time direct chat between claimant and finder to verify and coordinate handover.
-- ------------------------------------------------------------
CREATE TABLE claim_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    claim_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_claim_messages_claim FOREIGN KEY (claim_id) REFERENCES claims(id) ON DELETE CASCADE,
    CONSTRAINT fk_claim_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_claim_messages_claim_id (claim_id),
    INDEX idx_claim_messages_created_at (created_at)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 6. NOTIFICATIONS TABLE
-- Real-time in-app user notifications for matches, claims & returns.
-- ------------------------------------------------------------
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    link VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 7. ITEM_HISTORY TABLE
-- Audit log of status transitions (e.g. ACTIVE -> CLAIM_PENDING -> RETURNED)
-- ------------------------------------------------------------
CREATE TABLE item_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    changed_by_user_id BIGINT NOT NULL,
    previous_status VARCHAR(50) NOT NULL,
    new_status VARCHAR(50) NOT NULL,
    change_note VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_history_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT fk_history_user FOREIGN KEY (changed_by_user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

