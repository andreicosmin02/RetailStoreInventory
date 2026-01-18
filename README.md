RETAIL STORE INVENTORY APPLICATION
==================================

PROJECT OVERVIEW
================

This is an Android retail inventory management application built with Kotlin and Jetpack Compose.
The app allows retailers to manage inventory, scan barcodes, record sales, and track inventory changes.

___

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

___

# BUILDING AND RUNNING

Prerequisites:
- Android Studio version 2024 or later
- Minimum SDK: 24
- Target SDK: 36

Build Process:
1. Open project in Android Studio
2. File > Invalidate Caches > Invalidate and Restart
3. Build > Clean Project
4. Build > Rebuild Project
5. Run > Run app

If Build Fails:
- Check that all screen files were copied correctly
- Verify MainActivity and ProductViewModel have required methods
- Clean and rebuild again
- Check Android Studio console for specific errors
___
DATABASE
========

The app uses Room database with four tables:

Products: Stores product information (name, barcode, price, quantity)
Transactions: Records all sales (product, quantity, price, timestamp)
AuditLog: Tracks all changes (what changed, when, by whom)
InventoryState: Denormalized view for fast lookups

Sample data is automatically initialized on first app launch.
___
NAVIGATION
==========

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
___
USAGE EXAMPLES
==============

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
___
TESTING
=======

Manual Testing Checklist:
- Main screen displays all products
- Search filters work correctly
- Can tap product to see details
- Details screen shows complete information
- Quantity selector works (+/- buttons)
- Can record a sale
- Stock quantity decreases after sale
- Orders screen shows sales history
- Providers screen displays suppliers
- Logs screen shows activity trail
- Back button works from all screens
- No crashes when navigating

Sample Barcode for Testing:
The app includes a test product with barcode 4980416 for testing the scanner.
___
MOCK DATA
=========

Some screens use mock data for demonstration:
- Orders screen shows sample sales
- Providers screen shows sample suppliers
- Logs screen shows sample audit entries

These can be replaced with real database queries later.


