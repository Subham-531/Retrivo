# FindIt REST API Specifications

Base URL: `http://localhost:8080/api`

## Authentication (`/api/auth`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/auth/register` | Public | Register new student account (`fullName`, `email`, `password`, `phone`) |
| `POST` | `/auth/login` | Public | Authenticate user (`email`, `password`), returns JWT token |
| `GET` | `/auth/me` | Authenticated | Get current authenticated user profile |

## Categories (`/api/categories`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/categories` | Public | List all item categories (Electronics, Bags, Books, etc.) |

## Items (`/api/items`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/items` | Authenticated | Submit new LOST or FOUND report |
| `GET` | `/items/browse` | Public | Search, filter, and paginate through active reports |
| `GET` | `/items/recent` | Public | Retrieve 6 most recent active reports |
| `GET` | `/items/my` | Authenticated | Retrieve reports submitted by the logged-in user |
| `GET` | `/items/{id}` | Public/Masked | Get item detail (masks identifying details unless owner/admin) |
| `PUT` | `/items/{id}` | Owner/Admin | Update item report details or status |
| `DELETE` | `/items/{id}` | Owner/Admin | Remove report (cascades to matches and claims) |
| `POST` | `/items/upload-image`| Authenticated | Secure file upload (JPG/PNG/WEBP $\le 5$MB, UUID names) |

## Smart Matches (`/api/matches`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/matches` | Authenticated | Retrieve algorithmic matches for the current user's items |
| `GET` | `/matches/item/{itemId}` | Public | Retrieve matches for a specific item |
| `GET` | `/matches/all` | Public | Retrieve all campus matches above minimum threshold |

## Claims & Recovery (`/api/claims`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/claims` | Authenticated | Submit ownership claim against a match recommendation |
| `GET` | `/claims/my` | Authenticated | Get all claims involving current user (as claimant or finder) |
| `PUT` | `/claims/{id}/review` | Finder/Admin | Review claim (`APPROVED` or `REJECTED` with note) |
| `PUT` | `/claims/{id}/complete`| Authenticated | Mark handover completed |

## In-App Notifications (`/api/notifications`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/notifications` | Authenticated | Get user notifications list |
| `GET` | `/notifications/unread-count` | Authenticated | Get count of unread notifications |
| `PUT` | `/notifications/{id}/read` | Authenticated | Mark specific notification as read |
| `PUT` | `/notifications/read-all` | Authenticated | Mark all notifications as read |

## Admin Console (`/api/admin`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/admin/stats` | ADMIN only | Aggregate metrics, monthly trends, category distribution |
| `GET` | `/admin/users` | ADMIN only | Directory of registered campus accounts |
| `GET` | `/admin/items` | ADMIN only | Paginated report moderation list |
| `PUT` | `/admin/items/{id}/status` | ADMIN only | Administrative status override |
| `DELETE` | `/admin/items/{id}` | ADMIN only | Moderator removal of inappropriate posts |

