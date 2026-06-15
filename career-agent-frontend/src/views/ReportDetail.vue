<template>
  <div class="report-detail-page" v-loading="loading">
    <el-page-header @back="$router.push('/my')" class="page-back">
      <template #content><span>报告详情</span></template>
    </el-page-header>

    <div class="status-bar" v-if="reportStatus">
      <el-tag :type="reportStatus==='complete'?'success':reportStatus==='failed'?'danger':'warning'" size="large">
        {{ reportStatus==='complete'?'已完成':reportStatus==='failed'?'生成失败':'生成中...' }}
      </el-tag>
      <span class="status-hint" v-if="reportStatus==='pending'">报告正在后台生成，请稍候...</span>
    </div>

    <div v-if="reportDetail && reportDetail.content" class="report-content" v-html="renderedContent"></div>
    <div v-else-if="reportDetail" class="empty-content"><el-empty description="报告内容为空" /></div>
    <div v-else class="loading-state"><span>加载报告...</span></div>

    <div class="match-section" v-if="matchResult && matchResult.totalScore !== undefined">
      <h3>匹配分析</h3>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-progress type="dashboard" :percentage="Math.round((matchResult.totalScore||0)*100)" :width="160">
            <span class="score-label">{{ Math.round((matchResult.totalScore||0)*100) }}分</span>
          </el-progress>
        </el-col>
        <el-col :span="12" class="dimension-list">
          <div v-for="(val,key) in matchResult.dimensions" :key="key" class="dim-row">
            <span class="dim-label">{{ {basic:'基础条件',hard_skill:'硬技能',soft_skill:'软技能',potential:'潜力',hardSkill:'硬技能',softSkill:'软技能'}[key]||key }}</span>
            <el-progress :percentage="Math.round((val||0)*100)" :stroke-width="8" :color="dimColor(key)" />
          </div>
        </el-col>
      </el-row>
      <div class="gap-analysis" v-if="matchResult.gapAnalysis">
        <h4>差距分析</h4>
        <p>{{ matchResult.gapAnalysis }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request.js'

const route = useRoute()
const reportId = computed(() => route.params.id)
const loading = ref(true)
const reportDetail = ref(null)
const reportStatus = ref('')
const matchResult = ref(null)
let pollTimer = null
let pollTimeout = null

const renderedContent = computed(() => {
  if (!reportDetail.value?.content) return ''
  return reportDetail.value.content.replace(/\n/g, '<br/>')
})

function dimColor(key) {
  const map = { basic: '#3B82D9', hard_skill: '#34D399', soft_skill: '#F59E0B', potential: '#EF4444', hardSkill: '#34D399', softSkill: '#F59E0B' }
  return map[key] || '#3B82D9'
}

async function loadReport() {
  loading.value = true
  try {
    const data = await request.get('/api/report/' + reportId.value)
    const r = data.data || data
    if (r) {
      reportStatus.value = r.status || ''
      if (r.status === 'complete') {
        reportDetail.value = r
        loadMatchResult(r)
      } else if (r.status === 'failed') {
        reportDetail.value = r
      } else if (r.status === 'pending') {
        startPolling()
        loading.value = false
        return
      }
    }
  } catch (e) {} finally { loading.value = false }
}

async function loadMatchResult(r) {
  if (!r.matchResultJson || r.matchResultJson === '{}') return
  try {
    const mr = typeof r.matchResultJson === 'string' ? JSON.parse(r.matchResultJson) : r.matchResultJson
    matchResult.value = mr.data || mr
  } catch (e) {}
}

function startPolling() {
  pollTimer = setInterval(async () => {
    try {
      const data = await request.get('/api/report/' + reportId.value)
      const r = data.data || data
      reportStatus.value = r.status || ''
      if (r.status === 'complete') {
        stopPolling()
        reportDetail.value = r
        loadMatchResult(r)
        ElMessage.success('报告已生成')
      } else if (r.status === 'failed') {
        stopPolling()
        reportDetail.value = r
        ElMessage.error('报告生成失败')
      }
    } catch (e) {}
  }, 3000)
  pollTimeout = setTimeout(() => {
    stopPolling()
    ElMessage.warning('报告生成超时，请稍后在"我的"页面中查看')
  }, 120000)
}

function stopPolling() {
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
  if (pollTimeout) { clearTimeout(pollTimeout); pollTimeout = null }
}

onMounted(() => { loadReport() })
onUnmounted(() => { stopPolling() })
</script>

<style scoped>
.report-detail-page { max-width: 900px; margin: 0 auto; }
.page-back { margin-bottom: 24px; }
.status-bar { margin-bottom: 20px; display: flex; align-items: center; gap: 12px; }
.status-hint { font-size: 14px; color: #94a3b8; }
.report-content { background: #fff; padding: 32px; border-radius: 12px; border: 1px solid #e2e8f0; line-height: 2; font-size: 15px; color: #334155; }
.empty-content, .loading-state { text-align: center; padding: 48px 0; color: #94a3b8; }
.match-section { margin-top: 24px; background: #fff; padding: 24px; border-radius: 12px; border: 1px solid #e2e8f0; }
.match-section h3 { font-size: 18px; color: #1e3a5f; margin-bottom: 20px; }
.score-label { font-size: 28px; font-weight: 700; color: #3B82D9; }
.dimension-list { display: flex; flex-direction: column; gap: 12px; }
.dim-row { display: flex; align-items: center; gap: 12px; }
.dim-label { width: 80px; font-size: 13px; color: #64748b; flex-shrink: 0; }
.gap-analysis { margin-top: 20px; padding: 16px; background: #f8fafc; border-radius: 8px; }
.gap-analysis h4 { font-size: 15px; color: #1e3a5f; margin-bottom: 8px; }
.gap-analysis p { font-size: 14px; color: #475569; line-height: 1.8; }
</style>
