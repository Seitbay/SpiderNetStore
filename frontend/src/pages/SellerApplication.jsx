import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { sellerApp } from '@/lib/api';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Textarea } from '@/components/ui/textarea';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Store, CheckCircle, Clock, XCircle } from 'lucide-react';
import { toast } from 'sonner';
import PageHeader from '@/components/shared/PageHeader';
import LoadingSpinner from '@/components/shared/LoadingSpinner';

export default function SellerApplication() {
  const [status, setStatus] = useState(null);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({ description: '', experience: '' });
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    sellerApp.myStatus().then(setStatus).catch(() => null).finally(() => setLoading(false));
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await sellerApp.apply(form);
      toast.success('Заявка подана!');
      navigate('/dashboard');
    } catch (err) {
      toast.error(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <LoadingSpinner />;

  if (status?.status === 'PENDING') {
    return (
      <div>
        <PageHeader title="Заявка на продавца" />
        <Card className="bg-card border-border max-w-lg">
          <CardContent className="p-8 text-center">
            <Clock className="w-12 h-12 text-warning mx-auto mb-4" />
            <h3 className="text-lg font-semibold mb-2">Заявка на рассмотрении</h3>
            <p className="text-sm text-muted-foreground">Мы рассмотрим вашу заявку и свяжемся с вами.</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (status?.status === 'APPROVED') {
    return (
      <div>
        <PageHeader title="Заявка на продавца" />
        <Card className="bg-card border-border max-w-lg">
          <CardContent className="p-8 text-center">
            <CheckCircle className="w-12 h-12 text-success mx-auto mb-4" />
            <h3 className="text-lg font-semibold mb-2">Заявка одобрена!</h3>
            <p className="text-sm text-muted-foreground">Вы уже являетесь продавцом.</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div>
      <PageHeader title="Стать продавцом" description="Заполните заявку" />
      <Card className="bg-card border-border max-w-lg">
        <CardHeader>
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-primary/15 flex items-center justify-center">
              <Store className="w-5 h-5 text-primary" />
            </div>
            <div>
              <CardTitle>Заявка на продавца</CardTitle>
              <CardDescription>Расскажите о себе и вашем опыте</CardDescription>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {status?.status === 'REJECTED' && (
            <div className="flex items-center gap-2 p-3 bg-destructive/10 rounded-lg text-destructive text-sm mb-4">
              <XCircle className="w-4 h-4" /> Ваша предыдущая заявка была отклонена. Попробуйте ещё раз.
            </div>
          )}
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label>Описание</Label>
              <Textarea placeholder="Что вы планируете продавать?" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} required />
            </div>
            <div className="space-y-2">
              <Label>Опыт</Label>
              <Input placeholder="Ваш опыт в продажах" value={form.experience} onChange={(e) => setForm({ ...form, experience: e.target.value })} />
            </div>
            <Button type="submit" className="w-full" disabled={submitting}>
              {submitting ? 'Отправка...' : 'Подать заявку'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}