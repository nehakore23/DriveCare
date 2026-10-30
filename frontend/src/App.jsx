import LandingPage from "./LandingPage";
import { useState } from "react";
import {
  Bell, Calendar, Car, ClipboardList, IndianRupee, LayoutDashboard, LogOut, Star, Users, Wrench,
} from "lucide-react";
import { clearSession, getSession, saveSession } from "./api";
import Auth from "./Auth";
import Home from "./Home";
import { AdminPortal, CustomerPortal, MechanicPortal } from "./portals";
import { Brand, CloseMenu, FeedbackProvider, MobileBar } from "./ui";

const MENUS = {
  ADMIN: [
    { id: "dashboard", label: "Dashboard", icon: LayoutDashboard },
    { id: "booking", label: "Bookings", icon: ClipboardList },
    { id: "team", label: "Mechanics", icon: Users },
    { id: "service", label: "Services", icon: Wrench },
    { id: "payment", label: "Payments", icon: IndianRupee },
    { id: "review", label: "Reviews", icon: Star },
  ],
  CUSTOMER: [
    { id: "dashboard", label: "Dashboard", icon: LayoutDashboard },
    { id: "vehicle", label: "My Vehicles", icon: Car },
    { id: "booking", label: "Book Service", icon: Calendar },
    { id: "payment", label: "Payments", icon: IndianRupee },
    { id: "review", label: "Reviews", icon: Star },
  ],
  MECHANIC: [
    { id: "dashboard", label: "Dashboard", icon: LayoutDashboard },
    { id: "jobs", label: "My Jobs", icon: Wrench },
    { id: "booking", label: "Schedule", icon: Calendar },
    { id: "bell", label: "Notifications", icon: Bell },
  ],
};

function AppShell() {
  const [session, setSession] = useState(getSession());
  const [view, setView] = useState(session ? "portal" : "home");
  const [authMode, setAuthMode] = useState("login");
  const [afterAuth, setAfterAuth] = useState("dashboard");
  const [page, setPage] = useState("dashboard");
  const [menuOpen, setMenuOpen] = useState(false);

  const logout = () => {
    clearSession();
    setSession(null);
    setView("home");
    setPage("dashboard");
  };

  const onSuccess = (data, nextPage = "dashboard") => {
    saveSession(data);
    setSession(data);
    setPage(data.role === "CUSTOMER" ? nextPage : "dashboard");
    setView("portal");
  };

  const openAuth = (mode = "login", next = "dashboard") => {
    setAuthMode(mode);
    setAfterAuth(next);
    setView("auth");
  };

  const book = () => {
    if (session?.role === "CUSTOMER") {
      setPage("booking");
      setView("portal");
      return;
    }
    if (session) {
      setView("portal");
      return;
    }
    openAuth("register", "booking");
  };

  if (view === "home" || (!session && view !== "auth")) {
    return (
      <Home
        session={session}
        onBook={book}
        onAuth={(mode) => openAuth(mode)}
        onDashboard={() => setView("portal")}
        onExplore={() => document.getElementById("services")?.scrollIntoView({ behavior: "smooth" })}
      />
    );
  }

  if (!session) {
    return <Auth mode={authMode} setMode={setAuthMode} onSuccess={onSuccess} onHome={() => setView("home")} afterAuth={afterAuth} />;
  }

  const menus = MENUS[session.role] || MENUS.CUSTOMER;
  const title = page === "dashboard"
    ? `Hello, ${session.name}`
    : menus.find((item) => item.id === page)?.label || page;

  return (
    <div className="app-shell">
      <MobileBar title="DriveCare" onMenu={() => setMenuOpen(true)} onLogout={logout} />
      {menuOpen && <button type="button" className="nav-scrim" aria-label="Close menu" onClick={() => setMenuOpen(false)} />}
      <aside className={`sidebar ${menuOpen ? "open" : ""}`}>
        <div className="sidebar-top">
          <Brand light onClick={() => { setView("home"); setMenuOpen(false); }} />
          <CloseMenu onClick={() => setMenuOpen(false)} />
        </div>
        <div className="role-label">{session.role} PORTAL</div>
        <nav>
          {menus.map((item) => {
            const Icon = item.icon;
            return (
              <button
                key={item.id}
                type="button"
                className={page === item.id ? "nav-active" : ""}
                onClick={() => { setPage(item.id); setMenuOpen(false); }}
              >
                <i><Icon size={16} /></i>
                {item.label}
              </button>
            );
          })}
        </nav>
        <button type="button" className="logout" onClick={logout}><LogOut size={16} /> Sign out</button>
      </aside>
      <main className="content">
        <header>
          <div>
            <p className="eyebrow">DRIVECARE / {session.role}</p>
            <h1>{title}</h1>
          </div>
          <div className="header-actions">
            <button type="button" className="ghost" onClick={() => setView("home")}>Homepage</button>
            <div className="profile">
              <span>{session.name?.[0]?.toUpperCase()}</span>
              <div>
                <b>{session.name}</b>
                <small>{session.role}</small>
              </div>
            </div>
          </div>
        </header>
        {session.role === "ADMIN" && <AdminPortal token={session.token} page={page} />}
        {session.role === "MECHANIC" && <MechanicPortal token={session.token} page={page} />}
        {session.role === "CUSTOMER" && <CustomerPortal token={session.token} page={page} go={setPage} />}
      </main>
    </div>
  );
}

export default function App() {
  return (
    <FeedbackProvider>
      <AppShell />
    </FeedbackProvider>
  );
}
