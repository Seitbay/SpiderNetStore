import React, { useState, useEffect } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { useMarketAuth } from '@/lib/AuthContext';
import { payments, orders as ordersApi, sellerApp } from '@/lib/api';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Wallet, ShoppingCart, Package, ArrowRight, Store } from 'lucide-react';
import PageHeader from '@/components/shared/PageHeader';
import LoadingSpinner from '@/components/shared/LoadingSpinner';

export default function Dashboard() {
  const { currentUser, isSeller, isAuthenticated } = useMarketAuth();
  const [balance, setBalance] = useState(null);
  const [myOrders, setMyOrders] = useState([]);
  const [appStatus, setAppStatus] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isAuthenticated) {
      setLoading(false);
      return;
    }
    const load = async () => {
      try {
        const [b, o] = await Promise.all([
          payments.balance().catch(() => ({ balance: 0 })),
          ordersApi.myPurchases().catch(() => ({ items: [] })),
        ]);
        const bal = b?.balance ?? b;
        setBalance(bal);
        const raw = o?.items ?? o?.content ?? (Array.isArray(o) ? o : []);
        setMyOrders(Array.isArray(raw) ? raw : []);
        if (!isSeller) {
          const s = await sellerApp.myStatus().catch(() => null);
          setAppStatus(s);
        }
      } catch {
        setBalance(0);
        setMyOrders([]);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [isAuthenticated, isSeller]);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (loading) return <LoadingSpinner />;

  const pendingOrders = myOrders.filter((o) => o.status === 'PENDING').length;
  const balanceNum =
    typeof balance === 'number' ? balance : balance != null ? Number(balance) : 0;

  return (
    <div>
      <PageHeader
        title={`Привет, ${currentUser?.username || 'пользователь'}`}
        description="Обзор вашего аккаунта"
      />

      <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
        <Card className="bg-card border-border">
          <CardContent className="p-6">
            <div className="flex items-center justify-between mb-3">
              <div className="w-10 h-10 rounded-xl bg-primary/15 flex items-center justify-center">
                <Wallet className="w-5 h-5 text-primary" />
              </div>
              <Link to="/wallet">
                <ArrowRight className="w-4 h-4 text-muted-foreground" />
              </Link>
            </div>
            <p className="text-2xl font-bold">{Number.isFinite(balanceNum) ? `${balanceNum.toFixed(2)} ₽` : '—'}</p>
            <p className="text-sm text-muted-foreground mt-1">Баланс</p>
          </CardContent>
        </Card>

        <Card className="bg-card border-border">
          <CardContent className="p-6">
            <div className="flex items-center justify-between mb-3">
              <div className="w-10 h-10 rounded-xl bg-warning/15 flex items-center justify-center">
                <ShoppingCart className="w-5 h-5 text-warning" />
              </div>
              <Link to="/orders">
                <ArrowRight className="w-4 h-4 text-muted-foreground" />
              </Link>
            </div>
            <p className="text-2xl font-bold">{pendingOrders}</p>
            <p className="text-sm text-muted-foreground mt-1">Активных заказов</p>
          </CardContent>
        </Card>

        <Card className="bg-card border-border">
          <CardContent className="p-6">
            <div className="flex items-center justify-between mb-3">
              <div className="w-10 h-10 rounded-xl bg-success/15 flex items-center justify-center">
                <Package className="w-5 h-5 text-success" />
              </div>
              <Link to="/orders">
                <ArrowRight className="w-4 h-4 text-muted-foreground" />
              </Link>
            </div>
            <p className="text-2xl font-bold">{myOrders.length}</p>
            <p className="text-sm text-muted-foreground mt-1">Всего покупок</p>
          </CardContent>
        </Card>
      </div>

      {!isSeller && (
        <Card className="bg-card border-border mb-6">
          <CardContent className="p-6 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-xl bg-primary/15 flex items-center justify-center">
                <Store className="w-6 h-6 text-primary" />
              </div>
              <div>
                <h3 className="font-semibold">Стать продавцом</h3>
                <p className="text-sm text-muted-foreground">
                  {appStatus?.status === 'PENDING'
                    ? 'Ваша заявка на рассмотрении'
                    : appStatus?.status === 'REJECTED'
                      ? 'Заявка отклонена'
                      : 'Подайте заявку, чтобы начать продавать'}
                </p>
              </div>
            </div>
            {(!appStatus || appStatus.status === 'REJECTED') && (
              <Link to="/seller-application">
                <Button>Подать заявку</Button>
              </Link>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  );
}
