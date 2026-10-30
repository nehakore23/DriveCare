import { useState } from "react";
import { ShieldCheck } from "lucide-react";
import { api } from "./api";
import { IMAGES } from "./lib";
import { Brand, Card, useFeedback } from "./ui";

export default function Auth({ mode, setMode, onSuccess, onHome, afterAuth }) {
  const { toast } = useFeedback();
  const [form, setForm] = useState({ name: "", email: "", phone: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const change = (event) => setForm({ ...form, [event.target.name]: event.target.value });

  async function submit(event) {
    event.preventDefault();
    setError("");
    setLoading(true);
    try {
      if (mode === "register") {
        if (!form.name.trim()) throw new Error("Please enter your full name.");
        if (form.password.length < 6) throw new Error("Password must be at least 6 characters.");
        await api("/api/auth/register", { method: "POST", body: form });
        const data = await api("/api/auth/login", { method: "POST", body: form });
        toast("Account created. Welcome to DriveCare.");
        onSuccess(data, afterAuth);
      } else {
        const path = mode === "mechanic" ? "/api/auth/mechanic/login" : "/api/auth/login";
        onSuccess(await api(path, { method: "POST", body: form }), afterAuth);
        toast("Signed in successfully.");
      }
    } catch (err) {
      setError(err.message);
      toast(err.message, "error");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="auth-page">
      <div className="auth-copy">
        <Brand onClick={onHome} />
        <p className="eyebrow">SMART VEHICLE SERVICE PLATFORM</p>
        <h1>Care for every<br /><em>kilometre.</em></h1>
        <p>Book a service, assign skilled mechanics, track every repair and keep service history in one place.</p>
        <div className="auth-trust">
          <span><ShieldCheck size={14} /> Verified workshops</span>
          <span>Live job updates</span>
          <span>Transparent pricing</span>
        </div>
      </div>
      <Card className="auth-card">
        <div className="auth-tabs">
          <button type="button" className={mode === "login" ? "active" : ""} onClick={() => setMode("login")}>Sign in</button>
          <button type="button" className={mode === "register" ? "active" : ""} onClick={() => setMode("register")}>Register</button>
          <button type="button" className={mode === "mechanic" ? "active" : ""} onClick={() => setMode("mechanic")}>Mechanic</button>
        </div>
        <h2>{mode === "register" ? "Create your account" : mode === "mechanic" ? "Mechanic sign in" : "Welcome back"}</h2>
        <p className="subtext">{mode === "register" ? "Register as a customer to start booking services." : "Use your registered DriveCare account."}</p>
        <form onSubmit={submit}>
          {mode === "register" && (
            <>
              <label>Full name<input name="name" value={form.name} onChange={change} required autoComplete="name" /></label>
              <label>Phone number<input name="phone" value={form.phone} onChange={change} autoComplete="tel" /></label>
            </>
          )}
          <label>Email address<input name="email" type="email" value={form.email} onChange={change} required autoComplete="email" /></label>
          <label>Password<input name="password" type="password" value={form.password} onChange={change} minLength="6" required autoComplete="current-password" /></label>
          {error && <p className="form-error">{error}</p>}
          <button className="primary wide" disabled={loading} type="submit">
            {loading ? "Please wait…" : mode === "register" ? "Create customer account" : "Sign in securely"}
          </button>
        </form>
        <button type="button" className="link-button" onClick={onHome}>Back to homepage</button>
        <div className="demo">
          <b>Demo credentials</b><br />
          Admin: admin@drivecare.com / admin123<br />
          Customer: neha@drivecare.com / neha123<br />
          Mechanic: rahul@drivecare.com / rahul123
        </div>
      </Card>
      <div className="auth-photo" style={{ backgroundImage: `url(${IMAGES.mechanic})` }} aria-hidden="true" />
    </main>
  );
}
