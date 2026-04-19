import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { products as productsApi, orders as ordersApi, reviews as reviewsApi } from '@/lib/api';
import { useMarketAuth } from '@/lib/MarketAuthContext';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { ShoppingCart, Star, Package, User, ArrowLeft } from 'lucide-react';
import { toast } from 'sonner';
import LoadingSpinner from '@/components/shared/LoadingSpinner';
import StatusBadge from '@/components/shared/StatusBadge';

export default function ProductDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { currentUser } = useMarketAuth();
  const [product, setProduct] = useState(null);
  const [productReviews, setProductReviews] = useState([]);
  const [canLeave, setCanLeave] = useState(false);
  const [loading, setLoading] = useState(true);
  const [buying, setBuying] = useState(false);
  const [reviewForm, setReviewForm] = useState({ rating: '5', comment: '' });

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      const p = await productsApi.get(id);
      setProduct(p);
      const r = await reviewsApi.forProduct(id).catch(() => []);
      setProductReviews(Array.isArray(r) ? r : r.content || []);
      if (currentUser) {
        const cl = await reviewsApi.canLeave(id).catch(() => ({ canLeave: false }));
        setCanLeave(cl.canLeave || cl === true);
      }
      setLoading(false);
    };
    load();
  }, [id, currentUser]);

  const handleBuy = async () => {
    if (!currentUser) { navigate('/login'); return; }
    setBuying(true);
    try {
      await ordersApi.create({ productId: parseInt(id) });
      toast.success('Заказ создан!');
      navigate('/orders');
    } catch (err) {
      toast.error(err.message);
    } finally {
      setBuying(false);
    }
  };

  const handleReview = async (e) => {
    e.preventDefault();
    try {
      await reviewsApi.create({ productId: parseInt(id), rating: parseInt(reviewForm.rating), comment: reviewForm.comment });
      toast.success('Отзыв отправлен');
      setCanLeave(false);
      const r = await reviewsApi.forProduct(id);
      setProductReviews(Array.isArray(r) ? r : r.content || []);
    } catch (err) {
      toast.error(err.message);
    }
  };

  if (loading) return <LoadingSpinner />;
  if (!product) return <p className="text-center py-16 text-muted-foreground">Товар не найден</p>;

  return (
    <div>
      <Button variant="ghost" size="sm" className="mb-4 text-muted-foreground" onClick={() => navigate(-1)}>
        <ArrowLeft className="w-4 h-4 mr-1" /> Назад
      </Button>

      <div className="grid lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <div className="aspect-video bg-secondary rounded-xl overflow-hidden">
            {product.imageUrl || product.image_url ? (
              <img src={product.imageUrl || product.image_url} alt={product.name || product.title} className="w-full h-full object-cover" />
            ) : (
              <div className="w-full h-full flex items-center justify-center"><Package className="w-16 h-16 text-muted-foreground/20" /></div>
            )}
          </div>

          <Card className="bg-card border-border">
            <CardHeader><CardTitle>Описание</CardTitle></CardHeader>
            <CardContent>
              <p className="text-sm text-muted-foreground whitespace-pre-wrap">{product.description || 'Описание отсутствует'}</p>
            </CardContent>
          </Card>

          <Card className="bg-card border-border">
            <CardHeader><CardTitle>Отзывы ({productReviews.length})</CardTitle></CardHeader>
            <CardContent className="space-y-4">
              {canLeave && (
                <form onSubmit={handleReview} className="space-y-3 p-4 bg-secondary rounded-lg">
                  <div className="flex items-center gap-3">
                    <Select value={reviewForm.rating} onValueChange={(v) => setReviewForm({ ...reviewForm, rating: v })}>
                      <SelectTrigger className="w-24"><SelectValue /></SelectTrigger>
                      <SelectContent>
                        {[5, 4, 3, 2, 1].map(n => <SelectItem key={n} value={String(n)}>{n} ★</SelectItem>)}
                      </SelectContent>
                    </Select>
                    <span className="text-sm text-muted-foreground">Оставьте свой отзыв</span>
                  </div>
                  <Textarea
                    placeholder="Ваш комментарий..."
                    value={reviewForm.comment}
                    onChange={(e) => setReviewForm({ ...reviewForm, comment: e.target.value })}
                  />
                  <Button type="submit" size="sm">Отправить</Button>
                </form>
              )}
              {productReviews.length === 0 ? (
                <p className="text-sm text-muted-foreground text-center py-4">Пока нет отзывов</p>
              ) : (
                productReviews.map((r) => (
                  <div key={r.id} className="p-4 bg-secondary rounded-lg space-y-2">
                    <div className="flex items-center gap-2">
                      <div className="w-7 h-7 rounded-full bg-primary/20 flex items-center justify-center">
                        <User className="w-3.5 h-3.5 text-primary" />
                      </div>
                      <span className="text-sm font-medium">{r.username || r.buyerUsername || 'Пользователь'}</span>
                      <div className="flex items-center gap-0.5 ml-auto">
                        {Array.from({ length: r.rating }, (_, i) => (
                          <Star key={i} className="w-3.5 h-3.5 fill-warning text-warning" />
                        ))}
                      </div>
                    </div>
                    <p className="text-sm text-muted-foreground">{r.comment}</p>
                  </div>
                ))
              )}
            </CardContent>
          </Card>
        </div>

        <div className="space-y-4">
          <Card className="bg-card border-border sticky top-8">
            <CardContent className="p-6 space-y-4">
              <h2 className="text-xl font-bold">{product.name || product.title}</h2>
              {product.category && <Badge variant="outline">{product.category}</Badge>}
              <Separator />
              <div className="text-3xl font-bold text-primary">
                {product.price != null ? `${product.price} ₽` : 'Бесплатно'}
              </div>
              {product.rating != null && (
                <div className="flex items-center gap-1.5">
                  <Star className="w-4 h-4 fill-warning text-warning" />
                  <span className="text-sm font-medium">{product.rating?.toFixed(1)}</span>
                  <span className="text-xs text-muted-foreground">({product.reviewCount || 0} отзывов)</span>
                </div>
              )}
              {product.stockCount != null && (
                <p className="text-sm text-muted-foreground">В наличии: {product.stockCount} шт.</p>
              )}
              <Separator />
              {product.seller && (
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-full bg-primary/20 flex items-center justify-center">
                    <User className="w-4 h-4 text-primary" />
                  </div>
                  <div>
                    <p className="text-sm font-medium">{product.seller.username || product.sellerUsername}</p>
                    <p className="text-xs text-muted-foreground">Продавец</p>
                  </div>
                </div>
              )}
              <Button className="w-full" size="lg" onClick={handleBuy} disabled={buying}>
                <ShoppingCart className="w-4 h-4 mr-2" />
                {buying ? 'Оформление...' : 'Купить'}
              </Button>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}