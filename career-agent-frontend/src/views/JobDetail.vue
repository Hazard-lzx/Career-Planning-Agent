<template>
  <div class="job-detail-page" v-loading="loading">
    <!-- 返回按钮 -->
    <el-page-header @back="$router.push('/jobs')" class="page-back">
      <template #content>
        <span class="page-header-title">{{ jobDetail.title || '岗位详情' }}</span>
      </template>
    </el-page-header>

    <!-- 岗位基本信息 -->
    <el-card class="info-card">
      <template #header>
        <div class="card-header">
          <span class="card-header-title">
            <el-icon class="title-icon"><Briefcase /></el-icon>岗位信息
          </span>
          <div class="card-header-actions">
            <el-button
              v-if="!hasProfile"
              type="warning"
              @click="generateProfile"
              :loading="generatingProfile"
              round
            >
              <el-icon><MagicStick /></el-icon>生成岗位画像
            </el-button>
            <el-button type="primary" @click="startMatch" :loading="matching" :disabled="!hasProfile" round>
              <el-icon><DataAnalysis /></el-icon>匹配分析
            </el-button>
          </div>
        </div>
      </template>

      <!-- 基本信息描述 -->
      <el-descriptions :column="2" border>
        <el-descriptions-item label="岗位名称">{{ jobDetail.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="岗位级别">
          <el-tag v-if="jobDetail.level" :type="levelTagType(jobDetail.level)" effect="plain" round>{{ jobDetail.level }}</el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="公司">{{ jobDetail.companyShortName || jobDetail.companyFullName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="薪资">{{ jobDetail.salaryRange || '-' }}</el-descriptions-item>
        <el-descriptions-item label="工作地点">{{ jobDetail.workLocation || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属行业">{{ jobDetail.industry || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 无画像提示 -->
      <el-alert
        v-if="!hasProfile"
        title="该岗位尚未生成画像，请点击【生成岗位画像】按钮，AI将自动解析岗位描述生成能力要求画像"
        type="info"
        show-icon
        :closable="false"
        class="no-profile-alert"
      />

      <!-- 画像内容（有画像时显示） -->
      <template v-if="hasProfile">
        <div class="section-divider">
          <span class="section-dot"></span>
          <span class="section-text">岗位要求画像</span>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="学历要求">{{ jobProfile.basic_requirements?.degree || '-' }}</el-descriptions-item>
          <el-descriptions-item label="专业要求">{{ jobProfile.basic_requirements?.major || '-' }}</el-descriptions-item>
          <el-descriptions-item label="经验要求">{{ jobProfile.experience || '-' }}</el-descriptions-item>
          <el-descriptions-item label="证书要求">
            <template v-if="jobProfile.certificates && jobProfile.certificates.length">
              <el-tag v-for="cert in jobProfile.certificates" :key="cert" size="small" type="warning" effect="plain" class="skill-tag" round>
                {{ cert }}
              </el-tag>
            </template>
            <span v-else>-</span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 硬技能 -->
        <div class="section-block" v-if="jobProfile.hard_skills && jobProfile.hard_skills.length">
          <h4 class="section-label">硬技能要求</h4>
          <div class="tags-area">
            <el-tag v-for="skill in jobProfile.hard_skills" :key="skill" effect="plain" class="skill-tag" round>
              {{ skill }}
            </el-tag>
          </div>
        </div>

        <!-- 软技能 -->
        <div class="section-block" v-if="jobProfile.soft_skills && Object.keys(jobProfile.soft_skills).length">
          <h4 class="section-label">软技能要求</h4>
          <div class="tags-area">
            <el-tag
              v-for="(val, key) in jobProfile.soft_skills"
              :key="key"
              :type="val === '高' ? 'danger' : val === '中' ? 'warning' : 'success'"
              effect="plain"
              class="skill-tag"
              round
            >
              {{ key }}: {{ val }}
            </el-tag>
          </div>
        </div>
      </template>
    </el-card>

    <!-- 匹配结果区域 -->
    <el-card v-if="matchResult" class="match-card">
      <template #header>
        <div class="card-header">
          <span class="card-header-title">
            <el-icon class="title-icon"><DataAnalysis /></el-icon>匹配分析结果
          </span>
          <el-button type="primary" @click="generateReport" :loading="generating" round>
            <el-icon><Document /></el-icon>生成完整报告
          </el-button>
        </div>
      </template>

      <el-row :gutter="24">
        <el-col :span="8">
          <div class="score-panel">
            <el-progress
              type="dashboard"
              :percentage="Math.round((matchResult.totalScore || matchResult.total_score || 0) * 100)"
              :width="160"
              :stroke-width="12"
              :color="scoreColor"
            >
              <template #default="{ percentage }">
                <div class="score-text">
                  <span class="score-number">{{ percentage }}</span>
                  <span class="score-unit">分</span>
                </div>
              </template>
            </el-progress>
            <p class="score-label">综合匹配度</p>
          </div>
        </el-col>
        <el-col :span="16">
          <RadarChart :data="matchResult.dimensions" height="350px" />
        </el-col>
      </el-row>

      <el-alert
        :title="matchResult.gapAnalysis || matchResult.gap_analysis"
        type="warning"
        show-icon
        :closable="false"
        class="gap-alert"
      />

      <el-row :gutter="24" class="skill-compare">
        <el-col :span="12">
          <div class="compare-box compare-match">
            <h4 class="section-label">已匹配技能</h4>
            <div class="tags-area">
              <el-tag v-for="skill in (matchResult.matchedSkills || matchResult.matched_skills || [])" :key="skill" type="success" effect="plain" class="skill-tag" round>{{ skill }}</el-tag>
              <span v-if="!(matchResult.matchedSkills || matchResult.matched_skills)?.length" class="empty-hint">暂无</span>
            </div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="compare-box compare-miss">
            <h4 class="section-label">缺失技能</h4>
            <div class="tags-area">
              <el-tag v-for="skill in (matchResult.missingSkills || matchResult.missing_skills || [])" :key="skill" type="danger" effect="plain" class="skill-tag" round>{{ skill }}</el-tag>
              <span v-if="!(matchResult.missingSkills || matchResult.missing_skills)?.length" class="empty-hint">暂无</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 路径信息 -->
    <el-card class="path-card">
      <template #header>
        <span class="card-header-title">
          <el-icon class="title-icon"><Guide /></el-icon>职业发展路径
        </span>
      </template>
      <el-tabs v-model="activePathTab" class="path-tabs">
        <el-tab-pane label="垂直晋升路径" name="vertical">
          <PathTimeline :paths="verticalPaths" />
        </el-tab-pane>
        <el-tab-pane label="水平换岗路径" name="horizontal">
          <div v-if="horizontalPaths && horizontalPaths.length" class="horizontal-list">
            <el-card v-for="(path, index) in horizontalPaths" :key="index" shadow="hover" class="horizontal-item">
              <div class="horizontal-header">
                <h4>{{ path.targetTitle || path.title }}</h4>
                <el-tag effect="plain" size="small" round>相似度: {{ (path.similarity * 100).toFixed(0) }}%</el-tag>
              </div>
              <p class="horizontal-reason">{{ path.reason || '技能重叠度高，具备转型基础' }}</p>
            </el-card>
          </div>
          <el-empty v-else description="暂无水平换岗路径" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import RadarChart from '../components/RadarChart.vue'
import PathTimeline from '../components/PathTimeline.vue'

const route = useRoute()
const router = useRouter()
const jobId = route.params.id

const loading = ref(false)
const matching = ref(false)
const generating = ref(false)
const generatingProfile = ref(false)

const jobDetail = ref({})
const jobProfile = ref({
  basic_requirements: { degree: '', major: '' },
  hard_skills: [],
  soft_skills: {},
  certificates: [],
  experience: '',
})

// 是否有画像数据
const hasProfile = computed(() => !!jobDetail.value.profileJson)

const matchResult = ref(null)
const activePathTab = ref('vertical')
const verticalPaths = ref([])
const horizontalPaths = ref([])

// 从localStorage获取studentId
const studentId = computed(() => {
  const id = localStorage.getItem('studentId')
  return id ? Number(id) : null
})

const scoreColor = computed(() => {
  if (!matchResult.value) return '#3B82D9'
  const s = matchResult.value.totalScore || matchResult.value.total_score || 0
  if (s >= 0.8) return '#34D399'
  if (s >= 0.6) return '#FBBF24'
  return '#F87171'
})

function levelTagType(level) {
  const map = { '初级': 'success', '中级': '', '高级': 'warning', '资深': 'danger' }
  return map[level] || 'info'
}

// 解析profileJson
function parseProfileJson(data) {
  if (data.profileJson) {
    try {
      const profile = typeof data.profileJson === 'string' ? JSON.parse(data.profileJson) : data.profileJson
      jobProfile.value = { ...jobProfile.value, ...profile }
    } catch (e) {
      console.error('解析profileJson失败', e)
    }
  }
}

async function loadJobDetail() {
  loading.value = true
  try {
    const data = await request.get(`/api/job/${jobId}`)
    const job = data.data || data; jobDetail.value = job || {}
    parseProfileJson(data)
  } catch (e) { ElMessage.error('报告生成失败: ' + (e?.response?.data?.message || e.message)) } finally {
    loading.value = false
  }
}

async function loadPaths() {
  try {
    const [vertData, horizData] = await Promise.all([
      request.get(`/api/path/vertical/${jobId}`),
      request.get(`/api/path/horizontal/${jobId}`),
    ])
    verticalPaths.value = Array.isArray(vertData) ? vertData : (vertData?.paths || [])
    horizontalPaths.value = Array.isArray(horizData) ? horizData : (horizData?.paths || [])
  } catch (e) { }
}

// 生成岗位画像
async function generateProfile() {
  generatingProfile.value = true
  try {
    const data = await request.post(`/api/job/${jobId}/generate-profile`)
    const job = data.data || data; jobDetail.value = job || jobDetail.value
    parseProfileJson(job)
    ElMessage.success('岗位画像生成成功')
  } catch (e) {
    ElMessage.error('岗位画像生成失败，请检查AI服务是否正常运行')
  } finally {
    generatingProfile.value = false
  }
}

async function startMatch() {
  if (!studentId.value) {
    ElMessage.warning('请先在个人中心上传简历，创建学生画像')
    return
  }
  if (!hasProfile.value) {
    ElMessage.warning('请先生成岗位画像')
    return
  }
  matching.value = true
  try {
    const data = await request.post('/api/match/calculate', {
      studentId: studentId.value,
      jobId: Number(jobId),
      weights: { basic: 0.2, hard_skill: 0.4, soft_skill: 0.25, potential: 0.15 },
    })
    matchResult.value = data.data || data
    ElMessage.success('匹配分析完成')
  } catch (e) { ElMessage.error('报告生成失败: ' + (e?.response?.data?.message || e.message)) } finally {
    matching.value = false
  }
}

async function generateReport() {
  generating.value = true
  try {
    const data = await request.post('/api/report/generate', {
      studentId: studentId.value,
      jobId: Number(jobId),
    })
    const reportId = data.data.id || data.data.reportId || data.id || data.reportId
    if (reportId) {
      ElMessage.success('报告生成中，请稍候...')
      router.push(`/report/${reportId}`)
    }
  } catch (e) { ElMessage.error('报告生成失败: ' + (e?.response?.data?.message || e.message)) } finally {
    generating.value = false
  }
}

onMounted(() => {
  loadJobDetail()
  loadPaths()
})
</script>

<style scoped>
.job-detail-page {
  max-width: 1100px;
  margin: 0 auto;
}

.page-back {
  margin-bottom: 24px;
}

.page-header-title {
  font-size: 18px;
  font-weight: 700;
  color: #1e3a5f;
}

.info-card,
.match-card,
.path-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header-actions {
  display: flex;
  gap: 8px;
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

.no-profile-alert {
  margin-top: 16px;
  border-radius: 10px;
}

.section-divider {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 24px 0 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f4fa;
}

.section-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #3B82D9;
}

.section-text {
  font-size: 14px;
  font-weight: 700;
  color: #1e3a5f;
}

.section-block {
  margin-top: 20px;
}

.section-label {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 10px;
}

.tags-area {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.skill-tag {
  margin: 0;
}

.score-panel {
  text-align: center;
  padding: 24px 0;
}

.score-text { text-align: center; }
.score-number { font-size: 36px; font-weight: 800; color: #1e3a5f; }
.score-unit { font-size: 14px; color: #94a3b8; }
.score-label { margin-top: 8px; color: #64748b; font-size: 14px; }

.gap-alert { margin-top: 24px; border-radius: 10px; }

.skill-compare { margin-top: 24px; }

.compare-box { padding: 16px; border-radius: 10px; }
.compare-match { background: #F0FDF4; border: 1px solid #BBF7D0; }
.compare-match .section-label { color: #16A34A; }
.compare-miss { background: #FFF1F2; border: 1px solid #FECDD3; }
.compare-miss .section-label { color: #E11D48; }

.empty-hint { color: #cbd5e1; font-size: 13px; }

.path-tabs :deep(.el-tabs__item.is-active) { color: #3B82D9; }
.path-tabs :deep(.el-tabs__active-bar) { background-color: #3B82D9; }

.horizontal-list { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
.horizontal-item { cursor: default; border: 1px solid #e8f0fe; }
.horizontal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.horizontal-header h4 { color: #1e3a5f; font-size: 15px; font-weight: 600; margin: 0; }
.horizontal-reason { color: #64748b; font-size: 13px; line-height: 1.6; }
</style>
