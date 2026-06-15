import { reactive } from 'vue'

const TOKEN_KEY = 'career_agent_token'
const USER_KEY = 'career_agent_user'

export const authStore = reactive({
  token: localStorage.getItem(TOKEN_KEY) || '',
  username: localStorage.getItem(USER_KEY) || '',
  isLoggedIn: !!localStorage.getItem(TOKEN_KEY),

  login(token, username) {
    this.token = token
    this.username = username
    this.isLoggedIn = true
    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(USER_KEY, username)
  },

  logout() {
    this.token = ''
    this.username = ''
    this.isLoggedIn = false
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }
})
