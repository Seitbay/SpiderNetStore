const path = require('path');

module.exports = {
  webpack: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  // Иначе webpack-dev-server может получить allowedHosts: [''] и падать с:
  // "options.allowedHosts[0] should be a non-empty string"
  devServer: {
    allowedHosts: 'all',
  },
};
