"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { CheckCircle, Clock, Bike, ChefHat, Loader2, AlertCircle } from "lucide-react";

const STATUS_STEPS = [
  { status: "PENDING_PAYMENT", label: "Order Placed", icon: Clock },
  { status: "PAID", label: "Payment Confirmed", icon: CheckCircle },
  { status: "PREPARING", label: "Preparing Your Food", icon: ChefHat },
  { status: "OUT_FOR_DELIVERY", label: "Out for Delivery", icon: Bike },
  { status: "DELIVERED", label: "Delivered", icon: CheckCircle },
];

export default function TrackOrderPage() {
  const params = useParams();
  const router = useRouter();
  const orderId = params.id as string;

  const [currentStatus, setCurrentStatus] = useState("PENDING_PAYMENT");
  const [statusMessage, setStatusMessage] = useState("Connecting to restaurant...");
  const [error, setError] = useState("");

  useEffect(() => {
    console.log("🚀 Tracking page loaded for order:", orderId);
    
    const apiUrl = process.env.NEXT_PUBLIC_API_URL;
    if (!apiUrl) {
      console.error("❌ NEXT_PUBLIC_API_URL is not defined!");
      setError("API URL not configured.");
      return;
    }

    const token = localStorage.getItem("token");
    if (!token) {
      console.error("❌ No authentication token found!");
      setError("Not authenticated. Please log in again.");
      return;
    }

    const trackUrl = `${apiUrl}/orders/${orderId}/track?token=${token}`;
    console.log("📡 Connecting to SSE endpoint:", trackUrl);

    const eventSource = new EventSource(trackUrl);

    eventSource.onopen = () => {
      console.log("✅ SSE connection established!");
      setStatusMessage("Connected to restaurant. Waiting for updates...");
    };

    eventSource.addEventListener("status-update", (event: any) => {
      console.log("📨 Received status update:", event.data);
      const data = JSON.parse(event.data);
      setCurrentStatus(data.status);
      setStatusMessage(data.message);
    });

    eventSource.onerror = (err) => {
      console.error("❌ SSE connection error:", err);
      if (eventSource.readyState === 2) {
        setError("Connection closed. Please refresh the page.");
      }
    };

    return () => {
      console.log("🔌 Closing SSE connection");
      eventSource.close();
    };
  }, [orderId]);

  const getStatusIndex = (status: string) => STATUS_STEPS.findIndex(s => s.status === status);
  const currentIndex = getStatusIndex(currentStatus);

  return (
    <main className="min-h-screen bg-gray-50 p-6">
      <div className="mx-auto max-w-2xl">
        <button onClick={() => router.push("/")} className="mb-6 text-sm text-gray-600 hover:text-gray-900">
          ← Back to Home
        </button>

        <div className="rounded-xl bg-white p-8 shadow-sm">
          <h1 className="mb-2 text-2xl font-bold text-gray-900">Order Tracking</h1>
          <p className="mb-8 text-sm text-gray-500">Order ID: {orderId}</p>

          {error ? (
            <div className="flex items-center rounded-md bg-red-50 p-4 text-red-700">
              <AlertCircle className="mr-2 h-5 w-5" />
              {error}
            </div>
          ) : (
            <div className="space-y-8">
              {/* Live Status Message */}
              <div className="flex items-center gap-3 rounded-lg bg-orange-50 p-4 text-orange-800">
                <Loader2 className={`h-5 w-5 ${currentIndex >= STATUS_STEPS.length - 1 ? "hidden" : "animate-spin"}`} />
                <span className="font-medium">{statusMessage}</span>
              </div>

              {/* Progress Steps */}
              <div className="relative">
                {STATUS_STEPS.map((step, index) => {
                  const Icon = step.icon;
                  const isActive = index <= currentIndex;
                  const isCurrent = index === currentIndex;

                  return (
                    <div key={step.status} className="relative flex items-start pb-8 last:pb-0">
                      {/* Vertical Line */}
                      {index < STATUS_STEPS.length - 1 && (
                        <div className={`absolute left-[15px] top-8 h-full w-0.5 ${isActive ? "bg-orange-500" : "bg-gray-200"}`} />
                      )}
                      
                      {/* Icon Circle */}
                      <div className={`relative z-10 flex h-8 w-8 items-center justify-center rounded-full border-2 ${
                        isActive ? "border-orange-500 bg-orange-500 text-white" : "border-gray-300 bg-white text-gray-400"
                      }`}>
                        <Icon className="h-4 w-4" />
                      </div>

                      {/* Text */}
                      <div className="ml-4 flex-1 pt-1">
                        <p className={`font-semibold ${isActive ? "text-gray-900" : "text-gray-400"}`}>
                          {step.label}
                        </p>
                        {isCurrent && (
                          <p className="mt-1 text-sm text-orange-600 animate-pulse">
                            {statusMessage}
                          </p>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </div>
      </div>
    </main>
  );
}