import RestaurantClient from "./RestaurantClient";

export const instant = false;

// This is a Server Component. It safely reads the URL params 
// and passes them down to the Client Component.
export default async function Page({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  
  return <RestaurantClient restaurantId={id} />;
}