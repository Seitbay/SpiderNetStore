import React from 'react';
import { Link } from 'react-router-dom';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Star, Package } from 'lucide-react';

export default function ProductCard({ product }) {
  const title = product.title ?? product.name;
  const categoryLabel = product.categories?.[0] ?? product.category;

  return (
    <Link to={`/products/${product.id}`}>
      <Card className="bg-card border-border hover:border-primary/40 transition-all duration-300 overflow-hidden group">
        <div className="aspect-video bg-secondary relative overflow-hidden">
          {product.imageUrl || product.image_url ? (
            <img
              src={product.imageUrl || product.image_url}
              alt={title}
              className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center">
              <Package className="w-10 h-10 text-muted-foreground/30" />
            </div>
          )}
          {categoryLabel ? (
            <Badge className="absolute top-3 left-3 bg-background/80 backdrop-blur text-foreground text-xs">
              {categoryLabel}
            </Badge>
          ) : null}
        </div>
        <CardContent className="p-4 space-y-2">
          <h3 className="font-semibold text-sm truncate group-hover:text-primary transition-colors">
            {title}
          </h3>
          <div className="flex items-center justify-between">
            <span className="text-lg font-bold text-primary">
              {product.price != null ? `${product.price} ₽` : '—'}
            </span>
            {product.rating != null && (
              <div className="flex items-center gap-1 text-xs text-muted-foreground">
                <Star className="w-3.5 h-3.5 fill-warning text-warning" />
                <span>{Number(product.rating).toFixed(1)}</span>
              </div>
            )}
          </div>
          {product.stockCount != null && (
            <p className="text-xs text-muted-foreground">В наличии: {product.stockCount}</p>
          )}
        </CardContent>
      </Card>
    </Link>
  );
}
