# FH-Java-2025


## Warehouse Management System
### Team 19

### Team Members: 
* Md Saidur Rahman
* Anh Phuc Dang
* Hai Dang Duong


## Task Distribution:
*Md Saidur Rahman:
**Package:** warehouse
**Project Management** 
**Maven Integration**

* Anh Phuc Dang:
**Package: taskManager, pathFinding, equipmentManager**

* Hai Dang Duong: 
**Package: application (JavaFX), logger**
	

---

## Requirements
This is a JavaFX application built using **Maven**, with all dependencies (including JavaFX) downloaded automatically at build time.

Before running the project, please ensure you have:

### Java Development Kit (JDK) **21 or newer**

---

## Project Structure

```
WMSJFX/
 ├── src/
 ├── pom.xml
 ├── mvnw
 ├── mvnw.cmd
 └── .mvn/
```

The project includes the **Maven Wrapper**, so users do **NOT** need Maven installed.

---

# How to Run the Application

You can run WMSJFX in two ways:  
1. **Using Maven Wrapper** (recommended)  
2. **Using installed Maven**

---

##  Option A: Run with Maven Wrapper (Recommended)

### 🪟 Windows
```powershell
.\mvnw clean javafx:run
```

###  macOS /  Linux
```bash
./mvnw clean javafx:run
```

---

##  Option B: Run with Installed Maven

Check Maven installation:
```bash
mvn -v
```

Run:
```bash
mvn clean javafx:run
```

---

# 🛠 Building the Project

### Windows
```powershell
.\mvnw clean compile
```

### macOS / Linux
```bash
./mvnw clean compile
```

---


# JavaFX Dependencies

JavaFX is managed by Maven — **no manual installation required**. 
Maven automatically downloads JavaFX modules for your OS. 

The project includes:

- `javafx-controls`
- `javafx-fxml`


---
