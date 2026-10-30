export const IMAGES = {
  hero: "https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?auto=format&fit=crop&w=2200&q=80",
  service: "https://images.unsplash.com/photo-1486006920555-c77dcf18193c?auto=format&fit=crop&w=1400&q=80",
  wash: "https://images.unsplash.com/photo-1520340358512-cc0c78e1eb31?auto=format&fit=crop&w=1400&q=80",
  engine: "https://images.unsplash.com/photo-1487754180451-c456f719a1fc?auto=format&fit=crop&w=1400&q=80",
  brake: "https://images.unsplash.com/photo-1486262715619-67b85e0b08d3?auto=format&fit=crop&w=1400&q=80",
  mechanic: "https://images.unsplash.com/photo-1619642751034-765dfdf7c58e?auto=format&fit=crop&w=1400&q=80",
  car: "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=1400&q=80",
};

export const SHOWCASE = [
  { title: "Car Servicing", text: "Scheduled inspections, fluids and genuine care for every kilometre.", image: IMAGES.service },
  { title: "Premium Wash", text: "Showroom-level wash, vacuum and finish that keeps your car camera-ready.", image: IMAGES.wash },
  { title: "Engine Repair", text: "Diagnostics, tuning and repairs by specialists who know the machine.", image: IMAGES.engine },
  { title: "Brake Inspection", text: "Pads, fluid and stopping power checked before the next long drive.", image: IMAGES.brake },
  { title: "Expert Mechanics", text: "Assigned technicians, live job updates and workshop-grade workmanship.", image: IMAGES.mechanic },
];

export const STEPS = ["PENDING", "ASSIGNED", "IN_PROGRESS", "COMPLETED"];

export const rupees = (value) => value == null
  ? "—"
  : new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 0 }).format(value);

export const statusClass = (value) => String(value || "PENDING").toLowerCase().replaceAll("_", "-");

export const statusLabel = (value) => String(value || "PENDING").replaceAll("_", " ");

export const timeLabel = (value) => String(value || "").slice(0, 5) || "—";

export const matchesQuery = (item, query, fields) => {
  if (!query.trim()) return true;
  const hay = fields.map((field) => String(item[field] ?? "")).join(" ").toLowerCase();
  return hay.includes(query.trim().toLowerCase());
};

export function mediaForService(name = "") {
  const n = name.toLowerCase();
  if (n.includes("wash") || n.includes("clean")) return IMAGES.wash;
  if (n.includes("engine")) return IMAGES.engine;
  if (n.includes("brake")) return IMAGES.brake;
  if (n.includes("oil")) return IMAGES.engine;
  if (n.includes("ac") || n.includes("a/c")) return IMAGES.service;
  if (n.includes("mechanic")) return IMAGES.mechanic;
  return IMAGES.service;
}

export function stepIndex(status) {
  if (status === "CANCELLED") return -1;
  const index = STEPS.indexOf(status);
  return index < 0 ? 0 : index;
}
