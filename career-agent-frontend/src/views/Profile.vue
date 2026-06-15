<template>
  <div class="profile-page">
    <h2 class="page-title">
      <el-icon><User /></el-icon>个人中心 / 简历录入
    </h2>

    <!-- 简历上传区域 -->
    <el-card class="upload-card">
      <template #header>
        <div class="card-header">
          <span class="card-header-title">
            <el-icon class="title-icon"><Upload /></el-icon>上传简历
          </span>
          <span class="card-header-sub">支持 PDF / DOC / DOCX 格式</span>
        </div>
      </template>
      <el-upload
        ref="uploadRef"
        class="resume-upload"
        drag
        :action="uploadUrl"
        :headers="uploadHeaders"
        :before-upload="beforeUpload"
        :on-success="onUploadSuccess"
        :on-error="onUploadError"
        :limit="1"
        :auto-upload="true"
        accept=".pdf,.doc,.docx"
      >
        <div class="upload-inner">
          <div class="upload-icon-box">
            <el-icon :size="36" color="#3B82D9"><UploadFilled /></el-icon>
          </div>
          <div class="upload-text">
            <p class="upload-main">将简历文件拖到此处，或<em>点击上传</em></p>
            <p class="upload-hint">AI将自动解析简历内容，生成能力画像</p>
          </div>
        </div>
      </el-upload>
    </el-card>

    <!-- 能力画像编辑表单 -->
    <el-card class="profile-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="card-header-title">
            <el-icon class="title-icon"><DataAnalysis /></el-icon>能力画像
          </span>
          <el-button type="primary" @click="saveProfile" :loading="saving" round>
            <el-icon><Check /></el-icon>保存画像
          </el-button>
        </div>
      </template>

      <el-form :model="profile" label-width="100px" label-position="top">
        <!-- 教育背景 -->
        <div class="section-divider">
          <span class="section-dot"></span>
          <span class="section-text">教育背景</span>
        </div>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="学历">
              <el-select v-model="profile.education.degree" placeholder="选择学历" style="width: 100%">
                <el-option label="专科" value="专科" />
                <el-option label="本科" value="本科" />
                <el-option label="硕士" value="硕士" />
                <el-option label="博士" value="博士" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="专业">
              <el-input v-model="profile.education.major" placeholder="如：计算机科学与技术" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="学校">
              <el-input v-model="profile.education.school" placeholder="如：XX大学" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 技能标签 -->
        <div class="section-divider">
          <span class="section-dot"></span>
          <span class="section-text">技能</span>
        </div>
        <el-form-item label="专业技能">
          <div class="tag-input-area">
            <el-tag
              v-for="skill in profile.skills"
              :key="skill"
              closable
              effect="plain"
              @close="removeSkill(skill)"
              class="skill-tag"
            >
              {{ skill }}
            </el-tag>
            <el-input
              v-if="skillInputVisible"
              ref="skillInputRef"
              v-model="skillInputValue"
              size="small"
              style="width: 140px"
              placeholder="输入技能"
              @keyup.enter="addSkill"
              @blur="addSkill"
            />
            <el-button v-else size="small" round @click="showSkillInput">
              + 添加技能
            </el-button>
          </div>
        </el-form-item>

        <!-- 证书 -->
        <div class="section-divider">
          <span class="section-dot dot-green"></span>
          <span class="section-text">证书</span>
        </div>
        <el-form-item label="证书列表">
          <div class="tag-input-area">
            <el-tag
              v-for="cert in profile.certificates"
              :key="cert"
              closable
              type="success"
              effect="plain"
              @close="removeCertificate(cert)"
              class="skill-tag"
            >
              {{ cert }}
            </el-tag>
            <el-input
              v-if="certInputVisible"
              ref="certInputRef"
              v-model="certInputValue"
              size="small"
              style="width: 140px"
              placeholder="输入证书"
              @keyup.enter="addCertificate"
              @blur="addCertificate"
            />
            <el-button v-else size="small" round @click="showCertInput">
              + 添加证书
            </el-button>
          </div>
        </el-form-item>

        <!-- 软技能 -->
        <div class="section-divider">
          <span class="section-dot dot-purple"></span>
          <span class="section-text">软技能</span>
        </div>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="沟通能力">
              <el-select v-model="profile.soft_skills.communication" placeholder="选择等级" style="width: 100%">
                <el-option label="强" value="强" />
                <el-option label="中" value="中" />
                <el-option label="弱" value="弱" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="学习能力">
              <el-select v-model="profile.soft_skills.learning" placeholder="选择等级" style="width: 100%">
                <el-option label="强" value="强" />
                <el-option label="中" value="中" />
                <el-option label="弱" value="弱" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="抗压能力">
              <el-select v-model="profile.soft_skills.pressure" placeholder="选择等级" style="width: 100%">
                <el-option label="强" value="强" />
                <el-option label="中" value="中" />
                <el-option label="弱" value="弱" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 实习经历 -->
        <div class="section-divider">
          <span class="section-dot dot-cyan"></span>
          <span class="section-text">实习经历</span>
        </div>
        <div v-for="(intern, index) in profile.internships" :key="index" class="experience-block">
          <el-row :gutter="16">
            <el-col :span="6">
              <el-form-item label="公司名称">
                <el-input v-model="intern.company" placeholder="XX公司" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="职位">
                <el-input v-model="intern.position" placeholder="Java开发" />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="时长">
                <el-input v-model="intern.duration" placeholder="3个月" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="工作描述">
                <el-input v-model="intern.description" placeholder="负责..." />
              </el-form-item>
            </el-col>
            <el-col :span="2" class="remove-btn-col">
              <el-button type="danger" :icon="Delete" circle size="small" @click="removeInternship(index)" />
            </el-col>
          </el-row>
        </div>
        <el-button type="primary" plain round @click="addInternship" class="add-btn">
          <el-icon><Plus /></el-icon>添加实习经历
        </el-button>

        <!-- 项目经历 -->
        <div class="section-divider">
          <span class="section-dot dot-orange"></span>
          <span class="section-text">项目经历</span>
        </div>
        <div v-for="(proj, index) in profile.projects" :key="index" class="experience-block">
          <el-row :gutter="16">
            <el-col :span="7">
              <el-form-item label="项目名称">
                <el-input v-model="proj.name" placeholder="XX系统" />
              </el-form-item>
            </el-col>
            <el-col :span="5">
              <el-form-item label="角色">
                <el-input v-model="proj.role" placeholder="后端开发" />
              </el-form-item>
            </el-col>
            <el-col :span="10">
              <el-form-item label="项目描述">
                <el-input v-model="proj.description" placeholder="负责..." />
              </el-form-item>
            </el-col>
            <el-col :span="2" class="remove-btn-col">
              <el-button type="danger" :icon="Delete" circle size="small" @click="removeProject(index)" />
            </el-col>
          </el-row>
        </div>
        <el-button type="primary" plain round @click="addProject" class="add-btn">
          <el-icon><Plus /></el-icon>添加项目经历
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import request from '../utils/request'

const uploadRef = ref(null)
const uploadUrl = '/api/student/resume/upload'
function getUploadHeaders() { const t = localStorage.getItem('career_agent_token'); return t ? { Authorization: 'Bearer ' + t } : {} }
const uploadHeaders = getUploadHeaders()
const loading = ref(false)
const saving = ref(false)
const studentId = ref(null) // 上传简历后由后端返回

const profile = reactive({
  education: { degree: '', major: '', school: '' },
  skills: [],
  certificates: [],
  soft_skills: { communication: '', learning: '', pressure: '' },
  internships: [],
  projects: [],
})

const skillInputVisible = ref(false)
const skillInputValue = ref('')
const skillInputRef = ref(null)

function showSkillInput() {
  skillInputVisible.value = true
  nextTick(() => skillInputRef.value?.focus())
}

function addSkill() {
  const val = skillInputValue.value.trim()
  if (val && !profile.skills.includes(val)) profile.skills.push(val)
  skillInputVisible.value = false
  skillInputValue.value = ''
}

function removeSkill(skill) {
  profile.skills = profile.skills.filter((s) => s !== skill)
}

const certInputVisible = ref(false)
const certInputValue = ref('')
const certInputRef = ref(null)

function showCertInput() {
  certInputVisible.value = true
  nextTick(() => certInputRef.value?.focus())
}

function addCertificate() {
  const val = certInputValue.value.trim()
  if (val && !profile.certificates.includes(val)) profile.certificates.push(val)
  certInputVisible.value = false
  certInputValue.value = ''
}

function removeCertificate(cert) {
  profile.certificates = profile.certificates.filter((c) => c !== cert)
}

function addInternship() {
  profile.internships.push({ company: '', position: '', duration: '', description: '' })
}

function removeInternship(index) {
  profile.internships.splice(index, 1)
}

function addProject() {
  profile.projects.push({ name: '', role: '', description: '' })
}

function removeProject(index) {
  profile.projects.splice(index, 1)
}

function beforeUpload(file) {
  const validTypes = [
    'application/pdf',
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  ]
  if (!validTypes.includes(file.type)) {
    ElMessage.error('仅支持 PDF、DOC、DOCX 格式')
    return false
  }
  loading.value = true
  return true
}

function onUploadSuccess(response) {
  loading.value = false
  if (response) {
    // el-upload不走axios拦截器，需要手动解包Result格式
    const result = response.code !== undefined ? response.data : response
    if (!result) return
    // 保存学生ID
    if (result.id) {
      studentId.value = result.id
      localStorage.setItem('studentId', result.id)
    }
    // 解析profileJson
    if (result.profileJson) {
      try {
        const parsed = typeof result.profileJson === 'string' ? JSON.parse(result.profileJson) : result.profileJson
        fillProfile(parsed)
      } catch (e) {
        fillProfile(result)
      }
    } else {
      fillProfile(result)
    }
    ElMessage.success('简历解析成功，已填充能力画像')
  }
}

function onUploadError() {
  loading.value = false
  ElMessage.error('简历上传失败，请重试')
}

function fillProfile(data) {
  if (data.education) Object.assign(profile.education, data.education)
  if (data.skills) profile.skills = data.skills
  if (data.certificates) profile.certificates = data.certificates
  if (data.soft_skills) Object.assign(profile.soft_skills, data.soft_skills)
  if (data.internships) profile.internships = data.internships
  if (data.projects) profile.projects = data.projects
}

async function saveProfile() {
  if (!studentId.value) {
    ElMessage.warning('请先上传简历，系统会自动创建学生记录')
    return
  }
  saving.value = true
  try {
    // 后端期望RequestBody为profileJson字符串
    await request.put(`/api/student/profile/${studentId.value}`, JSON.stringify(profile), {
      headers: { 'Content-Type': 'application/json' },
    })
    ElMessage.success('画像保存成功')
  } catch (e) { } finally {
    saving.value = false
  }
}

async function loadProfile() {
  // 尝试从localStorage恢复studentId
  const savedId = localStorage.getItem('studentId')
  if (savedId) studentId.value = Number(savedId)
  if (!studentId.value) return // 没有studentId则显示空表单

  loading.value = true
  try {
    const data = await request.get(`/api/student/profile/${studentId.value}`)
    if (data) {
      // 后端返回Student对象，画像在profileJson字段中
      if (data.profileJson) {
        try {
          const parsed = typeof data.profileJson === 'string' ? JSON.parse(data.profileJson) : data.profileJson
          fillProfile(parsed)
        } catch (e) {
          fillProfile(data)
        }
      } else {
        fillProfile(data)
      }
    }
  } catch (e) {
    // 学生不存在时静默处理，显示空表单
  } finally {
    loading.value = false
  }
}

loadProfile()
</script>

<style scoped>
.profile-page {
  max-width: 1100px;
  margin: 0 auto;
}

.upload-card {
  margin-bottom: 24px;
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

/* 上传区域美化 */
.resume-upload :deep(.el-upload-dragger) {
  border: 2px dashed #BFDBFE;
  border-radius: 12px;
  background: #FAFCFF;
  padding: 32px;
  transition: border-color 0.2s, background 0.2s;
}

.resume-upload :deep(.el-upload-dragger:hover) {
  border-color: #3B82D9;
  background: #F0F7FF;
}

.upload-inner {
  display: flex;
  align-items: center;
  gap: 20px;
}

.upload-icon-box {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: linear-gradient(135deg, #EFF6FF, #DBEAFE);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.upload-main {
  font-size: 15px;
  color: #334155;
  margin-bottom: 4px;
}

.upload-main em {
  color: #3B82D9;
  font-style: normal;
  font-weight: 600;
}

.upload-hint {
  font-size: 12px;
  color: #94a3b8;
}

.profile-card {
  margin-bottom: 24px;
}

/* 自定义分隔线 */
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

.section-dot.dot-green { background: #34D399; }
.section-dot.dot-purple { background: #818CF8; }
.section-dot.dot-cyan { background: #38BDF8; }
.section-dot.dot-orange { background: #FB923C; }

.section-text {
  font-size: 14px;
  font-weight: 700;
  color: #1e3a5f;
}

/* 标签输入区域 */
.tag-input-area {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.skill-tag {
  margin: 0;
  border-radius: 6px;
}

/* 经历块 */
.experience-block {
  margin-bottom: 12px;
  padding: 16px 20px;
  background: #FAFCFF;
  border-radius: 10px;
  border: 1px solid #f0f4fa;
  transition: border-color 0.2s;
}

.experience-block:hover {
  border-color: #DBEAFE;
}

.remove-btn-col {
  display: flex;
  align-items: flex-end;
  padding-bottom: 4px;
}

.add-btn {
  margin-top: 8px;
}
</style>
