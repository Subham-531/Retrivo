# FindIt — DBMS Viva Examination & Presentation Guide

This guide is tailored specifically for a **3rd Semester Computer Science & Engineering (CSE) Student** presenting **FindIt: Smart Lost & Found Matcher** for their Database Management Systems (DBMS) laboratory and course viva.

---

## 1. Project High-Level Overview

- **Problem Statement**: College students routinely misplace personal belongings (chargers, ID cards, keys, water bottles, calculators) across sprawling campus buildings. Existing lost-and-found boxes are chaotic, lack privacy, and rely on manual bulletin boards.
- **Solution**: **FindIt** is a full-stack, normalized relational database web application featuring a transparent 5-tier weighted matching engine that pairs LOST and FOUND reports automatically while safeguarding student privacy through masked sensitive fields.

---

## 2. Key Database Questions & Answers (Examiner Favorites)

### Q1: What database engine and storage engine are used? Why?
> **Answer**: MySQL 8.0 with the **InnoDB** storage engine.
> - **InnoDB** is selected because it fully supports **ACID** properties (Atomicity, Consistency, Isolation, Durability).
> - It enforces foreign-key constraints with referential actions (`ON DELETE CASCADE`, `ON DELETE RESTRICT`).
> - It supports row-level locking (`SELECT ... FOR UPDATE`), which prevents race conditions when two students attempt to claim the same found item simultaneously.

### Q2: Prove that your schema is normalized to Third Normal Form (3NF).
> **Answer**:
> 1. **1NF**: All column values are atomic (e.g. `full_name`, `colour`, `event_date`). There are no repeating columns or composite comma-separated arrays. Every relation has a surrogate primary key (`id`).
> 2. **2NF**: It is in 1NF, and because all tables use single-attribute primary keys (`id`), there are **no partial dependencies** on candidate keys. Non-key attributes depend on the entire primary key.
> 3. **3NF**: It is in 2NF, and there are **no transitive dependencies** ($X \to Y \to Z$).
>    - *Example*: In `items`, we do not store `category_name` or `category_description`. If we did, `item_id -> category_id -> category_name` would be a transitive dependency violating 3NF. Instead, we reference `categories(id)` as a foreign key. The same applies to `users(id)`.

### Q3: How do you prevent duplicate match suggestions?
> **Answer**: At the database level, the `matches` table enforces a composite unique constraint:
> ```sql
> CONSTRAINT uk_lost_found_pair UNIQUE (lost_item_id, found_item_id)
> ```
> And a check constraint ensures an item is never matched against itself:
> ```sql
> CONSTRAINT chk_different_items CHECK (lost_item_id <> found_item_id)
> ```

### Q4: Explain the Smart Matching Algorithm.
> **Answer**: The matching engine runs on a transparent **100-point rule-based model**:
> 1. **Category Match (30 points)**: Exact match between category IDs (`lost.category_id = found.category_id`).
> 2. **Colour Match (20 points)**: 20 points for exact colour match, 12 points for partial overlap.
> 3. **Campus Location Proximity (20 points)**: 20 points for exact room/floor match, 16 points for matching campus facility/building zones (e.g., both in "Library" or "CS Lab").
> 4. **Event Date Proximity (20 points)**: Exponential/stepped decay:
>    - Same date = 20 pts
>    - Within 24 hours = 18 pts
>    - Within 48 hours = 16 pts
>    - Within 5 days = 12 pts
>    - Within 10 days = 8 pts
> 5. **Text & Brand Similarity (10 points)**: Normalized Jaccard token overlap on title + description and brand match.
> 
> **Thresholds**:
> - $\ge 80\%$: Strong Match
> - $50 - 79\%$: Possible Match
> - $< 50\%$: Low Compatibility

### Q5: How do transactions guarantee ACID properties during claim recovery?
> **Answer**:
> Approving an ownership claim is a multi-step operation. If one step succeeds but the server crashes before updating the second item, the database enters an inconsistent state.
> In `ClaimService.java` (annotated with `@Transactional`) and in `database/transactions.sql`:
> 1. We lock the claim row with `SELECT ... FOR UPDATE`.
> 2. Update claim status to `APPROVED`.
> 3. Update both `lost_item.status` and `found_item.status` to `RETURNED`.
> 4. Update `matches.status` to `RESOLVED`.
> 5. Insert audit log records into `item_history`.
> 6. Create in-app notification for the claimant.
> 7. All steps commit together. If any check fails, `ROLLBACK` is triggered and nothing is modified.

---

## 3. Top SQL Demonstrations to Run in Front of External Examiner

Open MySQL Workbench or CLI and run queries from `database/important_queries.sql`:

1. **Category Loss Frequency (Aggregate & Group By)**:
   ```sql
   SELECT c.name, COUNT(i.id) AS total_lost
   FROM categories c
   JOIN items i ON c.id = i.category_id
   WHERE i.report_type = 'LOST'
   GROUP BY c.id, c.name
   ORDER BY total_lost DESC;
   ```

2. **Index Performance Verification (`EXPLAIN`)**:
   ```sql
   EXPLAIN SELECT id, item_name, colour, location 
   FROM items 
   WHERE report_type = 'FOUND' AND status = 'ACTIVE' AND category_id = 1;
   ```
   *(Point out to examiner that MySQL uses `idx_items_matching` with `ref` index condition instead of a full-table scan).*

3. **Pending Claims Across 5 Tables (Multi-table INNER JOIN)**:
   ```sql
   SELECT cl.id, cl.message, claimant.full_name, lost_i.item_name, found_i.item_name, m.compatibility_score
   FROM claims cl
   JOIN users claimant ON cl.claimant_user_id = claimant.id
   JOIN matches m ON cl.match_id = m.id
   JOIN items lost_i ON m.lost_item_id = lost_i.id
   JOIN items found_i ON m.found_item_id = found_i.id;
   ```

