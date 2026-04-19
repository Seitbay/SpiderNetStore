const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function setupProxy(app) {
  const target = process.env.REACT_APP_API_PROXY || 'http://localhost:8080';
  const longTimeout = Number(process.env.REACT_APP_PROXY_TIMEOUT_MS) || 180000;

  app.use(
    '/api',
    createProxyMiddleware({
      target,
      changeOrigin: true,
      /** Иначе при долгом первом запросе (JPA/БД) dev-server отдаёт 504 Gateway Timeout */
      proxyTimeout: longTimeout,
    })
  );
  app.use(
    '/ws',
    createProxyMiddleware({
      target,
      changeOrigin: true,
      ws: true,
      proxyTimeout: longTimeout,
    })
  );
};
