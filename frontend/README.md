# DriveCare Frontend

React + Vite client for the DriveCare Spring Boot API.

## Run

1. Start the Spring Boot backend on `http://localhost:8080`.
2. In this folder, run:

```bash
npm install
npm run dev
```

3. Open `http://localhost:5173`.

## Authentication and authorization

- Customer registration and customer/admin login use `/api/auth/*`.
- Mechanic login uses `/api/auth/mechanic/login`.
- The returned JWT is stored in browser local storage and sent as `Authorization: Bearer <token>` on every protected API call.
- The portal is selected by the authenticated role: `ADMIN`, `CUSTOMER`, or `MECHANIC`.
- Role protection is additionally enforced by the Spring Boot backend.

## Optional API URL

To use another backend URL, create a `.env` file:

```text
VITE_API_URL=http://localhost:8080
```
