"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { fetchRestaurants } from "@/lib/api";
import { MapPin, Star, AlertCircle, RefreshCw, Loader2 } from "lucide-react";

type Restaurant = {
  id: string;
  name: string;
  addressLine: string;
};

export default function Home() {
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [userLocation, setUserLocation] = useState<{ lat: number; lng: number } | null>(null);

  const loadRestaurants = async (lat?: number, lng?: number) => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await fetchRestaurants(lat, lng, 10000); // 10km radius
      setRestaurants(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "An unexpected error occurred");
    } finally {
      setIsLoading(false);
    }
  };

   // Request location on initial load
   useEffect(() => {
    // TEMPORARY: Force New York coordinates to match our seeded test data!
    // This proves the geospatial query works. In a real app, we'd use real GPS.
    const testLat = 40.7128;
    const testLng = -74.0060;
    
    setUserLocation({ lat: testLat, lng: testLng });
    loadRestaurants(testLat, testLng);

    /* 
    // REAL GPS CODE (Commented out for now):
    if ("geolocation" in navigator) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const { latitude, longitude } = position.coords;
          setUserLocation({ lat: latitude, lng: longitude });
          loadRestaurants(latitude, longitude);
        },
        (error) => {
          console.warn("Geolocation denied or failed, loading all restaurants:", error);
          loadRestaurants(); // Fallback to all restaurants
        }
      );
    } else {
      loadRestaurants(); // Fallback if geolocation isn't supported
    }
    */
  }, []);

  // 1. Loading State
  if (isLoading) {
    return (
      <main className="min-h-screen bg-gray-50 p-6 flex flex-col items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-orange-600 mb-4" />
        <p className="text-gray-600">Finding restaurants near you...</p>
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
        <Button onClick={() => loadRestaurants(userLocation?.lat, userLocation?.lng)} variant="primary">
          <RefreshCw className="mr-2 h-4 w-4" />
          Try Again
        </Button>
      </main>
    );
  }

  // 3. Empty State
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
          <p className="mt-2 text-gray-600 flex items-center">
            {userLocation ? (
              <>
                <MapPin className="mr-1 h-4 w-4 text-green-600" />
                Showing restaurants near your location
              </>
            ) : (
              "Discover the best food delivery in your area."
            )}
          </p>
        </header>

        <div className="grid gap-4 md:grid-cols-2">
          {restaurants.map((restaurant) => (
            <article 
              key={restaurant.id} 
              className="flex flex-col items-start space-y-4 rounded-lg border bg-white p-4 shadow-sm transition-shadow hover:shadow-md sm:flex-row sm:items-center sm:space-x-4 sm:space-y-0"
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
              
              <Link href={`/restaurants/${restaurant.id}`} className="mt-2 w-full sm:mt-0 sm:w-auto">
                <Button variant="outline" className="w-full">View Menu</Button>
              </Link>
            </article>
          ))}
        </div>
      </div>
    </main>
  );
}