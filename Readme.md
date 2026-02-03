# Portfolio Management System

A comprehensive portfolio management application built with **Spring Boot**, featuring real-time price tracking, portfolio analytics, and data integrity verification.

---

## Table of Contents

1. [Backend Architecture](#backend-architecture)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [Core Components](#core-components)
5. [API Endpoints](#api-endpoints)
6. [Setup and Configuration](#setup-and-configuration)

---

## Backend Architecture

The backend is a **Spring Boot 4.0.2** application built on Java 21, designed as a RESTful API for managing investment portfolios with real-time price updates and audit trail functionality.

### Key Architecture Features:
- **REST API** with cross-origin support
- **Scheduled price updates** running every 15 minutes
- **Database-driven** with MySQL for persistent storage
- **Service-oriented architecture** with separation of concerns
- **Global exception handling** for consistent error responses
- **Audit logging** with cryptographic integrity verification
- **DTO pattern** for API response formatting

---

## Technology Stack

### Backend Framework
- **Spring Boot 4.0.2** - Application framework
- **Java 21** - Programming language
- **Maven** - Build automation tool

### Persistence & Database
- **Spring Data JPA** - Object-relational mapping
- **Hibernate** - JPA implementation
- **MySQL** - Relational database
- **Connection Pool** - For database connection management

### Dependencies
- **Spring Boot Actuator** - Application metrics and monitoring
- **Spring Boot Web** - MVC framework for REST APIs
- **Spring Boot Dev Tools** - Live reload and development features
- **MySQL Connector** - JDBC driver for MySQL
- **JUnit 5** - Testing framework

---

## Project Structure

```
backend/
├── src/main/java/com/backend/Portfolio_Backend/
│   ├── PortfolioBackendApplication.java       # Application entry point
│   │
│   ├── controller/                             # REST API controllers
│   │   ├── PortfolioController.java           # Main portfolio endpoints
│   │   └── PortfolioIntegrityController.java  # Integrity verification endpoints
│   │
│   ├── service/                                # Business logic layer
│   │   ├── PortfolioService.java              # Portfolio management
│   │   ├── PriceService.java                  # Price data operations
│   │   ├── PriceIngestionService.java         # Price fetching from APIs
│   │   ├── PortfolioAuditService.java         # Audit trail management
│   │   ├── PortfolioIntegrityService.java     # Data integrity verification
│   │   ├── HashService.java                   # Cryptographic hashing
│   │   └── TickerNameService.java             # Ticker symbol management
│   │
│   ├── model/                                  # Database entities
│   │   ├── Portfolio.java                     # Root portfolio entity
│   │   ├── PortfolioAsset.java                # Individual assets/holdings
│   │   ├── DailyPrice.java                    # Daily OHLC prices
│   │   ├── IntradayPrice.java                 # Intraday price updates
│   │   └── PortfolioAuditLog.java             # Audit trail records
│   │
│   ├── repository/                             # Data access layer
│   │   ├── PortfolioRepository.java           # Portfolio CRUD
│   │   ├── PortfolioAssetRepository.java      # Asset CRUD
│   │   ├── DailyPriceRepository.java          # Daily price queries
│   │   ├── IntradayPriceRepository.java       # Intraday price queries
│   │   └── PortfolioAuditLogRepository.java   # Audit log queries
│   │
│   ├── dto/                                    # Data Transfer Objects
│   │   ├── HoldingDTO.java                    # Holding representation
│   │   ├── AssetDetailDTO.java                # Detailed asset info
│   │   ├── AllocationDTO.java                 # Asset allocation data
│   │   ├── PortfolioSummaryDTO.java           # Portfolio overview
│   │   ├── PortfolioGrowthDTO.java            # Growth metrics
│   │   ├── PriceApiResponse.java              # API response wrapper
│   │   └── PriceData.java                     # Price data structure
│   │
│   ├── exception/                              # Error handling
│   │   ├── GlobalExceptionHandler.java        # Centralized exception handling
│   │   ├── ResourceNotFoundException.java     # 404 exceptions
│   │   └── BadRequestException.java           # 400 exceptions
│   │
│   └── scheduler/                              # Scheduled tasks
│       └── PriceScheduler.java                # Automated price updates
│
├── src/main/resources/
│   ├── application.properties                  # Configuration file
│   ├── static/                                 # Static assets
│   └── templates/                              # Thymeleaf templates (if used)
│
├── pom.xml                                     # Maven dependencies
└── target/                                     # Build artifacts
```

---

## Core Components

### 1. **Controllers** - REST API Layer

#### PortfolioController (`PortfolioController.java`)
Main REST endpoint for portfolio operations:
- `GET /api/portfolio/holdings` - Retrieve all holdings in frontend format
- `GET /api/portfolio/summary` - Get portfolio summary (total value, gains/losses)
- `GET /api/portfolio/allocation` - Asset allocation breakdown
- `GET /api/portfolio/growth` - Historical growth metrics
- `POST /api/portfolio/asset` - Add new asset to portfolio
- `PUT /api/portfolio/asset/{id}` - Update asset details
- `DELETE /api/portfolio/asset/{id}` - Remove asset from portfolio
- `GET /api/portfolio/asset/{id}/details` - Get detailed asset information
- `GET /api/portfolio/asset/{ticker}/price` - Get latest price for ticker

#### PortfolioIntegrityController (`PortfolioIntegrityController.java`)
Endpoints for verifying data integrity:
- `POST /api/portfolio/integrity/verify` - Verify blockchain-style audit chain
- `GET /api/portfolio/integrity/status` - Check integrity status

---

### 2. **Services** - Business Logic Layer

#### PortfolioService (`PortfolioService.java`)
Core portfolio management logic:
- `getOrCreatePortfolio()` - Initialize portfolio if none exists
- `getAssets()` - Fetch all assets in current portfolio
- `getAssetById(Long id)` - Get specific asset by ID
- `getAssetByTicker(String ticker)` - Get asset by stock ticker
- `addAsset()` - Add new holding to portfolio
- `updateAsset()` - Modify asset details
- `deleteAsset()` - Remove asset from portfolio
- All operations trigger audit logging

#### PriceService (`PriceService.java`)
Price data retrieval and management:
- `getLatestPrice(String ticker)` - Get most recent price (intraday → daily fallback)
- `getYesterdayPrice(String ticker)` - Get previous day's closing price
- `getPriceHistory(String ticker, LocalDate start, LocalDate end)` - Historical prices
- `calculateDayChange()` - Daily price change calculations
- Supports both intraday and daily OHLC data

#### PriceIngestionService (`PriceIngestionService.java`)
External API integration for price updates:
- `fetchAndStore(String ticker)` - Fetch latest price from external API
- `processApiResponse()` - Parse API responses into database models
- Handles both daily and intraday price ingestion
- Called automatically by scheduler every 15 minutes

#### PortfolioAuditService (`PortfolioAuditService.java`)
Audit trail management:
- `logAction(String action, String ticker, ...)` - Record portfolio changes
- `getAuditLogs()` - Retrieve audit trail
- `generateHash()` - Create integrity hash for audit entries
- Creates immutable record of all portfolio modifications

#### PortfolioIntegrityService (`PortfolioIntegrityService.java`)
Blockchain-style integrity verification:
- `verifyIntegrity()` - Validate entire audit chain using SHA-256
- Recalculates hashes for all audit logs
- Detects any tampering with historical data
- Returns true only if all hashes match original chain

#### HashService (`HashService.java`)
Cryptographic operations:
- `sha256(String input)` - Generate SHA-256 hash
- Used for audit log integrity
- Ensures tamper-evident audit trail

#### TickerNameService (`TickerNameService.java`)
Ticker symbol management:
- `validateTicker()` - Check if ticker is valid
- `getTickerInfo()` - Retrieve ticker information
- `resolveTicker()` - Convert ticker aliases

---

### 3. **Models** - Database Entities

#### Portfolio (`Portfolio.java`)
Root entity representing an investment portfolio:
- **Fields**: `id`, `createdAt`
- **Relationships**: One-to-many with PortfolioAsset
- **Lifecycle**: Auto-created on first request

#### PortfolioAsset (`PortfolioAsset.java`)
Individual holdings within a portfolio:
- **Fields**: `id`, `portfolio`, `ticker`, `quantity`, `avgBuyPrice`, `createdAt`, `updatedAt`
- **Relationships**: Many-to-one with Portfolio
- **Methods**: Calculate current value, gain/loss, daily change

#### DailyPrice (`DailyPrice.java`)
Daily OHLC (Open, High, Low, Close) price data:
- **Fields**: `id`, `ticker`, `date`, `open`, `high`, `low`, `close`, `volume`
- **Purpose**: Historical daily price snapshots
- **Usage**: Long-term analysis, portfolio valuation

#### IntradayPrice (`IntradayPrice.java`)
Real-time intraday price updates:
- **Fields**: `id`, `ticker`, `timestamp`, `close`, `volume`
- **Purpose**: Current market prices throughout trading day
- **Usage**: Live portfolio valuations, alerts

#### PortfolioAuditLog (`PortfolioAuditLog.java`)
Immutable audit trail entry:
- **Fields**: `id`, `portfolio`, `action`, `ticker`, `quantity`, `avgBuyPrice`, `timestamp`, `currentHash`, `previousHash`
- **Purpose**: Record all portfolio modifications
- **Security**: SHA-256 hashing for integrity verification
- **Blockchain-style**: Each entry references previous hash creating unbreakable chain

---

### 4. **Repositories** - Data Access Layer

Using **Spring Data JPA** for database operations:

#### PortfolioRepository
```java
@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> { }
```
Basic CRUD operations for portfolios

#### PortfolioAssetRepository
Custom queries:
- `findByPortfolio(Portfolio)` - All assets in portfolio
- `findByPortfolioAndTicker()` - Specific asset lookup
- `findDistinctTickers()` - All unique tickers for scheduling

#### DailyPriceRepository & IntradayPriceRepository
Price queries:
- `findLatestByTicker()` - Get most recent price
- `findLatestByTickerAndDate()` - Historical price lookup
- `findByTickerBetweenDates()` - Date range queries

#### PortfolioAuditLogRepository
- `findByPortfolioOrderByIdAsc()` - Retrieve audit chain
- `findLatestByPortfolio()` - Most recent audit entry

---

### 5. **DTOs** - Data Transfer Objects

Transfer data between API and frontend:

#### HoldingDTO
```json
{
  "id": 1,
  "ticker": "AAPL",
  "quantity": 100,
  "currentPrice": 150.25,
  "totalValue": 15025.00,
  "avgBuyPrice": 145.00,
  "gainLoss": 525.00,
  "gainLossPercent": 3.62
}
```

#### AssetDetailDTO
Extended holding information:
- Holdings data
- Historical prices
- Price trends
- Performance metrics

#### AllocationDTO
Asset allocation breakdown:
- `ticker` - Stock symbol
- `percentage` - % of portfolio
- `value` - Dollar amount
- `quantity` - Number of shares

#### PortfolioSummaryDTO
Overall portfolio snapshot:
- `totalValue` - Current portfolio value
- `totalCost` - Initial investment
- `totalGain` - Absolute gain in dollars
- `totalGainPercent` - Percentage gain
- `dayChange` - Today's change
- `holdingCount` - Number of assets

#### PortfolioGrowthDTO
Growth metrics and trends:
- Daily valuations
- Weekly averages
- Monthly performance
- Year-to-date growth
- Historical data points

#### PriceApiResponse & PriceData
API response structures:
- Wrapper for external API data
- Price data objects
- OHLC information

---

### 6. **Exception Handling** - Error Management

#### GlobalExceptionHandler (`GlobalExceptionHandler.java`)
Centralized error handling with consistent responses:

**ResourceNotFoundException (404)**
```json
{
  "timestamp": "2026-02-03T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Asset not found with ticker: XYZ"
}
```

**BadRequestException (400)**
```json
{
  "timestamp": "2026-02-03T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input parameters"
}
```

**Generic Exception (500)**
```json
{
  "timestamp": "2026-02-03T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Something went wrong"
}
```

---

### 7. **Scheduler** - Automated Tasks

#### PriceScheduler (`PriceScheduler.java`)
Automated price updates:

**On Application Startup**
- `@EventListener(ApplicationReadyEvent.class)` - Runs once when app fully loads
- Fetches initial prices for all tracked tickers

**Every 15 Minutes**
- `@Scheduled(cron = "0 */15 * * * *")` - Cron expression for 15-min intervals
- Retrieves all unique tickers from portfolio
- Calls `PriceIngestionService.fetchAndStore()` for each ticker
- Updates both IntradayPrice and DailyPrice tables

---

## API Endpoints

### Portfolio Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/portfolio/holdings` | Get all portfolio holdings |
| `GET` | `/api/portfolio/summary` | Get portfolio summary statistics |
| `GET` | `/api/portfolio/allocation` | Get asset allocation breakdown |
| `GET` | `/api/portfolio/growth` | Get growth metrics and history |
| `POST` | `/api/portfolio/asset` | Add new asset to portfolio |
| `PUT` | `/api/portfolio/asset/{id}` | Update asset details |
| `DELETE` | `/api/portfolio/asset/{id}` | Remove asset from portfolio |
| `GET` | `/api/portfolio/asset/{id}/details` | Get detailed asset information |
| `GET` | `/api/portfolio/asset/{ticker}/price` | Get current price for ticker |

### Integrity Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/portfolio/integrity/verify` | Verify audit chain integrity |
| `GET` | `/api/portfolio/integrity/status` | Check integrity status |

### Example Request/Response
**GET /api/portfolio/holdings**
```json
[
  {
    "id": 1,
    "ticker": "AAPL",
    "quantity": 50,
    "currentPrice": 150.25,
    "totalValue": 7512.50,
    "avgBuyPrice": 145.00,
    "gainLoss": 262.50,
    "gainLossPercent": 3.62
  },
  {
    "id": 2,
    "ticker": "GOOGL",
    "quantity": 30,
    "currentPrice": 140.50,
    "totalValue": 4215.00,
    "avgBuyPrice": 135.00,
    "gainLoss": 165.00,
    "gainLossPercent": 3.70
  }
]
```

---

## Setup and Configuration

### Prerequisites
- Java 21 JDK
- Maven 3.8+
- MySQL 8.0+
- Git

### Database Setup

1. **Create MySQL Database**
   ```sql
   CREATE DATABASE portfolio;
   CREATE USER 'root'@'localhost' IDENTIFIED BY 'fowzan2744';
   GRANT ALL PRIVILEGES ON portfolio.* TO 'root'@'localhost';
   FLUSH PRIVILEGES;
   ```

2. **Connection Details** (`application.properties`)
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/portfolio?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=fowzan2744
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   ```

### Application Configuration

Key settings in `application.properties`:

```properties
# Server
server.port=8080

# Hibernate DDL Auto
spring.jpa.hibernate.ddl-auto=update  # Auto-create/update tables

# Logging
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### Build & Run

**1. Build the Application**
```bash
cd backend
mvn clean package
```

**2. Run the Application**
```bash
mvn spring-boot:run
```

**3. Access the API**
```
http://localhost:8080/api/portfolio
```

**4. View Actuator Endpoints** (Monitoring)
```
http://localhost:8080/actuator
```

---

## Data Flow Diagram

```
┌─────────────────────────────────────────────┐
│          Frontend (React/Vue)               │
└──────────────┬──────────────────────────────┘
               │ REST API Calls
               │ /api/portfolio/*
               ▼
┌─────────────────────────────────────────────┐
│      REST Controllers                       │
│  - PortfolioController                      │
│  - PortfolioIntegrityController             │
└──────────────┬──────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────┐
│      Service Layer (Business Logic)         │
│  - PortfolioService                         │
│  - PriceService                             │
│  - PriceIngestionService                    │
│  - PortfolioAuditService                    │
│  - PortfolioIntegrityService                │
└──────────────┬──────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────┐
│      Repository Layer (Data Access)         │
│  - PortfolioRepository                      │
│  - PortfolioAssetRepository                 │
│  - DailyPriceRepository                     │
│  - IntradayPriceRepository                  │
│  - PortfolioAuditLogRepository              │
└──────────────┬──────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────┐
│      MySQL Database                         │
│  Tables:                                    │
│  - portfolio                                │
│  - portfolio_asset                          │
│  - daily_price                              │
│  - intraday_price                           │
│  - portfolio_audit_log                      │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│      PriceScheduler (Background Job)        │
│  - Runs every 15 minutes                    │
│  - Fetches prices from external APIs        │
│  - Updates IntraDay & Daily Price tables    │
└─────────────────────────────────────────────┘
```

---

## Security Features

### 1. **Audit Logging**
- Every portfolio modification is logged
- Includes action, ticker, quantity, timestamp
- Creates immutable history of changes

### 2. **Integrity Verification**
- SHA-256 cryptographic hashing
- Each audit log references previous hash
- Blockchain-style chain makes tampering detectable
- `/api/portfolio/integrity/verify` validates entire chain

### 3. **Exception Handling**
- Global exception handler prevents information leakage
- Consistent error responses
- No stack traces exposed to client

### 4. **CORS Support**
- `@CrossOrigin(origins = "*")` allows frontend communication
- Can be restricted to specific domains in production

---

## Performance Considerations

### Scheduling
- **Frequency**: Every 15 minutes (configurable)
- **Load**: Parallel ticker processing
- **Storage**: Both intraday and daily prices for redundancy

### Database Optimization
- JPA auto-generates efficient queries
- Indexes on ticker and date columns
- Connection pooling for concurrent requests

### API Response
- DTOs minimize data transfer
- Only required fields sent to frontend
- Lazy loading avoids N+1 queries

---

## Future Enhancements

1. **User Authentication** - Multi-user support with login
2. **Email Alerts** - Notifications for price changes
3. **Risk Analysis** - Portfolio risk metrics
4. **Dividends Tracking** - Record dividend income
5. **Tax Reporting** - Capital gains calculations
6. **Mobile App** - Native mobile frontend
7. **Real-time WebSocket** - Live price updates
8. **Machine Learning** - Portfolio recommendations

---

## Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

---

## License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## Contact & Support

For questions or support, please reach out to the development team.

**Application Name**: Portfolio Management System  
**Version**: 0.0.1-SNAPSHOT  
**Last Updated**: February 2026
