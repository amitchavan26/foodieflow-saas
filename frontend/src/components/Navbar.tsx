"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { LogOut, ShoppingBag } from "lucide-react";
import { useCart } from "@/context/CartContext";

export default function Navbar() {
  const router = useRouter();
  const pathname = usePathname();
  const { items, clearCart, totalInCents } = useCart();

  const [isHydrated, setIsHydrated] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    const syncAuth = () => {
      setIsLoggedIn(Boolean(localStorage.getItem("token")));
    };

    syncAuth();
    setIsHydrated(true);

    window.addEventListener("storage", syncAuth);
    window.addEventListener("auth-change", syncAuth);

    return () => {
      window.removeEventListener("storage", syncAuth);
      window.removeEventListener("auth-change", syncAuth);
    };
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("foodieflow-cart");
    clearCart();
    window.dispatchEvent(new Event("auth-change"));
    router.push("/login");
  };

  // Keep the auth pages clean and focused
  if (pathname.startsWith("/login") || pathname.startsWith("/register")) {
    return null;
  }

  return (
    <header className="sticky top-0 z-40 border-b border-gray-200 bg-white/95 backdrop-blur">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
        <Link href="/" className="flex items-center gap-2">
          <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-orange-600 text-white">
            <ShoppingBag className="h-5 w-5" />
          </span>
          <span className="text-xl font-bold tracking-tight text-gray-900">
            Foodie<span className="text-orange-600">Flow</span>
          </span>
        </Link>

        <nav className="flex items-center gap-3 text-sm">
          {!isHydrated ? (
            <span className="h-8 w-24 animate-pulse rounded-md bg-gray-100" />
          ) : isLoggedIn ? (
            <>
              <span className="hidden rounded-full bg-orange-50 px-3 py-1 text-orange-700 sm:inline">
                {items.length > 0
                  ? `${items.length} in cart · ₹${(totalInCents / 100).toFixed(2)}`
                  : "Cart is empty"}
              </span>
              <button
                onClick={handleLogout}
                className="flex items-center gap-2 rounded-md border border-gray-300 px-3 py-2 font-medium text-gray-700 transition hover:bg-gray-50"
              >
                <LogOut className="h-4 w-4" />
                Logout
              </button>
            </>
          ) : (
            <>
              <Link
                href="/login"
                className="rounded-md px-3 py-2 font-medium text-gray-700 hover:bg-gray-50"
              >
                Login
              </Link>
              <Link
                href="/register"
                className="rounded-md bg-orange-600 px-4 py-2 font-semibold text-white transition hover:bg-orange-700"
              >
                Sign up
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}