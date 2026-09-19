# VaaniStock Backend Service

Java 21 and Spring Boot 3.4.3 modular monolith powering the VaaniStock Voice-First Inventory Management & Stock Intelligence System.

## Architecture
- `com.vaanistock.auth`: User registration, authentication, JWT tokens, and 1-click demo login
- `com.vaanistock.business`: Business profiles (Wholesale & Retail) and tenant isolation
- `com.vaanistock.category`: Category management and default category provisioning
- `com.vaanistock.product`: Product catalog, trade units, minimum stock, reorder levels, target stock
- `com.vaanistock.inventory`: Current inventory tracking, stock movements (+Add, -Remove), and negative stock prevention
- `com.vaanistock.transaction`: Immutable transaction audit ledger (ADD, REMOVE, SALE, ADJUSTMENT)
- `com.vaanistock.analytics`: 30-day sales velocity, trends (today, 7 days, 30 days), top-selling, fast/slow-moving categorizations
- `com.vaanistock.recommendation`: Deterministic Stock Intelligence & Reorder Recommendation engine
- `com.vaanistock.alert`: Low-stock detection, severity evaluation, and alert dismissal
- `com.vaanistock.voice`: Multilingual Speech-to-Text, Intent/Entity extraction (Telugu, Hindi, English), and localized response synthesis
- `com.vaanistock.demo`: Pre-seeded demo account (*"Sri Lakshmi Wholesale"*) matching acceptance test scenario

## Build & Run Locally
```bash
export JAVA_HOME=/path/to/jdk-21
mvn clean test
mvn spring-boot:run
```
OpenAPI / Swagger: `http://localhost:8080/swagger-ui/index.html`
