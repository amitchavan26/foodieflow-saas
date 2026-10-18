import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { CartProvider } from "@/context/CartContext";

// This defines the "inter" variable that was causing the error
const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "FoodieFlow",
  description: "Food Delivery SaaS Platform",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className={inter.className}>
        <CartProvider>
          {children}
        </CartProvider>
      </body>
    </html>
  );
}