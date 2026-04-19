import React from 'react';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';

const statusStyles = {
  PENDING: 'bg-warning/15 text-warning border-warning/30',
  COMPLETED: 'bg-success/15 text-success border-success/30',
  CANCELLED: 'bg-destructive/15 text-destructive border-destructive/30',
  ACTIVE: 'bg-success/15 text-success border-success/30',
  APPROVED: 'bg-success/15 text-success border-success/30',
  REJECTED: 'bg-destructive/15 text-destructive border-destructive/30',
  OPEN: 'bg-warning/15 text-warning border-warning/30',
  RESOLVED: 'bg-primary/15 text-primary border-primary/30',
  ESCALATED: 'bg-destructive/15 text-destructive border-destructive/30',
  AVAILABLE: 'bg-success/15 text-success border-success/30',
  SOLD: 'bg-muted text-muted-foreground border-border',
  DEPOSIT: 'bg-success/15 text-success border-success/30',
  PAYOUT: 'bg-warning/15 text-warning border-warning/30',
  PURCHASE: 'bg-primary/15 text-primary border-primary/30',
  REFUND: 'bg-destructive/15 text-destructive border-destructive/30',
};

const statusLabels = {
  PENDING: 'Ожидает',
  COMPLETED: 'Завершён',
  CANCELLED: 'Отменён',
  ACTIVE: 'Активен',
  APPROVED: 'Одобрен',
  REJECTED: 'Отклонён',
  OPEN: 'Открыт',
  RESOLVED: 'Решён',
  ESCALATED: 'Эскалирован',
  AVAILABLE: 'В наличии',
  SOLD: 'Продан',
  DEPOSIT: 'Пополнение',
  PAYOUT: 'Вывод',
  PURCHASE: 'Покупка',
  REFUND: 'Возврат',
};

export default function StatusBadge({ status, className }) {
  return (
    <Badge
      variant="outline"
      className={cn(
        "text-xs font-medium",
        statusStyles[status] || 'bg-secondary text-secondary-foreground',
        className
      )}
    >
      {statusLabels[status] || status}
    </Badge>
  );
}