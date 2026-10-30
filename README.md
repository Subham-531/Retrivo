# FindIt — Smart Lost & Found Matcher for College Campuses

> **Full-Stack DBMS Project** developed with **Java 21, Spring Boot 3.3, MySQL 8.0, and modern vanilla HTML5/CSS3/JavaScript**.  
> Features a transparent **5-tier rule-based smart item-matching algorithm**, **BCrypt authentication**, **ACID transactions**, **3NF normalization**, and an interactive **Admin Analytics Dashboard with Chart.js**.

---

## 1. Project Overview

On university campuses, thousands of personal items (chargers, ID cards, keys, textbooks, headphones, water bottles) are misplaced every semester. Traditional physical lost-and-found desks are inefficient, lack searchability, and expose student belongings to opportunistic theft.

**FindIt** solves this by providing a unified digital recovery platform:
1. Students file detailed **LOST** or **FOUND** reports with category, colour, campus location, date, and description.
2. A **rule-based matching algorithm** calculates compatibility scores ($0-100\%$) between reports based on multiple weighted parameters.
3. High-confidence matches trigger **in-app notifications**.
4. The claimant files an **ownership claim** detailing secret identifying marks (e.g. engravings, stickers, lock screen wallpapers).
5. The finder reviews the proof and approves or rejects the claim.
6. Approval executes an **ACID transaction** updating both reports to `RETURNED`, logging the change in `item_history`, and notifying the owner to arrange a safe campus handover.

---

## 2. Technology Stack

- **Backend**: Java 21 LTS, Spring Boot 3.3.4
- **Security**: Spring Security 6 with BCrypt password hashing & stateless JWT token authentication
- **Database**: MySQL 8.0 Server (InnoDB storage engine, UTF-8 unicode collation)
- **ORM / Persistence**: Spring Data JPA & Hibernate 6
- **Frontend**: HTML5, Modern CSS3 (CSS Grid, Flexbox, custom design system), Vanilla JavaScript (ES6+)
- **Data Visualizations**: Chart.js (monthly reporting vs. recovery line charts, category doughnut charts)
- **Icons & Typography**: Lucide Icons, Google Inter font
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
│   │   │   │   ├── model/                # JPA Entities (User, Item, Match, Claim, etc.)
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
│   │   └── style.css                     # Design system (navy/indigo/teal palette, cards, tables, modals)
│   └── js/
│       ├── api.js                        # Centralized REST API client with JWT storage
│       └── app.js                        # Client-side router, form handlers, Chart.js views
├── database/                             # Complete SQL demonstration package for DBMS Viva
│   ├── schema.sql                        # DDL table creation with 3NF constraints & FKs
│   ├── seed.sql                          # Realistic campus seed data (students, admin, items, matches)
│   ├── views.sql                         # 4 relational views (active reports, strong matches, stats)
│   ├── indexes.sql                       # Performance indexes & EXPLAIN query execution plans
│   ├── transactions.sql                  # Multi-step ACID transactions (COMMIT and ROLLBACK)
│   ├── important_queries.sql             # 7 specific viva queries + advanced subqueries & window functions
│   └── er_diagram.md                     # Mermaid ER diagram & normalization proof up to 3NF
├── docs/
│   ├── VIVA_GUIDE.md                     # Comprehensive DBMS Viva examiner questions & model answers
│   └── API_DOCUMENTATION.md              # REST endpoints specification & parameter tables
├── run.bat                               # Windows Batch 1-click launcher
├── run.ps1                               # PowerShell 1-click launcher
├── .env.example                          # Environment configuration template
└── README.md                             # Complete project documentation
```

---

## 4. Quick Start: Running in VS Code

### Option A: One-Click Launch (Recommended)

1. Open PowerShell or Command Prompt in the project workspace:
   ```powershell
   .\run.ps1
   ```
   *Or double click `run.bat` in File Explorer.*
2. Open your browser and navigate to:
   ```
   http://localhost:8080/
   ```

### Option B: Running Manually in VS Code

1. **Start the MySQL Server**:
   Ensure MySQL is running on port `3307` (or `3306` with your credentials configured in `backend/src/main/resources/application.properties`).
2. **Run Spring Boot**:
   Open a terminal in VS Code and run:
   ```powershell
   $env:JAVA_HOME = "C:\Users\DELL\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.11.v20260515-1531\jre"
   $env:PATH = "$env:JAVA_HOME\bin;C:\Users\DELL\apache-maven-3.9.6\bin;$env:PATH"
   cd backend
   mvn spring-boot:run
   ```
   *Or run the pre-built jar directly:*
   ```powershell
   java -jar backend\target\findit-backend-1.0.0.jar
   ```
3. Open `http://localhost:8080/` in Chrome or Edge.

---

## 5. Demo Accounts for Presentation

| Role | Name | Email | Password |
|---|---|---|---|
| **ADMIN** | Campus Security Administrator | `admin@findit.campus.edu` | `Admin@123` |
| **STUDENT** | Suyash Kaistha | `suyash@campus.edu` | `Student@123` |
| **STUDENT** | Aarav Sharma | `aarav@campus.edu` | `Student@123` |
| **STUDENT** | Ananya Verma | `ananya@campus.edu` | `Student@123` |
| **STUDENT** | Rohan Mehta | `rohan@campus.edu` | `Student@123` |
| **STUDENT** | Priya Patel | `priya@campus.edu` | `Student@123` |

*(Tip: In the login dialog, click any of the "One-Click Demo Account" buttons to prefill credentials instantly!)*

---

## 6. Smart Matching Engine Architecture

The matching engine in `MatchingService.java` calculates compatibility between compatible opposite report types (`LOST` vs `FOUND`) using a **100-point weighted scoring model**:

| Criteria | Maximum Points | Scoring Rule |
|---|---|---|
| **Category Match** | **30 pts** | 30 points if `lost.category_id = found.category_id` |
| **Colour Match** | **20 pts** | 20 points for exact colour match; 12 points for partial colour overlap |
| **Location Proximity** | **20 pts** | 20 points for exact match; 16 points for same campus facility zone (e.g., "Library", "CS Lab", "Cafeteria", "Sports Complex") |
| **Date Proximity** | **20 pts** | 20 points (same day), 18 pts (within 24 hrs), 16 pts (within 48 hrs), 12 pts (within 5 days), 8 pts (within 10 days) |
| **Title & Description Similarity**| **10 pts** | Normalized tokenized Jaccard similarity between words + brand matching boost |

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
- **`indexes.sql`**: Composite indexes (`idx_items_matching`, `idx_notifications_user_unread`) and `EXPLAIN` query execution plans.
- **`transactions.sql`**: Multi-statement ACID transactions illustrating **COMMIT** on successful recovery and **ROLLBACK** on conflict detection.
- **`important_queries.sql`**: Demonstrates `INNER JOIN`, `LEFT JOIN`, `GROUP BY`, `HAVING`, correlated subqueries, and window functions (`DENSE_RANK()`).
- **`er_diagram.md`**: Mermaid ER diagram and comprehensive formal proof of **Third Normal Form (3NF)**.

### Running SQL Queries via MySQL CLI

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -P 3307 -u root findit_db
```
Then execute:
```sql
SOURCE database/important_queries.sql;
```

---

## 8. Summary of REST APIs

| Endpoint | Method | Role | Description |
|---|---|---|---|
| `/api/auth/register` | `POST` | Public | Create new student account |
| `/api/auth/login` | `POST` | Public | Authenticate user & receive JWT token |
| `/api/items/browse` | `GET` | Public | Search and filter reports with pagination |
| `/api/items` | `POST` | Student/Admin | Submit lost or found item report |
| `/api/items/{id}` | `GET` | Public | View item (masks identifying details unless owner) |
| `/api/matches` | `GET` | Student/Admin | Get smart matches for user's items |
| `/api/claims` | `POST` | Student | Submit ownership claim with proof statement |
| `/api/claims/{id}/review`| `PUT` | Finder/Admin | Review and approve/reject claim |
| `/api/claims/{id}/complete`| `PUT`| Finder/Admin | Mark physical handover completed |
| `/api/admin/stats` | `GET` | Admin | Real-time database metrics & chart data |
| `/api/admin/items/{id}` | `DELETE` | Admin | Moderate / delete inappropriate item |

---

## 9. Troubleshooting

- **MySQL Port Conflict**: If your default MySQL service is already on `3306`, the application is configured by default to connect to the dedicated local development instance on port `3307`. You can easily adjust this in `backend/src/main/resources/application.properties` or by setting the `DB_URL` environment variable.
- **Browser Cache**: If you update static assets, press `Ctrl + F5` to force refresh the browser cache.
- **Uploading Images**: Uploaded images are stored in `./uploads/` with safe UUID filenames and served under `/uploads/**`.

