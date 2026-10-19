// Prevent blocking-prerender dynamic route failure during build
export const instant = false;

import RestaurantClient from "./RestaurantClient";

export default async function Page({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  
  return <RestaurantClient restaurantId={id} />;
}