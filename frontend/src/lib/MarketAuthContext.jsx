/**
 * Совместимость: раньше здесь был отдельный React Context без провайдера в App.
 * Фактическое приложение использует {@link AuthProvider} из `./AuthContext`.
 * Импорты из `@/lib/MarketAuthContext` и `@/lib/AuthContext` ведут на один контекст.
 */
export { AuthProvider as MarketAuthProvider, useMarketAuth } from './AuthContext';
