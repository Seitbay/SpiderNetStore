import React, { useState, useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '@/lib/AuthContext';
import { payments } from '@/lib/api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Wallet as WalletIcon, ArrowDownLeft, ArrowUpRight, History } from 'lucide-react';
import { toast } from 'sonner';
import PageHeader from '@/components/shared/PageHeader';
import LoadingSpinner from '@/components/shared/LoadingSpinner';
import { format } from 'date-fns';

export default function Wallet() {
  const { isAuthenticated } = useAuth();
  const [balance, setBalance] = useState(0);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [depositAmount, setDepositAmount] = useState('');
  const [payoutAmount, setPayoutAmount] = useState('');

  const load = async () => {
    try {
      const [b, t] = await Promise.all([
        payments.balance().catch(() => ({ balance: 0 })),
        payments.history().catch(() => ({ items: [] })),
      ]);
      const bal = b?.balance ?? b;
      setBalance(typeof bal === 'number' ? bal : bal != null ? Number(bal) : 0);
      const list = t?.items ?? (Array.isArray(t) ? t : []);
      setTransactions(Array.isArray(list) ? list : []);
    } catch {
      setBalance(0);
      setTransactions([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!isAuthenticated) {
      setLoading(false);
      return;
    }
    load();
  }, [isAuthenticated]);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  const handleDeposit = async (e) => {
    e.preventDefault();
    const amount = parseFloat(depositAmount, 10);
    if (Number.isNaN(amount) || amount <= 0) {
      toast.error('Укажите сумму');
      return;
    }
    try {
      await payments.deposit({ amount });
      toast.success('Баланс пополнен');
      setDepositAmount('');
      await load();
    } catch (err) {
      toast.error(err.message || 'Ошибка');
    }
  };

  const handlePayout = async (e) => {
    e.preventDefault();
    const amount = parseFloat(payoutAmount, 10);
    if (Number.isNaN(amount) || amount <= 0) {
      toast.error('Укажите сумму');
      return;
    }
    try {
      await payments.payout({ amount });
      toast.success('Вывод выполнен');
      setPayoutAmount('');
      await load();
    } catch (err) {
      toast.error(err.message || 'Ошибка');
    }
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      <PageHeader title="Кошелёк" description="Управление балансом" />

      <Card className="bg-card border-border mb-8">
        <CardContent className="p-6">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
            <div className="flex items-center gap-4">
              <div className="w-14 h-14 rounded-2xl bg-primary/15 flex items-center justify-center">
                <WalletIcon className="w-7 h-7 text-primary" />
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Текущий баланс</p>
                <p className="text-3xl font-bold">{balance.toFixed(2)} ₽</p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="grid md:grid-cols-2 gap-6 mb-8">
        <Card className="bg-card border-border">
          <CardHeader>
            <CardTitle className="text-base flex items-center gap-2">
              <ArrowDownLeft className="w-4 h-4" /> Пополнение
            </CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleDeposit} className="space-y-4">
              <div className="space-y-2">
                <Label>Сумма (₽)</Label>
                <Input
                  type="number"
                  min="0.01"
                  step="0.01"
                  value={depositAmount}
                  onChange={(e) => setDepositAmount(e.target.value)}
                />
              </div>
              <Button type="submit" className="w-full">
                Пополнить
              </Button>
            </form>
          </CardContent>
        </Card>

        <Card className="bg-card border-border">
          <CardHeader>
            <CardTitle className="text-base flex items-center gap-2">
              <ArrowUpRight className="w-4 h-4" /> Вывод
            </CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handlePayout} className="space-y-4">
              <div className="space-y-2">
                <Label>Сумма (₽)</Label>
                <Input
                  type="number"
                  min="0.01"
                  step="0.01"
                  max={balance}
                  value={payoutAmount}
                  onChange={(e) => setPayoutAmount(e.target.value)}
                />
              </div>
              <Button type="submit" variant="outline" className="w-full">
                Вывести
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>

      <Card className="bg-card border-border">
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base">
            <History className="w-5 h-5" /> История транзакций
          </CardTitle>
        </CardHeader>
        <CardContent>
          {transactions.length === 0 ? (
            <p className="text-center text-sm text-muted-foreground py-8">Нет транзакций</p>
          ) : (
            <div className="space-y-2">
              {transactions.map((tx) => (
                <div
                  key={tx.id ?? `${tx.createdAt}-${tx.amount}`}
                  className="flex items-center justify-between p-3 bg-secondary rounded-lg"
                >
                  <div>
                    <p className="text-sm font-medium">{tx.type || tx.description || 'Операция'}</p>
                    <p className="text-xs text-muted-foreground">
                      {tx.createdAt
                        ? format(new Date(tx.createdAt), 'dd.MM.yyyy HH:mm')
                        : ''}
                    </p>
                  </div>
                  <span className="font-semibold text-sm">
                    {tx.amount != null ? `${tx.amount} ₽` : '—'}
                  </span>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
