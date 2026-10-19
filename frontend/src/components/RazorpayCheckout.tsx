"use client";

import { Button } from "@/components/ui/button";
import { AlertCircle, Loader2 } from "lucide-react";
import { useState, useEffect } from "react";

type PaymentDetails = {
  razorpayOrderId: string;
  amount: number;
  currency: string;
  keyId: string;
  name: string;
  description: string;
};

export default function RazorpayCheckout({ 
  paymentDetails, 
  onSuccess 
}: { 
  paymentDetails: PaymentDetails; 
  onSuccess: () => void;
}) {
  const [isProcessing, setIsProcessing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isScriptLoaded, setIsScriptLoaded] = useState(false);

  // Dynamically load the official Razorpay script
  useEffect(() => {
    if (!isScriptLoaded) {
      const script = document.createElement("script");
      script.src = "https://checkout.razorpay.com/v1/checkout.js";
      script.async = true;
      script.onload = () => setIsScriptLoaded(true);
      script.onerror = () => setError("Failed to load Razorpay SDK.");
      document.body.appendChild(script);
    }
  }, [isScriptLoaded]);

  const handlePayment = () => {
    if (!isScriptLoaded) return;

    setIsProcessing(true);
    setError(null);

    const options = {
      key: paymentDetails.keyId,
      amount: paymentDetails.amount,
      currency: paymentDetails.currency,
      name: paymentDetails.name,
      description: paymentDetails.description,
      order_id: paymentDetails.razorpayOrderId,
      handler: function (response: any) {
        // Payment successful!
        console.log("Payment successful:", response);
        setIsProcessing(false);
        onSuccess();
      },
      prefill: {
        name: "Test Customer",
        email: "customer@test.com",
      },
      theme: {
        color: "#ea580c", // Orange-600
      },
      modal: {
        ondismiss: function () {
          setIsProcessing(false);
          setError("Payment was cancelled.");
        },
      },
    };

    // Initialize Razorpay using the window object
    const razorpayInstance = new (window as any).Razorpay(options);
    razorpayInstance.open();
  };

  return (
    <div className="space-y-4">
      <div className="rounded-lg border bg-orange-50 p-4 text-sm text-orange-800">
        <p className="font-semibold">Test Card Details:</p>
        <p>Card: 4111 1111 1111 1111</p>
        <p>Expiry: Any future date (e.g. 12/25)</p>
        <p>CVV: Any 3 digits (e.g. 123)</p>
        <p>OTP: 1234</p>
      </div>

      {error && (
        <div className="flex items-center rounded-md bg-red-50 p-2 text-sm text-red-700">
          <AlertCircle className="mr-2 h-4 w-4 flex-shrink-0" />
          {error}
        </div>
      )}

      <Button 
        onClick={handlePayment} 
        variant="primary" 
        isLoading={isProcessing || !isScriptLoaded} 
        className="w-full"
        disabled={!isScriptLoaded}
      >
        {isProcessing ? (
          <><Loader2 className="mr-2 h-4 w-4 animate-spin" /> Opening Payment...</>
        ) : !isScriptLoaded ? (
          "Loading Payment Gateway..."
        ) : (
          `Pay ₹${(paymentDetails.amount / 100).toFixed(2)} with Razorpay`
        )}
      </Button>
    </div>
  );
}