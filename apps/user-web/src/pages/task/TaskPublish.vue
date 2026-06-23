<template>
  <SubPageLayout title="发布任务" :bottom-bar="true">
    <div class="publish-page">
      <van-form @submit="handleSubmit">
        <!-- 基本信息 -->
        <div class="form-section">
          <h3 class="section-title">基本信息</h3>
          <van-field
            v-model="form.title"
            label="任务标题"
            placeholder="请输入任务标题"
            :rules="[{ required: true, message: '请输入标题' }]"
          />
          <div class="field-block">
            <div class="field-label">分类</div>
            <div class="cat-chips">
              <span
                v-for="c in categories"
                :key="c.id"
                class="cat-chip"
                :class="{ active: form.categoryId === Number(c.id) }"
                @click="selectCategory(c)"
              >{{ c.name }}</span>
              <span v-if="!categories.length" class="empty-hint">暂无分类</span>
            </div>
          </div>
          <van-field
            v-model="form.description"
            label="任务描述"
            type="textarea"
            rows="3"
            placeholder="描述任务要求、交付物..."
            :rules="[{ required: true, message: '请输入描述' }]"
          />
          <van-field v-model="levelText" label="接单等级" placeholder="1" type="digit" />
          <van-field v-model="slotsText" label="名额" placeholder="1" type="digit" />
          <van-field v-model="form.deadline" label="截止日期" placeholder="YYYY-MM-DD" />
        </div>

        <!-- 技能要求 -->
        <div class="form-section">
          <h3 class="section-title">技能要求</h3>
          <van-field v-model="skillInput" label="技能" placeholder="输入技能后点添加">
            <template #button>
              <van-button size="small" type="primary" @click.prevent="addSkill">添加</van-button>
            </template>
          </van-field>
          <div class="skill-chips">
            <van-tag v-for="(s, i) in form.skills" :key="i" closeable type="primary" plain @close="form.skills.splice(i, 1)">{{ s }}</van-tag>
          </div>
        </div>

        <!-- 里程碑(分阶段结算) -->
        <div class="form-section">
          <div class="section-head">
            <h3 class="section-title">里程碑 · 分阶段结算</h3>
            <van-button size="mini" icon="plus" type="primary" plain @click.prevent="addMilestone">添加</van-button>
          </div>
          <div v-for="(m, i) in form.milestones" :key="i" class="milestone-row">
            <div class="row-head">
              <span class="row-index">里程碑 {{ i + 1 }}</span>
              <van-button v-if="form.milestones.length > 1" size="mini" type="danger" plain @click.prevent="form.milestones.splice(i, 1)">删除</van-button>
            </div>
            <van-field v-model="m.title" label="标题" placeholder="如：需求确认" :rules="[{ required: true, message: '必填' }]" />
            <van-field v-model="m.rewardText" label="酬金" placeholder="金额" type="number" :rules="[{ required: true, message: '必填' }]">
              <template #button>元</template>
            </van-field>
            <van-field v-model="m.description" label="说明" placeholder="可选" />
          </div>
          <div class="reward-sum">
            总酬金 <span class="sum-val">¥{{ totalReward }}</span>
            <span class="sum-hint">(各里程碑金额之和,即任务总酬金)</span>
          </div>
        </div>

        <div class="submit-bar">
          <van-button round block type="primary" native-type="submit" :loading="submitting">发布任务</van-button>
        </div>
      </van-form>
    </div>
  </SubPageLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast } from 'vant'
import SubPageLayout from '@/layouts/SubPageLayout.vue'
import { getTaskCategories, publishTask, getIdempotentToken } from '@/api/task'
import type { TaskCategory, PublishTaskPayload } from '@/types/task'

const router = useRouter()
const categories = ref<TaskCategory[]>([])
const submitting = ref(false)
const skillInput = ref('')
const publishToken = ref('')

const form = reactive({
  title: '',
  categoryId: undefined as number | undefined,
  category: '',
  description: '',
  levelRequired: 1,
  totalSlots: 1,
  deadline: '',
  skills: [] as string[],
  milestones: [
    { title: '', rewardText: '', description: '' },
  ],
})

const levelText = computed({
  get: () => String(form.levelRequired),
  set: (v: string) => { form.levelRequired = Math.max(1, Number(v) || 1) },
})
const slotsText = computed({
  get: () => String(form.totalSlots),
  set: (v: string) => { form.totalSlots = Math.max(1, Number(v) || 1) },
})

const totalReward = computed(() =>
  form.milestones.reduce((s, m) => s + (Number(m.rewardText) || 0), 0),
)

onMounted(async () => {
  try {
    categories.value = await getTaskCategories()
  } catch {
    categories.value = []
  }
  try {
    publishToken.value = await getIdempotentToken()
  } catch {
    publishToken.value = ''
  }
})

function selectCategory(c: TaskCategory) {
  form.categoryId = Number(c.id)
  form.category = c.name
}

function addSkill() {
  const s = skillInput.value.trim()
  if (s && !form.skills.includes(s)) form.skills.push(s)
  skillInput.value = ''
}

function addMilestone() {
  form.milestones.push({ title: '', rewardText: '', description: '' })
}

async function handleSubmit() {
  for (const m of form.milestones) {
    if (!m.title.trim() || !(Number(m.rewardText) > 0)) {
      showToast('请完整填写每个里程碑的标题和金额')
      return
    }
  }
  const total = totalReward.value
  if (total <= 0) {
    showToast('总酬金须大于0')
    return
  }

  submitting.value = true
  try {
    const payload: PublishTaskPayload = {
      title: form.title.trim(),
      categoryId: form.categoryId,
      description: form.description.trim(),
      reward: total,
      levelRequired: form.levelRequired,
      totalSlots: form.totalSlots,
      deadline: form.deadline || undefined,
      skills: form.skills,
      milestones: form.milestones.map((m, i) => ({
        title: m.title.trim(),
        description: m.description || undefined,
        reward: Number(m.rewardText),
        milestoneOrder: i + 1,
      })),
    }
    await publishTask(payload, publishToken.value)
    showSuccessToast('发布成功')
    router.replace('/tasks/published')
  } catch (e) {
    showToast((e as Error).message || '发布失败')
    // 发布失败后重新获取 token,允许重试
    try { publishToken.value = await getIdempotentToken() } catch { /* ignore */ }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.publish-page {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-bottom: 100px;
}

.form-section {
  padding: var(--spacing-lg) var(--spacing-xl);
  background: var(--bg-card);
  border-bottom: 8px solid var(--bg-primary);
}

.section-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.section-head .section-title {
  margin-bottom: 0;
}

.field-block {
  padding: var(--spacing-sm) 0;
}

.field-label {
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
  margin-bottom: var(--spacing-xs);
  padding: 0 16px;
}

.cat-chips {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
  padding: 0 16px;
}

.cat-chip {
  padding: 4px 12px;
  border-radius: 20px;
  background: var(--bg-tertiary);
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
}

.cat-chip.active {
  background: var(--primary);
  color: #fff;
}

.empty-hint {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.skill-chips {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm) 16px 0;
}

.milestone-row {
  background: var(--bg-primary);
  border-radius: var(--radius-sm);
  padding: var(--spacing-sm) 0;
  margin-bottom: var(--spacing-md);
}

.row-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px var(--spacing-xs);
}

.row-index {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--primary);
}

.reward-sum {
  margin-top: var(--spacing-md);
  padding: var(--spacing-md);
  background: var(--bg-primary);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.sum-val {
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: var(--warning);
}

.sum-hint {
  display: block;
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
  margin-top: 4px;
}

.submit-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: var(--spacing-md) var(--spacing-xl);
  background: var(--bg-card);
  border-top: 1px solid var(--border);
  max-width: var(--max-width);
  margin: 0 auto;
}
</style>
