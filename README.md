# Inventory Management System (SE 4111 Course Project)

A small desktop application (Java Swing) to manage the inventory of a store.

## Features
- Add, edit and delete products (FR1, FR2, FR3)
- Increase or decrease the stock of a product (FR4)
- Search by name or category (FR5)
- Show all products in a table (FR6)
- Show low stock products (less than 5 pieces) in red, and filter them (FR7)

## Design
- **Architecture:** MVC (`model`, `view`, `controller` packages)
- **Design pattern:** Observer (`InventoryModel` is the subject, `InventoryView` is the observer)
- **Immutable ADT:** `model.Product` (with RI and AF in the comments)
- **Equality:** `equals()` and `hashCode()` in `Product`

## Folder structure
```
src/model/       Product, InventoryModel, InventoryObserver
src/view/        InventoryView
src/controller/  InventoryController
src/app/         Main
test/            AllTests.java (17 simple tests, no library needed)
```

## How to run
You need Java 11 or newer (JDK).

Linux / Mac:
```
./run.sh
```
Windows:
```
run.bat
```
Or by hand:
```
javac -d out $(find src -name "*.java")
java -cp out app.Main
```
Or use the ready jar: `java -jar Inventory.jar`

## How to run the tests
```
./run_tests.sh        (Windows: run_tests.bat)
```
Every test print PASS or FAIL.
