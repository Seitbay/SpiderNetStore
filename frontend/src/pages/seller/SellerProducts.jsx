import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { products as productsApi } from '@/lib/api';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Plus, Package, Pencil, Trash2, ArrowRight } from 'lucide-react';
import { toast } from 'sonner';
import PageHeader from '@/components/shared/PageHeader';
import LoadingSpinner from '@/components/shared/LoadingSpinner';
import EmptyState from '@/components/shared/EmptyState';

export default function SellerProducts() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    try {
      const data = await productsApi.myProducts();
      const list = data?.items ?? data?.content ?? (Array.isArray(data) ? data : []);
      setItems(Array.isArray(list) ? list : []);
    } catch {
      setItems([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleDelete = async (id) => {
    if (!window.confirm('Удалить товар?')) return;
    await productsApi.delete(id);
    toast.success('Товар удалён');
    load();
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      <PageHeader title="Мои товары" description="Управление вашими товарами">
        <Link to="/seller/products/new">
          <Button><Plus className="w-4 h-4 mr-2" /> Добавить товар</Button>
        </Link>
      </PageHeader>

      {items.length === 0 ? (
        <EmptyState icon={Package} title="Нет товаров" description="Создайте первый товар" />
      ) : (
        <div className="space-y-3">
          {items.map(product => (
            <Card key={product.id} className="bg-card border-border hover:border-primary/30 transition-colors">
              <CardContent className="p-4 flex items-center justify-between">
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 rounded-lg bg-secondary overflow-hidden">
                    {product.imageUrl || product.image_url ? (
                      <img src={product.imageUrl || product.image_url} alt="" className="w-full h-full object-cover" />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center"><Package className="w-5 h-5 text-muted-foreground/30" /></div>
                    )}
                  </div>
                  <div>
                    <p className="font-medium text-sm">{product.name || product.title}</p>
                    <p className="text-xs text-muted-foreground">{product.price} ₽ · {product.stockCount ?? 0} в наличии</p>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <Link to={`/seller/products/${product.id}/edit`}>
                    <Button variant="ghost" size="icon" className="h-8 w-8"><Pencil className="w-4 h-4" /></Button>
                  </Link>
                  <Link to={`/seller/products/${product.id}/stock`}>
                    <Button variant="ghost" size="icon" className="h-8 w-8"><Package className="w-4 h-4" /></Button>
                  </Link>
                  <Button variant="ghost" size="icon" className="h-8 w-8 text-destructive" onClick={() => handleDelete(product.id)}>
                    <Trash2 className="w-4 h-4" />
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}