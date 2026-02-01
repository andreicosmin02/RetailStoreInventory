# Retail Store Inventory Application

## Project Overview

This is an Android retail inventory management application built with Kotlin and Jetpack Compose.
The app allows retailers to manage inventory, scan barcodes, record sales, and track inventory changes.

| Inventory                                 | Product Details                       | Barcode Scanner                       |
| ----------------------------------------- | ------------------------------------- | ------------------------------------- |
| ![inventory](/screenshots/inventory.jpeg) | ![details](/screenshots/details.jpeg) | ![scanner](/screenshots/scanner.jpeg) |

| Sales History                       | Providers                                 | Audit Logs                      |
| ----------------------------------- | ----------------------------------------- | ------------------------------- |
| ![orders](/screenshots/orders.jpeg) | ![providers](/screenshots/providers.jpeg) | ![logs](/screenshots/logs.jpeg) |

---

# Framework:

- Kotlin programming language
- Jetpack Compose for UI
- Material 3 design system

Database:

- Room for local SQLite database
- Coroutines for async operations
- Flow for reactive data streams

Dependency Injection:

- Hilt for dependency injection

Camera & Barcode:

- CameraX for camera integration
- ZXing for barcode detection

---

## Key Features

### Core Inventory Management

- **Real-time Inventory Tracking** - Live product quantity updates with atomic transactions
- **Product Search & Filtering** - Instant search by name or barcode with real-time filtering
- **Low Stock Alerts** - Automatic notifications when inventory falls below threshold
- **Inventory State Denormalization** - Fast queries without joins for optimal performance

### Barcode Scanning

- **CameraX Integration** - Hardware-accelerated camera processing
- **ZXing Barcode Detection** - Multi-format barcode recognition (CODE_128, etc.)
- **Real-time Scanning Feedback** - Visual feedback with animated scan line overlay
- **Automatic Product Lookup** - One-scan checkout and product access

### Sales Management

- **Point-of-Sale Recording** - Quick sale entry with quantity +/- controls
- **Dynamic Pricing** - Override unit price at point of sale
- **Transaction Ledger** - Immutable append-only sales history
- **Revenue Analytics** - Total revenue calculation with date range filtering

### Audit & Compliance

- **Complete Audit Trail** - Immutable log of all create/update/delete operations
- **Change Tracking** - Before/after snapshots of all data modifications
- **User Activity Log** - Comprehensive activity history for accountability
- **JSON-based Audit Records** - Detailed old/new value tracking for compliance

### Data Reliability

- **Atomic Transactions** - All-or-nothing database operations
- **Foreign Key Constraints** - Referential integrity enforcement
- **Database Triggers** - Automatic validation (e.g., no negative quantities)
- **Cascading Deletes** - Safe cleanup of related data

---

## Architecture

### Design Patterns

The application follows **MVVM (Model-View-ViewModel)** architecture with clean separation of concerns:

```
UI Layer (Jetpack Compose)
        ↓
ViewModel Layer (StateFlow, Business Logic)
        ↓
Repository Layer (Data Abstraction)
        ↓
Data Layer (Room Database, DAOs)
```

---

## Technologies & Dependencies

### Core Framework

- **Kotlin 2.0.21** - Modern, concise language with null safety
- **Jetpack Compose** - Declarative UI framework with Material 3
- **Material 3** - Latest Material Design system with dark theme support
- **Android API Level 24-36** - Support for wide range of devices

### Database & Persistence

- **Room 2.6.1** - Type-safe SQLite database wrapper
- **SQLite** - Local relational database engine
- **Coroutines 1.7.3** - Async/await for non-blocking operations
- **Flow & StateFlow** - Reactive data streams

### Dependency Injection

- **Hilt 2.48** - Compile-time dependency injection
- **Hilt Navigation Compose 1.1.0** - Navigation support with DI

### Camera & Barcode

- **CameraX 1.5.1** - Modern camera access API (camera2, lifecycle)
- **ZXing 3.5.3** - Multi-format barcode detection library

### Background Tasks

- **WorkManager 2.9.0** - Scheduled background work
- **Hilt Work 1.2.0** - DI support for Worker classes

### Testing

- **JUnit 4.13.2** - Unit testing framework
- **Espresso 3.7.0** - UI testing framework
- **Compose Test 2024.09.00** - Compose UI testing utilities

---

### Directory Explanation

| Directory          | Purpose                                                     |
| ------------------ | ----------------------------------------------------------- |
| `data/`            | All data layer logic (database, entities, DAOs, repository) |
| `data/local/`      | Room database configuration and initialization              |
| `data/models/`     | Domain models with business logic and validation            |
| `data/dtos/`       | Data Transfer Objects shaped for UI consumption             |
| `data/repository/` | Repository pattern implementation (abstraction over DAOs)   |
| `data/monitoring/` | Background tasks and alert services                         |
| `ui/screens/`      | Composable screens (pure UI, no business logic)             |
| `ui/viewmodel/`    | ViewModels managing state and business logic                |
| `ui/theme/`        | Material 3 theming configuration                            |
| `di/`              | Hilt dependency injection modules                           |

---

## Navigation

Main Navigation:

- Drawer menu with 4 main sections
- Inventory: View and manage products
- Orders: See sales history
- Providers: Manage suppliers
- Logs: View audit trail

Product Flow:

- Main screen: Browse inventory
- Scan button: Open barcode scanner
- Scanner: Point at barcode
- Details: View product and record sale
- Auto-navigate: Returns to main screen after recording

---

## Usage Examples

Recording a Sale:

1. From main screen, click on a product or scan its barcode
2. On details screen, adjust quantity using +/- buttons
3. Review the sale total
4. Click "Record Sale"
5. Inventory automatically updates
6. Transaction is saved to database

Searching Products:

1. On main screen, use search bar at top
2. Type product name or barcode
3. Results filter in real-time
4. Click product to see details

Checking Sales History:

1. Open drawer menu
2. Select "Orders"
3. View total revenue and all transactions
4. See when each sale was made

---

## Unit Tests

**Location:** `app/src/test/`

Run with: `./gradlew test`

**Coverage:**

- `ModelsTest.kt` - Domain model validation
- `MappersTest.kt` - Entity to domain conversion
- `DtosMappersTest.kt` - Domain to DTO formatting

### Instrumented Tests

**Location:** `app/src/androidTest/`

Run with: `./gradlew connectedAndroidTest`

**Coverage:**

- `ProductDaoTest.kt` - CRUD operations, search, quantity updates
- `TransactionDaoTest.kt` - Append-only ledger, revenue calculations
- `AuditLogAndAlertDaoTest.kt` - Immutable logging, alerting
- `InventoryStateDaoTest.kt` - Low stock queries, cascading deletes

---

## Building and Running

### Prerequisites

- **Android Studio** 2024 or later
- **Android SDK** with API level 24-36
- **Gradle** 8.13 (included with Android Studio)
- **JDK** 11 or later
- **Emulator** or physical device running Android 7.0+ (API 24+)

### Step-by-Step Build

```bash
# 1. Clone the repository
git clone https://github.com/yourusername/retailstoreinventory.git
cd retailstoreinventory

# 2. Open in Android Studio
# File → Open → Select project folder

# 3. Let Android Studio sync Gradle
# Android Studio will automatically download dependencies

# 4. Clear build cache (if issues occur)
# Build → Clean Project

# 5. Rebuild the project
# Build → Rebuild Project

# 6. Run on emulator or device
# Run → Run 'app' (or Shift+F10)
```

---

## License

MIT License. See the [LICENSE](LICENSE) file for details.
