import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { products as productsApi } from '@/lib/api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { ArrowLeft, Plus, Trash2, Package } from 'lucide-react';
import { toast } from 'sonner';
import PageHeader from '@/components/shared/PageHeader';
import LoadingSpinner from '@/components/shared/LoadingSpinner';
import StatusBadge from '@/components/shared/StatusBadge';

export default function StockManagement() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [textData, setTextData] = useState('');
  const [files, setFiles] = useState(null);

  const load = async () => {
    const p = await productsApi.getManage(id);
    setProduct(p);
    setLoading(false);
  };

  useEffect(() => { load(); }, [id]);

  const handleAddText = async (e) => {
    e.preventDefault();
    const items = textData.split('\n').filter(Boolean).map(content => ({ content }));
    await productsApi.addStock(id, { items });
    toast.success(`Добавлено ${items.length} единиц`);
    setTextData('');
    load();
  };

  const handleAddFiles = async (e) => {
    e.preventDefault();
    if (!files) return;
    const formData = new FormData();
    Array.from(files).forEach(f => formData.append('files', f));
    await productsApi.addStockFiles(id, formData);
    toast.success('Файлы загружены');
    setFiles(null);
    load();
  };

  const handleDeleteStock = async (stockId) => {
    await productsApi.deleteStock(id, stockId);
    toast.success('Единица удалена');
    load();
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      <Button variant="ghost" size="sm" className="mb-4 text-muted-foreground" onClick={() => navigate(-1)}>
        <ArrowLeft className="w-4 h-4 mr-1" /> Назад
      </Button>
      <PageHeader title={`Склад: ${product?.name || product?.title}`} description={`${product?.stockCount || 0} единиц в наличии`} />

      <div className="grid lg:grid-cols-2 gap-6">
        <Card className="bg-card border-border">
          <CardHeader><CardTitle>Добавить на склад</CardTitle></CardHeader>
          <CardContent>
            <Tabs defaultValue="text">
              <TabsList className="bg-secondary mb-4">
                <TabsTrigger value="text">Текстовые данные</TabsTrigger>
                <TabsTrigger value="files">Файлы</TabsTrigger>
              </TabsList>
              <TabsContent value="text">
                <form onSubmit={handleAddText} className="space-y-4">
                  <div className="space-y-2">
                    <Label>Данные (каждая строка — отдельная единица)</Label>
                    <Textarea className="h-32 font-mono text-sm" value={textData} onChange={(e) => setTextData(e.target.value)} placeholder="login:password&#10;login2:password2" />
                  </div>
                  <Button type="submit"><Plus className="w-4 h-4 mr-2" /> Добавить</Button>
                </form>
              </TabsContent>
              <TabsContent value="files">
                <form onSubmit={handleAddFiles} className="space-y-4">
                  <Input type="file" multiple onChange={(e) => setFiles(e.target.files)} className="bg-secondary" />
                  <Button type="submit"><Plus className="w-4 h-4 mr-2" /> Загрузить</Button>
                </form>
              </TabsContent>
            </Tabs>
          </CardContent>
        </Card>

        <Card className="bg-card border-border">
          <CardHeader><CardTitle>Текущий склад</CardTitle></CardHeader>
          <CardContent>
            {!product?.stock || product.stock.length === 0 ? (
              <p className="text-sm text-muted-foreground text-center py-8">Склад пуст</p>
            ) : (
              <div className="space-y-2 max-h-96 overflow-y-auto">
                {product.stock.map(item => (
                  <div key={item.id} className="flex items-center justify-between p-3 bg-secondary rounded-lg">
                    <div className="flex items-center gap-3 flex-1 min-w-0">
                      <Package className="w-4 h-4 text-muted-foreground shrink-0" />
                      <span className="text-sm truncate font-mono">{item.content || item.fileName || `#${item.id}`}</span>
                    </div>
                    <div className="flex items-center gap-2 shrink-0">
                      <StatusBadge status={item.status || 'AVAILABLE'} />
                      {(item.status === 'AVAILABLE' || !item.status) && (
                        <Button variant="ghost" size="icon" className="h-7 w-7 text-destructive" onClick={() => handleDeleteStock(item.id)}>
                          <Trash2 className="w-3.5 h-3.5" />
                        </Button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}