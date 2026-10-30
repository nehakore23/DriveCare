import { useEffect, useState } from "react";
import {
  ArrowRight, CalendarCheck, Car, Droplets, Gauge, Menu, ShieldCheck, Sparkles, Wrench, X,
} from "lucide-react";
import { api } from "./api";
import { IMAGES, SHOWCASE, mediaForService, rupees } from "./lib";
import { Brand } from "./ui";

const ICONS = [Wrench, Droplets, Gauge, ShieldCheck, Car, Sparkles];

export default function Home({ session, onBook, onAuth, onDashboard, onExplore }) {
  const [services, setServices] = useState([]);
  const [error, setError] = useState("");
  const [open, setOpen] = useState(false);

  useEffect(() => {
    api("/api/public/services")
      .then(setServices)
      .catch((err) => setError(err.message));
  }, []);

  const go = (fn) => { setOpen(false); fn(); };

  return (
    <div className="site">
      <header className="site-nav">
        <Brand onClick={() => window.scrollTo({ top: 0, behavior: "smooth" })} />
        <nav className={open ? "open" : ""}>
          <a href="#services" onClick={() => setOpen(false)}>Services</a>
          <a href="#workshop" onClick={() => setOpen(false)}>Workshop</a>
          <a href="#process" onClick={() => setOpen(false)}>How it works</a>
          {session
            ? <button type="button" className="ghost" onClick={() => go(onDashboard)}>Dashboard</button>
            : <button type="button" className="ghost" onClick={() => go(() => onAuth("login"))}>Sign in</button>}
          <button type="button" className="primary" onClick={() => go(onBook)}>Book a Service</button>
        </nav>
        <button type="button" className="icon-btn nav-toggle" onClick={() => setOpen(!open)} aria-label="Toggle navigation">
          {open ? <X size={20} /> : <Menu size={20} />}
        </button>
      </header>

      <section className="hero" style={{ backgroundImage: `linear-gradient(90deg, rgba(8,18,36,.86) 8%, rgba(8,18,36,.42) 55%, rgba(8,18,36,.18)), url(${IMAGES.hero})` }}>
        <div className="hero-copy reveal">
          <p className="eyebrow light">Premium automotive care</p>
          <h1>Your Car Deserves the Best Care</h1>
          <p>Book servicing, washing, engine work and brake inspections with verified mechanics. Track every job from appointment to handover.</p>
          <div className="hero-actions">
            <button type="button" className="primary lg" onClick={onBook}>Book a Service <ArrowRight size={18} /></button>
            <button type="button" className="ghost light lg" onClick={onExplore}>Explore Services</button>
          </div>
        </div>
      </section>

      <section id="services" className="section">
        <div className="section-intro">
          <p className="eyebrow">Live catalogue</p>
          <h2>Services priced from the workshop</h2>
          <p>Cards below use active services from the DriveCare API, not placeholder prices.</p>
        </div>
        {error && <p className="form-error">{error}. Sign in after the API is available to book live slots.</p>}
        <div className="service-grid">
          {(services.length ? services : SHOWCASE.map((item) => ({ serviceName: item.title, description: item.text, price: null }))).map((service, index) => {
            const Icon = ICONS[index % ICONS.length];
            return (
              <article key={service.id || service.serviceName} className="service-card">
                <div className="service-photo" style={{ backgroundImage: `url(${mediaForService(service.serviceName)})` }}>
                  <span className="service-icon"><Icon size={18} /></span>
                </div>
                <div className="service-body">
                  <h3>{service.serviceName}</h3>
                  <p>{service.description}</p>
                  <div className="service-meta">
                    <b>{service.price != null ? rupees(service.price) : "Sign in for pricing"}</b>
                    <button type="button" className="primary" onClick={onBook}>Book now</button>
                  </div>
                </div>
              </article>
            );
          })}
        </div>
      </section>

      <section id="workshop" className="section">
        <div className="section-intro">
          <p className="eyebrow">The workshop</p>
          <h2>Care for every surface, system and journey</h2>
        </div>
        <div className="showcase-grid">
          {SHOWCASE.map((item) => (
            <article key={item.title} className="showcase-card">
              <img src={item.image} alt={item.title} />
              <div>
                <h3>{item.title}</h3>
                <p>{item.text}</p>
              </div>
            </article>
          ))}
        </div>
      </section>

      <section id="process" className="section process">
        <div className="section-intro">
          <p className="eyebrow">Simple flow</p>
          <h2>Book. Assign. Service. Track.</h2>
        </div>
        <div className="process-grid">
          {[
            ["01", "Choose a service", "Pick from live catalogue pricing and your garage vehicles."],
            ["02", "Confirm the slot", "Select date, time and notes. We lock the booking instantly."],
            ["03", "Workshop takes over", "Admins assign a mechanic. You watch progress in real time."],
            ["04", "Drive away sure", "Pay from receipts, review the job and keep service history."],
          ].map(([n, title, text]) => (
            <article key={n} className="process-card">
              <span>{n}</span>
              <h3>{title}</h3>
              <p>{text}</p>
            </article>
          ))}
        </div>
      </section>

      <section className="cta-band">
        <div>
          <CalendarCheck size={28} />
          <div>
            <h2>Ready for a quieter, cleaner drive?</h2>
            <p>Create a customer account or sign in to book against real workshop availability.</p>
          </div>
        </div>
        <button type="button" className="primary lg" onClick={onBook}>Book a Service</button>
      </section>

      <footer className="site-footer">
        <Brand />
        <p>Vehicle service and booking platform. JWT-secured customer, admin and mechanic portals.</p>
      </footer>
    </div>
  );
}
