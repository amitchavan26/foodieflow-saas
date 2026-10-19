import RestaurantClient from "./RestaurantClient";

export default async function Page({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  
  // RestaurantClient is a "use client" component, which naturally 
  // handles the dynamic rendering and state management safely.
  return <RestaurantClient restaurantId={id} />;
}