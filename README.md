# Library Management System

A Java CLI application for managing a library's books, members, loans, reservations, and fines backed by a MariaDB database.

---

## Features

- **Book Management** — add, update, remove, and search books by title or author; manage physical copies with barcode tracking
- **Member Management** — register members, track account status (Active, Suspended, Closed)
- **Loan Management** — check books in and out, track due dates and loan status
- **Reservations** — place and cancel holds on books, manage wait-lists per title, auto-notify members when their hold is ready
- **Fines & Payments** — issue fines for overdue returns, process payments, view payment history
- **Notifications** — automatic in-system alerts when a reserved book becomes available

---

## Project Structure

```
src/
├── application/
│   └── Main.java                  # Entry point
├── cli/
│   ├── MainMenu.java              # Top-level CLI hub
│   ├── BookMenu.java              # Book & copy operations
│   ├── MemberMenu.java            # Member account operations
│   ├── LoanMenu.java              # Loan / return operations
│   └── ReservationMenu.java       # Reservations & fine payments
├── domain/
│   ├── Book.java
│   ├── BookCopy.java
│   ├── Member.java
│   ├── Librarian.java
│   ├── Loan.java
│   ├── Reservation.java
│   ├── Fine.java
│   ├── Payment.java
│   ├── Notification.java
│   └── LibraryCatalog.java        # Implements Searchable
├── database/
│   ├── DatabaseConnection.java    # Singleton DB connection
│   ├── BookDAO.java
│   ├── MemberDAO.java
│   ├── LoanDAO.java
│   ├── ReservationDAO.java        
│   └── FineDAO.java               
├── enums/
│   ├── AccountStatus.java         
│   ├── CopyStatus.java            
│   ├── LoanStatus.java
│   ├── ReservationStatus.java     
│   ├── FineStatus.java            
│   └── PaymentStatus.java         
├── exceptions/
│   ├── BookNotFoundException.java
│   ├── AccountSuspendedException.java
│   ├── MaxBorrowLimitException.java
│   └── DatabaseException.java
└── interfaces/
    └── Searchable.java            # searchByTitle(), searchByAuthor()
```

---

## Prerequisites

| Requirement |
|-------------|
| Java JDK |
| MariaDB |


---

## Database Setup

1. Start your MariaDB server and log in:
```sql
mysql -u root -p
```

2. Create the database:
```sql
CREATE DATABASE librarydb;
USE librarydb;
```

3. Create the tables:
```sql
CREATE TABLE books (
    isbn        VARCHAR(20)  PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    author      VARCHAR(255),
    subject     VARCHAR(255),
    publisher   VARCHAR(255)
);

CREATE TABLE book_copies (
    copy_id     INT          AUTO_INCREMENT PRIMARY KEY,
    barcode     VARCHAR(50)  NOT NULL UNIQUE,
    status      VARCHAR(20)  NOT NULL,
    book_isbn   VARCHAR(20)  NOT NULL,
    FOREIGN KEY (book_isbn) REFERENCES books(isbn)
);

CREATE TABLE members (
    member_id   INT          AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    phone       VARCHAR(20),
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE loans (
    loan_id      INT  AUTO_INCREMENT PRIMARY KEY,
    member_id    INT  NOT NULL,
    copy_id      INT  NOT NULL,
    loan_date    DATE NOT NULL,
    due_date     DATE NOT NULL,
    return_date  DATE,
    status       VARCHAR(20) NOT NULL,
    FOREIGN KEY (member_id) REFERENCES members(member_id),
    FOREIGN KEY (copy_id)   REFERENCES book_copies(copy_id)
);

CREATE TABLE reservations (
    reservation_id   INT  AUTO_INCREMENT PRIMARY KEY,
    member_id        INT  NOT NULL,
    book_isbn        VARCHAR(20) NOT NULL,
    reservation_date DATE NOT NULL,
    expiry_date      DATE NOT NULL,
    status           VARCHAR(30) NOT NULL,
    FOREIGN KEY (member_id)  REFERENCES members(member_id),
    FOREIGN KEY (book_isbn)  REFERENCES books(isbn)
);

CREATE TABLE fines (
    fine_id      INT    AUTO_INCREMENT PRIMARY KEY,
    loan_id      INT    NOT NULL,
    member_id    INT    NOT NULL,
    amount       DOUBLE NOT NULL,
    issued_date  DATE   NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    FOREIGN KEY (loan_id)   REFERENCES loans(loan_id),
    FOREIGN KEY (member_id) REFERENCES members(member_id)
);

CREATE TABLE payments (
    payment_id   INT    AUTO_INCREMENT PRIMARY KEY,
    fine_id      INT    NOT NULL,
    member_id    INT    NOT NULL,
    amount       DOUBLE NOT NULL,
    payment_date DATE   NOT NULL,
    status       VARCHAR(20) NOT NULL,
    FOREIGN KEY (fine_id)   REFERENCES fines(fine_id),
    FOREIGN KEY (member_id) REFERENCES members(member_id)
);

CREATE TABLE notifications (
    notification_id INT  AUTO_INCREMENT PRIMARY KEY,
    member_id       INT  NOT NULL,
    message         TEXT NOT NULL,
    sent_date       DATE NOT NULL,
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (member_id) REFERENCES members(member_id)
);
```

---

## Configuration

Open `src/database/DatabaseConnection.java` and update the credentials to match your environment:

```java
private static final String URL      = "jdbc:mariadb://localhost:3306/librarydb";
private static final String USERNAME = "root";
private static final String PASSWORD = "password";
```

---

## Build & Run

### In Eclipse

1. Right-click the project → **Build Path → Add External JARs** and add the MariaDB JDBC `.jar`
2. Right-click `Main.java` → **Run As → Java Application**

### From the command line

```bash
# Compile (adjust paths as needed)
javac -cp .:mariadb-java-client-x.x.x.jar -d out $(find src -name "*.java")

# Run
java -cp out:mariadb-java-client-x.x.x.jar application.Main
```

On Windows replace `:` with `;` in the classpath.

---

## Design Patterns

**Singleton** — `DatabaseConnection` ensures only one database connection is opened for the entire application lifetime.

**DAO (Data Access Object)** — each domain entity has a dedicated DAO class. Domain objects stay free of SQL; all queries are isolated in the `database/` package.

**Interface / Contract** — `Searchable` defines the search contract. `LibraryCatalog` implements it, guaranteeing consistent search behaviour regardless of the underlying data source.

---

## Exception Handling

| Exception | Thrown when |
|-----------|-------------|
| `BookNotFoundException` | A book ISBN lookup returns no results |
| `AccountSuspendedException` | A suspended member attempts to borrow |
| `MaxBorrowLimitException` | A member exceeds the borrowing limit |
| `DatabaseException` | Any DAO SQL operation fails |

---

## Team

This project was built collaboratively across three workstreams:

- **Ebube Okutalukwe** — Books, copies, catalog, search (`BookDAO`, `LibraryCatalog`, `BookMenu`)
- **David Azuka** — Members, loans, account management (`MemberDAO`, `LoanDAO`, `MemberMenu`, `LoanMenu`)
- **Adrian Gorny** — Reservations, fines, payments, CLI hub (`ReservationDAO`, `FineDAO`, `ReservationMenu`, `MainMenu`, `Main`)