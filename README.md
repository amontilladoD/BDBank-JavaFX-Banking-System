# BDBank — JavaFX Banking Management System

BDBank is a desktop banking management system developed in **JavaFX** as an academic project. It provides separate interfaces for customers and administrators while demonstrating practical use of **OOP, MVC architecture, SQLite, JSON, REST API integration, multithreading, and exception handling**.

The goal was to build something that feels like a small real-world banking application rather than just a collection of CRUD screens.

## ✨ Features

### Customer Portal

* Secure login and registration
* Account balance and account information
* Money transfers through multiple channels
* Monthly statements
* Export statements as TXT or JSON
* Loan applications and loan tracking
* DPS and FDR services
* Card and cheque book requests
* Locker services
* Dollar endorsement
* Mobile recharge
* Utility bill payments
* Notifications
* Live support chat
* Logout and session management

### Admin Portal

* Customer account management
* Account approval and blocking
* Cash deposits
* Transaction management and filtering
* Daily and monthly statements
* Loan scheme management
* DPS and FDR management
* Card and cheque approval
* Locker management
* Interest-rate configuration
* Bank asset management
* Dollar-rate management
* Customer support chat

## 🛠️ Technologies

* **Java 17+**
* **JavaFX**
* **FXML & Scene Builder**
* **SQLite**
* **JDBC**
* **Jackson**
* **Maven**
* **REST API**
* **Multithreading / ExecutorService**
* **MVC Architecture**

## 🏗️ Architecture

The project follows an MVC-oriented structure:

```text
com.bdbank
├── model
│   ├── Account
│   ├── Transaction
│   ├── ServiceRequest
│   ├── Schemes
│   └── Other domain models
│
├── service
│   ├── Db
│   ├── AuthService
│   ├── AccountService
│   ├── RequestService
│   ├── SchemeService
│   ├── ConfigService
│   ├── ChatService
│   └── Other business services
│
├── controller
│   ├── LoginController
│   ├── RegisterController
│   ├── UserDashboardController
│   └── AdminDashboardController
│
├── view
│   └── JavaFX UI panels
│
└── util
    ├── SessionManager
    ├── FileManager
    ├── JsonUtil
    └── Exception utilities
```

FXML is used for the main application screens, while the feature panels are created and managed dynamically inside the dashboard.

## 💾 Database

The application uses **SQLite** for persistent storage.

The database contains tables for:

* Accounts
* Administrators
* Transactions
* Notifications
* Service requests
* Loan schemes
* DPS schemes
* FDR schemes
* Bank configuration
* Chat messages

The application also keeps in-memory collections for frequently accessed data while synchronizing changes with the SQLite database.

## 🌐 JSON & API Integration

Jackson is used for both reading and writing JSON.

One of the main examples is the exchange-rate service. The application makes an HTTPS request to a currency API and parses the returned JSON to obtain the USD → BDT exchange rate.

If the API cannot be reached, the application uses a local fallback response so the JSON-processing workflow can still operate offline.

## 🧵 Multithreading

Several operations run outside the JavaFX Application Thread so the interface remains responsive.

Examples include:

* Exchange-rate updates
* Interest calculation
* Login processing
* Balance transfers
* Live-support responses
* Background scheduled tasks

`ExecutorService` and JavaFX's `Platform.runLater()` are used to coordinate background work with UI updates.

## 🔐 Security & Error Handling

The application includes:

* Password-masked login fields
* SHA-256 password hashing
* Session management
* Custom `BankException`
* Input validation
* Business-rule validation
* Database error handling
* User-friendly error dialogs and status messages

## 🚀 Running the Project

### Requirements

* JDK 17 or newer
* IntelliJ IDEA
* Maven
* JavaFX-compatible environment

### 1. Open the project

Open the project folder containing `pom.xml` in IntelliJ IDEA.

Allow IntelliJ/Maven to import the project and download the required dependencies.

### 2. Configure the JDK

Set the project SDK to **JDK 17 or newer**.

### 3. Run the application

Create an IntelliJ **Application** run configuration.

Use:

```text
Main class: com.bdbank.Launcher
```

Then run the project normally from IntelliJ.

On the first run, the application creates the SQLite database inside:

```text
data/bdbank.db
```

## 👤 Default Admin Account

```text
Admin ID: admin
Password: admin123
```

New customers can register through the registration screen and wait for administrator approval.

## 📂 Database Inspection

The generated SQLite database can be opened using **DB Browser for SQLite**.

This makes it easy to inspect the actual tables, records, transactions, and configuration data while demonstrating the project.

## 🎓 Concepts Demonstrated

This project was built to demonstrate several core programming concepts:

* Object-Oriented Programming
* Encapsulation
* Abstraction
* Inheritance
* Polymorphism
* MVC architecture
* Singleton pattern
* JavaFX GUI development
* FXML
* JDBC
* Relational databases
* JSON serialization/deserialization
* REST API communication
* Multithreading
* Synchronization
* Exception handling
* File handling
* Asynchronous UI operations

## 📌 Project Status

This project was developed as an academic banking management system and is intended primarily for **learning and demonstration purposes**.

Before presenting or deploying it in another environment, build and test it locally in IntelliJ with the required JavaFX and Maven dependencies.

---

### Built with Java ☕ and JavaFX

A practical banking project combining desktop UI development, database programming, APIs, JSON processing, and concurrent programming.
