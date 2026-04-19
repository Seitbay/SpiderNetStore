import React, { useState, useEffect } from 'react';
import { sellerApp } from '@/lib/api';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { CheckCircle, XCircle, User } from 'lucide-react';
import { toast } from 'sonner';
import PageHeader from '@/components/shared/PageHeader';
import LoadingSpinner from '@/components/shared/LoadingSpinner';
import EmptyState from '@/components/shared/EmptyState';
import StatusBadge from '@/components/shared/StatusBadge';

export default function AdminApplications() {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    const data = await sellerApp.listAll().catch(() => []);
    setApplications(Array.isArray(data) ? data : data.content || []);
    setLoading(false);
  };

  useEffect(() => { load(); }, []);

  const handleApprove = async (id) => {
    await sellerApp.approve(id);
    toast.success('Заявка одобрена');
    load();
  };

  const handleReject = async (id) => {
    await sellerApp.reject(id);
    toast.success('Заявка отклонена');
    load();
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      <PageHeader title="Заявки на продавца" description="Управление заявками пользователей" />

      {applications.length === 0 ? (
        <EmptyState icon={User} title="Нет заявок" />
      ) : (
        <div className="space-y-3">
          {applications.map(app => (
            <Card key={app.id} className="bg-card border-border">
              <CardContent className="p-4">
                <div className="flex items-start justify-between gap-4">
                  <div className="flex items-start gap-4">
                    <div className="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center shrink-0">
                      <User className="w-5 h-5 text-primary" />
                    </div>
                    <div>
                      <p className="font-medium text-sm">{app.username || app.user?.username || `User #${app.userId}`}</p>
                      <p className="text-xs text-muted-foreground mb-2">{app.email || app.user?.email || ''}</p>
                      {app.description && <p className="text-sm text-muted-foreground">{app.description}</p>}
                      {app.experience && <p className="text-xs text-muted-foreground mt-1">Опыт: {app.experience}</p>}
                    </div>
                  </div>
                  <div className="flex items-center gap-2 shrink-0">
                    <StatusBadge status={app.status} />
                    {app.status === 'PENDING' && (
                      <>
                        <Button size="sm" variant="outline" className="text-success border-success/30 hover:bg-success/10" onClick={() => handleApprove(app.id)}>
                          <CheckCircle className="w-4 h-4 mr-1" /> Одобрить
                        </Button>
                        <Button size="sm" variant="outline" className="text-destructive border-destructive/30 hover:bg-destructive/10" onClick={() => handleReject(app.id)}>
                          <XCircle className="w-4 h-4 mr-1" /> Отклонить
                        </Button>
                      </>
                    )}
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}