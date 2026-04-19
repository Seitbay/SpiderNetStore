import React, {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
  useMemo,
} from 'react';
import * as api from './api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const refreshUser = useCallback(async () => {
    try {
      const u = await api.user.me();
      setUser(u);
    } catch {
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refreshUser();
  }, [refreshUser]);

  const login = useCallback(
    async (credentials) => {
      await api.auth.login(credentials);
      await refreshUser();
    },
    [refreshUser]
  );

  const register = useCallback(
    async (data) => {
      await api.auth.register(data);
      // Spring не выставляет jwt-cookie при register — только при login
      await api.auth.login({
        email: data.email,
        password: data.password,
      });
      await refreshUser();
    },
    [refreshUser]
  );

  const logout = useCallback(async () => {
    try {
      await api.auth.logout();
    } finally {
      setUser(null);
    }
  }, []);

  const isRole = useCallback(
    (role) => user?.role === role,
    [user]
  );

  const value = useMemo(
    () => ({
      user,
      currentUser: user,
      loading,
      isLoadingAuth: loading,
      refreshUser,
      login,
      register,
      logout,
      isAuthenticated: !!user,
      isAdmin: isRole('ADMIN') || isRole('MODERATOR'),
      isSeller: isRole('SELLER') || isRole('ADMIN'),
      isBuyer: isRole('BUYER'),
    }),
    [user, loading, refreshUser, login, register, logout, isRole]
  );

  return (
    <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth должен использоваться внутри AuthProvider');
  }
  return ctx;
}

/** Совместимость со старым именем из сайдбара */
export function useMarketAuth() {
  return useAuth();
}
