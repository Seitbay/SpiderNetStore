/**
 * Клиент REST API SpiderNet (Spring Boot). Базовый префикс /api задаётся прокси CRA.
 */
const API_BASE = '/api';

async function parseError(res) {
  const text = await res.text();
  if (!text) return { message: res.statusText };
  try {
    const json = JSON.parse(text);
    return {
      message: json.message || json.error || res.statusText,
      body: json,
    };
  } catch {
    return { message: text || res.statusText };
  }
}

export async function request(path, options = {}) {
  const { headers: optHeaders = {}, body, ...rest } = options;
  const headers = { ...optHeaders };
  let finalBody = body;
  if (body !== undefined && body !== null) {
    if (typeof body === 'object' && !(body instanceof FormData) && !(body instanceof Blob)) {
      finalBody = JSON.stringify(body);
      if (!headers['Content-Type']) {
        headers['Content-Type'] = 'application/json';
      }
    }
  }

  const res = await fetch(`${API_BASE}${path}`, {
    credentials: 'include',
    headers,
    body: finalBody,
    ...rest,
  });

  if (!res.ok) {
    const err = await parseError(res);
    const e = new Error(err.message || `HTTP ${res.status}`);
    e.status = res.status;
    e.body = err.body;
    throw e;
  }

  if (res.status === 204) return null;
  const ct = res.headers.get('content-type');
  if (ct && ct.includes('application/json')) {
    return res.json();
  }
  return res.text();
}

export function multipartRequest(path, formData, method = 'POST') {
  return fetch(`${API_BASE}${path}`, {
    method,
    credentials: 'include',
    body: formData,
  }).then(async (res) => {
    if (!res.ok) {
      const err = await parseError(res);
      throw new Error(err.message || `HTTP ${res.status}`);
    }
    return res.json();
  });
}

/** Auth */
export const auth = {
  register: (data) => request('/auth/register', { method: 'POST', body: data }),
  login: (data) => request('/auth/login', { method: 'POST', body: data }),
  logout: () => request('/auth/logout', { method: 'POST' }),
};

/** Текущий пользователь: /api/users/... */
export const user = {
  me: () => request('/users/me'),
  updateMe: (data) =>
    request('/users/me', { method: 'PUT', body: data }),
  changePassword: (data) =>
    request('/users/me/password', { method: 'PATCH', body: data }),
  balance: () => request('/users/me/balance'),
  getPublic: (id) => request(`/users/${id}`),
  /** Непрочитанные входящие по заказам: { totalUnread, byOrderId } */
  chatUnreadSummary: () => request('/users/me/chat/unread'),
};

/** Заявка продавца + админ */
export const sellerApp = {
  apply: () => request('/users/me/seller-application', { method: 'POST' }),
  myStatus: () => request('/users/me/seller-application'),
  list: (status = 'PENDING') =>
    request(`/admin/seller-applications?status=${encodeURIComponent(status)}`),
  approve: (id) =>
    request(`/admin/seller-applications/${id}/approve`, { method: 'POST' }),
  reject: (id, comment) => {
    const q = comment != null ? `?comment=${encodeURIComponent(comment)}` : '';
    return request(`/admin/seller-applications/${id}/reject${q}`, {
      method: 'POST',
    });
  },
};

/** Категории товаров (справочник для формы продавца) */
export const categories = {
  list: () => request('/categories'),
};

/** Товары */
export const products = {
  list: (params = {}) => {
    const q = new URLSearchParams();
    if (params.q != null) q.set('q', params.q);
    if (params.categoryId != null) q.set('categoryId', String(params.categoryId));
    q.set('page', String(params.page ?? 0));
    q.set('size', String(params.size ?? 20));
    return request(`/products?${q.toString()}`);
  },
  get: (id) => request(`/products/${id}`),
  myProducts: (page = 0, size = 20) =>
    request(`/products/my?page=${page}&size=${size}`),
  getManage: (id) => request(`/products/${id}/manage`),
  create: (data) =>
    request('/products', { method: 'POST', body: data }),
  createWithFiles: (formData) =>
    multipartRequest('/products/with-files', formData),
  update: (id, data) =>
    request(`/products/${id}`, { method: 'PUT', body: data }),
  /** Обложка товара (JPEG/PNG/WebP/GIF, до 5 МБ). */
  uploadCover: (id, formData) =>
    multipartRequest(`/products/${id}/cover`, formData),
  delete: (id) => request(`/products/${id}`, { method: 'DELETE' }),
  addStock: (id, data) =>
    request(`/products/${id}/stock`, {
      method: 'POST',
      body: data,
    }),
  addStockFiles: (id, formData) =>
    multipartRequest(`/products/${id}/stock/files`, formData),
  deleteStock: (productId, stockId) =>
    request(`/products/${productId}/stock/${stockId}`, { method: 'DELETE' }),
};

/** Заказы */
export const orders = {
  create: (data) =>
    request('/orders', { method: 'POST', body: data }),
  myPurchases: (page = 0, size = 20) =>
    request(`/orders/my?page=${page}&size=${size}`),
  mySales: (page = 0, size = 20) =>
    request(`/orders/sales?page=${page}&size=${size}`),
  get: (id) => request(`/orders/${id}`),
  cancel: (id) =>
    request(`/orders/${id}/cancel`, { method: 'POST' }),
  complete: (id) =>
    request(`/orders/${id}/complete`, { method: 'POST' }),
  refund: (id) =>
    request(`/orders/${id}/refund`, { method: 'POST' }),
  openDispute: (orderId, reason) =>
    request(`/orders/${orderId}/dispute`, {
      method: 'POST',
      body: { reason },
    }),
  getDisputeForOrder: (orderId) =>
    request(`/orders/${orderId}/dispute`),
};

/** Платежи */
export const payments = {
  balance: () => request('/payments/balance'),
  history: (page = 0, size = 20) =>
    request(`/payments/my?page=${page}&size=${size}`),
  deposit: (data) =>
    request('/payments/deposit', {
      method: 'POST',
      body: data,
    }),
  payout: (data) =>
    request('/payments/payout', {
      method: 'POST',
      body: data,
    }),
};

/** Споры (часть сценариев — через заказ) */
export const disputes = {
  get: (disputeId) => request(`/disputes/${disputeId}`),
  respond: (disputeId, data) =>
    request(`/disputes/${disputeId}/respond`, {
      method: 'POST',
      body: data,
    }),
  escalate: (disputeId) =>
    request(`/disputes/${disputeId}/escalate`, { method: 'POST' }),
  resolve: (disputeId, data) =>
    request(`/admin/disputes/${disputeId}/resolve`, {
      method: 'POST',
      body: data,
    }),
};

/** Чат по заказу (страница: items, hasOlder, oldestMessageId, newestMessageId) */
export const chat = {
  messages: (orderId, params = {}) => {
    const q = new URLSearchParams();
    if (params.beforeId != null) q.set('beforeId', String(params.beforeId));
    if (params.afterId != null) q.set('afterId', String(params.afterId));
    if (params.limit != null) q.set('limit', String(params.limit));
    const qs = q.toString();
    return request(
      `/orders/${orderId}/chat/messages${qs ? `?${qs}` : ''}`
    );
  },
  send: (orderId, data) =>
    request(`/orders/${orderId}/chat/messages`, {
      method: 'POST',
      body: data,
    }),
  markRead: (orderId) =>
    request(`/orders/${orderId}/chat/read`, { method: 'POST' }),
  deleteMessage: (orderId, messageId) =>
    request(`/orders/${orderId}/chat/messages/${messageId}`, { method: 'DELETE' }),
};

/** Отзывы */
export const reviews = {
  create: (data) =>
    request('/reviews/review', {
      method: 'POST',
      body: data,
    }),
  forProduct: (productId) =>
    request(`/reviews/product/${productId}`),
  canLeave: (productId) =>
    request(`/reviews/product/${productId}/can-leave`),
};
