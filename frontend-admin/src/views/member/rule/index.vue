<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import { getMemberBenefitConfigs, saveMemberBenefitConfig } from '@/api/modules/member'
import type {
  MemberBenefitConfig,
  MemberBenefitRule,
  PpdBenefitScene,
} from '@/api/types'

const sceneTabs: Array<{ value: PpdBenefitScene; label: string }> = [
  { value: 'SUBMIT_YPAT', label: '发布约拍' },
  { value: 'APPLY_YPAT', label: '发起约拍申请' },
  { value: 'VIEW_CONTACT', label: '查看联系方式' },
]
const levelNames: Record<string, string> = {
  BASIC: '基础会员',
  PLUS: '高级会员',
  PRO: '专业会员',
}

const loading = ref(false)
const savingScene = ref<PpdBenefitScene | null>(null)
const activeScene = ref<PpdBenefitScene>('SUBMIT_YPAT')
const configs = ref<MemberBenefitConfig[]>([])
const editVisible = ref(false)
const editFormRef = ref()
const editForm = reactive<MemberBenefitRule>({
  id: 0,
  levelCode: 'BASIC',
  levelName: '基础会员',
  scene: 'SUBMIT_YPAT',
  sceneName: '发布约拍',
  benefitType: 'PPD_DISCOUNT',
  benefitTypeName: '拍豆减免',
  discountPpd: 0,
  minActualPpd: 0,
  effective: '1',
  status: '1',
  description: '',
})

const currentConfig = computed(() => configs.value.find((item) => item.scene === activeScene.value))
const savingCurrent = computed(() => savingScene.value === activeScene.value)
const hasDiscountRisk = computed(
  () =>
    currentConfig.value?.rules.some(
      (rule) => rule.discountPpd > currentConfig.value!.originalPpd,
    ) ?? false,
)

const editRules = {
  discountPpd: [{ required: true, message: '请输入优惠拍豆', trigger: 'blur' }],
  minActualPpd: [
    { required: true, message: '请输入最低实扣', trigger: 'blur' },
    {
      validator: (_rule: unknown, value: number, callback: (error?: Error) => void) => {
        if (currentConfig.value && value > currentConfig.value.originalPpd) {
          callback(new Error('最低实扣不能高于基础消耗'))
          return
        }
        callback()
      },
      trigger: 'change',
    },
  ],
}

async function loadConfigs() {
  loading.value = true
  try {
    const response = await getMemberBenefitConfigs()
    configs.value = response.data || []
  } catch {
    ElMessage.error('权益配置加载失败，请重试')
  } finally {
    loading.value = false
  }
}

function displayLevelName(row: MemberBenefitRule) {
  return row.levelName || levelNames[row.levelCode] || '其他会员等级'
}

function displayBenefitType(row: MemberBenefitRule) {
  return row.benefitTypeName || '拍豆减免'
}

function openEdit(row: MemberBenefitRule) {
  Object.assign(editForm, row)
  editVisible.value = true
}

async function applyEdit() {
  await editFormRef.value?.validate()
  const config = currentConfig.value
  if (!config) return
  const index = config.rules.findIndex((row) => row.id === editForm.id)
  if (index < 0) return
  config.rules.splice(index, 1, { ...editForm })
  editVisible.value = false
}

async function saveCurrentConfig() {
  const config = currentConfig.value
  if (!config) return
  if (
    config.originalPpd < 0 ||
    config.rules.some((rule) => rule.minActualPpd > config.originalPpd)
  ) {
    ElMessage.error('最低实扣不能高于基础消耗')
    return
  }
  savingScene.value = config.scene
  try {
    await saveMemberBenefitConfig(config.scene, {
      ...config,
      rules: config.rules.map((rule) => ({ ...rule })),
    })
    ElMessage.success('配置已保存')
    await loadConfigs()
  } catch (error) {
    const message = error instanceof Error ? error.message : ''
    if (message.includes('已被其他管理员修改') || message.includes('1006')) {
      ElMessage.error('配置已被其他管理员修改，请刷新后重试')
    } else {
      ElMessage.error('配置保存失败，请检查后重试')
    }
  } finally {
    savingScene.value = null
  }
}

onMounted(loadConfigs)
</script>

<template>
  <main
    v-loading="loading"
    class="benefit-page"
  >
    <header class="benefit-page__header">
      <h1>权益配置</h1>
      <el-button
        type="primary"
        :loading="savingCurrent"
        :disabled="!currentConfig"
        @click="saveCurrentConfig"
      >
        保存当前场景
      </el-button>
    </header>

    <el-tabs
      v-model="activeScene"
      class="scene-tabs"
    >
      <el-tab-pane
        v-for="scene in sceneTabs"
        :key="scene.value"
        :label="scene.label"
        :name="scene.value"
      />
    </el-tabs>

    <template v-if="currentConfig">
      <section
        class="base-pricing"
        aria-labelledby="base-pricing-title"
      >
        <div class="base-pricing__label">
          <h2 id="base-pricing-title">
            基础消耗拍豆
          </h2>
          <p>非会员及无有效优惠时按此扣除</p>
        </div>
        <div class="base-pricing__controls">
          <el-input-number
            v-model="currentConfig.originalPpd"
            :min="0"
            :step="1"
            :disabled="savingCurrent"
          />
          <span>拍豆</span>
          <el-input
            v-model="currentConfig.description"
            class="base-pricing__description"
            maxlength="256"
            placeholder="配置说明"
            :disabled="savingCurrent"
          />
        </div>
      </section>

      <el-alert
        v-if="hasDiscountRisk"
        class="discount-alert"
        type="warning"
        :closable="false"
        title="部分会员优惠高于基础消耗，实际报价将按最低实扣计算"
        show-icon
      />

      <section
        class="rule-section"
        aria-labelledby="rule-section-title"
      >
        <div class="rule-section__heading">
          <div>
            <h2 id="rule-section-title">
              会员减免规则
            </h2>
            <span>{{ currentConfig.sceneName }}</span>
          </div>
          <span class="rule-section__type">拍豆减免</span>
        </div>

        <el-table
          :data="currentConfig.rules"
          border
        >
          <el-table-column
            label="会员等级"
            min-width="130"
          >
            <template #default="{ row }">
              <span class="level-name">{{ displayLevelName(row) }}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="discountPpd"
            label="优惠拍豆"
            width="120"
            align="center"
          />
          <el-table-column
            prop="minActualPpd"
            label="最低实扣"
            width="120"
            align="center"
          />
          <el-table-column
            label="生效"
            width="110"
            align="center"
          >
            <template #default="{ row }">
              <el-switch
                v-model="row.effective"
                active-value="1"
                inactive-value="0"
                :disabled="savingCurrent"
                aria-label="规则是否生效"
              />
            </template>
          </el-table-column>
          <el-table-column
            label="状态"
            width="110"
            align="center"
          >
            <template #default="{ row }">
              <el-switch
                v-model="row.status"
                active-value="1"
                inactive-value="0"
                :disabled="savingCurrent"
                aria-label="规则是否启用"
              />
            </template>
          </el-table-column>
          <el-table-column
            prop="description"
            label="说明"
            min-width="200"
            show-overflow-tooltip
          />
          <el-table-column
            label="操作"
            width="100"
            align="center"
            fixed="right"
          >
            <template #default="{ row }">
              <el-button
                type="primary"
                link
                :disabled="savingCurrent"
                @click="openEdit(row)"
              >
                编辑
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </template>

    <el-empty
      v-else-if="!loading"
      description="暂无权益配置"
    />

    <el-dialog
      v-model="editVisible"
      title="编辑会员减免"
      width="560px"
      destroy-on-close
    >
      <div class="rule-identity">
        <div><span>会员等级</span><strong>{{ displayLevelName(editForm) }}</strong></div>
        <div><span>使用场景</span><strong>{{ editForm.sceneName }}</strong></div>
        <div><span>权益类型</span><strong>{{ displayBenefitType(editForm) }}</strong></div>
      </div>
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="96px"
      >
        <el-form-item
          label="优惠拍豆"
          prop="discountPpd"
        >
          <el-input-number
            v-model="editForm.discountPpd"
            :min="0"
            :step="1"
          />
        </el-form-item>
        <el-form-item
          label="最低实扣"
          prop="minActualPpd"
        >
          <el-input-number
            v-model="editForm.minActualPpd"
            :min="0"
            :max="currentConfig?.originalPpd"
            :step="1"
          />
        </el-form-item>
        <el-form-item label="生效">
          <el-switch
            v-model="editForm.effective"
            active-value="1"
            inactive-value="0"
            active-text="已生效"
            inactive-text="未生效"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="editForm.status"
            active-value="1"
            inactive-value="0"
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>
        <el-form-item label="说明">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            maxlength="256"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          @click="applyEdit"
        >
          应用修改
        </el-button>
      </template>
    </el-dialog>
  </main>
</template>

<style scoped lang="scss">
.benefit-page {
  min-height: 100%;
  padding: $spacing-lg;
  background: #fff;
  color: #303133;
}

.benefit-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-lg;
  margin-bottom: $spacing-md;

  h1 {
    margin: 0;
    font-size: 22px;
    font-weight: 600;
    letter-spacing: 0;
  }
}

.scene-tabs {
  margin-bottom: 4px;
}

.base-pricing {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-xl;
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
}

.base-pricing__label,
.rule-section__heading {
  h2 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    letter-spacing: 0;
  }
}

.base-pricing__label p {
  margin: 6px 0 0;
  color: #909399;
  font-size: 13px;
}

.base-pricing__controls {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  min-width: 0;
}

.base-pricing__description {
  width: 280px;
}

.discount-alert {
  margin-top: $spacing-md;
}

.rule-section {
  padding-top: 22px;
}

.rule-section__heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;

  div {
    display: flex;
    align-items: baseline;
    gap: 10px;
  }

  span {
    color: #909399;
    font-size: 13px;
  }
}

.rule-section__type {
  padding: 3px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #f5f7fa;
}

.level-name {
  font-weight: 500;
}

.rule-identity {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 22px;
  padding: 14px 16px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #f7f8fa;

  div {
    min-width: 0;
  }

  span,
  strong {
    display: block;
    letter-spacing: 0;
  }

  span {
    margin-bottom: 4px;
    color: #909399;
    font-size: 12px;
  }

  strong {
    overflow: hidden;
    font-size: 14px;
    font-weight: 500;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

@media (max-width: 900px) {
  .base-pricing {
    align-items: flex-start;
    flex-direction: column;
  }

  .base-pricing__controls {
    flex-wrap: wrap;
    justify-content: flex-start;
    width: 100%;
  }

  .base-pricing__description {
    flex: 1 1 240px;
    width: auto;
  }
}
</style>
