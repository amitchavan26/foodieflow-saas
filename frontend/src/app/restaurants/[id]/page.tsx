// Force Next.js to render this route dynamically at request time
export const dynamic = 'force-dynamic';

import RestaurantClient from "./RestaurantClient";

export default async function Page({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  
  return <RestaurantClient restaurantId={id} />;
}