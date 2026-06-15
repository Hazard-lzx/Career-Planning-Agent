<template>
  <div class="my-page">
    <h2 class="page-title"><el-icon><UserFilled /></el-icon>我的</h2>

    <el-card class="info-card">
      <div class="user-row">
        <div class="user-avatar"><el-icon :size="36" color="#3B82D9"><UserFilled /></el-icon></div>
        <div class="user-text">
          <h3>{{ username }}</h3>
          <p>学生ID：{{ studentId || '请先在简历录入页面上传简历' }}</p>
        </div>
        <el-button text type="danger" @click="logout">退出</el-button>
      </div>
    </el-card>

    <el-card class="section-card" v-if="hasProfile" v-loading="loading">
      <template #header><span class="card-title"><el-icon><DataAnalysis /></el-icon>能力画像</span></template>
      <div class="section-label">教育背景</div>
      <div class="info-row">
        <span><strong>学历：</strong>{{ profile.education.degree || '未填写' }}</span>
        <span><strong>专业：</strong>{{ profile.education.major || '未填写' }}</span>
        <span><strong>学校：</strong>{{ profile.education.school || '未填写' }}</span>
      </div>
      <div class="section-label">专业技能</div>
      <div class="tag-row">
        <el-tag v-for="s in profile.skills" :key="s" round size="small">{{ s }}</el-tag>
        <span v-if="!profile.skills.length" class="no-data">暂无</span>
      </div>
      <div class="section-label">证书</div>
      <div class="tag-row">
        <el-tag v-for="c in profile.certificates" :key="c" type="success" round size="small">{{ c }}</el-tag>
        <span v-if="!profile.certificates.length" class="no-data">暂无</span>
      </div>
      <div class="section-label">软技能</div>
      <div class="info-row">
        <span><strong>沟通：</strong>{{ profile.softSkills.communication || '未评估' }}</span>
        <span><strong>学习：</strong>{{ profile.softSkills.learning || '未评估' }}</span>
        <span><strong>抗压：</strong>{{ profile.softSkills.pressure || '未评估' }}</span>
      </div>
    </el-card>

    <el-card class="section-card" v-if="!hasProfile && studentId">
      <el-empty description="暂无画像，请去简历录入上传简历" />
    </el-card>

    <el-card class="section-card" v-if="studentId">
      <template #header>
        <div class="card-header">
          <span class="card-title"><el-icon><Document /></el-icon>我的报告</span>
          <div class="header-actions">
            <span v-if="!batchMode && reports.length" class="card-count">共 {{ reports.length }} 份</span>
            <span v-if="batchMode" class="card-count">已选 {{ selectedReports.length }} 份</span>
            <el-button v-if="!batchMode && reports.length" size="small" text @click="enterBatch">批量管理</el-button>
            <template v-if="batchMode">
              <el-button size="small" type="danger" @click="batchDelete" plain>删除</el-button>
              <el-button size="small" @click="cancelBatch">取消</el-button>
            </template>
          </div>
        </div>
      </template>
      <div v-if="!reports.length" class="no-data" style="text-align:center;padding:24px 0">暂无报告</div>
      <div v-else class="report-list">
        <div v-for="r in reports" :key="r.id" class="report-row" :class="{ batchRow: batchMode }">
          <el-checkbox v-if="batchMode" v-model="selectedReports" :value="r.id" class="select-check" />
          <div class="report-info" @click="!batchMode && openReport(r.id)">
            <el-tag :type="r.status==='complete'?'success':r.status==='failed'?'danger':'warning'" size="small">{{ r.status==='complete'?'已完成':r.status==='failed'?'失败':'生成中' }}</el-tag>
            <span>目标岗位 ID：{{ r.targetJobId }}</span>
            <span class="report-date">{{ fmtDate(r.createdAt) }}</span>
          </div>
          <el-button v-if="!batchMode" text type="danger" size="small" :icon="Delete" @click.stop="deleteOne(r.id)" class="del-btn" />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { authStore } from '../stores/auth.js'
import request from '../utils/request.js'

const router = useRouter()
const username = ref(authStore.username || '用户')
const studentId = ref(localStorage.getItem('studentId') ? Number(localStorage.getItem('studentId')) : null)
const loading = ref(false)
const reports = ref([])
const selectedReports = ref([])
const batchMode = ref(false)

const profile = reactive({
  education: { degree: '', major: '', school: '' },
  skills: [],
  certificates: [],
  softSkills: { communication: '', learning: '', pressure: '' }
})

const hasProfile = computed(() => {
  return profile.education.degree || profile.skills.length > 0 || profile.certificates.length > 0
})

async function loadProfile() {
  if (!studentId.value) return
  loading.value = true
  try {
    const data = await request.get('/api/student/profile/' + studentId.value)
    const s = data.data || data
    if (!s) return
    if (s.profileJson) {
      try {
        const p = typeof s.profileJson === 'string' ? JSON.parse(s.profileJson) : s.profileJson
        if (p.education) Object.assign(profile.education, p.education)
        if (p.skills) profile.skills = p.skills
        if (p.certificates) profile.certificates = p.certificates
        if (p.softSkills || p.soft_skills) Object.assign(profile.softSkills, p.softSkills || p.soft_skills)
      } catch (e) {}
    }
  } catch (e) {} finally { loading.value = false }
}

async function loadReports() {
  if (!studentId.value) return
  try {
    const data = await request.get('/api/report/list/' + studentId.value)
    reports.value = Array.isArray(data.data) ? data.data : (data.data || [])
  } catch (e) {}
}

function openReport(id) { router.push('/report/' + id) }
function fmtDate(d) { if (!d) return ''; return new Date(d).toLocaleDateString('zh-CN') }
function logout() { authStore.logout(); router.push('/login') }

function enterBatch() { batchMode.value = true; selectedReports.value = [] }
function cancelBatch() { batchMode.value = false; selectedReports.value = [] }

async function deleteOne(id) {
  try {
    await ElMessageBox.confirm('确认删除该报告？', '提示', { type: 'warning' })
    await request.delete('/api/report/' + id)
    ElMessage.success('已删除')
    loadReports()
  } catch (e) {}
}

async function batchDelete() {
  if (!selectedReports.value.length) return
  try {
    await ElMessageBox.confirm('确认删除选中的 ' + selectedReports.value.length + ' 份报告？', '批量删除', { type: 'warning' })
    for (const id of selectedReports.value) { await request.delete('/api/report/' + id) }
    ElMessage.success('已删除')
    cancelBatch()
    loadReports()
  } catch (e) {}
}

onMounted(() => { loadProfile(); loadReports() })
</script>

<style scoped>
.my-page { max-width: 800px; margin: 0 auto; }
.info-card { margin-bottom: 24px; }
.user-row { display: flex; align-items: center; gap: 16px; }
.user-avatar { width: 52px; height: 52px; border-radius: 50%; background: #EFF6FF; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.user-text { flex: 1; }
.user-text h3 { font-size: 18px; color: #1e3a5f; margin-bottom: 2px; }
.user-text p { font-size: 13px; color: #94a3b8; }
.section-card { margin-bottom: 24px; }
.card-header { display: flex; justify-content: space-between; align-items: center; width: 100%; }
.card-title { font-size: 15px; font-weight: 700; color: #1e3a5f; display: flex; align-items: center; gap: 8px; }
.header-actions { display: flex; align-items: center; gap: 12px; }
.card-count { font-size: 13px; color: #94a3b8; }
.section-label { font-size: 13px; font-weight: 600; color: #64748b; margin: 12px 0 6px; padding-bottom: 4px; border-bottom: 1px solid #f0f4fa; }
.info-row { display: flex; gap: 24px; flex-wrap: wrap; font-size: 14px; color: #334155; }
.info-row strong { color: #1e3a5f; }
.tag-row { display: flex; flex-wrap: wrap; gap: 8px; }
.no-data { font-size: 13px; color: #94a3b8; }
.report-list { display: flex; flex-direction: column; gap: 8px; }
.report-row { display: flex; align-items: center; gap: 12px; padding: 10px 16px; background: #FAFCFF; border-radius: 8px; border: 1px solid #f0f4fa; }
.report-row:hover { border-color: #BFDBFE; }
.report-row.batchRow { background: #f8fafc; }
.report-info { flex: 1; display: flex; align-items: center; gap: 12px; cursor: pointer; font-size: 14px; color: #475569; }
.report-date { margin-left: auto; font-size: 12px; color: #94a3b8; }
.del-btn { flex-shrink: 0; opacity: 0; transition: opacity .2s; }
.report-row:hover .del-btn { opacity: 1; }
.select-check { flex-shrink: 0; }
</style>
