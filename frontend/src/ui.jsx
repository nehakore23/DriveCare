import { createContext, useContext, useMemo, useState } from "react";
import {
  AlertCircle, Car, CheckCircle2, LayoutDashboard, LogOut, Menu, Search, X,
} from "lucide-react";
import { IMAGES, statusClass, statusLabel, stepIndex } from "./lib";

const FeedbackContext = createContext({ toast: () => {}, confirm: async () => false });
export const useFeedback = () => useContext(FeedbackContext);

export function FeedbackProvider({ children }) {
  const [toasts, setToasts] = useState([]);
  const [dialog, setDialog] = useState(null);

  const value = useMemo(() => ({
    toast: (message, type = "success") => {
      const id = Date.now() + Math.random();
      setToasts((current) => [...current, { id, message, type }]);
      setTimeout(() => setToasts((current) => current.filter((item) => item.id !== id)), 4200);
    },
    confirm: ({ title, message, confirmLabel = "Confirm", danger = false }) =>
      new Promise((resolve) => setDialog({ title, message, confirmLabel, danger, resolve })),
  }), []);

  const closeDialog = (result) => {
    dialog?.resolve(result);
    setDialog(null);
  };

  return (
    <FeedbackContext.Provider value={value}>
      {children}
      <div className="toast-stack" aria-live="polite">
        {toasts.map((item) => (
          <div key={item.id} className={`toast toast-${item.type}`}>
            {item.type === "error" ? <AlertCircle size={16} /> : <CheckCircle2 size={16} />}
            <span>{item.message}</span>
          </div>
        ))}
      </div>
      {dialog && (
        <div className="modal-backdrop" role="presentation" onClick={() => closeDialog(false)}>
          <div className="modal" role="dialog" aria-modal="true" aria-labelledby="confirm-title" onClick={(e) => e.stopPropagation()}>
            <h3 id="confirm-title">{dialog.title}</h3>
            <p>{dialog.message}</p>
            <div className="modal-actions">
              <button type="button" className="ghost" onClick={() => closeDialog(false)}>Cancel</button>
              <button type="button" className={dialog.danger ? "danger" : "primary"} onClick={() => closeDialog(true)}>
                {dialog.confirmLabel}
              </button>
            </div>
          </div>
        </div>
      )}
    </FeedbackContext.Provider>
  );
}

export const Brand = ({ light = false, onClick }) => (
  <button type="button" className={`logo ${light ? "logo-light" : ""}`} onClick={onClick} aria-label="DriveCare home">
    <img src="/drivecare-mark.svg" alt="" />
    <span>DriveCare</span>
  </button>
);

export const Card = ({ children, className = "" }) => <section className={`card ${className}`}>{children}</section>;

export const Empty = ({ icon: Icon = Car, title = "Nothing here yet", children }) => (
  <div className="empty">
    <Icon size={28} />
    <b>{title}</b>
    <p>{children}</p>
  </div>
);

export const Loading = ({ label = "Loading DriveCare data…" }) => (
  <Card className="loading-card">
    <div className="skeleton-grid">
      <div className="skeleton" /><div className="skeleton" /><div className="skeleton" />
    </div>
    <p className="loading">{label}</p>
  </Card>
);

export const ErrorView = ({ message, onRetry }) => (
  <Card className="error-card">
    <AlertCircle size={22} />
    <p className="form-error">{message}</p>
    <p>Ensure the Spring Boot API is running at <b>http://localhost:8080</b>.</p>
    {onRetry && <button type="button" className="primary" onClick={onRetry}>Try again</button>}
  </Card>
);

export const Status = ({ value }) => (
  <span className={`status ${statusClass(value)}`}>{statusLabel(value)}</span>
);

export function Section({ title, text, action }) {
  return (
    <div className="section-head">
      <div>
        <h2>{title}</h2>
        {text && <p>{text}</p>}
      </div>
      {action}
    </div>
  );
}

export function Stats({ values }) {
  return (
    <div className="stats">
      {values.map(([label, value, Icon], index) => (
        <Card key={label} className={`stat stat-${index}`}>
          <i>{Icon ? <Icon size={16} /> : <LayoutDashboard size={16} />}</i>
          <span>{label}</span>
          <b>{value ?? 0}</b>
        </Card>
      ))}
    </div>
  );
}

export function Metric({ label, value }) {
  return (
    <div className="metric">
      <span>{label}</span>
      <b>{value ?? 0}</b>
    </div>
  );
}

export function ProgressTracker({ status, booking = {}, compact = false }) {
  if (status === "CANCELLED") {
    return (
      <p className="progress-cancelled">
        This booking was cancelled.
      </p>
    );
  }

  const steps = [
    { key: "PENDING", label: "Booking Confirmed", date: booking.createdAt },
    { key: "ASSIGNED", label: "Mechanic Assigned", date: booking.assignedAt },
    { key: "IN_PROGRESS", label: "Service Started", date: booking.startedAt },
    { key: "COMPLETED", label: "Service Completed", date: booking.completedAt },
  ];

  const current = stepIndex(status);

  const formatDate = (value) => {
    if (!value) return "";
    const date = new Date(value);

    if (Number.isNaN(date.getTime())) return "";

    return date.toLocaleString("en-IN", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  return (
    <ol
      className={`service-timeline ${compact ? "compact" : ""}`}
      aria-label="Service progress"
    >
      {steps.map((step, index) => {
        const isDone = index < current || status === "COMPLETED";
        const isActive = index === current;

        return (
          <li
            key={step.key}
            className={`${isDone ? "done" : ""} ${isActive ? "active" : ""}`}
          >
            <span className="timeline-marker">
              {isDone ? "✓" : index + 1}
            </span>

            <div className="timeline-content">
              <strong>{step.label}</strong>

              <small>
                {isDone
                  ? formatDate(step.date) || "Completed"
                  : isActive
                    ? "Current status"
                    : "Pending"}
              </small>
            </div>
          </li>
        );
      })}
    </ol>
  );
}


export function BookingCard({ booking, action, extra }) {
  const imageBase = import.meta.env.VITE_API_URL || "http://localhost:8080";

  const vehicleImage = booking.vehicleImageUrl
    ? booking.vehicleImageUrl.startsWith("http")
      ? booking.vehicleImageUrl
      : `${imageBase}${booking.vehicleImageUrl}`
    : IMAGES.car;

  return (
    <article className="booking-card">
      <div className="job-icon">
        <Car size={20} />
      </div>

      <div className="job-copy">
        <Status value={booking.status} />

        <h3>#{booking.id} · {booking.serviceName}</h3>

        <p>
          {booking.customerName && (
            <><b>{booking.customerName}</b> · </>
          )}
          {booking.vehicleBrand} {booking.vehicleModel} · {booking.vehicleNumber}
        </p>

        {booking.mechanicNotes && (
          <p className="notes">{booking.mechanicNotes}</p>
        )}

        {extra}
      </div>

      <div className="vehicle-thumb">
        <img
          src={vehicleImage}
          alt={`${booking.vehicleBrand || "Vehicle"} ${booking.vehicleModel || ""}`}
          onError={(e) => {
            e.currentTarget.onerror = null;
            e.currentTarget.src = IMAGES.car;
          }}
        />
      </div>

      {action && (
        <div className="booking-action">
          {action}
        </div>
      )}
    </article>
  );
}


export function Toolbar({ query, setQuery, placeholder, children }) {
  return (
    <div className="toolbar">
      <label className="search-field">
        <Search size={16} />
        <input value={query} onChange={(e) => setQuery(e.target.value)} placeholder={placeholder} aria-label={placeholder} />
      </label>
      {children}
    </div>
  );
}

export function Form({ fields, form, setForm, onSubmit, button, extra }) {
  return (
    <form className="form-grid" onSubmit={onSubmit}>
      {fields.map(([name, label, type = "text"]) => (
        <label key={name}>
          {label}
          <input
            type={type}
            value={form[name]}
            onChange={(e) => setForm({ ...form, [name]: e.target.value })}
            required={name !== "phone"}
            minLength={name === "password" ? 6 : undefined}
          />
        </label>
      ))}
      {extra}
      <button className="primary" type="submit">{button}</button>
    </form>
  );
}

export function List({ title, text, items = [], render, empty = "No records available yet." }) {
  return (
    <Card>
      <Section title={title} text={text} />
      {items.length
        ? <div className="list">{items.map((item, index) => <article className="list-row" key={item.id || index}>{render(item)}</article>)}</div>
        : <Empty title={empty} />}
    </Card>
  );
}

export function MobileBar({ onMenu, onLogout, title }) {
  return (
    <div className="mobile-bar">
      <button type="button" className="icon-btn" onClick={onMenu} aria-label="Open menu"><Menu size={20} /></button>
      <strong>{title}</strong>
      <button type="button" className="icon-btn" onClick={onLogout} aria-label="Sign out"><LogOut size={18} /></button>
    </div>
  );
}

export function CloseMenu({ onClick }) {
  return <button type="button" className="icon-btn sidebar-close" onClick={onClick} aria-label="Close menu"><X size={18} /></button>;
}
