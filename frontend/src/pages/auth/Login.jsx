import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useMarketAuth } from '@/lib/AuthContext';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { ShoppingBag, Eye, EyeOff } from 'lucide-react';
import { toast } from 'sonner';

export default function Login() {
  const [form, setForm] = useState({ email: '', password: '' });
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const { login } = useMarketAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await login(form);
      toast.success('Вход выполнен');
      navigate('/');
    } catch (err) {
      toast.error(err.message || 'Ошибка входа');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-background px-4">
      <div className="w-full max-w-md">
        <div className="flex justify-center mb-8">
          <div className="w-12 h-12 rounded-xl bg-primary flex items-center justify-center">
            <ShoppingBag className="w-6 h-6 text-primary-foreground" />
          </div>
        </div>
        <Card className="bg-card border-border">
          <CardHeader className="text-center">
            <CardTitle className="text-xl">Вход</CardTitle>
            <CardDescription>Войдите в свой аккаунт</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label>Email</Label>
                <Input
                  type="email"
                  placeholder="you@example.com"
                  value={form.email}
                  onChange={(e) => setForm({ ...form, email: e.target.value })}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label>Пароль</Label>
                <div className="relative">
                  <Input
                    type={showPassword ? 'text' : 'password'}
                    placeholder="••••••••"
                    value={form.password}
                    onChange={(e) => setForm({ ...form, password: e.target.value })}
                    required
                  />
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    className="absolute right-1 top-1/2 -translate-y-1/2 h-7 w-7"
                    onClick={() => setShowPassword(!showPassword)}
                  >
                    {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </Button>
                </div>
              </div>
              <Button type="submit" className="w-full" disabled={loading}>
                {loading ? 'Вход...' : 'Войти'}
              </Button>
            </form>
            <p className="text-center text-sm text-muted-foreground mt-4">
              Нет аккаунта?{' '}
              <Link to="/register" className="text-primary hover:underline">Регистрация</Link>
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
