<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/modules/auth'
import { getCaptcha, type LoginParams } from '@/api/modules/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const captchaImg = ref('')

const loginForm = reactive<LoginParams>({
  mobile: '',
  password: '',
  captchaId: '',
  captchaCode: '',
})

const rules: FormRules = {
  mobile: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
}

async function refreshCaptcha(): Promise<void> {
  try {
    const res = await getCaptcha()
    captchaImg.value = res.data.img
    loginForm.captchaId = res.data.captchaId
  } catch {
    ElMessage.error('获取验证码失败')
  }
}

async function handleLogin(): Promise<void> {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await authStore.login(loginForm)
      ElMessage.success('登录成功')
      const redirect = (route.query.redirect as string) || '/dashboard'
      router.push(redirect)
    } catch {
      // 错误信息已在拦截器中处理
      refreshCaptcha()
    } finally {
      loading.value = false
    }
  })
}

onMounted(() => {
  refreshCaptcha()
})
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-left">
        <div class="brand">
          <h1 class="brand-title">YPAT</h1>
          <p class="brand-subtitle">摄影约拍撮合平台</p>
          <p class="brand-desc">管理后台系统</p>
        </div>
      </div>
      <div class="login-right">
        <h2 class="login-title">管理员登录</h2>
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="rules"
          size="large"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="mobile">
            <el-input
              v-model="loginForm.mobile"
              placeholder="请输入手机号"
              :prefix-icon="'User'"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="'Lock'"
              show-password
            />
          </el-form-item>
          <el-form-item prop="captchaCode">
            <div class="captcha-row">
              <el-input
                v-model="loginForm.captchaCode"
                placeholder="请输入验证码"
                :prefix-icon="'Picture'"
              />
              <div class="captcha-img" @click="refreshCaptcha">
                <img v-if="captchaImg" :src="captchaImg" alt="验证码" />
                <span v-else>点击获取</span>
              </div>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1a2a3a 0%, #2c3e50 50%, #3a5269 100%);
  position: relative;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background-image: radial-gradient(circle at 20% 50%, rgb(64 158 255 / 8%) 0%, transparent 50%),
      radial-gradient(circle at 80% 20%, rgb(103 194 58 / 6%) 0%, transparent 50%);
  }
}

.login-card {
  display: flex;
  width: 800px;
  height: 460px;
  border-radius: $radius-lg;
  overflow: hidden;
  box-shadow: 0 20px 60px rgb(0 0 0 / 30%);
  position: relative;
  z-index: 1;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #1d6fd1 0%, #337ecc 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.brand {
  text-align: center;
}

.brand-title {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: $spacing-sm;
  letter-spacing: 4px;
}

.brand-subtitle {
  font-size: $font-size-lg;
  opacity: 0.9;
  margin-bottom: $spacing-xs;
}

.brand-desc {
  font-size: $font-size-base;
  opacity: 0.7;
}

.login-right {
  flex: 1;
  background-color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 48px;
}

.login-title {
  text-align: center;
  margin-bottom: $spacing-xl;
  color: $text-primary;
  font-size: $font-size-xl;
  font-weight: 600;
}

.captcha-row {
  display: flex;
  gap: $spacing-sm;
  width: 100%;
}

.captcha-img {
  flex-shrink: 0;
  width: 120px;
  height: 40px;
  border: 1px solid $border-base;
  border-radius: $radius-sm;
  cursor: pointer;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: $bg-page;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.login-btn {
  width: 100%;
}

@media (width <= 768px) {
  .login-card {
    width: 90%;
    flex-direction: column;
    height: auto;
  }

  .login-left {
    display: none;
  }

  .login-right {
    padding: $spacing-xl;
  }
}
</style>
