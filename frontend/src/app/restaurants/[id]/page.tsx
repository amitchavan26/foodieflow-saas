"use client";

import { useState, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import { useCart } from "@/context/CartContext";
import { createOrder, fetchMenuItems } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { AlertCircle, ShoppingCart, CheckCircle, Loader2 } from "lucide-react";

type MenuItem = {
  id: string;
  name: string;
  description: string | null;
  priceInCents: number;
  isAvailable: boolean;
};

export default function RestaurantPage() {
  const params = useParams();
  const router = useRouter();
  const restaurantId = params.id as string;
  
  const { items, addItem, removeItem, totalInCents, clearCart } = useCart();

  const [menuItems, setMenuItems] = useState<MenuItem[]>([]);
  const [isLoadingMenu, setIsLoadingMenu] = useState(true);
  const [menuError, setMenuError] = useState<string | null>(null);

  const [deliveryAddress, setDeliveryAddress] = useState("");
  const [addressError, setAddressError] = useState("");
  
  const [isOrderLoading, setIsOrderLoading] = useState(false);
  const [orderError, setOrderError] = useState<string | null>(null);
  const [isSuccess, setIsSuccess] = useState(false);

  // Fetch real menu data on load
  useEffect(() => {
    const loadMenu = async () => {
      try {
        const data = await fetchMenuItems(restaurantId);
        setMenuItems(data);
      } catch (err) {
        setMenuError(err instanceof Error ? err.message : "Failed to load menu");
      } finally {
        setIsLoadingMenu(false);
      }
    };
    loadMenu();
  }, [restaurantId]);

  const handleAddToCart = (item: MenuItem) => {
    addItem({ id: item.id, name: item.name, priceInCents: item.priceInCents });
  };

  const handleSubmitOrder = async (e: React.FormEvent) => {
    e.preventDefault();
    setAddressError("");
    setOrderError(null);

    if (!deliveryAddress.trim()) {
      setAddressError("Delivery address is required");
      return;
    }
    if (items.length === 0) {
      setOrderError("Your cart is empty. Please add items before ordering.");
      return;
    }

    setIsOrderLoading(true);

    try {
      const payload = {
        deliveryAddress,
        items: items.map((item) => ({ menuItemId: item.id, quantity: item.quantity })),
      };

      await createOrder(restaurantId, payload);
      setIsSuccess(true);
      clearCart();
    } catch (err) {
      setOrderError(err instanceof Error ? err.message : "Failed to place order. Please try again.");
    } finally {
      setIsOrderLoading(false);
    }
  };

  // --- STATE 1: Success ---
  if (isSuccess) {
    return (
      <main className="flex min-h-screen flex-col items-center justify-center bg-gray-50 p-6 text-center">
        <CheckCircle className="mb-4 h-16 w-16 text-green-500" />
        <h2 className="mb-2 text-2xl font-bold text-gray-900">Order Placed Successfully!</h2>
        <p className="mb-6 text-gray-600">The restaurant has received your order and will start preparing it shortly.</p>
        <Button onClick={() => router.push("/")} variant="primary">Back to Home</Button>
      </main>
    );
  }

  // --- STATE 2: Loading Menu ---
  if (isLoadingMenu) {
    return (
      <main className="min-h-screen bg-gray-50 p-6 flex items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-orange-600" />
      </main>
    );
  }

  // --- STATE 3: Error Loading Menu ---
  if (menuError) {
    return (
      <main className="flex min-h-screen flex-col items-center justify-center bg-gray-50 p-6 text-center">
        <AlertCircle className="mb-4 h-12 w-12 text-red-500" />
        <h2 className="mb-2 text-xl font-semibold text-gray-900">Failed to load menu</h2>
        <p className="mb-6 text-gray-600">{menuError}</p>
        <Button onClick={() => router.push("/")} variant="outline">Back to Restaurants</Button>
      </main>
    );
  }

  // --- STATE 4: Success (Menu Loaded) ---
  return (
    <main className="min-h-screen bg-gray-50 p-6">
      <div className="mx-auto max-w-4xl">
        <header className="mb-8">
          <Button variant="ghost" onClick={() => router.back()} className="mb-4 pl-0">← Back</Button>
          <h1 className="text-3xl font-bold text-gray-900">Restaurant Menu</h1>
          <p className="mt-2 text-gray-600">Select your favorite items and checkout.</p>
        </header>

        <div className="grid gap-8 md:grid-cols-3">
          {/* Left Column: Menu */}
          <div className="md:col-span-2 space-y-4">
            {menuItems.length === 0 ? (
              <p className="text-center text-gray-500 py-8">No items available at the moment.</p>
            ) : (
              menuItems.map((item) => (
                <div key={item.id} className="flex items-center justify-between rounded-lg border bg-white p-4 shadow-sm">
                  <div>
                    <h3 className="font-semibold text-gray-900">{item.name}</h3>
                    {item.description && <p className="text-sm text-gray-600">{item.description}</p>}
                    <p className="mt-1 font-medium text-orange-600">₹{(item.priceInCents / 100).toFixed(2)}</p>
                  </div>
                  <Button variant="outline" onClick={() => handleAddToCart(item)}>Add</Button>
                </div>
              ))
            )}
          </div>

          {/* Right Column: Cart & Checkout Form */}
          <div className="md:col-span-1">
            <div className="sticky top-6 rounded-lg border bg-white p-4 shadow-sm">
              <h2 className="mb-4 flex items-center text-lg font-semibold text-gray-900">
                <ShoppingCart className="mr-2 h-5 w-5" /> Your Cart
              </h2>

              {items.length === 0 ? (
                <p className="py-4 text-center text-sm text-gray-500">Your cart is empty.</p>
              ) : (
                <div className="mb-4 space-y-2">
                  {items.map((item) => (
                    <div key={item.id} className="flex justify-between text-sm">
                      <span>{item.name} x {item.quantity}</span>
                      <div className="flex items-center gap-2">
                        <span>₹{((item.priceInCents * item.quantity) / 100).toFixed(2)}</span>
                        <button onClick={() => removeItem(item.id)} className="text-red-500 hover:text-red-700 font-bold">×</button>
                      </div>
                    </div>
                  ))}
                  <div className="mt-4 border-t pt-2 font-bold">
                    <div className="flex justify-between">
                      <span>Total</span>
                      <span>₹{(totalInCents / 100).toFixed(2)}</span>
                    </div>
                  </div>
                </div>
              )}

              {/* Checkout Form */}
              <form onSubmit={handleSubmitOrder} className="mt-4 space-y-3">
                <div>
                  <label className="mb-1 block text-sm font-medium text-gray-700">Delivery Address</label>
                  <textarea
                    value={deliveryAddress}
                    onChange={(e) => setDeliveryAddress(e.target.value)}
                    className={`w-full rounded-md border p-2 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500 ${addressError ? "border-red-500" : "border-gray-300"}`}
                    rows={3}
                    placeholder="Enter your full delivery address"
                    disabled={isOrderLoading}
                  />
                  {addressError && <p className="mt-1 text-xs text-red-500">{addressError}</p>}
                </div>

                {orderError && (
                  <div className="flex items-center rounded-md bg-red-50 p-2 text-sm text-red-700">
                    <AlertCircle className="mr-2 h-4 w-4 flex-shrink-0" />
                    {orderError}
                  </div>
                )}

                <Button type="submit" variant="primary" isLoading={isOrderLoading} className="w-full" disabled={items.length === 0 || isOrderLoading}>
                  {isOrderLoading ? "Placing Order..." : `Place Order (₹${(totalInCents / 100).toFixed(2)})`}
                </Button>
              </form>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}