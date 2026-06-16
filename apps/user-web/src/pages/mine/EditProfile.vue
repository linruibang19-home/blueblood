<template>
  <SubPageLayout title="编辑个人资料" :bottom-bar="true">
    <div class="edit-profile">
      <!-- 头像 -->
      <div class="avatar-section">
        <div class="avatar-wrap">
          <UserAvatar :src="form.avatar" :size="'xl'" />
        </div>
        <van-button size="small" plain @click="showAvatarPicker = true">更换头像</van-button>
      </div>

      <!-- 表单 -->
      <van-form @submit="handleSave">
        <van-cell-group>
          <van-field
            v-model="form.name"
            label="姓名"
            placeholder="请输入姓名"
            :rules="[{ required: true, message: '请填写姓名' }]"
          />
          <van-field
            v-model="form.school"
            label="学校"
            placeholder="请输入学校"
          />
          <van-field
            v-model="form.major"
            label="专业"
            placeholder="请输入专业"
          />
          <van-field
            v-model="form.bio"
            label="个人简介"
            type="textarea"
            placeholder="介绍一下自己..."
            rows="3"
            autosize
          />
          <van-field
            v-model="form.github"
            label="GitHub"
            placeholder="请输入 GitHub 地址"
            prefix-icon="link"
          />
        </van-cell-group>

        <!-- 技能标签 -->
        <div class="skills-section">
          <div class="section-header">
            <span class="section-title">技能标签</span>
            <van-button size="small" plain type="primary" @click="showAddSkill = true">添加</van-button>
          </div>
          <div class="skills-list">
            <SkillTag
              v-for="skill in form.skills"
              :key="skill"
              :text="skill"
              removable
              @remove="removeSkill(skill)"
            />
            <SkillTag text="+" addable @click="showAddSkill = true" />
          </div>
        </div>

        <!-- 保存按钮 -->
        <div class="save-bar">
          <van-button type="primary" size="large" round block native-type="submit" :loading="saving">
            保存
          </van-button>
        </div>
      </van-form>

      <!-- 添加技能弹窗 -->
      <van-popup v-model:show="showAddSkill" position="bottom" round>
        <div class="add-skill-popup">
          <h3 class="popup-title">添加技能标签</h3>
          <van-field
            v-model="newSkill"
            placeholder="输入技能名称，如：Vue3"
            @keyup.enter="addSkill"
          />
          <div class="popup-actions">
            <van-button @click="showAddSkill = false">取消</van-button>
            <van-button type="primary" @click="addSkill">添加</van-button>
          </div>
        </div>
      </van-popup>
    </div>
  </SubPageLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import SubPageLayout from '@/layouts/SubPageLayout.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import SkillTag from '@/components/SkillTag.vue'
import { getCurrentUser, updateUserProfile } from '@/api/user'

const router = useRouter()
const form = ref({
  name: '',
  avatar: '',
  school: '',
  major: '',
  bio: '',
  github: '',
  skills: [] as string[],
})
const saving = ref(false)
const showAddSkill = ref(false)
const showAvatarPicker = ref(false)
const newSkill = ref('')

onMounted(async () => {
  const user = await getCurrentUser()
  form.value = {
    name: user.name,
    avatar: user.avatar,
    school: user.school,
    major: user.major,
    bio: user.bio,
    github: user.github,
    skills: [...user.skills],
  }
})

function removeSkill(skill: string) {
  form.value.skills = form.value.skills.filter(s => s !== skill)
}

function addSkill() {
  if (!newSkill.value.trim()) {
    showToast('请输入技能名称')
    return
  }
  if (form.value.skills.includes(newSkill.value.trim())) {
    showToast('该技能已添加')
    return
  }
  form.value.skills.push(newSkill.value.trim())
  newSkill.value = ''
  showAddSkill.value = false
}

async function handleSave() {
  saving.value = true
  try {
    await updateUserProfile(form.value)
    showToast('保存成功')
    router.back()
  } catch {
    showToast('保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.edit-profile {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-bottom: 80px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-2xl);
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.skills-section {
  padding: var(--spacing-xl);
  background: var(--bg-card);
  border-top: 1px solid var(--divider);
  margin-top: var(--spacing-lg);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.section-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
}

.skills-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.save-bar {
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

.add-skill-popup {
  padding: var(--spacing-xl);
}

.popup-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  text-align: center;
  margin-bottom: var(--spacing-lg);
}

.popup-actions {
  display: flex;
  gap: var(--spacing-md);
  margin-top: var(--spacing-lg);
}

.popup-actions .van-button {
  flex: 1;
}
</style>