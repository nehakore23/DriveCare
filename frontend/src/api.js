const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

export const getSession = () => {
  try { return JSON.parse(localStorage.getItem("drivecare_session")) || null; }
  catch { return null; }
};

export const saveSession = (data) => localStorage.setItem("drivecare_session", JSON.stringify(data));
export const clearSession = () => localStorage.removeItem("drivecare_session");

export async function api(path, { method = "GET", body, token } = {}) {
  const response = await fetch(`${BASE_URL}${path}`, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    ...(body ? { body: JSON.stringify(body) } : {}),
  });
  const text = await response.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch { data = { message: text }; }
  if (!response.ok) throw new Error(data?.message || "Something went wrong. Please try again.");
  return data;
}
