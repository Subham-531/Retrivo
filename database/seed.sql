-- ============================================================
-- Retrivo: Smart Campus Lost & Found Matcher
-- Realistic Seed Data for Testing & Demonstration
-- Passwords:
--   Admin:   Admin@123   ($2a$10$e8Z4J8qB5wH3sH0rF4q8.uD3fI2mQ6y8W1p3o5u7x9z1y3w5v7u9.)
--   Student: Student@123 ($2a$10$e8Z4J8qB5wH3sH0rF4q8.uD3fI2mQ6y8W1p3o5u7x9z1y3w5v7u9.)
-- ============================================================

USE findit_db;

-- ------------------------------------------------------------
-- SEED USERS
-- ------------------------------------------------------------
INSERT INTO users (id, full_name, email, password_hash, role, phone, created_at) VALUES
(1, 'Campus Security Administrator', 'admin@findit.campus.edu', '$2a$10$e8Z4J8qB5wH3sH0rF4q8.uD3fI2mQ6y8W1p3o5u7x9z1y3w5v7u9.', 'ADMIN', '+1-555-0100', '2026-10-01 08:00:00'),
(2, 'Suyash Kaistha', 'suyash@campus.edu', '$2a$10$e8Z4J8qB5wH3sH0rF4q8.uD3fI2mQ6y8W1p3o5u7x9z1y3w5v7u9.', 'STUDENT', '+1-555-0101', '2026-10-02 09:15:00'),
(3, 'Aarav Sharma', 'aarav@campus.edu', '$2a$10$e8Z4J8qB5wH3sH0rF4q8.uD3fI2mQ6y8W1p3o5u7x9z1y3w5v7u9.', 'STUDENT', '+1-555-0102', '2026-10-03 11:30:00'),
(4, 'Ananya Verma', 'ananya@campus.edu', '$2a$10$e8Z4J8qB5wH3sH0rF4q8.uD3fI2mQ6y8W1p3o5u7x9z1y3w5v7u9.', 'STUDENT', '+1-555-0103', '2026-10-04 14:00:00'),
(5, 'Rohan Mehta', 'rohan@campus.edu', '$2a$10$e8Z4J8qB5wH3sH0rF4q8.uD3fI2mQ6y8W1p3o5u7x9z1y3w5v7u9.', 'STUDENT', '+1-555-0104', '2026-10-05 16:45:00'),
(6, 'Priya Patel', 'priya@campus.edu', '$2a$10$e8Z4J8qB5wH3sH0rF4q8.uD3fI2mQ6y8W1p3o5u7x9z1y3w5v7u9.', 'STUDENT', '+1-555-0105', '2026-10-06 10:20:00'),
(7, 'Retrivo Administrator', 'admin@retrivo.campus.edu', '$2a$10$e8Z4J8qB5wH3sH0rF4q8.uD3fI2mQ6y8W1p3o5u7x9z1y3w5v7u9.', 'ADMIN', '+1-555-0100', '2026-10-01 08:00:00');

-- ------------------------------------------------------------
-- SEED CATEGORIES
-- ------------------------------------------------------------
INSERT INTO categories (id, name, description, icon) VALUES
(1, 'Electronics & Gadgets', 'Laptops, chargers, headphones, calculators, USB drives and cables', 'laptop'),
(2, 'Bags & Wallets', 'Backpacks, side bags, clutches, money wallets and card holders', 'briefcase'),
(3, 'Books & Stationery', 'Course textbooks, notebooks, pencil cases, geometry boxes and folders', 'book'),
(4, 'Keys & Access Cards', 'Hostel dorm room keys, vehicle keys, lab smart cards and lanyard badges', 'key'),
(5, 'Clothing & Accessories', 'Jackets, hoodies, caps, sunglasses, wristwatches and umbrellas', 'shirt'),
(6, 'ID Cards & Documents', 'University ID cards, driver licenses, exam admission tickets and certificates', 'file-text'),
(7, 'Water Bottles & Flasks', 'Insulated thermos flasks, metal gym bottles and tumblers', 'cup-soda'),
(8, 'Other Personal Items', 'Sports equipment, musical instruments, jewelry and miscellaneous belongings', 'box');

-- ------------------------------------------------------------
-- SEED ITEMS (LOST & FOUND)
-- ------------------------------------------------------------
INSERT INTO items (id, user_id, category_id, item_name, colour, brand, description, location, event_date, report_type, status, image_path, identifying_details, created_at) VALUES
-- Lost items
(1, 2, 1, 'Blue HP Pavilion Laptop Charger (65W)', 'Blue', 'HP', 'Black charging adapter block with blue barrel tip for HP Pavilion 15. Left at silent study cubicle.', 'Library 2nd Floor', '2026-10-24', 'LOST', 'ACTIVE', 'charger.jpg', 'Small notch scratch near the 3-pin wall plug cable', '2026-10-24 16:30:00'),
(2, 3, 2, 'Black Leather Fossil Bi-fold Wallet', 'Black', 'Fossil', 'Dark brown-black genuine leather bi-fold wallet. Dropped near the cafeteria checkout counter.', 'Main Cafeteria', '2026-10-25', 'LOST', 'CLAIM_PENDING', 'wallet.jpg', 'Student ID 25BCS10108 inside cash sleeve and blue metro transit pass', '2026-10-25 13:10:00'),
(3, 4, 1, 'Silver Apple AirPods Pro with Case', 'White', 'Apple', 'AirPods Pro 2nd Gen in white case with small silicone lanyard. Left on lab computer desk.', 'CS Computer Lab B', '2026-10-26', 'LOST', 'ACTIVE', 'airpods.jpg', 'Subtle laser engraving of initials A.V. on bottom rear of case', '2026-10-26 17:45:00'),
(4, 5, 3, 'Casio FX-991EX Scientific Calculator', 'Black', 'Casio', 'ClassWiz advanced scientific calculator with carbon pattern faceplate. Misplaced after exam.', 'Lecture Hall 102', '2026-10-22', 'LOST', 'RETURNED', 'calculator.jpg', 'Small barcode sticker CS-2026 on rear battery compartment', '2026-10-22 12:00:00'),
(5, 6, 7, 'Stainless Steel Milton Water Flask (750ml)', 'Silver', 'Milton', 'Silver brushed stainless steel insulated water flask with blue silicon gripper band.', 'Sports Complex', '2026-10-27', 'LOST', 'ACTIVE', 'bottle.jpg', 'Small indentation dent on the steel twist cap', '2026-10-27 18:20:00'),
(6, 2, 4, 'Dorm Room Key with Red Batman Keychain', 'Red', 'Yale', 'Brass cylinder key attached to a rubberized red and black Batman logo ring.', 'Hostel Block C Entrance', '2026-10-26', 'LOST', 'ACTIVE', 'keys.jpg', 'Key stamped with room number 304 on base', '2026-10-26 21:00:00'),

-- Found items
(7, 4, 1, 'HP Laptop Adapter Charger with Blue Connector Tip', 'Blue', 'HP', 'Found plugged into wall socket at reading desk 14 on library second floor.', 'Library 2nd Floor', '2026-10-24', 'FOUND', 'ACTIVE', 'found_charger.jpg', NULL, '2026-10-24 18:00:00'),
(8, 5, 2, 'Men\'s Black Leather Pocket Wallet', 'Black', 'Fossil', 'Found on corner table tray near tray-return rack in main cafeteria.', 'Main Cafeteria', '2026-10-25', 'FOUND', 'CLAIM_PENDING', 'found_wallet.jpg', NULL, '2026-10-25 14:00:00'),
(9, 6, 1, 'White Wireless Earbuds with Charging Case', 'White', 'Apple', 'Left on desk next to Workstation 18 in computer science lab B.', 'CS Computer Lab B', '2026-10-26', 'FOUND', 'ACTIVE', 'found_earbuds.jpg', NULL, '2026-10-26 19:15:00'),
(10, 3, 3, 'Casio Scientific Calculator ClassWiz Series', 'Black', 'Casio', 'Found on front instructor podium bench in lecture room 102.', 'Lecture Hall 102', '2026-10-22', 'FOUND', 'RETURNED', 'found_calc.jpg', NULL, '2026-10-22 13:30:00'),
(11, 2, 7, 'Silver Insulated Metal Water Bottle', 'Silver', 'Milton', 'Found under seating bleachers on court 2 in indoor sports complex.', 'Sports Complex', '2026-10-27', 'FOUND', 'ACTIVE', 'found_bottle.jpg', NULL, '2026-10-27 19:00:00'),
(12, 3, 1, 'Sony WH-1000XM4 Wireless Headphones', 'Black', 'Sony', 'Found resting on bean bag couch in the SAC common entertainment lounge.', 'Student Activity Center', '2026-10-28', 'FOUND', 'ACTIVE', 'found_sony.jpg', NULL, '2026-10-28 15:30:00');

-- ------------------------------------------------------------
-- SEED MATCHES
-- ------------------------------------------------------------
INSERT INTO matches (id, lost_item_id, found_item_id, compatibility_score, matching_reasons, status, created_at) VALUES
(1, 1, 7, 94, '["Exact category match: Electronics & Gadgets (+30 pts)", "Identical primary colour: Blue (+20 pts)", "Same campus location: Library 2nd Floor (+20 pts)", "Reported on the same day (+20 pts)", "High title keyword match: HP Laptop Charger (+4 pts)"]', 'SUGGESTED', '2026-10-24 18:05:00'),
(2, 2, 8, 92, '["Exact category match: Bags & Wallets (+30 pts)", "Identical colour: Black (+20 pts)", "Same campus location: Main Cafeteria (+20 pts)", "Reported on same day (+20 pts)", "Matching brand: Fossil (+2 pts)"]', 'CLAIMED', '2026-10-25 14:10:00'),
(3, 3, 9, 88, '["Exact category match: Electronics & Gadgets (+30 pts)", "Identical colour: White (+20 pts)", "Same campus location: CS Computer Lab B (+20 pts)", "Reported on same date (+18 pts)"]', 'SUGGESTED', '2026-10-26 19:20:00'),
(4, 4, 10, 96, '["Exact category match: Books & Stationery (+30 pts)", "Identical colour: Black (+20 pts)", "Exact room match: Lecture Hall 102 (+20 pts)", "Same event date (+20 pts)", "Exact model match: Casio ClassWiz (+6 pts)"]', 'RESOLVED', '2026-10-22 13:45:00'),
(5, 5, 11, 86, '["Exact category match: Water Bottles & Flasks (+30 pts)", "Identical colour: Silver (+20 pts)", "Same campus facility: Sports Complex (+20 pts)", "Same event date (+16 pts)"]', 'SUGGESTED', '2026-10-27 19:10:00');

-- ------------------------------------------------------------
-- SEED CLAIMS
-- ------------------------------------------------------------
INSERT INTO claims (id, match_id, claimant_user_id, message, status, reviewed_at, created_at) VALUES
(1, 2, 3, 'Hello Rohan, this is my Fossil wallet dropped during lunch. It has my college student ID ending in 108 and a blue transit pass inside.', 'PENDING', NULL, '2026-10-25 15:30:00'),
(2, 4, 5, 'Hi Aarav, thank you for finding my calculator! As proof of ownership, there is a small sticker CS-2026 pasted on the rear battery lid.', 'COMPLETED', '2026-10-22 15:00:00', '2026-10-22 14:00:00');

-- ------------------------------------------------------------
-- SEED NOTIFICATIONS
-- ------------------------------------------------------------
INSERT INTO notifications (id, user_id, title, message, is_read, link, created_at) VALUES
(1, 2, 'High Confidence Match Found', 'Your lost report "Blue HP Pavilion Laptop Charger" has a 94% match with a found report at Library 2nd Floor.', FALSE, '/matches', '2026-10-24 18:06:00'),
(2, 5, 'New Ownership Claim Received', 'Aarav Sharma has submitted an ownership claim for your found report "Men\'s Black Leather Pocket Wallet".', FALSE, '/claims', '2026-10-25 15:31:00'),
(3, 3, 'Claim Submitted Successfully', 'Your claim for "Men\'s Black Leather Pocket Wallet" was delivered to finder Rohan Mehta for verification.', TRUE, '/claims', '2026-10-25 15:30:00'),
(4, 5, 'Item Safely Recovered!', 'Your lost report for "Casio FX-991EX Scientific Calculator" has been marked as RETURNED. Congratulations!', TRUE, '/items/4', '2026-10-22 15:05:00'),
(5, 4, 'Possible Match Found', 'Your lost report "Silver Apple AirPods Pro" matched with earbuds found in CS Computer Lab B (88% compatibility).', FALSE, '/matches', '2026-10-26 19:22:00');

-- ------------------------------------------------------------
-- SEED CLAIM MESSAGES (DIRECT IN-APP CHAT)
-- ------------------------------------------------------------
INSERT INTO claim_messages (id, claim_id, sender_id, content, is_read, created_at) VALUES
(1, 1, 3, 'Hello Rohan, this is my Fossil wallet dropped during evening study hours. The inner flap has my student ID card with last 4 digits 8812.', 1, '2026-10-28 14:30:00'),
(2, 1, 5, 'Hi Aarav! Yes, I have the wallet safely kept with me. The student ID card matches! When can you meet at the Student Union Property Desk?', 1, '2026-10-28 15:05:00'),
(3, 1, 3, 'Thanks so much Rohan! Does today around 3:30 PM at the Property Office counter work for you?', 0, '2026-10-28 15:20:00');

-- ------------------------------------------------------------
-- SEED ITEM HISTORY
-- ------------------------------------------------------------
INSERT INTO item_history (id, item_id, changed_by_user_id, previous_status, new_status, change_note, created_at) VALUES
(1, 2, 3, 'ACTIVE', 'CLAIM_PENDING', 'Ownership claim #1 submitted by Aarav Sharma', '2026-10-25 15:30:00'),
(2, 8, 3, 'ACTIVE', 'CLAIM_PENDING', 'Ownership claim #1 submitted against matched found item', '2026-10-25 15:30:00'),
(3, 4, 5, 'ACTIVE', 'CLAIM_PENDING', 'Ownership claim #2 submitted by Rohan Mehta', '2026-10-22 14:00:00'),
(4, 4, 3, 'CLAIM_PENDING', 'RETURNED', 'Claim verified and item handed over in person. Marked recovered.', '2026-10-22 15:00:00'),
(5, 10, 3, 'ACTIVE', 'RETURNED', 'Handed over to verified owner Rohan Mehta', '2026-10-22 15:00:00');

