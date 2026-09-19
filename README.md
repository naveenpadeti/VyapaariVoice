# VaaniStock (వాణీస్టాక్)
### Voice-First Multilingual Inventory Management & Stock Intelligence System for Wholesale and Retail Shops

**VaaniStock** is a production-quality full-stack web application designed for Indian small and medium wholesale distributors, traders, kirana stores, and retailers. It allows shop owners to manage products, categories, stock, sales/stock movements, low-stock alerts, transaction history, and sales analytics through both a clean responsive web dashboard and a voice-first multilingual assistant supporting **Telugu**, **Hindi**, and **English** (including natural mixed-language/code-mixed speech like *"Rice 20 bags vachayi"* or *"5 bags rice ammamu"*).

---

## 1. Key Highlights & Architectural Principles

- **Deterministic Business Intelligence**: AI/LLMs **never directly modify the database or invent stock numbers**. All inventory mutations, sales velocity calculations, coverage days, and refill recommendations are computed deterministically by the Spring Boot service layer from real database transaction history.
- **Multilingual Voice-First Interaction**: Conversational voice commands in Telugu (`te-IN`), Hindi (`hi-IN`), and English (`en-IN`/`en-US`), with speech recognition, multilingual intent/entity extraction, validation, transaction recording, and natural language voice responses.
- **Pre-Seeded Demo Wholesale Shop**: Instant 1-click access to **"Sri Lakshmi Wholesale"** with 30 days of realistic sales, transactions, fast-moving items, and low-stock alerts matching the acceptance test scenario.
- **Clean Modular Monolith**: Thin controllers, isolated business logic in services, transactional consistency, positive stock enforcement, prevention of negative inventory, and tenant-level business isolation.

---

## 2. Architecture & Pipeline

```
USER SPEAKS (Microphone / Text Fallback)
                 ↓
     WEB SPEECH RECOGNITION / STT
                 ↓
            TRANSCRIPT (e.g. "Rice 20 bags vachayi")
                 ↓
    MULTILINGUAL INTENT & ENTITY EXTRACTION
                 ↓
STRUCTURED COMMAND: { intent: "ADD_STOCK", product: "Rice", quantity: 20, unit: "bags" }
                 ↓
        SPRING BOOT REST API
                 ↓
 VALIDATION & DATA ISOLATION (No negative stock allowed)
                 ↓
 INVENTORY & TRANSACTION LEDGER (PostgreSQL)
                 ↓
STOCK INTELLIGENCE & REORDER RECOMMENDATION ENGINE
(Avg daily sales = 5 bags/day, Coverage = 5 days, Refill = 50 bags)
                 ↓
LOCALIZED NATURAL LANGUAGE GENERATION (Telugu / Hindi / English)
                 ↓
TEXT-TO-SPEECH AUDIO FEEDBACK (Browser Web Speech API / TTS)
                 ↓
USER HEARS VERIFIED STOCK INTELLIGENCE & ADVICE
```

---

## 3. Technology Stack

### Backend
- **Java 21**
- **Spring Boot 3.4.3** (Web, Data JPA, Security, Validation)
- **PostgreSQL 16** (Production) / **PostgreSQL-compatible H2** (Zero-setup local development & testing)
- **JWT (io.jsonwebtoken 0.12.6)** & **BCrypt**
- **SpringDoc OpenAPI 2.8.5 (Swagger UI)**

### Frontend
- **React 18**
- **Vite 6**
- **Tailwind CSS 3** (Indian commerce warm palette: Saffron amber, emerald green, slate)
- **Recharts** (Area charts for sales trends, Bar charts for top-selling velocity)
- **Lucide React** (Icons)
- **Web Speech API** (Speech Recognition & Speech Synthesis)

### DevOps & Containerization
- **Docker** & **Docker Compose**
- Multi-stage Docker builds for backend and frontend (Nginx reverse proxy)

---

## 4. Database Design (PostgreSQL Relational Schema)

- **`users`**: `id`, `name`, `mobile`, `email`, `password_hash`, `preferred_language`, `created_at`, `updated_at`
- **`businesses`**: `id`, `user_id`, `business_name`, `business_type` (WHOLESALE, RETAIL), `location`, `created_at`, `updated_at`
- **`categories`**: `id`, `business_id`, `name`, `created_at`, `updated_at` (Pre-seeds: Grocery, Beverages, Food Items, Stationery, Household, Personal Care, FMCG, Other)
- **`products`**: `id`, `business_id`, `category_id`, `name`, `description`, `unit`, `minimum_stock`, `reorder_level`, `target_stock`, `created_at`, `updated_at`
- **`inventories`**: `id`, `product_id`, `current_quantity`, `unit`, `updated_at` (Formula: `Current = Previous + Added - Removed`)
- **`transactions`**: `id`, `business_id`, `product_id`, `type` (ADD, REMOVE, SALE, PURCHASE, ADJUSTMENT), `quantity`, `unit`, `source` (MANUAL, VOICE, SYSTEM), `reference`, `created_at`
- **`voice_commands`**: `id`, `business_id`, `user_id`, `transcript`, `language`, `intent`, `extracted_data`, `status`, `response_text`, `created_at` (Audit log)

---

## 5. Stock Intelligence & Deterministic Reorder Formulas

1. **Average Daily Sales (Velocity)**:
   $$\text{Average Daily Sales} = \frac{\sum \text{Sales Quantity in last 30 days}}{30}$$

2. **Stock Coverage (Days)**:
   $$\text{Stock Coverage Days} = \frac{\text{Current Stock}}{\text{Average Daily Sales}}$$
   *(If sales velocity is zero, division by zero is prevented and reported as "No recent sales data".)*

3. **Deterministic Refill Recommendation**:
   $$\text{Required Stock} = \max(\text{Target Stock}, \text{Average Daily Sales} \times 15)$$
   $$\text{Recommended Refill} = \max(0, \text{Required Stock} - \text{Current Stock})$$

---

## 6. Supported Voice Commands & Intent Matrix

| User Speech | Detected Language | Extracted Intent | Entities | System Action |
|-------------|-------------------|------------------|----------|---------------|
| *"Rice 20 bags vachayi"* | Telugu | `ADD_STOCK` | Product: Rice, Qty: 20, Unit: bags | +20 bags to Rice stock, logs VOICE transaction |
| *"5 bags rice ammamu"* | Telugu | `REMOVE_STOCK` | Product: Rice, Qty: 5, Unit: bags | -5 bags from Rice stock, records SALE transaction |
| *"Rice stock entha undi?"* | Telugu | `CHECK_STOCK` | Product: Rice | Queries database, returns 25 bags, 5 days coverage, recommends 50 bags refill |
| *"Rice refill cheyyala?"* | Telugu | `REORDER_RECOMMENDATION` | Product: Rice | Analyzes sales velocity and confirms refill amount |
| *"Which products are low?"* | English | `LOW_STOCK` | - | Lists products where Current Stock &le; Reorder Level |
| *"Grocery lo em stock undi?"* | Telugu | `CHECK_CATEGORY` | Category: Grocery | Lists stock of all products in Grocery |
| *"Na shop inventory summary cheppu"* | Telugu | `INVENTORY_SUMMARY` | - | Returns total products, stock units, low stock count, and recent sales |
| *"5 packet sugar becha"* | Hindi | `REMOVE_STOCK` | Product: Sugar, Qty: 5, Unit: packets | Records sale of 5 packets of sugar |
| *"Rice ke kitne bags stock mein hain?"* | Hindi | `CHECK_STOCK` | Product: Rice | Returns current stock and Hindi intelligence explanation |

---

## 7. Running Locally

### Prerequisites
- Java 21+ (`java -version`)
- Maven 3.9+ (`mvn -version`)
- Node.js 18+ and npm (`node -v`, `npm -v`)

### Backend Setup
```bash
cd backend
export JAVA_HOME=/path/to/jdk-21
mvn clean package -DskipTests
mvn spring-boot:run
```
Backend will start on: **`http://localhost:8080`**
Swagger OpenAPI UI: **`http://localhost:8080/swagger-ui/index.html`**

### Frontend Setup
```bash
cd frontend
npm install
npm run dev
```
Frontend will start on: **`http://localhost:5173`**

---

## 8. Docker Compose (Full Stack with PostgreSQL)

To start PostgreSQL 16, Spring Boot Backend, and Frontend all together:

```bash
docker compose up --build -d
```

- **Frontend App**: `http://localhost:3000`
- **Backend API**: `http://localhost:8080/api`
- **Swagger Documentation**: `http://localhost:8080/swagger-ui/index.html`
- **PostgreSQL Database**: `localhost:5432`

---

## 9. Instant Demo Account

On the Login screen (`/login`), click the button:
**"⚡ Explore Demo Shop (Sri Lakshmi Wholesale)"**

Or log in manually:
- **Identifier**: `demo@vaanistock.com` or `9876543210`
- **Password**: `demo123`

---

## 10. Acceptance Test Verification (Scenario #77)

1. Log in to the demo shop.
2. Verify **Rice**:
   - Current Stock = **25 bags**
   - Historical sales = **5 bags/day** (150 bags sold in last 30 days)
   - Reorder level = **15 bags**, Target stock = **75 bags**
3. Click the floating **`🎤 Ask VaaniStock`** button or navigate to `/voice`.
4. Speak or type:
   > *"Rice stock entha undi?"*
5. System output:
   - Intent: `CHECK_STOCK`
   - Product: `Rice`
   - Current Stock: `25 bags`
   - Velocity: `5 bags/day`
   - Coverage: `5 days`
   - Recommended Refill: `50 bags`
   - Telugu spoken response:
     > *"Rice stock 25 bags undi. Mee recent sales prakaram rojuki approximately 5 bags sale avutunnayi. Current stock around 5 days ki saripovachu. Mee target stock maintain cheyyadaniki approximately 50 bags refill cheyyadam consider cheyyachu."*
6. Audio plays back to user and card is visually rendered.
7. Speak or type:
   > *"Rice 20 bags vachayi"*
8. Stock updates from 25 to 45 bags, recorded in transaction ledger with channel `VOICE`.
