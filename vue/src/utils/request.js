import axios from 'axios'
import router from "@/router";

// create a new axios object
const request = axios.create({
    // baseURL: process.env.VUE_APP_BASEURL,   // back end ip:port
    // timeout: 30000                          // 30s timeout

    baseURL: process.env.NODE_ENV === 'development' 
        ? process.env.VUE_APP_BASEURL  // development use config address
        : `http://${window.location.hostname}:9090`,  // production use current host name
    timeout: 5000
})

// request interceptor
// can customize request before sending
// e.g. add token, encrypt request parameters
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';        // set request header format
    let user = JSON.parse(localStorage.getItem("xm-user") || '{}')  // get cached user info
    config.headers['token'] = user.token  // set request header

    return config
}, error => {
    console.error('request error: ' + error) // for debug
    return Promise.reject(error)
});

// response interceptor
// can handle results after interface response
request.interceptors.response.use(
    response => {
        let res = response.data;

        // compatible with string data returned by server
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }
        if (res.code === '401') {
            router.push('/login')
        }
        return res;
    },
    error => {
        console.error('response error: ' + error) // for debug
        return Promise.reject(error)
    }
)


export default request