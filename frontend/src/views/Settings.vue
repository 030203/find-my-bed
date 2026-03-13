<template>
  <div class="settings">
    <el-page-header content="系统设置" icon="ArrowLeft" @back="$router.push('/')" />
    <el-card shadow="hover">
      <template #header>基础偏好</template>
      <el-form label-width="120px" :model="prefs">
        <el-form-item label="界面主题">
          <el-select v-model="prefs.theme" placeholder="选择主题" style="width: 240px">
            <el-option label="亮色" value="light" />
            <el-option label="暗色" value="dark" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期格式">
          <el-select v-model="prefs.dateFormat" style="width: 240px">
            <el-option label="YYYY-MM-DD" value="YYYY-MM-DD" />
            <el-option label="YYYY/MM/DD" value="YYYY/MM/DD" />
          </el-select>
        </el-form-item>
        <el-form-item label="默认联系人">
          <el-input v-model="prefs.defaultContact" placeholder="用于快速预订填写" style="max-width: 320px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save">保存设置</el-button>
          <el-button @click="reset">恢复默认</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const DEFAULT_PREFS = {
  theme: 'light',
  dateFormat: 'YYYY-MM-DD',
  defaultContact: ''
}

const prefs = reactive({ ...DEFAULT_PREFS })

const load = () => {
  try {
    const saved = localStorage.getItem('app_prefs')
    if (saved) {
      Object.assign(prefs, JSON.parse(saved))
    }
  } catch (e) {
    // ignore
  }
}

const save = () => {
  localStorage.setItem('app_prefs', JSON.stringify(prefs))
  ElMessage.success('已保存')
}

const reset = () => {
  Object.assign(prefs, DEFAULT_PREFS)
  save()
}

onMounted(load)
</script>

<style scoped>
.settings {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
