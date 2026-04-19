import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { products as productsApi, categories as categoriesApi } from '@/lib/api';
import { Card, CardContent } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { ArrowLeft } from 'lucide-react';
import { toast } from 'sonner';
import PageHeader from '@/components/shared/PageHeader';
import LoadingSpinner from '@/components/shared/LoadingSpinner';

export default function ProductForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = Boolean(id);
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState({
    name: '',
    description: '',
    price: '',
    categorySlug: '',
    imageUrl: '',
  });
  const [files, setFiles] = useState(null);
  const [coverFile, setCoverFile] = useState(null);
  const [loading, setLoading] = useState(isEdit);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const cats = await categoriesApi.list();
        if (cancelled) return;
        const catList = Array.isArray(cats) ? cats : [];
        setCategories(catList);
        if (isEdit && id) {
          const p = await productsApi.getManage(id);
          if (cancelled) return;
          const slug =
            catList.find((c) => (p.categoryIds || []).includes(c.id))?.slug ?? '';
          setForm({
            name: p.title ?? '',
            description: p.description ?? '',
            price: String(p.price ?? ''),
            categorySlug: slug,
            imageUrl: p.imageUrl ?? '',
          });
        }
      } catch {
        if (!cancelled) setCategories([]);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [id, isEdit]);

  const categoryIdsFromSlug = () => {
    if (!form.categorySlug) return [];
    const c = categories.find((x) => x.slug === form.categorySlug);
    return c?.id != null ? [c.id] : [];
  };

  const buildCreatePayload = () => {
    const payload = {
      title: form.name.trim(),
      description: (form.description || '').trim(),
      price: parseFloat(form.price),
      categoryIds: categoryIdsFromSlug(),
    };
    const img = (form.imageUrl || '').trim();
    if (img) payload.imageUrl = img;
    return payload;
  };

  const buildUpdatePayload = () => ({
    title: form.name.trim(),
    description: (form.description || '').trim(),
    price: parseFloat(form.price),
    categoryIds: categoryIdsFromSlug(),
    imageUrl: (form.imageUrl || '').trim(),
  });

  const appendStockFilesToFormData = (formData, fileList) => {
    Array.from(fileList).forEach((f) => {
      const lower = (f.name || '').toLowerCase();
      if (lower.endsWith('.json')) {
        formData.append('jsonFiles', f);
      } else if (lower.endsWith('.txt') || lower.endsWith('.csv')) {
        formData.append('textFiles', f);
      } else {
        formData.append('archiveFiles', f);
      }
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      if (isEdit) {
        await productsApi.update(id, buildUpdatePayload());
        if (coverFile) {
          const fd = new FormData();
          fd.append('coverImage', coverFile);
          await productsApi.uploadCover(id, fd);
        }
        toast.success('Товар обновлён');
      } else {
        const hasStockFiles = files && files.length > 0;
        if (hasStockFiles || coverFile) {
          const payload = buildCreatePayload();
          const formData = new FormData();
          formData.append(
            'metadata',
            new Blob([JSON.stringify(payload)], { type: 'application/json' }),
          );
          if (hasStockFiles) appendStockFilesToFormData(formData, files);
          if (coverFile) formData.append('coverImage', coverFile);
          await productsApi.createWithFiles(formData);
        } else {
          await productsApi.create(buildCreatePayload());
        }
        toast.success('Товар создан');
      }
      navigate('/seller/products');
    } catch (err) {
      toast.error(err.message);
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      <Button variant="ghost" size="sm" className="mb-4 text-muted-foreground" onClick={() => navigate(-1)}>
        <ArrowLeft className="w-4 h-4 mr-1" /> Назад
      </Button>
      <PageHeader title={isEdit ? 'Редактировать товар' : 'Новый товар'} />

      <Card className="bg-card border-border max-w-xl">
        <CardContent className="p-6">
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label>Название</Label>
              <Input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
            </div>
            <div className="space-y-2">
              <Label>Описание</Label>
              <Textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Цена (₽)</Label>
                <Input
                  type="number"
                  min="0.01"
                  step="0.01"
                  value={form.price}
                  onChange={(e) => setForm({ ...form, price: e.target.value })}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label>Категория</Label>
                <Select
                  value={form.categorySlug}
                  onValueChange={(v) => setForm({ ...form, categorySlug: v })}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Выберите" />
                  </SelectTrigger>
                  <SelectContent>
                    {categories.map((c) => (
                      <SelectItem key={c.id} value={c.slug}>
                        {c.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
            <div className="space-y-2">
              <Label>Ссылка на картинку (опционально)</Label>
              <Input
                type="url"
                placeholder="https://…"
                value={form.imageUrl}
                onChange={(e) => setForm({ ...form, imageUrl: e.target.value })}
                className="bg-secondary"
              />
              <p className="text-xs text-muted-foreground">
                Либо укажите URL, либо загрузите файл ниже (файл перезапишет ссылку после сохранения).
              </p>
            </div>
            <div className="space-y-2">
              <Label>Обложка файлом (опционально)</Label>
              <Input
                type="file"
                accept="image/jpeg,image/png,image/webp,image/gif,.jpg,.jpeg,.png,.webp,.gif"
                onChange={(e) => setCoverFile(e.target.files?.[0] ?? null)}
                className="bg-secondary"
              />
            </div>
            {!isEdit && (
              <div className="space-y-2">
                <Label>Файлы для склада (опционально)</Label>
                <p className="text-xs text-muted-foreground">
                  .txt / .csv — по строке на позицию; .json — данные стока; остальное — как бинарные вложения.
                </p>
                <Input type="file" multiple onChange={(e) => setFiles(e.target.files)} className="bg-secondary" />
              </div>
            )}
            <Button type="submit" className="w-full" disabled={saving}>
              {saving ? 'Сохранение...' : isEdit ? 'Сохранить' : 'Создать товар'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
