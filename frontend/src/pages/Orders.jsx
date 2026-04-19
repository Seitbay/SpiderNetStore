import React, { useState, useEffect } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { useAuth } from '@/lib/AuthContext';
import { orders as ordersApi, user as userApi } from '@/lib/api';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Package, ArrowRight } from 'lucide-react';
import PageHeader from '@/components/shared/PageHeader';
import LoadingSpinner from '@/components/shared/LoadingSpinner';
import EmptyState from '@/components/shared/EmptyState';
import { format } from 'date-fns';

const TABS = [
  { id: 'all', label: 'Все' },
  { id: 'PENDING', label: 'Активные' },
  { id: 'COMPLETED', label: 'Завершённые' },
  { id: 'CANCELLED', label: 'Отменённые' },
];

export default function Orders() {
  const { isAuthenticated } = useAuth();
  const [myOrders, setMyOrders] = useState([]);
  const [unreadByOrder, setUnreadByOrder] = useState({});
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState('all');

  useEffect(() => {
    if (!isAuthenticated) {
      setLoading(false);
      return;
    }
    const load = async () => {
      try {
        const data = await ordersApi.myPurchases().catch(() => ({ items: [] }));
        const list = data?.items ?? data?.content ?? (Array.isArray(data) ? data : []);
        setMyOrders(Array.isArray(list) ? list : []);
        const u = await userApi.chatUnreadSummary().catch(() => ({}));
        const m = {};
        if (u?.byOrderId && typeof u.byOrderId === 'object') {
          Object.entries(u.byOrderId).forEach(([k, v]) => {
            m[Number(k)] = Number(v);
          });
        }
        setUnreadByOrder(m);
      } catch {
        setMyOrders([]);
        setUnreadByOrder({});
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [isAuthenticated]);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  const filtered =
    tab === 'all' ? myOrders : myOrders.filter((o) => o.status === tab);

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      <PageHeader title="Мои покупки" description="История ваших заказов" />

      <div className="flex flex-wrap gap-2 mb-6">
        {TABS.map((t) => (
          <Button
            key={t.id}
            type="button"
            size="sm"
            variant={tab === t.id ? 'default' : 'secondary'}
            onClick={() => setTab(t.id)}
          >
            {t.label}
          </Button>
        ))}
      </div>

      {filtered.length === 0 ? (
        <EmptyState
          icon={Package}
          title="Нет заказов"
          description="Ваши покупки будут здесь"
        />
      ) : (
        <div className="space-y-3">
          {filtered.map((order) => (
            <Link key={order.id} to={`/orders/${order.id}`}>
              <Card className="bg-card border-border hover:border-primary/30 transition-colors cursor-pointer">
                <CardContent className="p-4 flex items-center justify-between">
                  <div className="flex items-center gap-4">
                    <div className="w-10 h-10 rounded-lg bg-secondary flex items-center justify-center">
                      <Package className="w-5 h-5 text-muted-foreground" />
                    </div>
                    <div>
                      <p className="font-medium text-sm">
                        {order.productName ||
                          order.product?.name ||
                          `Заказ #${order.id}`}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        {order.createdAt
                          ? format(new Date(order.createdAt), 'dd.MM.yyyy HH:mm')
                          : ''}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    {unreadByOrder[order.id] > 0 && (
                      <Badge variant="destructive" className="shrink-0">
                        {unreadByOrder[order.id]}
                      </Badge>
                    )}
                    <span className="text-xs text-muted-foreground">{order.status}</span>
                    <span className="font-semibold text-sm">
                      {order.price ?? order.amount ?? '—'} ₽
                    </span>
                    <ArrowRight className="w-4 h-4 text-muted-foreground" />
                  </div>
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
