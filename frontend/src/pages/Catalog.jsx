import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { products as productsApi } from '@/lib/api';
import { useAuth } from '@/lib/AuthContext';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Search } from 'lucide-react';
import ProductCard from '@/components/products/ProductCard';
import PageHeader from '@/components/shared/PageHeader';
import LoadingSpinner from '@/components/shared/LoadingSpinner';
import EmptyState from '@/components/shared/EmptyState';

export default function Catalog() {
  const { isAuthenticated } = useAuth();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const data = await productsApi.list({
          q: search.trim() || undefined,
          page: 0,
          size: 24,
        });
        const list = data?.items ?? data?.content ?? (Array.isArray(data) ? data : []);
        setItems(Array.isArray(list) ? list : []);
      } catch {
        setItems([]);
      } finally {
        setLoading(false);
      }
    };
    const t = setTimeout(load, search ? 300 : 0);
    return () => clearTimeout(t);
  }, [search]);

  return (
    <div>
      <PageHeader title="Каталог" description="Найдите то, что вам нужно">
        {!isAuthenticated ? (
          <div className="flex gap-2">
            <Button variant="outline" asChild>
              <Link to="/login">Войти</Link>
            </Button>
            <Button asChild>
              <Link to="/register">Регистрация</Link>
            </Button>
          </div>
        ) : null}
      </PageHeader>

      <div className="flex flex-col sm:flex-row gap-3 mb-6">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <Input
            className="pl-10 bg-secondary border-border"
            placeholder="Поиск по названию..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
      </div>

      {loading ? (
        <LoadingSpinner />
      ) : items.length === 0 ? (
        <EmptyState title="Товары не найдены" description="Попробуйте другой запрос или зайдите позже" />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {items.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
      )}
    </div>
  );
}
