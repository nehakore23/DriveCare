# 🚗 DriveCare — Vehicle Service & Booking Platform

DriveCare is a full-stack **Vehicle Service & Booking Platform** designed to simplify vehicle servicing by connecting customers, mechanics, and administrators through a centralized system.

Customers can manage their vehicles, book services, track service progress, make online payments, and provide reviews. Mechanics can manage assigned service jobs and update service progress, while administrators manage mechanics, services, bookings, payments, and reports.

---

## 🛠️ Tech Stack

### Backend
- Java 17
- Spring Boot
- Spring Security
- JWT Authentication
- Hibernate / JPA
- REST APIs
- MySQL

### Frontend
- React.js
- HTML5
- CSS3
- JavaScript

### Payment
- Razorpay

### Tools
- Git
- GitHub
- Postman
- Spring Tool Suite (STS)
- VS Code

---

## 🏗️ Architecture

```text
                    React.js
                       │
                       ▼
                  REST APIs
                       │
                       ▼
                  Controller
                       │
                       ▼
                    Service
                       │
                       ▼
                  Repository
                       │
                       ▼
                 Hibernate/JPA
                       │
                       ▼
                    MySQL
```

### Request Flow

```text
Client Request
      ↓
Controller
      ↓
Service Layer
      ↓
Repository
      ↓
Hibernate / JPA
      ↓
MySQL Database
      ↓
Response
      ↓
React.js UI
```

---

## 🔐 Authentication & Authorization

DriveCare uses **JWT-based authentication with Spring Security** to secure REST APIs and provide role-based access.

```text
Login
  ↓
Credentials Validation
  ↓
JWT Token Generated
  ↓
Authorization: Bearer <token>
  ↓
JwtAuthenticationFilter
  ↓
JWT Validation
  ↓
SecurityContext
  ↓
Role-Based Authorization
  ↓
Protected API Access
```

### Security Features

- JWT-based stateless authentication
- Spring Security
- BCrypt password encryption
- Role-based authorization
- Protected REST APIs
- Public and secured endpoints
- Authentication and authorization validation

---

## 👥 User Roles

### 👨‍💼 ADMIN

```text
ADMIN
 ├── Manage Users
 ├── Manage Mechanics
 ├── Manage Services
 ├── Manage Bookings
 ├── Assign Mechanics
 ├── Manage Payments
 └── View Reports
```

### 👤 CUSTOMER

```text
CUSTOMER
 ├── Register / Login
 ├── Manage Vehicles
 ├── Browse Services
 ├── Book Vehicle Service
 ├── Select Date & Time
 ├── Track Service
 ├── Make Payment
 ├── View Service History
 └── Submit Rating & Review
```

### 🔧 MECHANIC

```text
MECHANIC
 ├── Login
 ├── View Assigned Jobs
 ├── Start Service
 ├── Update Service Status
 ├── Add Service Notes
 ├── Maintain Checklist
 └── Complete Service
```

---

## 🔄 Service Booking Flow

```text
Customer
    ↓
Select Vehicle
    ↓
Select Service
    ↓
Select Date & Time
    ↓
Create Booking
    ↓
Admin Reviews Booking
    ↓
Mechanic Assigned
    ↓
Mechanic Starts Service
    ↓
IN_PROGRESS
    ↓
Service Completed
    ↓
Payment
    ↓
Review & Rating
```

---

## 📌 Service Status Flow

```text
BOOKED
   ↓
MECHANIC_ASSIGNED
   ↓
IN_PROGRESS
   ↓
COMPLETED
```

This allows customers and administrators to track the complete service lifecycle.

---

## 💳 Razorpay Payment Integration

DriveCare integrates **Razorpay** for online payment processing.

```text
Customer
    ↓
Service Booking
    ↓
Payment Initiated
    ↓
Razorpay Checkout
    ↓
Payment Verification
    ↓
Payment Status Updated
    ↓
Booking / Service Payment Completed
```

---

## 🗄️ Database

DriveCare uses **MySQL** as the relational database and **Hibernate/JPA** for database operations.

### Main Entities

```text
User
Vehicle
Mechanic
Service
Booking
Payment
Review
```

Hibernate/JPA is used to map Java entities to database tables and manage CRUD operations and entity relationships.

---

## 📁 Project Structure

```text
DriveCare
│
├── backend
│   └── src/main/java
│       └── ...
│           ├── controller
│           ├── service
│           ├── repository
│           ├── entity
│           ├── dto
│           ├── security
│           ├── config
│           └── exception
│
├── frontend
│   └── src
│       ├── components
│       ├── pages
│       ├── services
│       └── ...
│
├── docs
│   └── screenshots
│
└── README.md
```

---

## 📸 Screenshots

### 🏠 Landing Page

![Landing Page](docs/screenshots/landing-page.png)

### 🔐 Login

![Login](docs/screenshots/login.png)

### 👤 Customer Dashboard

![Customer Dashboard](docs/screenshots/customer-dashboard.png)

### 🚗 Vehicle Management

![Vehicle Management](docs/screenshots/vehicles.png)

### 📅 Service Booking

![Service Booking](docs/screenshots/booking.png)

### 🔄 Service Tracking

![Service Tracking](docs/screenshots/service-tracking.png)

### 💳 Payment

![Payment](docs/screenshots/payment.png)

### 🔧 Mechanic Dashboard

![Mechanic Dashboard](docs/screenshots/mechanic-dashboard.png)

### 👨‍💼 Admin Dashboard

![Admin Dashboard](docs/screenshots/admin-dashboard.png)

### 📊 Reports

![Reports](docs/screenshots/reports.png)

### 🧾 Invoice

![Invoice](docs/screenshots/invoice.png)

---

## 🚀 Key Features

- User registration and login
- JWT-based authentication
- Role-based authorization
- Customer vehicle management
- Vehicle service booking
- Date and time selection
- Mechanic assignment
- Mechanic service management
- Service status tracking
- Service history
- Online Razorpay payment
- Ratings and reviews
- Admin dashboard
- Mechanic dashboard
- Customer dashboard
- Reports and invoice management
- RESTful API architecture

---

## 🧪 API Testing

REST APIs are tested using **Postman**.

The APIs cover:

```text
Authentication
Users
Vehicles
Mechanics
Services
Bookings
Payments
Reviews
```

---

## 🔑 Example Authentication Request

After successful login, the server generates a JWT token.

The token is sent with protected requests using:

```http
Authorization: Bearer <JWT_TOKEN>
```

Spring Security validates the token through the `JwtAuthenticationFilter` before allowing access to secured endpoints.

---

## 🎯 Project Objective

The main objective of DriveCare is to provide a centralized platform for managing vehicle servicing operations.

The system reduces manual booking processes and provides:

- Faster service booking
- Centralized vehicle records
- Better mechanic assignment
- Real-time service status tracking
- Secure authentication
- Online payment support
- Digital service history
- Customer feedback and reviews
- Administrative reporting

---

## 👩‍💻 Developed By

**Neha Kore**

**MCA | PG-DAC | Java Developer**

### Technologies

```text
Java | Spring Boot | Spring Security | JWT
Hibernate | JPA | MySQL | React.js
REST APIs | Razorpay
```

---

## ⭐ Project Status

**Currently in Development**

DriveCare is being continuously enhanced with additional dashboard features, service management functionality, payment integration, reporting, and UI improvements.
