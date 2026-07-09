<script setup lang="ts">
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import {
  getInviteConfig,
  saveInviteConfig,
  type InviteConfig,
} from '@/api/modules/invite'

const loading = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<InviteConfig>({
  enabled: '1',
  rewardPpd: 3,
  ruleText: '好友通过你的邀请码注册后，自动到账 3 拍拍豆。',
  shareTitle: '好友邀请你加入爱去拍，找摄影师、找模特更方便',
  landingTitle: '我正在使用爱去拍，找摄影师、找模特特别方便，推荐你也来体验。',
})

const rules: FormRules<InviteConfig> = {
  enabled: [{ required: true, message: '请选择启用状态', trigger: 'change' }],
  rewardPpd: [{ required: true, message: '请输入奖励拍拍豆', trigger: 'blur' }],
  ruleText: [{ required: true, message: '请输入规则说明', trigger: 'blur' }],
  shareTitle: [{ required: true, message: '请输入分享标题', trigger: 'blur' }],
  landingTitle: [{ required: true, message: '请输入落地页主文案', trigger: 'blur' }],
}

const statusText = computed(() => (form.enabled === '1' ? '当前启用' : '当前停用'))

async function fetchConfig() {
  loading.value = true
  try {
    const res = await getInviteConfig()
    Object.assign(form, res.data)
  } finally {
    loading.value = false
  }
}

async function submit() {
  await formRef.value?.validate()
  saving.value = true
  try {
    const res = await saveInviteConfig({ ...form })
    Object.assign(form, res.data)
    ElMessage.success('邀请配置已保存')
  } finally {
    saving.value = false
  }
}

onMounted(fetchConfig)
</script>

<template>
  <div class="invite-config" v-loading="loading">
    <div class="config-panel">
      <div class="panel-main">
        <div class="panel-title">
          <h2>邀请配置</h2>
          <el-tag :type="form.enabled === '1' ? 'success' : 'info'">{{ statusText }}</el-tag>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
          <el-form-item label="邀请功能" prop="enabled">
            <el-switch
              v-model="form.enabled"
              active-value="1"
              inactive-value="0"
              active-text="启用"
              inactive-text="停用"
            />
          </el-form-item>
          <el-form-item label="奖励拍拍豆" prop="rewardPpd">
            <el-input-number v-model="form.rewardPpd" :min="0" :max="999" controls-position="right" />
          </el-form-item>
          <el-form-item label="规则说明" prop="ruleText">
            <el-input
              v-model="form.ruleText"
              type="textarea"
              :rows="3"
              maxlength="300"
              show-word-limit
              placeholder="用于小程序我的邀请页展示"
            />
          </el-form-item>
          <el-form-item label="分享标题" prop="shareTitle">
            <el-input
              v-model="form.shareTitle"
              maxlength="120"
              show-word-limit
              placeholder="微信好友看到的小程序分享标题"
            />
          </el-form-item>
          <el-form-item label="落地页主文案" prop="landingTitle">
            <el-input
              v-model="form.landingTitle"
              type="textarea"
              :rows="3"
              maxlength="160"
              show-word-limit
              placeholder="被邀请人打开分享后的邀请落地页文案"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="submit">保存配置</el-button>
            <el-button @click="fetchConfig">刷新</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="preview-panel">
        <div class="preview-eyebrow">小程序展示预览</div>
        <div class="preview-title">{{ form.landingTitle }}</div>
        <div class="reward-box">
          <span class="reward-number">{{ form.rewardPpd }}</span>
          <span>{{ form.rewardUnit || '拍拍豆' }}</span>
        </div>
        <p>{{ form.ruleText }}</p>
        <div class="preview-share">{{ form.shareTitle }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.invite-config {
  min-height: 360px;
}

.config-panel {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: $spacing-lg;
  align-items: flex-start;
}

.panel-main,
.preview-panel {
  padding: $spacing-lg;
  background: #fff;
  border: 1px solid $border-light;
  border-radius: $radius-base;
}

.panel-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-lg;

  h2 {
    margin: 0;
    color: $text-primary;
    font-size: 18px;
    font-weight: 600;
  }
}

.preview-panel {
  background: linear-gradient(180deg, #f0fff6 0%, #fff 64%);
}

.preview-eyebrow {
  color: $color-primary;
  font-size: 13px;
  font-weight: 600;
}

.preview-title {
  margin-top: $spacing-sm;
  color: $text-primary;
  font-size: 18px;
  font-weight: 600;
  line-height: 1.5;
}

.reward-box {
  display: flex;
  align-items: baseline;
  gap: $spacing-xs;
  margin: $spacing-lg 0 $spacing-md;
  color: #d97706;
}

.reward-number {
  font-size: 40px;
  font-weight: 700;
}

.preview-panel p {
  margin: 0;
  color: $text-secondary;
  line-height: 1.7;
}

.preview-share {
  margin-top: $spacing-lg;
  padding: $spacing-sm $spacing-md;
  color: $text-regular;
  background: #fff;
  border: 1px dashed $color-primary;
  border-radius: $radius-base;
}

@media (max-width: 1080px) {
  .config-panel {
    grid-template-columns: 1fr;
  }
}
</style>
