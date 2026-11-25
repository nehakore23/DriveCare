import { useEffect, useMemo, useState } from "react";
import {
  Bell, Calendar, Car, CheckCircle2, ClipboardList, IndianRupee, Play, Star, Users, Wrench,
} from "lucide-react";
import { api } from "./api";
import { matchesQuery, rupees, timeLabel } from "./lib";
import {
  BookingCard, Card, Empty, ErrorView, Form, List, Loading, Metric, ProgressTracker,
  Section, Stats, Status, Toolbar, useFeedback,
} from "./ui";

export function AdminPortal({ token, page }) {
  const { toast, confirm } = useFeedback();
  const [data, setData] = useState({});
  const [error, setError] = useState("");
  const [query, setQuery] = useState("");
  const [status, setStatus] = useState("ALL");

  const load = async () => {
    try {
      const [dashboard, bookings, mechanics, services, payments, reviews, reports] = await Promise.all([
        api("/api/admin/dashboard", { token }),
        api("/api/admin/bookings", { token }),
        api("/api/admin/mechanics", { token }),
        api("/api/admin/services", { token }),
        api("/api/admin/payments", { token }),
        api("/api/admin/reviews", { token }),
        api("/api/admin/reports", { token }),
      ]);
      setError("");
      setData({ dashboard, bookings, mechanics, services, payments, reviews, reports });
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => { load(); }, []);

  const assign = async (bookingId, mechanicId) => {
    const mechanic = data.mechanics?.find((item) => String(item.id) === String(mechanicId));
    const ok = await confirm({
      title: "Assign mechanic",
      message: `Assign ${mechanic?.name || "this mechanic"} to booking #${bookingId}?`,
      confirmLabel: "Assign",
    });
    if (!ok) return;
    try {
      await api(`/api/admin/bookings/${bookingId}/assign`, { method: "POST", token, body: { mechanicId: Number(mechanicId) } });
      toast("Mechanic assigned.");
      load();
    } catch (err) {
      toast(err.message, "error");
    }
  };

  if (error) return <ErrorView message={error} onRetry={load} />;
  if (!data.dashboard) return <Loading />;

  const bookings = (data.bookings || []).filter((booking) => {
    const statusOk = status === "ALL" || booking.status === status;
    return statusOk && matchesQuery(booking, query, ["serviceName", "customerName", "vehicleNumber", "id", "mechanicName"]);
  });

  if (page === "dashboard") {
    return (
      <>
        <Stats values={[
          ["Customers", data.dashboard.totalCustomers, Users],
          ["Mechanics", data.dashboard.totalMechanics, Wrench],
          ["Vehicles", data.dashboard.totalVehicles, Car],
          ["Bookings", data.dashboard.totalBookings, Calendar],
          ["Pending", data.dashboard.pendingBookings, ClipboardList],
          ["Revenue", rupees(data.dashboard.revenue), IndianRupee],
        ]} />
        <div className="split">
          <Card>
            <Section title="Operational snapshot" text="Live counts from the admin dashboard API." />
            <div className="mini-grid">
              <Metric label="Assigned jobs" value={data.dashboard.assignedBookings} />
              <Metric label="In progress" value={data.dashboard.inProgressServices} />
              <Metric label="Completed" value={data.dashboard.completedServices} />
            </div>
          </Card>
          <Card>
            <Section title="Popular services" text="Based on actual booking volume." />
            <div className="mini-grid">
              {(data.reports?.popularServices || []).map((item) => (
                <Metric key={item.serviceName} label={item.serviceName} value={`${item.bookings} bookings`} />
              ))}
              {!data.reports?.popularServices?.length && <Empty title="No report data yet." />}
            </div>
          </Card>
        </div>
      </>
    );
  }

  if (page === "booking") {
    return (
      <Card>
        <Section title="Booking management" text="Search, filter and assign available mechanics." />
        <Toolbar query={query} setQuery={setQuery} placeholder="Search customer, vehicle or service">
          <select value={status} onChange={(e) => setStatus(e.target.value)} aria-label="Filter by status">
            {["ALL", "PENDING", "ASSIGNED", "IN_PROGRESS", "COMPLETED", "CANCELLED"].map((item) => (
              <option key={item} value={item}>{item.replaceAll("_", " ")}</option>
            ))}
          </select>
        </Toolbar>
        <div className="table-wrap">
          <table>
            <thead>
              <tr><th>Booking</th><th>Customer & vehicle</th><th>Slot</th><th>Status</th><th>Assignment</th></tr>
            </thead>
            <tbody>
              {bookings.map((booking) => (
                <tr key={booking.id}>
                  <td>#{booking.id}<br /><b>{booking.serviceName}</b><br /><small>{rupees(booking.totalPrice)}</small></td>
                  <td>
  <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
    <img
      src={
        booking.vehicleImageUrl
          ? booking.vehicleImageUrl.startsWith("http")
            ? booking.vehicleImageUrl
            : `${import.meta.env.VITE_API_URL || "http://localhost:8080"}${booking.vehicleImageUrl}`
          : "/images/default-vehicle.png"
      }
      alt="Vehicle"
     style={{
  width: "110px",
  height: "80px",
  objectFit: "contain",
  objectPosition: "center",
  backgroundColor: "#f5f6f8",
  borderRadius: "8px",
  padding: "4px",
  flexShrink: 0
}}
    />
    <div>
      {booking.customerName}
      <br />
      <small>{booking.vehicleBrand} {booking.vehicleModel} · {booking.vehicleNumber}</small>
    </div>
  </div>
</td>
                  <td>{booking.bookingDate}<br /><small>{timeLabel(booking.bookingTime)}</small></td>
                  <td><Status value={booking.status} /></td>
                  <td>
                    {["COMPLETED", "CANCELLED"].includes(booking.status) ? "—" : (
                      <select defaultValue={booking.mechanicId || ""} onChange={(e) => e.target.value && assign(booking.id, e.target.value)} aria-label={`Assign mechanic for booking ${booking.id}`}>
                        <option value="">Choose mechanic</option>
                        {data.mechanics?.filter((mechanic) => mechanic.available && mechanic.status).map((mechanic) => (
                          <option key={mechanic.id} value={mechanic.id}>{mechanic.name} · {mechanic.specialization}</option>
                        ))}
                      </select>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {!bookings.length && <Empty title="No bookings match this filter." />}
        </div>
      </Card>
    );
  }

  if (page === "team") return <AdminMechanics token={token} mechanics={data.mechanics || []} reload={load} />;
  if (page === "service") return <AdminServices token={token} services={data.services || []} reload={load} />;
  if (page === "payment") {
    return <List title="Payment receipts" items={data.payments} render={(payment) => (
      <><b>{payment.receiptNumber}</b><span>{payment.customerName} · Booking #{payment.bookingId} · {payment.status}</span><strong>{rupees(payment.amount)}</strong></>
    )} />;
  }
  return (
    <>
      <List title="Customer reviews" items={data.reviews} render={(review) => (
        <><b>{review.customerName} · {review.rating}/5</b><span>{review.serviceName} · {review.mechanicName || "Mechanic not assigned"}</span><p>{review.comment}</p></>
      )} />
    </>
  );
}

export function CustomerPortal({ token, page, go }) {
  const [data, setData] = useState({});
  const [error, setError] = useState("");

  const load = async () => {
    try {
      const [dashboard, vehicles, services, bookings, payments, reviews] = await Promise.all([
        api("/api/customer/dashboard", { token }),
        api("/api/customer/vehicles", { token }),
        api("/api/customer/services", { token }),
        api("/api/customer/bookings", { token }),
        api("/api/customer/payments", { token }),
        api("/api/customer/reviews", { token }),
      ]);
      setError("");
      setData({ dashboard, vehicles, services, bookings, payments, reviews });
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => { load(); }, []);
  if (error) return <ErrorView message={error} onRetry={load} />;
  if (!data.dashboard) return <Loading />;

  const upcoming = data.dashboard.upcomingBookings || [];
  const history = data.dashboard.serviceHistory || [];
  const tracked = data.dashboard.activeService || upcoming[0];

  if (page === "dashboard") {
    return (
      <div className="customer-dashboard-layout">
        <div className="customer-primary">
          <Stats values={[
            ["My vehicles", data.vehicles.length, Car],
            ["Upcoming", upcoming.length, Calendar],
            ["Service history", history.length, ClipboardList],
            ["Receipts", data.payments.length, IndianRupee],
          ]} />
          <Card>
            <Section title="Service progress" text="Live status from your latest active or upcoming booking." />
            {tracked ? (
              <>
                <p className="tracked-label">{tracked.serviceName} · {tracked.vehicleNumber}</p>
               <ProgressTracker
  status={tracked.status}
  booking={tracked}
/>
                <BookingCard booking={tracked} />
              </>
            ) : (
              <Empty title="No active service">No service is currently in progress. <button className="link-button" onClick={() => go("booking")}>Book a service</button></Empty>
            )}
          </Card>
          <div className="split">
            <Card>
              <Section title="Upcoming appointments" />
              {upcoming.length ? upcoming.map((booking) => <BookingCard key={booking.id} booking={booking} />) : <Empty title="No upcoming appointments." />}
            </Card>
            <Card>
              <Section title="Vehicle details" action={<button className="ghost" onClick={() => go("vehicle")}>Manage</button>} />
              {data.vehicles.length ? data.vehicles.map((vehicle) => (
                <article key={vehicle.id} className="vehicle-row">
                  <Car size={18} />
                  <div>
                    <b>{vehicle.brand} {vehicle.model}</b>
                    <span>{vehicle.vehicleNumber} · {vehicle.year || "—"} · {vehicle.fuelType || "—"}</span>
                  </div>
                </article>
              )) : <Empty title="Add a vehicle to start booking." />}
            </Card>
          </div>
          <Card>
            <Section title="Booking history" />
            {history.length ? history.map((booking) => <BookingCard key={booking.id} booking={booking} />) : <Empty title="No completed or cancelled jobs yet." />}
          </Card>
        </div>
        <CustomerPaymentSummary
          payments={data.payments}
          compact
          token={token}
          reload={load}
        />
      </div>
    );
  }
  if (page === "vehicle") return <Vehicles token={token} vehicles={data.vehicles} reload={load} />;
  if (page === "booking") return <CustomerBookings token={token} data={data} reload={load} />;
  if (page === "payment") {
    return (
      <div className="customer-payment-layout">
        <List title="Payments & receipts" items={data.payments} render={(payment) => (
          <><b>{payment.receiptNumber}</b><span>Booking #{payment.bookingId} · {payment.status}</span><strong>{rupees(payment.amount)}</strong></>
        )} />
        <CustomerPaymentSummary payments={data.payments}
         compact
          token={token}
  reload={load} />
      </div>
    );
  }
  return <Reviews token={token} reviews={data.reviews} bookings={data.bookings} reload={load} />;
}

export function MechanicPortal({ token, page }) {
  const { toast, confirm } = useFeedback();
  const [data, setData] = useState({});
  const [error, setError] = useState("");
  const [query, setQuery] = useState("");

  const load = async () => {
    try {
      const [dashboard, today, assigned, progress, done, schedule, notifications] = await Promise.all([
        api("/api/mechanic/dashboard", { token }),
        api("/api/mechanic/jobs/today", { token }),
        api("/api/mechanic/jobs/assigned", { token }),
        api("/api/mechanic/jobs/in-progress", { token }),
        api("/api/mechanic/jobs/completed", { token }),
        api("/api/mechanic/schedule", { token }),
        api("/api/mechanic/notifications", { token }),
      ]);
      setError("");
      setData({ dashboard, today, assigned, progress, done, schedule, notifications });
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
  load();
}, [token]);
  if (error) return <ErrorView message={error} onRetry={load} />;
  if (!data.dashboard) return <Loading />;

  const update = async (id, action) => {
    const ok = await confirm({
      title: action === "start" ? "Start this job?" : "Mark job completed?",
      message: action === "start" ? "The customer will see this booking as in progress." : "This will complete the service and generate payment records.",
      confirmLabel: action === "start" ? "Start service" : "Complete job",
    });
    if (!ok) return;
    try {
      await api(`/api/mechanic/jobs/${id}/${action}`, { method: "POST", token });
      toast(action === "start" ? "Service started." : "Job marked completed.");
      load();
    } catch (err) {
      toast(err.message, "error");
    }
  };

  const saveNotes = async (id, mechanicNotes) => {
    try {
      await api(`/api/mechanic/jobs/${id}/notes`, { method: "PUT", token, body: { mechanicNotes } });
      toast("Workshop notes saved.");
      load();
    } catch (err) {
      toast(err.message, "error");
    }
  };

  const markRead = async (id) => {
    try {
      await api(`/api/mechanic/notifications/${id}/read`, { method: "PUT", token });
      load();
    } catch (err) {
      toast(err.message, "error");
    }
  };

  const filterJobs = (jobs = []) => jobs.filter((job) => matchesQuery(job, query, ["serviceName", "customerName", "vehicleNumber", "vehicleBrand"]));

  if (page === "dashboard") {
    return (
      <>
        <Stats values={[
          ["Today's jobs", data.today.length, Calendar],
          ["Assigned", data.assigned.length, ClipboardList],
          ["In progress", data.progress.length, Play],
          ["Completed", data.done.length, CheckCircle2],
          ["Notifications", data.notifications.filter((item) => !item.readFlag).length, Bell],
        ]} />
        <JobList title="Today's priority" jobs={filterJobs(data.today)} update={update} saveNotes={saveNotes} query={query} setQuery={setQuery} />
      </>
    );
  }
  if (page === "jobs") return <JobList title="My assigned jobs" jobs={filterJobs(data.assigned)} update={update} saveNotes={saveNotes} query={query} setQuery={setQuery} />;
  if (page === "booking") return <JobList title="My schedule" jobs={filterJobs(data.schedule)} update={update} saveNotes={saveNotes} query={query} setQuery={setQuery} />;
  return (
    <List title="Notifications" items={data.notifications} render={(item) => (
      <>
        <b>{item.readFlag ? item.title : `NEW · ${item.title}`}</b>
        <span>{item.message}</span>
        {!item.readFlag && <button type="button" className="ghost" onClick={() => markRead(item.id)}>Mark read</button>}
      </>
    )} />
  );
}

function JobList({ title, jobs = [], update, saveNotes, query, setQuery }) {
  return (
    <Card>
      <Section title={title} />
      <Toolbar query={query} setQuery={setQuery} placeholder="Search jobs, customers or vehicles" />
      {jobs.length ? (
        <div className="job-list">
          {jobs.map((job) => (
            <BookingCard
              key={job.id}
              booking={job}
              extra={<JobNotes key={`${job.id}-${job.mechanicNotes || ""}`} job={job} onSave={saveNotes} />}
              action={job.status === "ASSIGNED"
                ? <button className="primary" onClick={() => update(job.id, "start")}><Play size={16} /> Start service</button>
                : job.status === "IN_PROGRESS"
                  ? <button className="primary" onClick={() => update(job.id, "complete")}><CheckCircle2 size={16} /> Mark completed</button>
                  : null}
            />
          ))}
        </div>
      ) : <Empty title="No jobs to show." />}
    </Card>
  );
}

function JobNotes({ job, onSave }) {
  const [notes, setNotes] = useState(job.mechanicNotes || "");
  return (
    <form className="notes-form" onSubmit={(e) => { e.preventDefault(); onSave(job.id, notes); }}>
      <label>
        Workshop notes
        <textarea value={notes} onChange={(e) => setNotes(e.target.value)} placeholder="Customer vehicle findings, parts used…" />
      </label>
      <p className="vehicle-facts">
        {job.customerName} · {job.customerPhone || "No phone"} · {job.vehicleBrand} {job.vehicleModel} ({job.vehicleNumber})
      </p>
      {["ASSIGNED", "IN_PROGRESS"].includes(job.status) && <button type="submit" className="ghost">Save notes</button>}
    </form>
  );
}

function CustomerPaymentSummary({
  payments = [],
  compact = false,
  token,
  reload,
}) {
  const { toast } = useFeedback();
  const [processingPayment, setProcessingPayment] = useState(null);

  const total = payments.reduce(
    (sum, item) => sum + Number(item.amount || 0),
    0
  );

  const paid = payments.filter(
    (item) =>
      item.status === "PAID" ||
      item.status === "SUCCESS" ||
      item.status === "COMPLETED"
  ).length;

  const loadRazorpay = () =>
    new Promise((resolve) => {
      if (window.Razorpay) {
        resolve(true);
        return;
      }

      const existingScript = document.querySelector(
        'script[src="https://checkout.razorpay.com/v1/checkout.js"]'
      );

      if (existingScript) {
        existingScript.addEventListener("load", () => resolve(true), { once: true });
        existingScript.addEventListener("error", () => resolve(false), { once: true });
        return;
      }

      const script = document.createElement("script");
      script.src = "https://checkout.razorpay.com/v1/checkout.js";
      script.onload = () => resolve(true);
      script.onerror = () => resolve(false);
      document.body.appendChild(script);
    });

  const startPayment = async (payment) => {
    try {
      setProcessingPayment(payment.bookingId);

      const loaded = await loadRazorpay();

      if (!loaded) {
        toast("Razorpay Checkout could not be loaded.", "error");
        setProcessingPayment(null);
        return;
      }

      const order = await api(
        `/api/customer/bookings/${payment.bookingId}/payment/order`,
        {
          method: "POST",
          token,
        }
      );

      if (!order?.keyId || !order?.orderId) {
        throw new Error("Invalid Razorpay order response.");
      }

      const options = {
        key: order.keyId,
        amount: order.amount,
        currency: order.currency || "INR",
        name: "DRIVECARE",
        description: `Vehicle Service - Booking #${payment.bookingId}`,
        order_id: order.orderId,

        handler: async (response) => {
          try {
            await api("/api/customer/payments/verify", {
              method: "POST",
              token,
              body: {
                razorpayOrderId: response.razorpay_order_id,
                razorpayPaymentId: response.razorpay_payment_id,
                razorpaySignature: response.razorpay_signature,
              },
            });

            toast("Payment successful.");
            await reload();
          } catch (err) {
            console.error("Payment verification failed:", err);
            toast(err.message || "Payment verification failed.", "error");
          } finally {
            setProcessingPayment(null);
          }
        },

        notes: {
          bookingId: String(payment.bookingId),
          paymentId: String(order.paymentId),
        },

        theme: {
          color: "#6d5dfc",
        },

        modal: {
          ondismiss: () => {
            setProcessingPayment(null);
            toast("Payment cancelled.");
          },
        },
      };

      const razorpay = new window.Razorpay(options);

      razorpay.on("payment.failed", (response) => {
        console.error("Razorpay payment failed:", response.error);
        toast(response.error?.description || "Payment failed.", "error");
        setProcessingPayment(null);
      });

      razorpay.open();
    } catch (err) {
      console.error("Unable to start payment:", err);
      toast(err.message || "Unable to start payment.", "error");
      setProcessingPayment(null);
    }
  };

  return (
    <aside className={`customer-payment-summary ${compact ? "compact" : ""}`}>
      <Card>
        <p className="summary-label">PAYMENT STATUS</p>

        <b className="summary-total">{rupees(total)}</b>

        <span className="summary-caption">
          Total from actual receipts
        </span>

        <div className="summary-rule" />

        <div className="summary-row">
          <span>Receipts</span>
          <b>{payments.length}</b>
        </div>

        <div className="summary-row">
          <span>Latest status</span>
          <b>{payments[0]?.status || "No payments"}</b>
        </div>

        <div className="summary-row">
          <span>Cleared</span>
          <b>{paid}</b>
        </div>

        {payments.some(
          (payment) =>
            payment.status === "PENDING" ||
            payment.status === "FAILED"
        ) && (
          <div className="payment-action-list">
            {payments
              .filter(
                (payment) =>
                  payment.status === "PENDING" ||
                  payment.status === "FAILED"
              )
              .map((payment) => (
                <div
                  key={payment.id}
                  className="pending-payment-card"
                >
                  <div>
                    <b>Booking #{payment.bookingId}</b>
                    <span>
                      {payment.status === "FAILED"
                        ? "Payment failed"
                        : "Payment pending"}
                    </span>
                    <strong>{rupees(payment.amount)}</strong>
                  </div>

                  <button
                    type="button"
                    className="primary"
                    onClick={() => startPayment(payment)}
                    disabled={processingPayment === payment.bookingId}
                  >
                    {processingPayment === payment.bookingId
                      ? "Opening payment..."
                      : "Pay Now"}
                  </button>
                </div>
              ))}
          </div>
        )}
      </Card>
    </aside>
  );
}

function AdminMechanics({ token, mechanics, reload }) {
  const { toast } = useFeedback();
  const [form, setForm] = useState({ name: "", email: "", password: "", phone: "", specialization: "", experience: 0 });
  const submit = async (e) => {
    e.preventDefault();
    try {
      await api("/api/admin/mechanics", { method: "POST", token, body: { ...form, experience: Number(form.experience) } });
      setForm({ name: "", email: "", password: "", phone: "", specialization: "", experience: 0 });
      toast("Mechanic account created.");
      reload();
    } catch (err) {
      toast(err.message, "error");
    }
  };
  return (
    <>
      <Card>
        <Section title="Add mechanic" text="Create a secure mechanic account." />
        <Form fields={[["name", "Name"], ["email", "Email", "email"], ["password", "Password", "password"], ["phone", "Phone"], ["specialization", "Specialization"], ["experience", "Experience", "number"]]} form={form} setForm={setForm} onSubmit={submit} button="Add mechanic" />
      </Card>
      <List title="Current team" items={mechanics} render={(mechanic) => (
        <><b>{mechanic.name}</b><span>{mechanic.specialization || "General technician"} · {mechanic.experience} years · {mechanic.available ? "Available" : "Busy"}</span></>
      )} />
    </>
  );
}

function AdminServices({ token, services, reload }) {
  const { toast } = useFeedback();
  const [form, setForm] = useState({ serviceName: "", description: "", price: "" });
  const submit = async (e) => {
    e.preventDefault();
    try {
      await api("/api/admin/services", { method: "POST", token, body: { ...form, price: Number(form.price), status: true } });
      setForm({ serviceName: "", description: "", price: "" });
      toast("Service added to the catalogue.");
      reload();
    } catch (err) {
      toast(err.message, "error");
    }
  };
  return (
    <>
      <Card>
        <Section title="Add service" />
        <Form fields={[["serviceName", "Service name"], ["description", "Description"], ["price", "Price (₹)", "number"]]} form={form} setForm={setForm} onSubmit={submit} button="Add service" />
      </Card>
      <List title="Available services" items={services} render={(service) => (
        <><b>{service.serviceName}</b><span>{service.description}</span><strong>{rupees(service.price)}</strong></>
      )} />
    </>
  );
}


function Vehicles({ token, vehicles, reload }) {
  const { toast } = useFeedback();

  const [form, setForm] = useState({
    vehicleNumber: "",
    brand: "",
    model: "",
    year: "",
    fuelType: "",
  });

  const [imageFile, setImageFile] = useState(null);
  const [saving, setSaving] = useState(false);

  const submit = async (e) => {
    e.preventDefault();

    try {
      setSaving(true);

      // First create vehicle
      const vehicle = await api("/api/customer/vehicles", {
        method: "POST",
        token,
        body: {
          ...form,
          year: form.year ? Number(form.year) : null,
        },
      });

      // Upload photo only when customer selects one
      if (imageFile) {
        const formData = new FormData();
        formData.append("file", imageFile);

        await api(`/api/customer/vehicles/${vehicle.id}/image`, {
          method: "POST",
          token,
          body: formData,
        });
      }

      setForm({
        vehicleNumber: "",
        brand: "",
        model: "",
        year: "",
        fuelType: "",
      });

      setImageFile(null);
      toast("Vehicle added successfully.");
      await reload();

    } catch (err) {
      toast(err.message, "error");
    } finally {
      setSaving(false);
    }
  };

  const imageBase = import.meta.env.VITE_API_URL || "http://localhost:8080";

  return (
    <>
      <Card>
        <Section
          title="Add a vehicle"
          text="Add your vehicle details and upload its photo (optional)."
        />

        <Form
          fields={[
            ["vehicleNumber", "Registration number"],
            ["brand", "Brand"],
            ["model", "Model"],
            ["year", "Year", "number"],
            ["fuelType", "Fuel type"],
          ]}
          form={form}
          setForm={setForm}
          onSubmit={submit}
          button={saving ? "Saving..." : "Add vehicle"}
        />

        <div className="form-grid">
          <label className="full">
            Vehicle photo (optional)
            <input
              type="file"
              accept="image/png,image/jpeg,image/webp"
              onChange={(e) =>
                setImageFile(e.target.files?.[0] || null)
              }
            />
          </label>

          {imageFile && (
            <p className="full">
              Selected photo: {imageFile.name}
            </p>
          )}
        </div>
      </Card>

      <List
        title="My garage"
        items={vehicles}
        render={(vehicle) => {
          const imageUrl = vehicle.imageUrl
            ? vehicle.imageUrl.startsWith("http")
              ? vehicle.imageUrl
              : `${imageBase}${vehicle.imageUrl}`
            : "/images/default-vehicle.png";

          return (
            <div className="vehicle-row">
              <img
                src={imageUrl}
                alt={`${vehicle.brand} ${vehicle.model}`}
                onError={(e) => {
                  e.currentTarget.onerror = null;
                  e.currentTarget.src = "/images/default-vehicle.png";
                }}
              style={{
  width: "140px",
  height: "100px",
  objectFit: "contain",
  objectPosition: "center",
  backgroundColor: "#f5f6f8",
  borderRadius: "10px",
  padding: "5px",
  flexShrink: 0
}}
              />

              <div>
                <b>{vehicle.brand} {vehicle.model}</b>
                <span>
                  {vehicle.vehicleNumber} · {vehicle.year || "—"} · {vehicle.fuelType || "—"}
                </span>
              </div>
            </div>
          );
        }}
      />
    </>
  );
}


function CustomerBookings({ token, data, reload }) {
  const { toast, confirm } = useFeedback();
  const [form, setForm] = useState({ vehicleId: "", serviceId: "", bookingDate: "", bookingTime: "", notes: "" });
  const [query, setQuery] = useState("");
  const [confirmed, setConfirmed] = useState(null);
  const [processingPayment, setProcessingPayment] = useState(false);
  const selected = data.services.find((service) => String(service.id) === String(form.serviceId));
  const today = new Date().toISOString().slice(0, 10);

  const loadRazorpay = () => new Promise((resolve) => {
    if (window.Razorpay) {
      resolve(true);
      return;
    }

    const script = document.createElement("script");
    script.src = "https://checkout.razorpay.com/v1/checkout.js";
    script.onload = () => resolve(true);
    script.onerror = () => resolve(false);
    document.body.appendChild(script);
  });

  const startPayment = async (booking) => {
    try {
      setProcessingPayment(true);

      const loaded = await loadRazorpay();
      if (!loaded) {
        toast("Razorpay Checkout could not be loaded.", "error");
        setProcessingPayment(false);
        return;
      }

      const order = await api(`/api/customer/bookings/${booking.id}/payment/order`, {
        method: "POST",
        token,
      });

      const options = {
        key: order.keyId,
        amount: order.amount,
        currency: order.currency || "INR",
        name: "DriveCare",
        description: `${booking.serviceName || "Vehicle Service"} - Booking #${booking.id}`,
        order_id: order.orderId,
        handler: async (response) => {
          try {
            const verification = await api("/api/customer/payments/verify", {
              method: "POST",
              token,
              body: {
                razorpayOrderId: response.razorpay_order_id,
                razorpayPaymentId: response.razorpay_payment_id,
                razorpaySignature: response.razorpay_signature,
              },
            });

            console.log("Payment verified:", verification);
            toast("Payment successful.");
            setConfirmed({ ...booking, paymentStatus: "PAID" });
            reload();
          } catch (err) {
            console.error("Payment verification failed:", err);
            toast(err.message || "Payment verification failed.", "error");
          } finally {
            setProcessingPayment(false);
          }
        },
        notes: {
          bookingId: String(booking.id),
          paymentId: String(order.paymentId),
        },
        theme: {
          color: "#6d5dfc",
        },
        modal: {
          ondismiss: () => {
            setProcessingPayment(false);
            toast("Payment cancelled.");
          },
        },
      };

      const razorpay = new window.Razorpay(options);

      razorpay.on("payment.failed", (response) => {
        console.error("Razorpay payment failed:", response.error);
        toast(response.error?.description || "Payment failed.", "error");
        setProcessingPayment(false);
      });

      razorpay.open();
    } catch (err) {
      console.error("Unable to start payment:", err);
      toast(err.message || "Unable to start payment.", "error");
      setProcessingPayment(false);
    }
  };

  const submit = async (e) => {
    e.preventDefault();
    if (!form.vehicleId) return toast("Select a vehicle.", "error");
    if (!form.serviceId) return toast("Select a service.", "error");
    if (form.bookingDate < today) return toast("Choose a date from today onwards.", "error");

    const ok = await confirm({
      title: "Confirm booking",
      message: `${selected?.serviceName || "Service"} for ${rupees(selected?.price)} on ${form.bookingDate} at ${form.bookingTime}.`,
      confirmLabel: "Place booking",
    });
    if (!ok) return;

    try {
      const booking = await api("/api/customer/bookings", {
        method: "POST",
        token,
        body: {
          ...form,
          vehicleId: Number(form.vehicleId),
          serviceId: Number(form.serviceId),
          bookingTime: `${form.bookingTime}:00`,
        },
      });

      setConfirmed(booking);
      setForm({ vehicleId: "", serviceId: "", bookingDate: "", bookingTime: "", notes: "" });
      toast("Booking created. Opening payment...");
      reload();

      await startPayment(booking);
    } catch (err) {
      toast(err.message, "error");
    }
  };

  const cancel = async (id) => {
    const ok = await confirm({ title: "Cancel this booking?", message: "Pending and assigned slots can be cancelled.", confirmLabel: "Cancel booking", danger: true });
    if (!ok) return;
    try {
      await api(`/api/customer/bookings/${id}/cancel`, { method: "POST", token });
      toast("Booking cancelled.");
      reload();
    } catch (err) {
      toast(err.message, "error");
    }
  };

  const bookings = useMemo(
    () => (data.bookings || []).filter((booking) => matchesQuery(booking, query, ["serviceName", "status", "vehicleNumber"])),
    [data.bookings, query],
  );

  return (
    <>
      <Card>
        <Section title="Choose your service slot" text="Price updates from the live service catalogue." />
        {!data.vehicles.length && <Empty title="Add a vehicle first">Open My Vehicles to register a car before booking.</Empty>}
        <form className="form-grid" onSubmit={submit}>
          <label>Vehicle
            <select value={form.vehicleId} onChange={(e) => setForm({ ...form, vehicleId: e.target.value })} required>
              <option value="">Select vehicle</option>
              {data.vehicles.map((vehicle) => <option key={vehicle.id} value={vehicle.id}>{vehicle.brand} {vehicle.model} · {vehicle.vehicleNumber}</option>)}
            </select>
          </label>
          <label>Service
            <select value={form.serviceId} onChange={(e) => setForm({ ...form, serviceId: e.target.value })} required>
              <option value="">Select service</option>
              {data.services.map((service) => <option key={service.id} value={service.id}>{service.serviceName} · {rupees(service.price)}</option>)}
            </select>
          </label>
          <label>Date<input type="date" value={form.bookingDate} min={today} onChange={(e) => setForm({ ...form, bookingDate: e.target.value })} required /></label>
          <label>Time<input type="time" value={form.bookingTime} onChange={(e) => setForm({ ...form, bookingTime: e.target.value })} required /></label>
          <label className="full">Service note<textarea value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} placeholder="Tell us about the issue (optional)" /></label>
          <div className="price-chip">{selected ? <>Payable now: <b>{rupees(selected.price)}</b></> : "Select a service to see the live price"}</div>
          <button className="primary" type="submit" disabled={!data.vehicles.length || processingPayment}>
            {processingPayment ? "Processing payment..." : "Confirm booking & pay"}
          </button>
        </form>
        {confirmed && (
          <div className="confirm-banner">
            <CheckCircle2 size={18} />
            {confirmed.paymentStatus === "PAID"
              ? `Booking #${confirmed.id} confirmed and payment successful.`
              : `Booking #${confirmed.id} created for ${confirmed.serviceName} on ${confirmed.bookingDate} at ${timeLabel(confirmed.bookingTime)}.`}
          </div>
        )}
      </Card>
      <Card>
        <Section title="My bookings" />
        <Toolbar query={query} setQuery={setQuery} placeholder="Search bookings" />
        {bookings.length ? bookings.map((booking) => (
          <BookingCard
            key={booking.id}
            booking={booking}
            action={["PENDING", "ASSIGNED"].includes(booking.status)
              ? <button className="ghost" onClick={() => cancel(booking.id)}>Cancel</button>
              : null}
          />
        )) : <Empty title="No bookings yet." />}
      </Card>
    </>
  );
}

function Reviews({ token, reviews, bookings, reload }) {
  const { toast } = useFeedback();
  const [form, setForm] = useState({ bookingId: "", rating: "5", comment: "" });
  const submit = async (e) => {
    e.preventDefault();
    try {
      await api("/api/customer/reviews", { method: "POST", token, body: { ...form, bookingId: Number(form.bookingId), rating: Number(form.rating) } });
      setForm({ bookingId: "", rating: "5", comment: "" });
      toast("Review submitted.");
      reload();
    } catch (err) {
      toast(err.message, "error");
    }
  };
  const completed = bookings.filter((booking) => booking.status === "COMPLETED");
  return (
    <>
      <Card>
        <Section title="Share your feedback" />
        <form className="form-grid" onSubmit={submit}>
          <label>Completed booking
            <select value={form.bookingId} onChange={(e) => setForm({ ...form, bookingId: e.target.value })} required>
              <option value="">Select booking</option>
              {completed.map((booking) => <option key={booking.id} value={booking.id}>#{booking.id} · {booking.serviceName}</option>)}
            </select>
          </label>
          <label>Rating
            <select value={form.rating} onChange={(e) => setForm({ ...form, rating: e.target.value })}>
              {[5, 4, 3, 2, 1].map((rating) => <option key={rating}>{rating}</option>)}
            </select>
          </label>
          <label className="full">Comment<textarea value={form.comment} onChange={(e) => setForm({ ...form, comment: e.target.value })} required /></label>
          <button className="primary" type="submit"><Star size={16} /> Submit review</button>
        </form>
      </Card>
      <List title="My reviews" items={reviews} render={(review) => (
        <><b>{review.serviceName} · {review.rating}/5</b><span>{review.comment}</span></>
      )} />
    </>
  );
}
