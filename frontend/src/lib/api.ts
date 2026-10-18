import { getToken, setToken } from "@/lib/auth";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api/v1";

// Helper to get default headers with auth
function getHeaders() {
  const headers: HeadersInit = {
    "Content-Type": "application/json",
  };
  const token = getToken();
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }
  return headers;
}

export async function fetchRestaurants() {
  const response = await fetch(`${API_BASE_URL}/restaurants`, {
    headers: getHeaders(),
    cache: "no-store",
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || "Failed to fetch restaurants");
  }
  return response.json();
}

export async function createOrder(restaurantId: string, payload: {
  deliveryAddress: string;
  items: { menuItemId: string; quantity: number }[];
}) {
  const response = await fetch(`${API_BASE_URL}/restaurants/${restaurantId}/orders`, {
    method: "POST",
    headers: getHeaders(), // <-- JWT is now included here
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || "Failed to create order");
  }

  return response.json();
}

export async function login(email: string, password: string) {
  const response = await fetch(`${API_BASE_URL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || "Login failed");
  }

  const data = await response.json();
  setToken(data.token); // Save the JWT (Now works because we imported it!)
  return data;
}

export async function fetchMenuItems(restaurantId: string) {
  const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/restaurants/${restaurantId}/menu-items`, {
    headers: getHeaders(),
    cache: "no-store",
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || "Failed to fetch menu items");
  }

  return response.json();
}