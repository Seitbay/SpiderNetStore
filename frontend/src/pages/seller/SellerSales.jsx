import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { orders as ordersApi, user as userApi } from '@/lib/api';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Package, ArrowRight } from 'lucide-react';
import PageHeader from '@/components/shared/PageHeader';
import LoadingSpinner from '@/components/shared/LoadingSpinner';
import EmptyState from '@/components/shared/EmptyState';
import StatusBadge from '@/components/shared/StatusBadge';
import { format } from 'date-fns';

export default function SellerSales() {
  const [sales, setSales] = useState([]);
  const [unreadByOrder, setUnreadByOrder] = useState({});
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState('all');

  useEffect(() => {
    Promise.all([ordersApi.mySales(), userApi.chatUnreadSummary().catch(() => ({}))])
      .then(([data, u]) => {
        const list = data?.items ?? data?.content ?? (Array.isArray(data) ? data : []);
        setSales(Array.isArray(list) ? list : []);
        const m = {};
        if (u?.byOrderId && typeof u.byOrderId === 'object') {
          Object.entries(u.byOrderId).forEach(([k, v]) => {
            m[Number(k)] = Number(v);
          });
        }
        setUnreadByOrder(m);
      })
      .catch(() => {
        setSales([]);
        setUnreadByOrder({});
      })
      .finally(() => setLoading(false));
  }, []);

  const filtered = tab === 'all' ? sales : sales.filter(o => o.status === tab);

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      <PageHeader title="Продажи" description="Ваши продажи как продавца" />

      <Tabs value={tab} onValueChange={setTab} className="mb-6">
        <TabsList className="bg-secondary">
          <TabsTrigger value="all">Все</TabsTrigger>
          <TabsTrigger value="PENDING">Активные</TabsTrigger>
          <TabsTrigger value="COMPLETED">Завершённые</TabsTrigger>
        </TabsList>
      </Tabs>

      {filtered.length === 0 ? (
        <EmptyState icon={Package} title="Нет продаж" description="Продажи появятся здесь" />
      ) : (
        <div className="space-y-3">
          {filtered.map(order => (
            <Link key={order.id} to={`/orders/${order.id}`}>
              <Card className="bg-card border-border hover:border-primary/30 transition-colors cursor-pointer">
                <CardContent className="p-4 flex items-center justify-between">
                  <div className="flex items-center gap-4">
                    <div className="w-10 h-10 rounded-lg bg-secondary flex items-center justify-center">
                      <Package className="w-5 h-5 text-muted-foreground" />
                    </div>
                    <div>
                      <p className="font-medium text-sm">
                        {order.productTitle || order.productName || order.product?.name || `Заказ #${order.id}`}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        Покупатель: {order.buyerUsername || order.buyer?.username || '—'} ·{' '}
                        {order.createdAt ? format(new Date(order.createdAt), 'dd.MM.yyyy') : ''}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    {unreadByOrder[order.id] > 0 && (
                      <Badge variant="destructive" className="shrink-0">
                        {unreadByOrder[order.id]}
                      </Badge>
                    )}
                    <StatusBadge status={order.status} />
                    <span className="font-semibold text-sm text-success">+{order.amount ?? order.price} ₽</span>
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