# 🚗 DriveCare – Vehicle Service & Booking Platform

DriveCare is a full-stack web application designed to simplify vehicle servicing and booking management. It connects customers, mechanics, and administrators through a single platform.

The application provides secure authentication, role-based access, service management, vehicle booking, and dashboard functionality.

## ✨ Features

### 🔐 Authentication & Authorization
- User registration and login
- JWT-based authentication
- Role-based access control
- Separate portals for Admin, Customer, and Mechanic

### 👨‍💼 Admin Portal
- Admin dashboard with live statistics
- Customer and mechanic overview
- Vehicle and booking statistics
- Pending bookings overview
- Revenue summary
- Operational snapshot
- Popular services overview
- Manage bookings, mechanics, services, payments, and reviews

### 👤 Customer Portal
- Customer dashboard
- Manage vehicles
- View available services
- Book vehicle services
- Track booking status
- View payment and booking history
- Submit reviews and ratings

### 🔧 Mechanic Portal
- Mechanic dashboard
- View assigned jobs
- Track job progress
- Update service status
- Manage assigned service tasks

## 🛠️ Tech Stack

**Frontend**
- React.js
- JavaScript
- HTML5
- CSS3
- Vite
- Lucide React

**Backend**
- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- JWT Authentication

**Database**
- MySQL

**Tools**
- Git & GitHub
- Postman
- VS Code
- Spring Tool Suite (STS)

## 🎨 UI & Design

- Premium automotive-inspired interface
- Responsive dashboard layout
- Navy blue, white, and orange color theme
- Modern cards and dashboard statistics
- Improved navigation and user experience
- Separate dashboard layouts for different roles

## 📁 Project Structure

```text
DriveCare/
│
├── backend/
│   └── src/
│       └── main/
│           ├── java/
│           └── resources/
│
├── frontend/
│   ├── src/
│   │   ├── App.jsx
│   │   ├── api.js
│   │   ├── main.jsx
│   │   ├── LandingPage.jsx
│   │   ├── LandingPage.css
│   │   └── styles.css
│   │
│   ├── package.json
│   └── index.html
│
└── README.md
```

## ⚙️ Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/nehakore23/DriveCare.git
cd DriveCare
```

### 2. Backend Setup

- Open the backend project in Spring Tool Suite or IntelliJ IDEA.
- Configure MySQL database credentials in `application.properties`.
- Create the database:

```sql
CREATE DATABASE drivecare;
```

- Run the Spring Boot application.

### 3. Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

Open the frontend using the local URL displayed in the terminal.

## 🔒 Security

- JWT-based authentication
- Password encryption using BCrypt
- Role-based authorization
- Protected API endpoints
- Secure access to role-specific functionality

## 🚀 Project Status

**Currently in Development**

The core backend functionality, authentication, role-based portals, and dashboard UI are being developed and enhanced. Further improvements include responsive UI refinements and additional booking and service management features.

⭐ If you find this project interesting, feel free to explore the repository!
