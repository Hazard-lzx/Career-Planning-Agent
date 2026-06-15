<template>
  <el-container class="app-container">
    <el-header class="app-header">
      <div class="header-inner">
        <div class="header-left" @click="$router.push('/')">
          <div class="logo-box">
            <el-icon :size="22" color="#fff"><Compass /></el-icon>
          </div>
          <span class="app-title">Career Agent</span>
        </div>
        <el-menu :default-active="activeMenu" mode="horizontal" :ellipsis="false" class="header-menu" router>
          <el-menu-item index="/">
            <el-icon><HomeFilled /></el-icon>首页
          </el-menu-item>
          <el-menu-item index="/agent">
            <el-icon><ChatDotRound /></el-icon>智能助手
          </el-menu-item>
          <el-menu-item index="/profile">
            <el-icon><User /></el-icon>简历录入
          </el-menu-item>
          <el-menu-item index="/jobs">
            <el-icon><Briefcase /></el-icon>岗位探索
          </el-menu-item>
          <el-menu-item index="/my">
            <el-icon><UserFilled /></el-icon>我的
          </el-menu-item>
        </el-menu>
        <div class="header-right">
          <span v-if="authStore.isLoggedIn" class="user-info">
            <el-icon><UserFilled /></el-icon> {{ authStore.username }}
            <el-button text size="small" @click="handleLogout">退出</el-button>
          </span>
        </div>
      </div>
    </el-header>

    <el-main class="app-main">
      <router-view />
    </el-main>

    <el-footer class="app-footer">
      <div class="footer-inner">
        <span>基于AI的职业规划智能体</span>
      </div>
    </el-footer>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { authStore } from './stores/auth.js'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => {
  if (route.path.startsWith('/jobs')) return '/jobs'
  if (route.path.startsWith('/agent')) return '/agent'
  if (route.path.startsWith('/profile')) return '/profile'
  if (route.path.startsWith('/reports')) return '/reports'
  if (route.path.startsWith('/my')) return '/my'
  return '/'
})

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: 'Microsoft YaHei', 'PingFang SC', -apple-system, sans-serif; background-color: #f0f6ff; color: #334155; }
.app-container { min-height: 100vh; display: flex; flex-direction: column; }
.app-header { background: #ffffff; border-bottom: 1px solid #e2e8f0; height: 64px !important; padding: 0; box-shadow: 0 1px 8px rgba(100, 149, 237, 0.08); position: sticky; top: 0; z-index: 100; }
.header-inner { max-width: 1400px; margin: 0 auto; display: flex; align-items: center; height: 100%; padding: 0 32px; }
.header-left { display: flex; align-items: center; gap: 12px; margin-right: 48px; cursor: pointer; }
.logo-box { width: 36px; height: 36px; border-radius: 10px; background: linear-gradient(135deg, #5BA7E7 0%, #3B82D9 100%); display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 8px rgba(59, 130, 217, 0.3); }
.app-title { font-size: 18px; font-weight: 700; color: #1e3a5f; white-space: nowrap; letter-spacing: 0.5px; }
.header-menu { border-bottom: none !important; flex: 1; background: transparent !important; }
.header-menu .el-menu-item { font-size: 14px; font-weight: 500; color: #64748b !important; border-bottom: 2px solid transparent !important; background: transparent !important; transition: color 0.2s, border-color 0.2s; height: 64px; line-height: 64px; }
.header-menu .el-menu-item:hover { color: #3B82D9 !important; background: #f0f6ff !important; }
.header-menu .el-menu-item.is-active { color: #3B82D9 !important; border-bottom-color: #3B82D9 !important; background: transparent !important; }
.header-right { display: flex; align-items: center; margin-left: auto; }
.user-info { display: flex; align-items: center; gap: 8px; font-size: 14px; color: #475569; white-space: nowrap; }
.app-main { flex: 1; padding: 28px 32px; max-width: 1400px; width: 100%; margin: 0 auto; }
.app-footer { text-align: center; color: #94a3b8; font-size: 13px; height: 52px !important; line-height: 52px; background-color: #ffffff; border-top: 1px solid #e2e8f0; }
.footer-inner { display: flex; align-items: center; justify-content: center; gap: 8px; }
.el-card { border-radius: 12px; border: 1px solid #e8f0fe; box-shadow: 0 1px 6px rgba(100, 149, 237, 0.06); transition: box-shadow 0.25s; }
.el-card:hover { box-shadow: 0 4px 16px rgba(100, 149, 237, 0.1); }
.el-card__header { border-bottom: 1px solid #f0f4fa; padding: 16px 20px; }
.page-title { font-size: 22px; font-weight: 700; color: #1e3a5f; margin-bottom: 24px; display: flex; align-items: center; gap: 10px; }
.page-title .el-icon { font-size: 24px; color: #3B82D9; }
.el-button--primary { --el-button-bg-color: #3B82D9; --el-button-border-color: #3B82D9; --el-button-hover-bg-color: #5BA7E7; --el-button-hover-border-color: #5BA7E7; }
:root { --el-color-primary: #3B82D9; --el-color-primary-light-3: #5BA7E7; --el-color-primary-light-5: #93C5FD; --el-color-primary-light-7: #BFDBFE; --el-color-primary-light-9: #EFF6FF; --el-color-primary-dark-2: #2563B0; }
</style>
