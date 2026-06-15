<template>
  <div class="job-list-page">
    <h2 class="page-title">
      <el-icon><Briefcase /></el-icon>岗位探索
    </h2>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-row :gutter="16" align="middle">
        <el-col :span="8">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索岗位名称或关键词"
            clearable
            :prefix-icon="Search"
            @keyup.enter="searchJobs"
          />
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterLevel" placeholder="岗位级别" clearable style="width: 100%">
            <el-option label="初级" value="初级" />
            <el-option label="中级" value="中级" />
            <el-option label="高级" value="高级" />
            <el-option label="资深" value="资深" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="searchJobs" :loading="loading" round>
            <el-icon><Search /></el-icon>搜索
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 岗位卡片网格 -->
    <el-row :gutter="20" v-loading="loading">
      <el-col :span="8" v-for="job in filteredJobs" :key="job.id" class="job-col">
        <el-card shadow="hover" class="job-card" @click="goDetail(job.id)">
          <div <div class="job-card-header">
          <h3 class="job-title">{{ job.title }}</h3>
          <span class="job-id-label">ID: {{ job.id }}</span>
          <el-tag v-if="job.level" :type="levelTagType(job.level)" :color="levelTagColor(job.level)" size="small" effect="plain" round>{{ job.level }}</el-tag>
        </div>
          <div class="job-meta">
            <span v-if="job.companyShortName" class="meta-item">
              <el-icon><OfficeBuilding /></el-icon>{{ job.companyShortName }}
            </span>
            <span v-if="job.salaryRange" class="meta-item salary">
              <el-icon><Money /></el-icon>{{ job.salaryRange }}
            </span>
            <span v-if="job.workLocation" class="meta-item">
              <el-icon><Location /></el-icon>{{ job.workLocation }}
            </span>
          </div>
          <p class="job-desc">{{ job.description || '暂无描述' }}</p>
          <div class="job-skills" v-if="getHardSkills(job).length">
            <el-tag
              v-for="skill in getHardSkills(job).slice(0, 5)"
              :key="skill"
              size="small"
              effect="plain"
              class="skill-tag"
              round
            >
              {{ skill }}
            </el-tag>
            <span v-if="getHardSkills(job).length > 5" class="more-tag">+{{ getHardSkills(job).length - 5 }}</span>
          </div>
          <div class="job-footer">
            <span class="job-degree">{{ getDegree(job) }}</span>
            <span class="job-link">
              查看详情 <el-icon><ArrowRight /></el-icon>
            </span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 空状态 -->
    <el-empty v-if="!loading && filteredJobs.length === 0" description="暂无岗位数据" />

    <!-- 分页 -->
    <div class="pagination-wrap" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadJobs"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import request from '../utils/request'

const router = useRouter()
const searchKeyword = ref('')
const filterLevel = ref('')
const loading = ref(false)
const jobs = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

// 按级别前端过滤（后端暂不支持level参数）
const filteredJobs = computed(() => {
  if (!filterLevel.value) return jobs.value
  return jobs.value.filter((j) => j.level === filterLevel.value)
})

function levelTagType(level) {
  const map = { '实习': 'info', '初级': 'success', '中级': 'primary', '高级': 'warning', '资深': 'danger', '专家': 'danger', '管理': 'danger', '总监': 'danger', 'VP': 'danger' }
  return map[level] || 'info'
}
function levelTagColor(level) {
  const map = { '管理': '#8B5CF6', '专家': '#8B5CF6', '总监': '#8B5CF6', 'VP': '#7C3AED' }
  return map[level] || null
}

// 从profileJson中解析硬技能
function getHardSkills(job) {
  if (!job.profileJson) return []
  try {
    const profile = typeof job.profileJson === 'string' ? JSON.parse(job.profileJson) : job.profileJson
    return profile.hardSkills || profile.hard_skills || []
  } catch {
    return []
  }
}

// 从profileJson中解析学历要求
function getDegree(job) {
  if (!job.profileJson) return '学历不限'
  try {
    const profile = typeof job.profileJson === 'string' ? JSON.parse(job.profileJson) : job.profileJson
    return profile.basicRequirements || profile.basic_requirements?.degree || '学历不限'
  } catch {
    return '学历不限'
  }
}

function searchJobs() {
  currentPage.value = 1
  loadJobs()
}

async function loadJobs() {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const data = await request.get('/api/job/list', { params })
    // 响应拦截器已解包Result.data，这里直接拿到PageResultDTO
    if (data && data.data.records) {
      jobs.value = data.data.records
      total.value = data.data.total || 0
    } else if (Array.isArray(data)) {
      jobs.value = data
      total.value = data.length
    }
  } catch (e) {
    console.error('加载岗位列表失败', e)
  } finally {
    loading.value = false
  }
}

function goDetail(id) {
  router.push(`/job/${id}`)
}

onMounted(() => {
  loadJobs()
})
</script>

<style scoped>
.job-list-page {
  max-width: 1200px;
  margin: 0 auto;
}

.search-card {
  margin-bottom: 28px;
}

.job-col {
  margin-bottom: 20px;
}

.job-card {
  cursor: pointer;
  transition: transform 0.25s, box-shadow 0.25s;
  height: 100%;
  border: 1px solid #e8f0fe;
}

.job-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(59, 130, 217, 0.1);
  border-color: #BFDBFE;
}

.job-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.job-title {
  font-size: 16px;
  font-weight: 700;
  color: #1e3a5f;
  margin: 0;
}

.job-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 10px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 3px;
  color: #64748b;
  font-size: 12px;
}

.meta-item.salary {
  color: #F59E0B;
  font-weight: 600;
}

.job-desc {
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.job-skills {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 14px;
}

.skill-tag {
  margin: 0;
}

.more-tag {
  font-size: 11px;
  color: #94a3b8;
  padding: 2px 8px;
  border-radius: 12px;
  background: #f0f4fa;
}

.job-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #f0f4fa;
  padding-top: 12px;
}

.job-degree {
  color: #94a3b8;
  font-size: 12px;
}

.job-link {
  color: #3B82D9;
  font-size: 13px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 2px;
  transition: gap 0.2s;
}

.job-card:hover .job-link {
  gap: 6px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}.job-id-label { font-size: 12px; color: #94a3b8; font-weight: 500; margin-right: 8px; }

</style>
