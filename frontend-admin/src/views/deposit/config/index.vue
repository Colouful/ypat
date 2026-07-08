<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getDepositConfig, saveDepositConfig } from '@/api/modules/deposit'

const loading = ref(false)
const saving = ref(false)
const form = reactive({
  id: 1,
  enabled: '1',
  amountFen: 19900,
  testEnabled: '1',
  testAmountFen: 1,
  refundWaitDays: 90,
  earlyRefundFeeRate: 15,
  agreementSummary: '',
})

function yuanToFen(value: number | undefined) {
  return Math.round((value || 0) * 100)
}

function fenToYuan(value: number | undefined) {
  return Number(((value || 0) / 100).toFixed(2))
}

const amountYuan = ref(199)
const testAmountYuan = ref(0.01)

async function loadConfig() {
  loading.value = true
  try {
    const res = await getDepositConfig()
    Object.assign(form, res.data)
    amountYuan.value = fenToYuan(res.data.amountFen)
    testAmountYuan.value = fenToYuan(res.data.testAmountFen)
  } finally {
    loading.value = false
  }
}

async function submit() {
  saving.value = true
  try {
    form.amountFen = yuanToFen(amountYuan.value)
    form.testAmountFen = yuanToFen(testAmountYuan.value)
    await saveDepositConfig(form)
    ElMessage.success('保存成功')
    await loadConfig()
  } finally {
    saving.value = false
  }
}

onMounted(loadConfig)
</script>

<template>
  <div v-loading="loading" class="config-page">
    <el-form :model="form" label-width="140px" class="config-form">
      <el-form-item label="服务状态">
        <el-switch v-model="form.enabled" active-value="1" inactive-value="0" active-text="启用" inactive-text="停用"/>
      </el-form-item>
      <el-form-item label="正式保证金">
        <el-input-number v-model="amountYuan" :min="0.01" :precision="2" :step="1"/>
        <span class="hint">元</span>
      </el-form-item>
      <el-form-item label="测试金额开关">
        <el-switch v-model="form.testEnabled" active-value="1" inactive-value="0" active-text="启用" inactive-text="关闭"/>
      </el-form-item>
      <el-form-item label="测试保证金">
        <el-input-number v-model="testAmountYuan" :min="0.01" :precision="2" :step="0.01"/>
        <span class="hint">元</span>
      </el-form-item>
      <el-form-item label="退款等待天数">
        <el-input-number v-model="form.refundWaitDays" :min="1" :step="1"/>
      </el-form-item>
      <el-form-item label="提前退款费率">
        <el-input-number v-model="form.earlyRefundFeeRate" :min="0" :max="100" :step="1"/>
        <span class="hint">%</span>
      </el-form-item>
      <el-form-item label="协议摘要">
        <el-input v-model="form.agreementSummary" type="textarea" :rows="4" maxlength="500" show-word-limit/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="submit">保存配置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped lang="scss">
.config-page {
  padding: $spacing-lg;
  background: #fff;
}
.config-form {
  max-width: 720px;
}
.hint {
  margin-left: 10px;
  color: #909399;
}
</style>
