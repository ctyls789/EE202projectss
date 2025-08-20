<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span>登入</span>
        </div>
      </template>
      <el-form @submit.prevent="handleLogin">
        <el-form-item label="使用者名稱">
          <el-input v-model="username"></el-input>
        </el-form-item>
        <el-form-item label="密碼">
          <el-input type="password" v-model="password"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin">登入</el-button>
        </el-form-item>
      </el-form>
      <div v-if="message" class="error-message">{{ message }}</div>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/AuthStore';
import { ElMessage } from 'element-plus';

const username = ref('');
const password = ref('');
const message = ref('');
const authStore = useAuthStore();
const router = useRouter();

const handleLogin = async () => {
  message.value = '';
  try {
    await authStore.login({ username: username.value, password: password.value });
    router.push('/'); // Redirect to home or dashboard after successful login
  } catch (error: any) {
    message.value = (error.response && error.response.data && error.response.data.message) || error.message || error.toString();
    ElMessage.error(message.value);
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f0f2f5;
}

.login-card {
  width: 400px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-radius: 8px;
}

.card-header {
  text-align: center;
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
}

.error-message {
  color: red;
  text-align: center;
  margin-top: 10px;
}
</style>