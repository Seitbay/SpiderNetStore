import React, { useState, useEffect } from 'react';
import { disputes as disputesApi } from '@/lib/api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { Gavel, AlertTriangle } from 'lucide-react';
import { toast } from 'sonner';
import PageHeader from '@/components/shared/PageHeader';
import LoadingSpinner from '@/components/shared/LoadingSpinner';
import EmptyState from '@/components/shared/EmptyState';
import StatusBadge from '@/components/shared/StatusBadge';
import { format } from 'date-fns';

export default function AdminDisputes() {
  const [disputesList, setDisputesList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [resolveForm, setResolveForm] = useState({ resolution: 'BUYER', comment: '' });
  const [resolveId, setResolveId] = useState(null);

  const load = async () => {
    const data = await disputesApi.listAll().catch(() => []);
    setDisputesList(Array.isArray(data) ? data : data.content || []);
    setLoading(false);
  };

  useEffect(() => { load(); }, []);

  const handleResolve = async () => {
    await disputesApi.resolve(resolveId, resolveForm);
    toast.success('Спор разрешён');
    setResolveId(null);
    load();
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      <PageHeader title="Споры" description="Управление спорами между покупателями и продавцами" />

      {disputesList.length === 0 ? (
        <EmptyState icon={Gavel} title="Нет споров" />
      ) : (
        <div className="space-y-3">
          {disputesList.map(dispute => (
            <Card key={dispute.id} className="bg-card border-border">
              <CardContent className="p-4">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <div className="flex items-center gap-2 mb-2">
                      <AlertTriangle className="w-4 h-4 text-warning" />
                      <p className="font-medium text-sm">Спор #{dispute.id} — Заказ #{dispute.orderId}</p>
                      <StatusBadge status={dispute.status} />
                    </div>
                    <p className="text-sm text-muted-foreground">{dispute.reason || dispute.description}</p>
                    <p className="text-xs text-muted-foreground mt-1">
                      Покупатель: {dispute.buyerUsername || '—'} · Продавец: {dispute.sellerUsername || '—'}
                      {dispute.createdAt && ` · ${format(new Date(dispute.createdAt), 'dd.MM.yyyy')}`}
                    </p>
                    {dispute.sellerResponse && (
                      <div className="mt-2 p-2 bg-secondary rounded text-sm">
                        <span className="text-xs text-muted-foreground">Ответ продавца:</span> {dispute.sellerResponse}
                      </div>
                    )}
                  </div>
                  {(dispute.status === 'OPEN' || dispute.status === 'ESCALATED') && (
                    <Dialog open={resolveId === dispute.id} onOpenChange={(open) => setResolveId(open ? dispute.id : null)}>
                      <DialogTrigger asChild>
                        <Button size="sm"><Gavel className="w-4 h-4 mr-1" /> Разрешить</Button>
                      </DialogTrigger>
                      <DialogContent>
                        <DialogHeader><DialogTitle>Разрешить спор #{dispute.id}</DialogTitle></DialogHeader>
                        <div className="space-y-4">
                          <div className="space-y-2">
                            <Label>Решение в пользу</Label>
                            <Select value={resolveForm.resolution} onValueChange={(v) => setResolveForm({ ...resolveForm, resolution: v })}>
                              <SelectTrigger><SelectValue /></SelectTrigger>
                              <SelectContent>
                                <SelectItem value="BUYER">Покупателя</SelectItem>
                                <SelectItem value="SELLER">Продавца</SelectItem>
                              </SelectContent>
                            </Select>
                          </div>
                          <div className="space-y-2">
                            <Label>Комментарий</Label>
                            <Textarea value={resolveForm.comment} onChange={(e) => setResolveForm({ ...resolveForm, comment: e.target.value })} />
                          </div>
                          <Button className="w-full" onClick={handleResolve}>Подтвердить</Button>
                        </div>
                      </DialogContent>
                    </Dialog>
                  )}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}