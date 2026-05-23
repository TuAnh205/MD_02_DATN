import React from "react";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";
import Header from "./components/Header";
import Footer from "./components/Footer";
import ErrorBoundary from "./components/ErrorBoundary";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Home from "./pages/Home";
import Products from "./pages/Products";
import ProductDetail from "./pages/ProductDetail";
import Cart from "./pages/Cart";
import Checkout from "./pages/Checkout";
import OrderSuccess from "./pages/OrderSuccess";
import Orders from "./pages/Orders";
import Profile from "./pages/Profile";
import AdminDashboard from "./pages/AdminDashboard";
import AdminHome from "./pages/AdminHome";
import AdminRevenue from "./pages/AdminRevenue";
import AdminUsers from "./pages/AdminUsers";
import AdminProducts from "./pages/AdminProducts";
import AdminOrders from "./pages/AdminOrders";
import AdminReviews from "./pages/AdminReviews";
import AdminFeedbacks from "./pages/AdminFeedbacks";
import AdminPosts from "./pages/AdminPosts";
import AdminVouchers from "./pages/AdminVouchers";
import ForgotPassword from "./pages/ForgotPassword";
import ResetPassword from "./pages/ResetPassword";
import ShopDashboard from "./pages/ShopDashboard";
import Favorites from "./pages/Favorites";
import ShopHome from "./pages/ShopHome";
import ShopProducts from "./pages/ShopProducts";
import ShopRevenue from "./pages/ShopRevenue";
import ShopOrders from "./pages/ShopOrders";
import ShopReviews from "./pages/ShopReviews";

function ProtectedRoute({ children }) {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="flex justify-center items-center h-screen">
        Loading...
      </div>
    );
  }

  return user ? children : <Navigate to="/login" replace />;
}

function AdminProtectedRoute({ children }) {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="flex justify-center items-center h-screen">
        Loading...
      </div>
    );
  }

  return user && user.role === "admin" ? (
    children
  ) : (
    <Navigate to="/login" replace />
  );
}

function ShopProtectedRoute({ children }) {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="flex justify-center items-center h-screen">
        Loading...
      </div>
    );
  }

  return user && user.role === "shop" ? (
    children
  ) : (
    <Navigate to="/login" replace />
  );
}

function AppContent() {
  return (
    <Router>
      <Header />
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/register"
          element={<Navigate to="/register/user" replace />}
        />
        <Route
          path="/register/user"
          element={<Register accountType="user" />}
        />
        <Route
          path="/register/shop"
          element={<Register accountType="shop" />}
        />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="/" element={<Home />} />
        <Route path="/products" element={<Products />} />
        <Route path="/products/:id" element={<ProductDetail />} />
        <Route
          path="/cart"
          element={
            <ProtectedRoute>
              <Cart />
            </ProtectedRoute>
          }
        />
        <Route
          path="/checkout"
          element={
            <ProtectedRoute>
              <Checkout />
            </ProtectedRoute>
          }
        />
        <Route
          path="/order-success/:orderId"
          element={
            <ProtectedRoute>
              <OrderSuccess />
            </ProtectedRoute>
          }
        />
        <Route
          path="/orders"
          element={
            <ProtectedRoute>
              <Orders />
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile"
          element={
            <ProtectedRoute>
              <Profile />
            </ProtectedRoute>
          }
        />
        <Route
          path="/favorites"
          element={
            <ProtectedRoute>
              <Favorites />
            </ProtectedRoute>
          }
        />
        <Route
          path="/shop"
          element={
            <ShopProtectedRoute>
              <ShopDashboard />
            </ShopProtectedRoute>
          }
        >
          <Route index element={<ShopHome />} />
          <Route path="home" element={<ShopHome />} />
          <Route path="products" element={<ShopProducts />} />
          <Route path="reviews" element={<ShopReviews />} />
          <Route path="revenue" element={<ShopRevenue />} />
          <Route path="orders" element={<ShopOrders />} />
        </Route>
        <Route
          path="/admin"
          element={
            <AdminProtectedRoute>
              <AdminDashboard />
            </AdminProtectedRoute>
          }
        >
          <Route index element={<AdminHome />} />
          <Route path="revenue" element={<AdminRevenue />} />
          <Route path="users" element={<AdminUsers />} />
          <Route path="products" element={<AdminProducts />} />
          <Route path="orders" element={<AdminOrders />} />
          <Route path="reviews" element={<AdminReviews />} />
          <Route path="feedbacks" element={<AdminFeedbacks />} />
          <Route path="posts" element={<AdminPosts />} />
          <Route path="vouchers" element={<AdminVouchers />} />
        </Route>
      </Routes>
      <Footer />
    </Router>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <ErrorBoundary>
        <AppContent />
      </ErrorBoundary>
    </AuthProvider>
  );
}
