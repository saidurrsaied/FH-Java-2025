# FH-Java-2025 — Warehouse Management System (WMSJFX)

A JavaFX-based Warehouse Management System that simulates inventory handling, robot dispatching, pathfinding, and logging. Built with Maven and designed for easy setup via Maven Wrapper.

## Team 19
- Md Saidur Rahman
- Anh Phuc Dang
- Hai Dang Duong


## Task Distribution:

- **Md Saidur Rahman**  
  - **Package:** `warehouse`  
  - **Project Management**  
  - **Maven Integration**

- **Anh Phuc Dang**  
  - **Packages:** `taskManager`, `pathFinding`, `equipmentManager`

- **Hai Dang Duong**  
  - **Packages:** `application (JavaFX)`, `logger`
---

## Project Overview
WMSJFX provides:
- A JavaFX UI (inventory, floor, robot, log screens)
- Core warehouse domain (inventory, shelves, packing/loading/charging stations)
- Robot equipment simulation with task execution and charging logic
- A* pathfinding over a warehouse grid
- File-based data import/export (CSV) and structured logging

---

## Tech
- Java 25 (JDK 21+ compatible runtime)
- JavaFX 25.x
- Maven (via Maven Wrapper)

---

## JavaFX Dependencies
Managed by Maven; modules are resolved per OS automatically:
- javafx-controls
- javafx-fxml

---

## Data and Logs
- data/: input CSVs (inventory.csv, warehouse_floor.csv)
- Logging/: created at runtime with categorized logs (Robot, Charging_Station, Inventory, System)

---


## Requirements
- JDK 21 or newer
- No manual JavaFX setup required (managed by Maven)
- The project includes the **Maven Wrapper**, so **NO** need Maven installed. See **How To Run**

---

### Package Guide
- application
  - JavaFX controllers and UI utilities (Inventory, Robot, Floor, Log screens, dialogs).
- equipmentManager
  - Robot runtime (threaded), EquipmentManager (dispatcher), Charging/Packing stations, states.
- logger
  - File-based logger and JavaFX log viewer (filter, open, move, delete, archive).
- pathFinding
  - WarehouseMap grid, nodes, A* pathfinding (thread-safe path calculations).
- taskManager
  - Task interface, order/stock/charging tasks, creation and submission via TaskManager.
- warehouse
  - Core domain: Inventory, InventoryItem, Product, StorageShelf, Packing/Loading/Charging stations, floor manager, data packets and CSV utilities.

---


# How to Run the Application

You can run the project **Using Maven Wrapper** (included)



### Windows
```powershell
.\mvnw clean install javafx:run
```

###  macOS /  Linux
```bash
./mvnw clean install javafx:run
```

---

## Optionally: Run with Installed Maven

Run:
```bash
mvn clean javafx:run
```

---

#  Building the Project

### Windows
```powershell
.\mvnw clean compile
```

### macOS / Linux
```bash
./mvnw clean compile
```
## Login Credentials
- User Name: admin
- Password: 1234
---


