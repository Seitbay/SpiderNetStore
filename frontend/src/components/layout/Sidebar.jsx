import React, { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useMarketAuth } from '@/lib/AuthContext';
import { user as userApi } from '@/lib/api';
import { useSidebar } from './AppLayout';
import {
  ShoppingBag,
  Package,
  LayoutDashboard,
  Wallet,
  Store,
  User,
  LogOut,
  ChevronLeft,
  ChevronRight,
  Gavel,
  ClipboardList,
  LogIn,
  UserPlus,
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';

const navItems = [
  { path: '/', label: 'Каталог', icon: ShoppingBag, public: true },
  { path: '/dashboard', label: 'Дашборд', icon: LayoutDashboard, auth: true },
  { path: '/orders', label: 'Мои покупки', icon: ClipboardList, auth: true, showChatBadge: true },
  { path: '/wallet', label: 'Кошелёк', icon: Wallet, auth: true },
];

const sellerItems = [
  { path: '/seller/products', label: 'Мои товары', icon: Package },
  { path: '/seller/sales', label: 'Продажи', icon: Store, showChatBadge: true },
];

const adminItems = [
  { path: '/admin/applications', label: 'Заявки', icon: ClipboardList },
  { path: '/admin/disputes', label: 'Споры', icon: Gavel },
];

export default function Sidebar() {
  const { collapsed, setCollapsed } = useSidebar();
  const { currentUser, isSeller, isAdmin, logout } = useMarketAuth();
  const location = useLocation();
  const [chatUnreadTotal, setChatUnreadTotal] = useState(0);

  useEffect(() => {
    if (!currentUser) {
      setChatUnreadTotal(0);
      return;
    }
    userApi
      .chatUnreadSummary()
      .then((s) => setChatUnreadTotal(Number(s?.totalUnread) || 0))
      .catch(() => setChatUnreadTotal(0));
  }, [currentUser]);

  const NavLink = ({ item, chatBadge }) => {
    const active = location.pathname === item.path;
    return (
      <Link
        to={item.path}
        className={cn(
          'relative flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-200',
          active
            ? 'bg-primary/15 text-primary'
            : 'text-muted-foreground hover:text-foreground hover:bg-secondary/60'
        )}
      >
        <item.icon className={cn('w-5 h-5 shrink-0', active && 'text-primary')} />
        {!collapsed && <span className="truncate flex-1">{item.label}</span>}
        {chatBadge > 0 && (
          <span className="shrink-0 min-w-[1.25rem] h-5 px-1 rounded-full bg-destructive text-destructive-foreground text-xs font-semibold flex items-center justify-center">
            {chatBadge > 99 ? '99+' : chatBadge}
          </span>
        )}
      </Link>
    );
  };

  return (
    <aside
      className={cn(
        'fixed left-0 top-0 h-screen bg-sidebar border-r border-sidebar-border flex flex-col z-40 transition-all duration-300',
        collapsed ? 'w-16' : 'w-60'
      )}
    >
      <div className="flex items-center justify-between p-4 h-16">
        {!collapsed && (
          <Link to="/" className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-primary flex items-center justify-center">
              <ShoppingBag className="w-4 h-4 text-primary-foreground" />
            </div>
            <span className="font-bold text-lg">SpiderNet</span>
          </Link>
        )}
        <Button
          variant="ghost"
          size="icon"
          className="w-8 h-8 text-muted-foreground"
          onClick={() => setCollapsed(!collapsed)}
        >
          {collapsed ? <ChevronRight className="w-4 h-4" /> : <ChevronLeft className="w-4 h-4" />}
        </Button>
      </div>

      <nav className="flex-1 px-3 py-2 space-y-1 overflow-y-auto">
        {navItems.filter((i) => i.public || (i.auth && currentUser)).map((item) => (
          <NavLink
            key={item.path}
            item={item}
            chatBadge={item.showChatBadge ? chatUnreadTotal : 0}
          />
        ))}

        {!currentUser && (
          <>
            {!collapsed && (
              <p className="px-3 pt-4 pb-1 text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                Аккаунт
              </p>
            )}
            {collapsed && <Separator className="my-2" />}
            <Link
              to="/login"
              className={cn(
                'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-200',
                location.pathname === '/login'
                  ? 'bg-primary/15 text-primary'
                  : 'text-muted-foreground hover:text-foreground hover:bg-secondary/60'
              )}
            >
              <LogIn className="w-5 h-5 shrink-0" />
              {!collapsed && <span>Войти</span>}
            </Link>
            <Link
              to="/register"
              className={cn(
                'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-200',
                location.pathname === '/register'
                  ? 'bg-primary/15 text-primary'
                  : 'text-muted-foreground hover:text-foreground hover:bg-secondary/60'
              )}
            >
              <UserPlus className="w-5 h-5 shrink-0" />
              {!collapsed && <span>Регистрация</span>}
            </Link>
          </>
        )}

        {isSeller && (
          <>
            {!collapsed && (
              <p className="px-3 pt-4 pb-1 text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                Продавец
              </p>
            )}
            {collapsed && <Separator className="my-2" />}
            {sellerItems.map((item) => (
              <NavLink
                key={item.path}
                item={item}
                chatBadge={item.showChatBadge ? chatUnreadTotal : 0}
              />
            ))}
          </>
        )}

        {isAdmin && (
          <>
            {!collapsed && (
              <p className="px-3 pt-4 pb-1 text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                Админ
              </p>
            )}
            {collapsed && <Separator className="my-2" />}
            {adminItems.map((item) => (
              <NavLink key={item.path} item={item} />
            ))}
          </>
        )}
      </nav>

      {currentUser && (
        <div className="p-3 border-t border-sidebar-border">
          <Link
            to="/profile"
            className={cn(
              'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm text-muted-foreground hover:text-foreground hover:bg-secondary/60 transition-colors'
            )}
          >
            <div className="w-8 h-8 rounded-full bg-primary/20 flex items-center justify-center shrink-0">
              <User className="w-4 h-4 text-primary" />
            </div>
            {!collapsed && (
              <div className="truncate">
                <p className="font-medium text-foreground text-sm truncate">
                  {currentUser.username || currentUser.email}
                </p>
                <p className="text-xs text-muted-foreground truncate">{currentUser.role}</p>
              </div>
            )}
          </Link>
          <Button
            variant="ghost"
            size="sm"
            className="w-full justify-start gap-3 px-3 text-muted-foreground hover:text-destructive mt-1"
            onClick={logout}
          >
            <LogOut className="w-4 h-4 shrink-0" />
            {!collapsed && 'Выйти'}
          </Button>
        </div>
      )}
    </aside>
  );
}
