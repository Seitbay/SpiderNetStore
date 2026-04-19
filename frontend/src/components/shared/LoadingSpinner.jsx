import React from 'react';
import { Loader2 } from 'lucide-react';

export default function LoadingSpinner({ className = '' }) {
  return (
    <div className={`flex items-center justify-center py-16 ${className}`}>
      <Loader2 className="w-8 h-8 animate-spin text-primary" />
    </div>
  );
}