import { Toaster } from "@/components/ui/toaster"
import { Toaster as SonnerToaster } from "@/components/ui/sonner"
import { QueryClientProvider } from '@tanstack/react-query'
import { queryClientInstance } from '@/lib/query-client'
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import PageNotFound from './lib/PageNotFound';
import { AuthProvider, useAuth } from '@/lib/AuthContext';

// Layout
import AppLayout from '@/components/layout/AppLayout';

// Auth pages
import Login from '@/pages/auth/Login';
import Register from '@/pages/auth/Register';

// Main pages
import Catalog from '@/pages/Catalog';
import ProductDetail from '@/pages/ProductDetail';
import Dashboard from '@/pages/Dashboard';
import Orders from '@/pages/Orders';
import OrderDetail from '@/pages/OrderDetail';
import Wallet from '@/pages/Wallet';
import Profile from '@/pages/Profile';
import SellerApplication from '@/pages/SellerApplication';
import UserPublicProfile from '@/pages/UserPublicProfile';

// Seller pages
import SellerProducts from '@/pages/seller/SellerProducts';
import ProductForm from '@/pages/seller/ProductForm';
import StockManagement from '@/pages/seller/StockManagement';
import SellerSales from '@/pages/seller/SellerSales';

// Admin pages
import AdminApplications from '@/pages/admin/AdminApplications';
import AdminDisputes from '@/pages/admin/AdminDisputes';

const AuthenticatedApp = () => {
  const { isLoadingAuth } = useAuth();

  if (isLoadingAuth) {
    return (
      <div className="fixed inset-0 flex items-center justify-center bg-background">
        <div className="w-8 h-8 border-4 border-primary/20 border-t-primary rounded-full animate-spin"></div>
      </div>
    );
  }

  return (
    <>
      <Routes>
        {/* Auth (no layout) */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Main app with layout */}
        <Route element={<AppLayout />}>
          <Route path="/" element={<Catalog />} />
          <Route path="/products/:id" element={<ProductDetail />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/orders" element={<Orders />} />
          <Route path="/orders/:id" element={<OrderDetail />} />
          <Route path="/wallet" element={<Wallet />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/seller-application" element={<SellerApplication />} />
          <Route path="/users/:id" element={<UserPublicProfile />} />

          {/* Seller */}
          <Route path="/seller/products" element={<SellerProducts />} />
          <Route path="/seller/products/new" element={<ProductForm />} />
          <Route path="/seller/products/:id/edit" element={<ProductForm />} />
          <Route path="/seller/products/:id/stock" element={<StockManagement />} />
          <Route path="/seller/sales" element={<SellerSales />} />

          {/* Admin */}
          <Route path="/admin/applications" element={<AdminApplications />} />
          <Route path="/admin/disputes" element={<AdminDisputes />} />
        </Route>

        <Route path="*" element={<PageNotFound />} />
      </Routes>
    </>
  );
};

function App() {
  return (
    <AuthProvider>
      <QueryClientProvider client={queryClientInstance}>
        <Router>
          <AuthenticatedApp />
        </Router>
        <Toaster />
        <SonnerToaster />
      </QueryClientProvider>
    </AuthProvider>
  )
}

export default App