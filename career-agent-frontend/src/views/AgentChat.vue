<template>
  <div class="agent-chat-page">
    <div class="page-title">
      <el-icon :size="28" color="#3B82D9"><ChatDotRound /></el-icon>
      智能规划助手
    </div>
    <p class="page-subtitle">告诉Agent你的目标，它会自动帮你分析简历、匹配岗位、生成规划报告</p>

    <div class="chat-container">
      <div class="chat-messages" ref="msgBox">
        <div v-if="messages.length === 0" class="chat-empty">
          <el-icon :size="48" color="#93C5FD"><ChatLineRound /></el-icon>
          <p>你好！我是你的职业规划Agent。</p>
          <p>试试告诉我：<br><em>我想到杭州做Java后端开发</em><br><em>帮我分析我的简历匹配度</em></p>
        </div>
        <div v-for="(msg, idx) in messages" :key="idx" :class="['msg-row', msg.role]">
          <div class="msg-avatar">
            <el-icon v-if="msg.role === 'user'" :size="20"><User /></el-icon>
            <el-icon v-else :size="20"><Compass /></el-icon>
          </div>
          <div class="msg-bubble" v-html="msg.content"></div>
        </div>
        <div v-if="loading" class="msg-row assistant">
          <div class="msg-avatar"><el-icon :size="20"><Compass /></el-icon></div>
          <div class="msg-bubble typing"><span></span><span></span><span></span></div>
        </div>
      </div>

      <div class="chat-input-area">
        <el-input v-model="inputText" placeholder="输入你的职业规划问题..." :rows="2" type="textarea" resize="none" @keydown.enter.exact.prevent="send" />
        <el-button type="primary" :disabled="!inputText.trim() || loading" @click="send">发送 <el-icon><Promotion /></el-icon></el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'

const inputText = ref('')
const messages = ref([])
const loading = ref(false)
const msgBox = ref(null)

const SESSION_KEY = 'agent_session'
const MSG_KEY = 'agent_messages'

let sessionId = localStorage.getItem(SESSION_KEY)
if (!sessionId) {
  sessionId = 'agent-' + Date.now()
  localStorage.setItem(SESSION_KEY, sessionId)
}

onMounted(() => {
  const saved = localStorage.getItem(MSG_KEY)
  if (saved) {
    try { messages.value = JSON.parse(saved) } catch (e) {}
  }
})

async function scrollDown() {
  await nextTick()
  if (msgBox.value) msgBox.value.scrollTop = msgBox.value.scrollHeight
}

function escapeHtml(s) {
  return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}

function formatReply(text) {
  return text.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>').replace(/\n/g, '<br>')
}

async function send() {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: escapeHtml(text) })
  inputText.value = ''
  loading.value = true
  saveMessages()
  await scrollDown()

  try {
    const res = await fetch('/api/agent/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + localStorage.getItem('career_agent_token')
      },
      body: JSON.stringify({ sessionId, message: text })
    })
    const json = await res.json()
    const reply = json?.data || json?.message || 'Agent收到，正在分析中...'
    messages.value.push({ role: 'assistant', content: formatReply(reply) })
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '请求失败：' + (e.message || '未知错误') })
  } finally {
    loading.value = false
    saveMessages()
    await scrollDown()
  }
}

function saveMessages() {
  try {
    localStorage.setItem(MSG_KEY, JSON.stringify(messages.value.slice(-50)))
  } catch (e) {}
}
</script>

<style scoped>
.agent-chat-page { max-width: 900px; margin: 0 auto; }
.page-subtitle { color: #64748b; font-size: 14px; margin-bottom: 24px; margin-top: -16px; }
.chat-container { background: #fff; border-radius: 16px; border: 1px solid #e2e8f0; box-shadow: 0 2px 12px rgba(100,149,237,0.06); overflow: hidden; display: flex; flex-direction: column; height: calc(100vh - 260px); min-height: 500px; }
.chat-messages { flex: 1; overflow-y: auto; padding: 24px; display: flex; flex-direction: column; gap: 16px; }
.chat-empty { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #94a3b8; text-align: center; gap: 8px; font-size: 14px; }
.chat-empty em { color: #3B82D9; font-style: normal; }
.msg-row { display: flex; gap: 10px; align-items: flex-start; }
.msg-row.user { flex-direction: row-reverse; }
.msg-avatar { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.msg-row.user .msg-avatar { background: linear-gradient(135deg, #5BA7E7, #3B82D9); color: #fff; }
.msg-row.assistant .msg-avatar { background: linear-gradient(135deg, #818cf8, #6366f1); color: #fff; }
.msg-bubble { max-width: 75%; padding: 12px 16px; border-radius: 16px; font-size: 14px; line-height: 1.7; word-break: break-word; }
.msg-row.user .msg-bubble { background: linear-gradient(135deg, #3B82D9, #5BA7E7); color: #fff; border-bottom-right-radius: 4px; }
.msg-row.assistant .msg-bubble { background: #f1f5f9; color: #334155; border-bottom-left-radius: 4px; }
.typing span { display: inline-block; width: 8px; height: 8px; border-radius: 50%; background: #94a3b8; margin: 0 3px; animation: typing-bounce 1.4s infinite ease-in-out both; }
.typing span:nth-child(1) { animation-delay: -0.32s; }
.typing span:nth-child(2) { animation-delay: -0.16s; }
.typing span:nth-child(3) { animation-delay: 0s; }
@keyframes typing-bounce { 0%, 80%, 100% { transform: scale(0.4); opacity: 0.4; } 40% { transform: scale(1); opacity: 1; } }
.chat-input-area { border-top: 1px solid #e2e8f0; padding: 16px 24px; display: flex; gap: 12px; align-items: flex-end; background: #fafbfc; }
.chat-input-area .el-button { height: 44px; padding: 0 20px; display: flex; align-items: center; gap: 6px; white-space: nowrap; }
</style>
