"use client";

import { PaymentElement, useStripe, useElements } from "@stripe/react-stripe-js";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import { AlertCircle } from "lucide-react";

export default function CheckoutForm({ onSuccess }: { onSuccess: () => void }) {
  const stripe = useStripe();
  const elements = useElements();
  const [isProcessing, setIsProcessing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!stripe || !elements) {
      return; // Stripe.js has not yet loaded
    }

    setIsProcessing(true);
    setError(null);

    // Trigger the payment
    const { error: stripeError, paymentIntent } = await stripe.confirmPayment({
      elements,
      confirmParams: {
        // We don't want to redirect, we want to handle it right here
        return_url: `${window.location.origin}/order-success`, 
      },
      redirect: "if_required", // Only redirect if 3D Secure is needed
    });

    if (stripeError) {
      setError(stripeError.message || "An unexpected error occurred.");
      setIsProcessing(false);
    } else if (paymentIntent && paymentIntent.status === "succeeded") {
      onSuccess();
    } else {
      setError("Payment is still processing. Please wait.");
      setIsProcessing(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {/* This renders the secure credit card form */}
      <PaymentElement />
      
      {error && (
        <div className="flex items-center rounded-md bg-red-50 p-2 text-sm text-red-700">
          <AlertCircle className="mr-2 h-4 w-4 flex-shrink-0" />
          {error}
        </div>
      )}

      <Button 
        type="submit" 
        variant="primary" 
        isLoading={isProcessing} 
        className="w-full" 
        disabled={!stripe || isProcessing}
      >
        {isProcessing ? "Processing Payment..." : "Pay Securely"}
      </Button>
      
      <p className="text-center text-xs text-gray-500">
        Test Card: 4242 4242 4242 4242 | Any future date | Any 3 digits
      </p>
    </form>
  );
}