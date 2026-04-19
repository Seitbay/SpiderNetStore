import React, { useState, useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '@/lib/AuthContext';
import { user as userApi } from '@/lib/api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { User, Lock } from 'lucide-react';
import { toast } from 'sonner';
import PageHeader from '@/components/shared/PageHeader';

export default function Profile() {
  const { currentUser, isAuthenticated, refreshUser } = useAuth();
  const [form, setForm] = useState({ username: '' });
  const [passForm, setPassForm] = useState({
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (currentUser?.username) {
      setForm({ username: currentUser.username });
    }
  }, [currentUser]);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  const handleUpdate = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await userApi.updateMe(form);
      await refreshUser();
      toast.success('Профиль обновлён');
    } catch (err) {
      toast.error(err.message || 'Ошибка');
    } finally {
      setSaving(false);
    }
  };

  const handlePassword = async (e) => {
    e.preventDefault();
    if (passForm.newPassword !== passForm.confirmPassword) {
      toast.error('Пароли не совпадают');
      return;
    }
    try {
      await userApi.changePassword({
        oldPassword: passForm.oldPassword,
        newPassword: passForm.newPassword,
      });
      toast.success('Пароль изменён');
      setPassForm({ oldPassword: '', newPassword: '', confirmPassword: '' });
    } catch (err) {
      toast.error(err.message || 'Ошибка');
    }
  };

  return (
    <div>
      <PageHeader title="Профиль" description="Управление аккаунтом" />

      <div className="grid lg:grid-cols-2 gap-6">
        <Card className="bg-card border-border">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <User className="w-5 h-5" /> Основная информация
            </CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleUpdate} className="space-y-4">
              <div className="space-y-2">
                <Label>Email</Label>
                <Input value={currentUser?.email || ''} disabled className="bg-muted" />
              </div>
              <div className="space-y-2">
                <Label>Имя пользователя</Label>
                <Input
                  value={form.username}
                  onChange={(e) => setForm({ ...form, username: e.target.value })}
                />
              </div>
              <div className="space-y-2">
                <Label>Роль</Label>
                <Input value={currentUser?.role || ''} disabled className="bg-muted" />
              </div>
              <Button type="submit" disabled={saving}>
                {saving ? 'Сохранение...' : 'Сохранить'}
              </Button>
            </form>
          </CardContent>
        </Card>

        <Card className="bg-card border-border">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Lock className="w-5 h-5" /> Смена пароля
            </CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handlePassword} className="space-y-4">
              <div className="space-y-2">
                <Label>Текущий пароль</Label>
                <Input
                  type="password"
                  value={passForm.oldPassword}
                  onChange={(e) =>
                    setPassForm({ ...passForm, oldPassword: e.target.value })
                  }
                  required
                />
              </div>
              <div className="space-y-2">
                <Label>Новый пароль</Label>
                <Input
                  type="password"
                  value={passForm.newPassword}
                  onChange={(e) =>
                    setPassForm({ ...passForm, newPassword: e.target.value })
                  }
                  required
                />
              </div>
              <div className="space-y-2">
                <Label>Подтвердите новый пароль</Label>
                <Input
                  type="password"
                  value={passForm.confirmPassword}
                  onChange={(e) =>
                    setPassForm({ ...passForm, confirmPassword: e.target.value })
                  }
                  required
                />
              </div>
              <Button type="submit">Изменить пароль</Button>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
