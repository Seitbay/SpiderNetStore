/**
 * Параметры окружения CRA (при необходимости задайте REACT_APP_* в .env).
 */
function env(name, fallback) {
  const v = process.env[name];
  return v != null && v !== '' ? v : fallback;
}

export const appParams = {
  apiBaseUrl: env('REACT_APP_API_BASE_URL', ''),
};
