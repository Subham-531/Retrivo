# Retrivo — Smart Campus Lost & Found Matcher

> **Full-Stack DBMS Project** developed with **Java 21, Spring Boot 3.3, MySQL 8.0, and modern vanilla HTML5/CSS3/JavaScript**.  
> Features a transparent **5-tier rule-based smart item-matching algorithm**, **BCrypt authentication**, **ACID transactions**, **3NF normalization**, **Direct In-App Chat**, **Midnight Archive Dark Theme**, and an interactive **Admin Analytics Dashboard with Chart.js**.

---

## 1. Project Overview

On university campuses, thousands of personal items (chargers, ID cards, keys, textbooks, headphones, water bottles) are misplaced every semester. Traditional physical lost-and-found desks and WhatsApp groups are inefficient, lack searchability, and expose student belongings to opportunistic theft or privacy breaches.

**Retrivo** solves this by providing a unified digital campus property office:
1. Students file detailed **LOST** or **FOUND** reports with category, colour, campus location, date, photos, and private identifying details.
2. A **rule-based matching algorithm** calculates compatibility scores ($0-100\%$) between reports based on multiple weighted parameters.
3. High-confidence matches trigger **in-app notifications**.
4. The claimant files an **ownership claim** detailing secret identifying marks (e.g. engravings, stickers, student ID numbers).
5. The claimant and finder coordinate via **in-app direct chat** to verify proof of ownership without sharing personal phone numbers.
6. The finder or admin approves the claim and confirms physical handover.
7. Handover executes an **ACID transaction** updating both reports to `RETURNED`, auto-closing competing claims, logging the change in `item_history`, and notifying both parties.

---

## 2. Technology Stack

- **Backend**: Java 21 LTS, Spring Boot 3.3.4
- **Security**: Spring Security 6 with BCrypt password hashing & stateless JWT token authentication
- **Database**: MySQL 8.0 Server (InnoDB storage engine, UTF-8 unicode collation, 3NF schema)
- **ORM / Persistence**: Spring Data JPA & Hibernate 6
- **Frontend**: HTML5, Modern CSS3 (CSS Grid, Flexbox, custom design system), Vanilla JavaScript (ES6+)
- **Theme**: Tactile University Property Office aesthetic with dual **Parchment (Light)** and **Midnight Archive (Dark)** themes
- **Data Visualizations**: Chart.js (monthly reporting vs. recovery trends, category distribution charts)
- **Icons & Typography**: Lucide Icons, Fraunces, Inter, and JetBrains Mono
- **Build Tool**: Apache Maven 3.9

---

## 3. Project Directory Structure

```text
DBMS_Project/
├── backend/                              # Spring Boot Maven backend application
│   ├── pom.xml                           # Dependencies & compiler configurations
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/edu/campus/findit/
│   │   │   │   ├── FindItApplication.java # Spring Boot entrypoint
│   │   │   │   ├── config/               # Security, CORS, Web, DataSeeder
│   │   │   │   ├── controller/           # Auth, Item, Match, Claim, Admin, Notification
│   │   │   │   ├── dto/                  # Request & Response Data Transfer Objects
│   │   │   │   ├── exception/            # GlobalExceptionHandler & custom exceptions
│   │   │   │   ├── model/                # JPA Entities (User, Item, Match, Claim, ClaimMessage, etc.)
│   │   │   │   ├── repository/           # Spring Data JPA repositories with custom queries
│   │   │   │   ├── security/             # JWT filter, UserDetails, token provider
│   │   │   │   └── service/              # MatchingService, ItemService, ClaimService, AdminService
│   │   │   └── resources/
│   │   │       ├── application.properties# MySQL database & JWT configurations
│   │   │       └── static/               # Mirrored frontend files served on port 8080
│   └── target/
│       └── findit-backend-1.0.0.jar      # Pre-compiled standalone executable JAR
├── frontend/                             # Standalone frontend for Live Server
│   ├── index.html                        # Single-page application master layout
│   ├── css/
│   │   └── style.css                     # Design system (Parchment & Midnight Archive dark mode)
│   └── js/
│       ├── api.js                        # Centralized REST API client with JWT storage
│       └── app.js                        # Client-side router, form handlers, Chat modal, Chart.js views
├── database/                             # Complete SQL demonstration package for DBMS Viva
│   ├── schema.sql                        # DDL table creation with 3NF constraints & FKs
│   ├── seed.sql                          # Realistic campus seed data (students, admin, items, matches, messages)
│   ├── views.sql                         # 4 relational views (active reports, strong matches, stats)
│   ├── indexes.sql                       # Performance indexes & EXPLAIN query execution plans
│   ├── transactions.sql                  # Multi-step ACID transactions (COMMIT and ROLLBACK)
│   ├── important_queries.sql             # 7 specific viva queries + advanced subqueries & window functions
│   └── er_diagram.md                     # Mermaid ER diagram & normalization proof up to 3NF
├── docs/
│   ├── VIVA_GUIDE.md                     # Comprehensive DBMS Viva examiner questions & model answers
│   └── API_DOCUMENTATION.md              # REST endpoints specification & parameter tables
├── screenshots/                          # 12 high-resolution screenshots of the application
├── run.bat                               # Windows Batch 1-click launcher
├── run.ps1                               # PowerShell 1-click launcher
├── run.sh                                # macOS / Linux launcher
├── .env.example                          # Environment configuration template
└── README.md                             # Complete project documentation
```

---

## 4. Quick Start: Running the Project

### Running on Localhost (Port 8080)

1. **Ensure MySQL is running** on your local machine:
   ```bash
   # Ensure MySQL service is running and database exists
   mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS findit_db;"
   ```
2. **Execute Database Scripts**:
   ```bash
   mysql -u root -p findit_db < database/schema.sql
   mysql -u root -p findit_db < database/seed.sql
   ```
3. **Launch the Spring Boot Backend**:
   ```bash
   java -jar backend/target/findit-backend-1.0.0.jar
   ```
4. **Open in Browser**:
   ```
   http://localhost:8080/
   ```

---

## 5. Demo Accounts for Presentation

| Role | Name | Email | Password |
|---|---|---|---|
| **ADMIN** | Retrivo Security Desk | `admin@retrivo.campus.edu` | `Admin@123` |
| **ADMIN (Fallback)** | Campus Security Administrator | `admin@findit.campus.edu` | `Admin@123` |
| **STUDENT** | Aarav Sharma (Lost Wallet) | `aarav@campus.edu` | `Student@123` |
| **STUDENT** | Rohan Mehta (Found Wallet) | `rohan@campus.edu` | `Student@123` |
| **STUDENT** | Suyash Kaistha (Lost Charger) | `suyash@campus.edu` | `Student@123` |
| **STUDENT** | Ananya Verma (Lost AirPods) | `ananya@campus.edu` | `Student@123` |
| **STUDENT** | Priya Patel (Lost Milton Bottle) | `priya@campus.edu` | `Student@123` |

*(Tip: On the Sign In modal, use the One-Click Demo buttons to log in as any role instantly!)*

---

## 6. Smart Matching Engine Architecture

The matching engine in `MatchingService.java` calculates compatibility between compatible opposite report types (`LOST` vs `FOUND`) using a **100-point weighted scoring model**:

| Criteria | Maximum Points | Scoring Rule |
|---|---|---|
| **Category Match** | **30 pts** | 30 points if `lost.category_id = found.category_id` |
| **Colour Match** | **20 pts** | 20 points for exact colour match; 12 points for partial colour overlap |
| **Location Proximity** | **20 pts** | 20 points for exact match; 16 points for same campus facility zone (e.g., "Library", "CS Lab", "Cafeteria", "Sports Complex") |
| **Date Proximity** | **16 pts** | 16 points (same day), 14 pts (within 24 hrs), 12 pts (within 48 hrs), 8 pts (within 5 days), 4 pts (within 10 days) |
| **Brand & Description Similarity**| **14 pts** | Normalized tokenized Jaccard similarity between words + brand matching boost |

### Compatibility Tiers
- **$\ge 80$ Points**: **Strong Match** (Triggers automated in-app notifications to both students)
- **$50 - 79$ Points**: **Possible Match** (Recommended in matches feed)
- **$< 50$ Points**: **Low Compatibility** (Filtered out of primary recommendations)

---

## 7. Database Demonstration for DBMS Viva

The `database/` folder is specifically prepared with all queries and concepts required for an examiner:

- **`schema.sql`**: Complete DDL with Primary Keys, Foreign Keys (`ON DELETE CASCADE` / `RESTRICT`), Unique constraints, and CHECK constraints.
- **`seed.sql`**: Populates realistic student reports across campus locations.
- **`views.sql`**: Relational views (`vw_active_reports`, `vw_high_confidence_matches`, `vw_category_loss_statistics`, `vw_pending_claims_detail`).
- **`indexes.sql`**: Composite indexes (`idx_items_matching`, `idx_notifications_user_unread`, `idx_claim_messages_claim_id`) and `EXPLAIN` query execution plans.
- **`transactions.sql`**: Multi-statement ACID transactions illustrating **COMMIT** on successful recovery and **ROLLBACK** on conflict detection.
- **`important_queries.sql`**: Demonstrates `INNER JOIN`, `LEFT JOIN`, `GROUP BY`, `HAVING`, correlated subqueries, and window functions (`DENSE_RANK()`).
- **`er_diagram.md`**: Mermaid ER diagram and comprehensive formal proof of **Third Normal Form (3NF)**.

---

## 8. Summary of REST APIs

| Endpoint | Method | Role | Description |
|---|---|---|---|
| `/api/auth/register` | `POST` | Public | Create new student account |
| `/api/auth/login` | `POST` | Public | Authenticate user & receive JWT token |
| `/api/stats/public` | `GET` | Public | Live landing page counters (reports, recoveries, recovery rate) |
| `/api/categories` | `GET` | Public | Retrieve master category list with icons |
| `/api/items/browse` | `GET` | Public | Search and filter reports with pagination |
| `/api/items` | `POST` | Student/Admin | Submit lost or found item report |
| `/api/items/{id}` | `GET` | Public | View item (masks identifying details unless owner) |
| `/api/matches` | `GET` | Student/Admin | Get smart matches for user's items |
| `/api/claims` | `GET` | Student/Admin | Get active claims involving user |
| `/api/claims` | `POST` | Student | Submit ownership claim with proof statement |
| `/api/claims/{id}/messages` | `GET` | Participant/Admin | Fetch direct chat stream for a claim |
| `/api/claims/{id}/messages` | `POST` | Participant/Admin | Send a new direct chat message in real-time |
| `/api/claims/{id}/review` | `PUT` | Finder/Admin | Review and approve/reject claim |
| `/api/claims/{id}/complete` | `PUT` | Finder/Admin | Confirm physical handover & mark item returned |
| `/api/admin/stats` | `GET` | Admin | Real-time database metrics & chart data |
| `/api/admin/users` | `GET` | Admin | View registered users and roles |
| `/api/admin/items/{id}` | `DELETE` | Admin | Moderate / delete inappropriate item |

---

## 9. Visual Gallery & Screenshots

Explore the UI designs and features in the `screenshots/` directory:
- **`01_home_page.png`**: Hero counter, quick stats, smart matching preview
- **`02_browse_items.png`**: Multi-filter catalog with category, color, status, and search
- **`03_login_modal.png`**: Modal with one-click demo logins
- **`05_report_lost_item.png`** / **`06_report_found_item.png`**: Structured item reporting with photo preview
- **`08_student_dashboard.png`**: My reports, claim statuses, and activity history
- **`09_smart_matches.png`**: Score indicators, compatibility breakdown, and claim initiation
- **`10_claims_center.png`**: In-app direct chat for proof verification and handover coordination
- **`12_admin_analytics_dashboard.png`**: Chart.js analytics, monthly trends, and moderation desk
