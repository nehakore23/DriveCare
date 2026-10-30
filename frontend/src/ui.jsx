import { createContext, useContext, useMemo, useState } from "react";
import {
  AlertCircle, Car, CheckCircle2, LayoutDashboard, LogOut, Menu, Search, X,
} from "lucide-react";
import { IMAGES, rupees, statusClass, statusLabel, stepIndex, STEPS, timeLabel } from "./lib";

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

export function ProgressTracker({ status, compact = false }) {
  if (status === "CANCELLED") {
    return <p className="progress-cancelled">This booking was cancelled.</p>;
  }
  const current = stepIndex(status);
  return (
    <ol className={`progress-track ${compact ? "compact" : ""}`} aria-label="Service progress">
      {STEPS.map((step, index) => (
        <li key={step} className={index <= current ? "done" : ""}>
          <span>{index + 1}</span>
          <small>{statusLabel(step)}</small>
        </li>
      ))}
    </ol>
  );
}

export function BookingCard({ booking, action, extra }) {
  return (
    <article className="booking-card">
      <div className="job-icon"><Car size={20} /></div>
      <div className="job-copy">
        <Status value={booking.status} />
        <h3>#{booking.id} · {booking.serviceName}</h3>
        <p>
          {booking.customerName && <><b>{booking.customerName}</b> · </>}
          {booking.vehicleBrand} {booking.vehicleModel} · {booking.vehicleNumber}
        </p>
        <small>
          {booking.bookingDate} · {timeLabel(booking.bookingTime)}
          {booking.totalPrice != null ? ` · ${rupees(booking.totalPrice)}` : ""}
          {booking.mechanicName ? ` · Mechanic: ${booking.mechanicName}` : " · Mechanic pending"}
        </small>
        <ProgressTracker status={booking.status} compact />
        {booking.mechanicNotes && <p className="notes">{booking.mechanicNotes}</p>}
        {extra}
      </div>
      <div className="vehicle-thumb" style={{ backgroundImage: `linear-gradient(90deg, rgba(11,31,58,.08), transparent), url(${IMAGES.car})` }} />
      {action}
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
