<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { getUserDetail, type OauthQo } from '@/api/modules/user'
import { getPaymentOrders } from '@/api/modules/payment'
import type { PaymentOrder } from '@/api/types'
import { AuditFlag, getUserStatusInfo } from '@/constants/enums'

const props = defineProps<{
  visible: boolean
  user: OauthQo | null
  loading: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  audit: [flag: string]
}>()

const detailLoading = ref(false)
const detail = ref<OauthQo | null>(null)
const orderLoading = ref(false)
const orderLoadFailed = ref(false)
const orders = ref<PaymentOrder[]>([])

const businessNameMap: Record<string, string> = {
  DEPOSIT: '保证金',
  MEMBER: '会员',
  PPD: '拍拍豆',
  REALNAME: '实名认证',
}

const paymentStatusMap: Record<string, string> = {
  PENDING: '待支付',
  PAID: '已支付',
  CLOSED: '已关闭',
  REFUNDED: '已退款',
  FAILED: '支付失败',
}

function getUserId(user?: OauthQo | null): number | undefined {
  return user?.userid ?? user?.id
}

function getPhotoLabel(index: number): string {
  const labels = ['身份证正面', '身份证反面', '手持身份证']
  return labels[index] ?? `证件照片${index + 1}`
}

// 是否已审核（已审核状态隐藏审核按钮）
const isAudited = computed(() => {
  const status = detail.value?.status ?? props.user?.status
  return status === AuditFlag.PASS || status === AuditFlag.REJECT
})

/** 加载详情 */
async function loadDetail(): Promise<void> {
  if (!props.user) return

  detailLoading.value = true
  orderLoading.value = true
  orderLoadFailed.value = false
  orders.value = []
  detail.value = props.user

  const userId = getUserId(props.user)
  if (!userId) {
    detailLoading.value = false
    orderLoading.value = false
    return
  }

  const [detailResult, orderResult] = await Promise.allSettled([
    getUserDetail(userId),
    getPaymentOrders({ userId, page: 0, size: 10 }),
  ])
  if (!props.visible || getUserId(props.user) !== userId) {
    detailLoading.value = false
    orderLoading.value = false
    return
  }

  if (detailResult.status === 'fulfilled' && detailResult.value.data) {
    detail.value = detailResult.value.data
  }
  if (orderResult.status === 'fulfilled') {
    orders.value = orderResult.value.data.content || []
  } else {
    orderLoadFailed.value = true
  }
  detailLoading.value = false
  orderLoading.value = false
}

function businessText(value?: string): string {
  if (!value) return '-'
  const name = businessNameMap[value]
  return name ? `${name}(${value})` : value
}

function fenText(value?: number): string {
  return Number.isFinite(value) ? `¥${(Number(value) / 100).toFixed(2)}` : '-'
}

function paymentStatusText(value?: string): string {
  if (!value) return '-'
  return paymentStatusMap[value] ? `${paymentStatusMap[value]}(${value})` : value
}

/** 关闭弹窗 */
function handleClose(): void {
  emit('update:visible', false)
}

/** 审核操作 */
function handleAudit(flag: string): void {
  emit('audit', flag)
}

// 监听 visible 变化加载详情
watch(
  () => props.visible,
  (val) => {
    if (val) {
      loadDetail()
    }
  },
)
</script>

<template>
  <el-dialog
    :model-value="visible"
    title="实名审核详情"
    width="900px"
    class="user-audit-dialog"
    :close-on-click-modal="false"
    @update:model-value="handleClose"
  >
    <div v-loading="detailLoading" class="audit-dialog-content">
      <!-- 图片画廊 -->
      <div class="image-section">
        <h4 class="section-title">证件照片</h4>
        <div v-if="detail?.pics && detail.pics.length > 0" class="image-gallery">
          <div
            v-for="(pic, index) in detail.pics"
            :key="index"
            class="image-item"
            :aria-label="getPhotoLabel(index)"
          >
            <div class="image-label">{{ getPhotoLabel(index) }}</div>
            <el-image
              :src="pic"
              fit="cover"
              style="width: 120px; height: 120px"
              :preview-src-list="detail.pics"
              :initial-index="index"
              preview-teleported
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                  <span>加载失败</span>
                </div>
              </template>
              <template #placeholder>
                <div class="image-placeholder">
                  <el-icon class="is-loading"><Loading /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
        </div>
        <el-empty v-else description="暂无照片" :image-size="60" />
      </div>

      <!-- 详细信息 -->
      <div class="info-section">
        <h4 class="section-title">基本信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID">
            {{ getUserId(detail) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="姓名">
            {{ detail?.name || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="证件号码">
            {{ detail?.certcode || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag
              v-if="detail?.status"
              :type="getUserStatusInfo(detail.status).type"
              size="small"
            >
              {{ getUserStatusInfo(detail.status).name }}
            </el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="order-section">
        <h4 class="section-title">充值订单</h4>
        <el-alert
          v-if="orderLoadFailed"
          title="订单信息加载失败，不影响当前审核操作"
          type="warning"
          :closable="false"
          show-icon
        />
        <el-table
          v-else
          v-loading="orderLoading"
          :data="orders"
          size="small"
          border
          max-height="240"
          empty-text="暂无充值订单"
        >
          <el-table-column label="业务" min-width="160">
            <template #default="{ row }">{{ businessText(row.businessType) }}</template>
          </el-table-column>
          <el-table-column label="金额" width="110" align="right">
            <template #default="{ row }">{{ fenText(row.amountFen) }}</template>
          </el-table-column>
          <el-table-column label="状态" min-width="150">
            <template #default="{ row }">{{ paymentStatusText(row.status) }}</template>
          </el-table-column>
          <el-table-column prop="outTradeNo" label="商户单号" min-width="190" show-overflow-tooltip />
          <el-table-column prop="createdAt" label="创建时间" min-width="170" />
          <el-table-column prop="paidAt" label="支付时间" min-width="170">
            <template #default="{ row }">{{ row.paidAt || '-' }}</template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 底部按钮 -->
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <template v-if="!isAudited">
          <el-button
            type="danger"
            :loading="loading"
            @click="handleAudit(AuditFlag.REJECT)"
          >
            审核不通过
          </el-button>
          <el-button
            type="success"
            :loading="loading"
            @click="handleAudit(AuditFlag.PASS)"
          >
            审核通过
          </el-button>
        </template>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
.audit-dialog-content {
  max-height: 500px;
  overflow-y: auto;
}

.section-title {
  font-size: $font-size-base;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-base;
  padding-left: $spacing-sm;
  border-left: 3px solid $color-primary;
}

.image-section {
  margin-bottom: $spacing-xl;
}

.image-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-base;
}

.image-item {
  border-radius: $radius-sm;
  overflow: hidden;
  border: 1px solid $border-lighter;
  transition: all 0.2s;

  &:hover {
    border-color: $color-primary;
    box-shadow: $shadow-base;
  }
}

.image-label {
  width: 120px;
  padding: $spacing-xs;
  color: $text-regular;
  font-size: $font-size-sm;
  text-align: center;
  background-color: $bg-page;
  border-bottom: 1px solid $border-lighter;
}

.image-error,
.image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 120px;
  background-color: $bg-page;
  color: $text-secondary;
  font-size: $font-size-sm;
  gap: $spacing-xs;
}

.info-section {
  margin-bottom: $spacing-base;
}

.order-section {
  margin-top: $spacing-xl;
}

:global(.user-audit-dialog) {
  max-width: calc(100vw - 32px);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-sm;
}
</style>
