import Vue from 'vue'
import App from './App.vue'
import router from './router'
import ElementUI from 'element-ui'
import locale from 'element-ui/lib/locale/lang/en'  // Add English language package
import 'element-ui/lib/theme-chalk/index.css'
import '@/assets/css/global.css'
import '@/assets/css/theme/index.css'
import 'font-awesome/css/font-awesome.min.css'
import axios from "axios";
import request from "@/utils/request";
import './assets/tailwind.css'  // Tailwind CSS

Vue.config.productionTip = false

Vue.prototype.$request = request
Vue.prototype.$baseUrl = process.env.VUE_APP_BASEURL

Vue.use(ElementUI, { locale, size: "small" })

new Vue({
    router,
    render: h => h(App)
}).$mount('#app')
