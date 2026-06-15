<template>
  <div class="report-list-page">
    <h2 class="page-title">
      <el-icon><Document /></el-icon>报告管理
    </h2>

    <!-- 报告列表 -->
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="card-header-title">
            <el-icon class="title-icon"><Folder /></el-icon>我的报告
          </span>
          <span class="card-header-sub">共 {{ reports.length }} 份报告</span>
        </div>
      </template>

      <div v-if="reports.length" class="report-grid">
        <el-card
          v-for="report in reports"
          :key="report.id"
          shadow="hover"
          class="report-card"
          @click="goDetail(report.id)"
        >
          <div class="report-icon-box">
            <el-icon :size="28" color="#3B82D9"><Document /></el-icon>
          </div>
          <div class="report-info">
            <h3 class="report-title">{{ report.jobTitle || report.job_title || '岗位匹配报告' }}</h3>
            <p class="report-meta">
              <span v-if="report.totalScore || report.total_score" class="report-score">
                匹配度: {{ Math.round((report.totalScore || report.total_score) * 100) }}%
              </span>
              <span class="report-date">{{ formatDate(report.createTime || report.create_time) }}</span>
            </p>
          </div>
          <div class="report-actions">
            <el-button
              type="danger"
              :icon="Delete"
              circle
              size="small"
              @click.stop="handleDelete(report)"
            />
          </div>
          <div class="report-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </el-card>
      </div>

      <el-empty v-else description="暂无报告，请先进行岗位匹配分析" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import request from '../utils/request'

const router = useRouter()
const loading = ref(false)
const reports = ref([])
const studentId = computed(() => {
  const id = localStorage.getItem('studentId')
  return id ? Number(id) : null
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

async function loadReports() {
  if (!studentId.value) return
  loading.value = true
  try {
    const data = await request.get(`/api/report/list/${studentId.value}`)
    reports.value = Array.isArray(data.data) ? data.data : (data.data || data?.records || [])
  } catch (e) { } finally {
    loading.value = false
  }
}

function goDetail(id) {
  router.push(`/report/${id}`)
}

async function handleDelete(report) {
  try {
    await ElMessageBox.confirm(
      '确定要删除该报告吗？删除后不可恢复。',
      '删除确认',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )
    await request.delete(`/report/${report.id}`)
    ElMessage.success('报告已删除')
    loadReports()
  } catch (e) {
    // 用户取消删除
  }
}

onMounted(() => {
  loadReports()
})
</script>

<style scoped>
.report-list-page {
  max-width: 900px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header-title {
  font-size: 15px;
  font-weight: 700;
  color: #1e3a5f;
  display: flex;
  align-items: center;
  gap: 6px;
}

.title-icon {
  color: #3B82D9;
}

.card-header-sub {
  font-size: 12px;
  color: #94a3b8;
}

.report-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.report-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid #e8f0fe;
}

.report-card:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 16px rgba(59, 130, 217, 0.08);
  border-color: #BFDBFE;
}

.report-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
}

.report-icon-box {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: linear-gradient(135deg, #EFF6FF, #DBEAFE);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.report-info {
  flex: 1;
  min-width: 0;
}

.report-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e3a5f;
  margin: 0 0 4px;
}

.report-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #94a3b8;
  margin: 0;
}

.report-score {
  color: #3B82D9;
  font-weight: 600;
}

.report-actions {
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.2s;
}

.report-card:hover .report-actions {
  opacity: 1;
}

.report-arrow {
  color: #BFDBFE;
  font-size: 16px;
  transition: color 0.2s, transform 0.2s;
}

.report-card:hover .report-arrow {
  color: #3B82D9;
  transform: translateX(2px);
}
</style>
