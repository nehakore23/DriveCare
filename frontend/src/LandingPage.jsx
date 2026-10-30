
import "./LandingPage.css";
import { useState } from "react";
import {
  ArrowRight,
  Menu,
  X,
  ShieldCheck,
  Clock3,
  Wrench,
  CarFront,
  Droplets,
  Settings,
  Disc3,
  CheckCircle2,
  Phone,
  Star,
  ChevronRight
} from "lucide-react";

const services = [
  {
    name: "Car Washing",
    description: "Premium exterior wash and interior detailing for a spotless finish.",
    price: "Explore pricing",
    icon: Droplets,
    image: "https://images.unsplash.com/photo-1520340356584-f9917d1eea6f?auto=format&fit=crop&w=900&q=85"
  },
  {
    name: "Engine Repair",
    description: "Professional engine diagnostics and reliable mechanical repairs.",
    price: "Explore pricing",
    icon: Settings,
    image: "https://images.unsplash.com/photo-1486262715619-67b85e0b08d3?auto=format&fit=crop&w=900&q=85"
  },
  {
    name: "Brake Inspection",
    description: "Brake pad, disc and safety inspections by skilled mechanics.",
    price: "Explore pricing",
    icon: Disc3,
    image: "https://images.unsplash.com/photo-1487754180451-c456f719a1fc?auto=format&fit=crop&w=900&q=85"
  },
  {
    name: "General Servicing",
    description: "Complete vehicle maintenance to keep every journey smooth.",
    price: "Explore pricing",
    icon: Wrench,
    image: "https://images.unsplash.com/photo-1632823471565-1ecdf5c7e9c8?auto=format&fit=crop&w=900&q=85"
  }
];

export default function LandingPage({ onLogin, onRegister }) {
  const [menuOpen, setMenuOpen] = useState(false);

  const goTo = (id) => {
    document.getElementById(id)?.scrollIntoView({
      behavior: "smooth"
    });
    setMenuOpen(false);
  };

  return (
    <div className="landing-page">

      <nav className="landing-nav">
        <a className="landing-logo" href="#home">
          <span className="logo-symbol"><CarFront size={25}/></span>
          DRIVE<span>CARE.</span>
        </a>

        <button
          className="mobile-menu-button"
          onClick={() => setMenuOpen(!menuOpen)}
          aria-label="Toggle navigation"
        >
          {menuOpen ? <X/> : <Menu/>}
        </button>

        <div className={`landing-links ${menuOpen ? "open" : ""}`}>
          <button onClick={() => goTo("home")}>Home</button>
          <button onClick={() => goTo("services")}>Services</button>
          <button onClick={() => goTo("about")}>About us</button>
          <button onClick={() => goTo("contact")}>Contact</button>
          <button className="nav-login" onClick={onLogin}>
            Login
          </button>
          <button className="nav-book" onClick={onRegister}>
            Get Started <ArrowRight size={16}/>
          </button>
        </div>
      </nav>

      <section className="hero-section" id="home">
        <div className="hero-overlay"></div>

        <div className="hero-content">
          <div className="hero-tag">
            <span></span> PREMIUM AUTOMOTIVE CARE
          </div>

          <h1>
            Your Car Deserves
            <br/>
            The <span>Best Care.</span>
          </h1>

          <p>
            Experience smarter vehicle servicing with expert mechanics,
            transparent pricing and effortless online booking.
            Your journey deserves nothing less.
          </p>

          <div className="hero-actions">
            <button className="hero-primary" onClick={onLogin}>
              Book a Service <ArrowRight size={18}/>
            </button>

            <button
              className="hero-secondary"
              onClick={() => goTo("services")}
            >
              Explore Services
            </button>
          </div>

          <div className="hero-trust">
            <div><ShieldCheck/> Trusted service</div>
            <div><Clock3/> Easy booking</div>
            <div><Wrench/> Expert mechanics</div>
          </div>
        </div>

        <div className="hero-bottom">
          <span>DRIVE WITH CONFIDENCE.</span>
          <span>01 / 04</span>
        </div>
      </section>

      <section className="benefits-section" id="about">
        <div className="benefit">
          <ShieldCheck/>
          <div>
            <h3>Trusted Professionals</h3>
            <p>Experienced vehicle service experts.</p>
          </div>
        </div>

        <div className="benefit">
          <Clock3/>
          <div>
            <h3>Easy Scheduling</h3>
            <p>Book your preferred service slot.</p>
          </div>
        </div>

        <div className="benefit">
          <CheckCircle2/>
          <div>
            <h3>Service Tracking</h3>
            <p>Stay updated on service progress.</p>
          </div>
        </div>
      </section>

      <section className="services-section" id="services">
        <div className="section-heading">
          <div>
            <span className="section-eyebrow">WHAT WE OFFER</span>
            <h2>Complete Care.<br/>Exceptional Service.</h2>
          </div>
          <p>
            From routine maintenance to complex repairs,
            everything your vehicle needs is right here.
          </p>
        </div>

        <div className="service-grid">
          {services.map((service, index) => {
            const Icon = service.icon;

            return (
              <article className="service-card" key={service.name}>
                <div className="service-image">
                  <img src={service.image} alt={service.name}/>
                  <span className="service-number">
                    0{index + 1}
                  </span>
                </div>

                <div className="service-info">
                  <div className="service-icon"><Icon/></div>
                  <h3>{service.name}</h3>
                  <p>{service.description}</p>
                  <div className="service-bottom">
                    <span>{service.price}</span>
                    <button
                      onClick={onLogin}
                      aria-label={`Book ${service.name}`}
                    >
                      <ArrowRight/>
                    </button>
                  </div>
                </div>
              </article>
            );
          })}
        </div>

        <div className="services-cta">
          <p>Looking for reliable vehicle maintenance?</p>
          <button onClick={onLogin}>
            Book Your Service <ArrowRight size={17}/>
          </button>
        </div>
      </section>

      <section className="about-section">
        <div className="about-image">
          <img
            src="https://images.unsplash.com/photo-1486262715619-67b85e0b08d3?auto=format&fit=crop&w=1200&q=85"
            alt="Professional automotive workshop"
          />
          <div className="about-image-label">
            <Star fill="currentColor"/>
            Quality You Can Trust
          </div>
        </div>

        <div className="about-copy">
          <span className="section-eyebrow">WHY DRIVECARE</span>
          <h2>More Than Service.<br/>It's Peace of Mind.</h2>
          <p>
            DriveCare makes vehicle maintenance simple.
            Manage your vehicles, schedule appointments,
            connect with mechanics and follow every service
            update from one convenient platform.
          </p>

          <ul>
            <li><CheckCircle2/> Easy online service booking</li>
            <li><CheckCircle2/> Professional mechanic assignment</li>
            <li><CheckCircle2/> Transparent service updates</li>
            <li><CheckCircle2/> Convenient service history</li>
          </ul>

          <button className="about-button" onClick={onRegister}>
            Create Your Account <ArrowRight size={17}/>
          </button>
        </div>
      </section>

      <section className="contact-section" id="contact">
        <div>
          <span className="section-eyebrow">READY TO GET STARTED?</span>
          <h2>Give Your Car the Care It Deserves.</h2>
          <p>Book your next service with DriveCare today.</p>
        </div>
        <button onClick={onLogin}>
          Book a Service <ChevronRight/>
        </button>
      </section>

      <footer className="landing-footer">
        <a className="landing-logo" href="#home">
          <CarFront/> DRIVE<span>CARE.</span>
        </a>
        <p>Smart vehicle care. Better journeys.</p>
        <span>© {new Date().getFullYear()} DriveCare. All rights reserved.</span>
      </footer>

    </div>
  );
}
