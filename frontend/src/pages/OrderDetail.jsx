import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { orders as ordersApi, chat as chatApi, disputes as disputesApi } from '@/lib/api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Separator } from '@/components/ui/separator';
import {
  ArrowLeft,
  Send,
  AlertTriangle,
  CheckCircle,
  XCircle,
  MessageSquare,
  Check,
  CheckCheck,
  Trash2,
  ChevronUp,
} from 'lucide-react';
import { toast } from 'sonner';
import LoadingSpinner from '@/components/shared/LoadingSpinner';
import StatusBadge from '@/components/shared/StatusBadge';
import { format } from 'date-fns';
import { useMarketAuth } from '@/lib/MarketAuthContext';

const DELETE_GRACE_MS = 15 * 60 * 1000;

function mergeMessagesByIdAsc(older, current) {
  const map = new Map();
  for (const m of [...older, ...current]) {
    if (m?.id != null) map.set(m.id, m);
  }
  return Array.from(map.values()).sort((a, b) => a.id - b.id);
}

function canSoftDelete(msg, currentUserId) {
  if (!msg?.sentAt || msg.deleted) return false;
  if (msg.senderId !== currentUserId) return false;
  const t = new Date(msg.sentAt).getTime();
  if (Number.isNaN(t)) return false;
  return Date.now() - t <= DELETE_GRACE_MS;
}

export default function OrderDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { currentUser } = useMarketAuth();
  const [order, setOrder] = useState(null);
  const [messages, setMessages] = useState([]);
  const [chatMeta, setChatMeta] = useState({
    hasOlder: false,
    oldestMessageId: null,
    newestMessageId: null,
  });
  const [loadingOlder, setLoadingOlder] = useState(false);
  const [newMessage, setNewMessage] = useState('');
  const [disputeReason, setDisputeReason] = useState('');
  const [showDispute, setShowDispute] = useState(false);
  const [loading, setLoading] = useState(true);
  const [peerTyping, setPeerTyping] = useState(false);
  const chatEndRef = useRef(null);
  const stompRef = useRef(null);
  const scrollRef = useRef(null);
  const typingSentRef = useRef(false);
  const typingIdleRef = useRef(null);
  const peerTypingClearRef = useRef(null);
  const newestMessageIdRef = useRef(0);

  const normalizePage = useCallback((page) => {
    if (!page) return [];
    if (Array.isArray(page)) return page;
    if (Array.isArray(page.items)) return page.items;
    if (Array.isArray(page.content)) return page.content;
    return [];
  }, []);

  const publishTyping = useCallback(
    (typing) => {
      const c = stompRef.current;
      if (!c?.connected) return;
      try {
        c.publish({
          destination: `/app/orders/${id}/chat/typing`,
          body: JSON.stringify({ typing }),
          headers: { 'content-type': 'application/json' },
        });
      } catch (e) {
        console.warn('typing publish', e);
      }
    },
    [id]
  );

  useEffect(() => {
    const load = async () => {
      const o = await ordersApi.get(id);
      setOrder(o);
      const page = await chatApi.messages(id).catch(() => ({ items: [] }));
      setMessages(normalizePage(page));
      setChatMeta({
        hasOlder: Boolean(page?.hasOlder),
        oldestMessageId: page?.oldestMessageId ?? null,
        newestMessageId: page?.newestMessageId ?? null,
      });
      await chatApi.markRead(id).catch(() => {});
      setLoading(false);
    };
    load();
  }, [id, normalizePage]);

  useEffect(() => {
    newestMessageIdRef.current = messages.reduce((acc, m) => Math.max(acc, m?.id ?? 0), 0);
  }, [messages]);

  /** Подтягивание новых сообщений с сервера (дубли с WebSocket отсекаются по id). */
  useEffect(() => {
    if (!id || loading) return undefined;
    const tick = async () => {
      const after = newestMessageIdRef.current ?? 0;
      try {
        const page = await chatApi.messages(id, { afterId: after, limit: 50 });
        const incoming = normalizePage(page);
        if (!incoming.length) return;
        setMessages((prev) => mergeMessagesByIdAsc(prev, incoming));
        setChatMeta((m) => ({
          ...m,
          newestMessageId: page?.newestMessageId ?? m.newestMessageId,
        }));
      } catch {
        /* сеть / 401 при истёкшей сессии */
      }
    };
    void tick();
    const interval = setInterval(tick, 4000);
    return () => clearInterval(interval);
  }, [id, loading, normalizePage]);

  const loadOlder = async () => {
    if (!chatMeta.hasOlder || loadingOlder || chatMeta.oldestMessageId == null) return;
    const el = scrollRef.current;
    const prevH = el?.scrollHeight ?? 0;
    const prevTop = el?.scrollTop ?? 0;
    setLoadingOlder(true);
    try {
      const page = await chatApi.messages(id, {
        beforeId: chatMeta.oldestMessageId,
        limit: 50,
      });
      const older = normalizePage(page);
      setMessages((prev) => mergeMessagesByIdAsc(older, prev));
      setChatMeta((m) => ({
        hasOlder: Boolean(page?.hasOlder),
        oldestMessageId: page?.oldestMessageId ?? m.oldestMessageId,
        newestMessageId: m.newestMessageId,
      }));
      requestAnimationFrame(() => {
        if (el) {
          el.scrollTop = el.scrollHeight - prevH + prevTop;
        }
      });
    } catch (e) {
      toast.error(e.message || 'Не удалось загрузить историю');
    } finally {
      setLoadingOlder(false);
    }
  };

  useEffect(() => {
    if (!id || loading) return undefined;

    const client = new Client({
      reconnectDelay: 4000,
      webSocketFactory: () => new SockJS(`${window.location.protocol}//${window.location.host}/ws`),
      onStompError: (f) => {
        console.warn('STOMP error', f.headers?.message);
      },
      onWebSocketClose: () => {},
    });

    client.onConnect = () => {
      client.subscribe(`/topic/orders/${id}/chat`, (frame) => {
        try {
          const env = JSON.parse(frame.body);
          if (env.type === 'NEW_MESSAGE' && env.message) {
            const msg = env.message;
            setMessages((prev) => {
              if (prev.some((p) => p.id === msg.id)) return prev;
              return [...prev, msg];
            });
          } else if (env.type === 'READ_RECEIPT' && env.read) {
            const { readerId, messageIds, readAt } = env.read;
            setMessages((prev) =>
              prev.map((m) => {
                if (!messageIds?.includes(m.id)) return m;
                const mine = m.senderId === currentUser?.id;
                if (readerId === currentUser?.id) {
                  if (!mine) return { ...m, read: true, readAt: readAt ?? m.readAt };
                  return m;
                }
                if (mine) return { ...m, read: true, readAt: readAt ?? m.readAt };
                return m;
              })
            );
          } else if (env.type === 'TYPING' && env.typing) {
            const { userId, typing } = env.typing;
            if (userId === currentUser?.id) return;
            if (peerTypingClearRef.current) clearTimeout(peerTypingClearRef.current);
            setPeerTyping(Boolean(typing));
            peerTypingClearRef.current = setTimeout(() => setPeerTyping(false), 3000);
          } else if (env.type === 'MESSAGE_DELETED' && env.message) {
            const updated = env.message;
            setMessages((prev) => prev.map((m) => (m.id === updated.id ? { ...m, ...updated } : m)));
          }
        } catch (err) {
          console.warn('chat ws parse', err);
        }
      });
    };

    client.activate();
    stompRef.current = client;
    return () => {
      stompRef.current = null;
      if (typingIdleRef.current) clearTimeout(typingIdleRef.current);
      if (peerTypingClearRef.current) clearTimeout(peerTypingClearRef.current);
      client.deactivate();
    };
  }, [id, loading, currentUser?.id]);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleMessageInputChange = (e) => {
    const v = e.target.value;
    setNewMessage(v);
    if (!typingSentRef.current) {
      typingSentRef.current = true;
      publishTyping(true);
    }
    if (typingIdleRef.current) clearTimeout(typingIdleRef.current);
    typingIdleRef.current = setTimeout(() => {
      typingSentRef.current = false;
      publishTyping(false);
    }, 2000);
  };

  const sendMessage = async (e) => {
    e.preventDefault();
    if (!newMessage.trim()) return;
    if (typingIdleRef.current) clearTimeout(typingIdleRef.current);
    typingSentRef.current = false;
    publishTyping(false);
    try {
      const dto = await chatApi.send(id, { text: newMessage.trim() });
      if (dto?.id != null) {
        setMessages((prev) => {
          if (prev.some((p) => p.id === dto.id)) return prev;
          return [...prev, dto];
        });
        setChatMeta((m) => ({
          ...m,
          newestMessageId: Math.max(m.newestMessageId ?? 0, dto.id),
        }));
      }
      setNewMessage('');
    } catch (err) {
      toast.error(err.message || 'Не удалось отправить');
    }
  };

  const handleDeleteMessage = async (messageId) => {
    try {
      const dto = await chatApi.deleteMessage(id, messageId);
      setMessages((prev) => prev.map((m) => (m.id === dto.id ? { ...m, ...dto } : m)));
    } catch (err) {
      toast.error(err.message || 'Не удалось удалить');
    }
  };

  const handleComplete = async () => {
    await ordersApi.complete(id);
    toast.success('Заказ подтверждён');
    const o = await ordersApi.get(id);
    setOrder(o);
  };

  const handleCancel = async () => {
    await ordersApi.cancel(id);
    toast.success('Заказ отменён');
    const o = await ordersApi.get(id);
    setOrder(o);
  };

  const handleDispute = async (e) => {
    e.preventDefault();
    await disputesApi.create({ orderId: parseInt(id, 10), reason: disputeReason });
    toast.success('Спор открыт');
    setShowDispute(false);
  };

  if (loading) return <LoadingSpinner />;
  if (!order) return <p className="text-center py-16 text-muted-foreground">Заказ не найден</p>;

  const isBuyer = currentUser?.id === order.buyerId || currentUser?.id === order.buyer?.id;

  return (
    <div>
      <Button variant="ghost" size="sm" className="mb-4 text-muted-foreground" onClick={() => navigate(-1)}>
        <ArrowLeft className="w-4 h-4 mr-1" /> Назад
      </Button>

      <div className="grid lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          {/* Order Info */}
          <Card className="bg-card border-border">
            <CardHeader className="flex flex-row items-center justify-between">
              <CardTitle>Заказ #{order.id}</CardTitle>
              <StatusBadge status={order.status} />
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <p className="text-muted-foreground">Товар</p>
                  <p className="font-medium">{order.productName || order.product?.name}</p>
                </div>
                <div>
                  <p className="text-muted-foreground">Сумма</p>
                  <p className="font-medium">{order.price ?? order.amount} ₽</p>
                </div>
                <div>
                  <p className="text-muted-foreground">Дата</p>
                  <p className="font-medium">{order.createdAt ? format(new Date(order.createdAt), 'dd.MM.yyyy HH:mm') : '—'}</p>
                </div>
                <div>
                  <p className="text-muted-foreground">Продавец</p>
                  <p className="font-medium">{order.sellerUsername || order.seller?.username || '—'}</p>
                </div>
              </div>

              {order.deliveryData && (
                <>
                  <Separator />
                  <div>
                    <p className="text-sm text-muted-foreground mb-1">Данные товара</p>
                    <pre className="text-sm bg-secondary p-3 rounded-lg whitespace-pre-wrap break-all">{typeof order.deliveryData === 'string' ? order.deliveryData : JSON.stringify(order.deliveryData, null, 2)}</pre>
                  </div>
                </>
              )}

              {isBuyer && order.status === 'PENDING' && (
                <div className="flex gap-3 pt-2">
                  <Button onClick={handleComplete} className="flex-1">
                    <CheckCircle className="w-4 h-4 mr-2" /> Подтвердить получение
                  </Button>
                  <Button variant="outline" onClick={handleCancel} className="flex-1">
                    <XCircle className="w-4 h-4 mr-2" /> Отменить
                  </Button>
                </div>
              )}

              {isBuyer && order.status === 'COMPLETED' && (
                <Button variant="outline" size="sm" onClick={() => setShowDispute(!showDispute)}>
                  <AlertTriangle className="w-4 h-4 mr-2" /> Открыть спор
                </Button>
              )}

              {showDispute && (
                <form onSubmit={handleDispute} className="space-y-3 p-4 bg-secondary rounded-lg">
                  <Textarea
                    placeholder="Опишите проблему..."
                    value={disputeReason}
                    onChange={(e) => setDisputeReason(e.target.value)}
                    required
                  />
                  <Button type="submit" variant="destructive" size="sm">Открыть спор</Button>
                </form>
              )}
            </CardContent>
          </Card>

          {/* Chat */}
          <Card className="bg-card border-border">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <MessageSquare className="w-5 h-5" /> Чат
              </CardTitle>
            </CardHeader>
            <CardContent>
              {chatMeta.hasOlder && (
                <div className="mb-2 flex justify-center">
                  <Button type="button" variant="outline" size="sm" disabled={loadingOlder} onClick={loadOlder}>
                    <ChevronUp className="w-4 h-4 mr-1" />
                    {loadingOlder ? 'Загрузка…' : 'Раньше'}
                  </Button>
                </div>
              )}
              <div ref={scrollRef} className="h-64 overflow-y-auto space-y-3 mb-2 p-3 bg-secondary rounded-lg">
                {messages.length === 0 ? (
                  <p className="text-center text-sm text-muted-foreground py-8">Нет сообщений</p>
                ) : (
                  messages.map((msg, i) => {
                    const isMe = msg.senderId === currentUser?.id || msg.senderUsername === currentUser?.username;
                    const at = msg.sentAt || msg.createdAt;
                    const body = msg.text ?? msg.content ?? msg.message;
                    const showDelete = isMe && canSoftDelete(msg, currentUser?.id);
                    return (
                      <div key={msg.id || i} className={`flex ${isMe ? 'justify-end' : 'justify-start'}`}>
                        <div
                          className={`relative max-w-[75%] px-3 py-2 rounded-xl text-sm ${
                            isMe ? 'bg-primary text-primary-foreground pr-9' : 'bg-card border border-border'
                          }`}
                        >
                          {showDelete && (
                            <Button
                              type="button"
                              variant="ghost"
                              size="icon"
                              className="absolute top-1 right-1 h-7 w-7 text-primary-foreground/80 hover:text-primary-foreground hover:bg-primary-foreground/10"
                              title="Удалить"
                              onClick={() => handleDeleteMessage(msg.id)}
                            >
                              <Trash2 className="w-3.5 h-3.5" />
                            </Button>
                          )}
                          <p
                            className={
                              msg.deleted ? `italic ${isMe ? 'opacity-80' : 'text-muted-foreground'}` : ''
                            }
                          >
                            {body}
                          </p>
                          <div
                            className={`text-xs mt-1 flex items-center gap-1 justify-end ${
                              isMe ? 'text-primary-foreground/70' : 'text-muted-foreground'
                            }`}
                          >
                            <span>{at ? format(new Date(at), 'HH:mm') : ''}</span>
                            {isMe && (
                              msg.read ? (
                                <CheckCheck className="w-3.5 h-3.5 shrink-0" aria-label="Прочитано" />
                              ) : (
                                <Check className="w-3.5 h-3.5 shrink-0 opacity-60" aria-label="Доставлено" />
                              )
                            )}
                          </div>
                        </div>
                      </div>
                    );
                  })
                )}
                <div ref={chatEndRef} />
              </div>
              {peerTyping && (
                <p className="text-xs text-muted-foreground mb-2 px-1">Собеседник печатает…</p>
              )}
              <form onSubmit={sendMessage} className="flex gap-2">
                <Input
                  className="bg-secondary"
                  placeholder="Сообщение..."
                  value={newMessage}
                  onChange={handleMessageInputChange}
                />
                <Button type="submit" size="icon">
                  <Send className="w-4 h-4" />
                </Button>
              </form>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
