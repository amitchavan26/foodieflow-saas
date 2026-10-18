"use client";

import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { fetchRestaurants } from "@/lib/api";
import Link from "next/link";
import { MapPin, Star, AlertCircle, RefreshCw } from "lucide-react";

// Mock type for now, we will align this with the backend DTO later
type Restaurant = {
  id: string;
  name: string;
  addressLine: string;
};

export default function Home() {
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadRestaurants = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await fetchRestaurants();
      setRestaurants(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "An unexpected error occurred");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadRestaurants();
  }, []);

  // 1. Loading State (Skeleton)
  if (isLoading) {
    return (
      <main className="min-h-screen bg-gray-50 p-6">
        <div className="mx-auto max-w-4xl space-y-4">
          <div className="h-8 w-48 animate-pulse rounded bg-gray-200" />
          {[1, 2, 3].map((i) => (
            <div key={i} className="flex animate-pulse items-center space-x-4 rounded-lg border bg-white p-4 shadow-sm">
              <div className="h-16 w-16 rounded-full bg-gray-200" />
              <div className="flex-1 space-y-2">
                <div className="h-4 w-3/4 rounded bg-gray-200" />
                <div className="h-3 w-1/2 rounded bg-gray-200" />
              </div>
            </div>
          ))}
        </div>
      </main>
    );
  }

  // 2. Error State
  if (error) {
    return (
      <main className="flex min-h-screen flex-col items-center justify-center bg-gray-50 p-6 text-center">
        <AlertCircle className="mb-4 h-12 w-12 text-red-500" />
        <h2 className="mb-2 text-xl font-semibold text-gray-900">Failed to load restaurants</h2>
        <p className="mb-6 max-w-md text-gray-600">{error}</p>
        <Button onClick={loadRestaurants} variant="primary">
          <RefreshCw className="mr-2 h-4 w-4" />
          Try Again
        </Button>
      </main>
    );
  }

  // 3. Empty State (FIXED THE TYPO HERE)
  if (restaurants.length === 0) {
    return (
      <main className="flex min-h-screen flex-col items-center justify-center bg-gray-50 p-6 text-center">
        <MapPin className="mb-4 h-12 w-12 text-gray-400" />
        <h2 className="mb-2 text-xl font-semibold text-gray-900">No restaurants found nearby</h2>
        <p className="text-gray-600">Try expanding your search area or check back later.</p>
      </main>
    );
  }

  // 4. Success State
  return (
    <main className="min-h-screen bg-gray-50 p-6">
      <div className="mx-auto max-w-4xl">
        <header className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">FoodieFlow</h1>
          <p className="mt-2 text-gray-600">Discover the best food delivery in your area.</p>
        </header>

        <div className="grid gap-4 md:grid-cols-2">
          {restaurants.map((restaurant) => (
            <article 
              key={restaurant.id} 
              className="flex items-start space-x-4 rounded-lg border bg-white p-4 shadow-sm transition-shadow hover:shadow-md"
            >
              <div className="flex h-16 w-16 flex-shrink-0 items-center justify-center rounded-full bg-orange-100 text-orange-600">
                <MapPin className="h-8 w-8" />
              </div>
              <div className="flex-1">
                <h3 className="text-lg font-semibold text-gray-900">{restaurant.name}</h3>
                <div className="mt-1 flex items-center text-sm text-gray-600">
                  <MapPin className="mr-1 h-4 w-4" />
                  {restaurant.addressLine}
                </div>
                <div className="mt-2 flex items-center">
                  <Star className="mr-1 h-4 w-4 fill-yellow-400 text-yellow-400" />
                  <span className="text-sm font-medium text-gray-900">4.5</span>
                  <span className="mx-1 text-gray-400">•</span>
                  <span className="text-sm text-gray-600">20-30 min</span>
                </div>
              </div>
               <Link href={`/restaurants/${restaurant.id}`} className="mt-2 block">
               <Button variant="outline" className="w-full">View Menu</Button>
               </Link>
            </article>
          ))}
        </div>
      </div>
    </main>
  );
}