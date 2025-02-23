<template>
  <div class="not-found-container">
    <div class="not-found-card">
      <h1 class="error-code">404</h1>
      <p class="error-message">Page not found</p>
      <router-link class="inline-block px-6 py-3 bg-blue-500 text-white rounded-md transition-transform duration-300 hover:scale-110 hover:bg-blue-600" to="/">Back to home page</router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: "NotFound",
  data() {
    return {}
  },
  created() {},
  methods: {}
}
</script>

<style scoped>
.not-found-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #ece9e6, #ffffff);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.not-found-card {
  background: #fff;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  text-align: center;
  max-width: 400px;
  width: 100%;
}

.error-code {
  font-size: 72px;
  margin: 0;
  color: #333;
  font-weight: bold;
}

.error-message {
  font-size: 24px;
  margin: 20px 0;
  color: #666;
}

.back-home-button:hover {
  background-color: #66b1ff;
}
</style>
