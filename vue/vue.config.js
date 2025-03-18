const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8080,
    proxy: {
      '/chat': {
        target: 'http://localhost:9090',
        changeOrigin: true
      }
    }
  },
  chainWebpack: config =>{
    config.plugin('html')
        .tap(args => {
          args[0].title = "Gym Panel";
          return args;
        })
  },
  publicPath: '/'
})
