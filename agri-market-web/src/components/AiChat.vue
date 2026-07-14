<template>
  <div class="ai-chat">
    <el-button v-if="!open" class="ai-fab" type="primary" circle @click="open = true">
      <el-icon><Service /></el-icon>
    </el-button>

    <div v-else class="ai-panel">
      <div class="ai-head">
        <div>
          <div class="ai-title">AI 客服</div>
          <div class="ai-sub">商品、下单、订单都可以问我</div>
        </div>
        <el-button text :icon="Close" @click="open = false" />
      </div>

      <div ref="bodyRef" class="ai-body">
        <div v-for="(msg, index) in messages" :key="index" :class="['ai-msg', msg.role]">
          {{ msg.content || '正在思考...' }}
        </div>
      </div>

      <div class="ai-input">
        <el-input
          v-model="text"
          placeholder="请输入问题..."
          :disabled="loading"
          @keyup.enter="send"
        />
        <el-button type="primary" :loading="loading" @click="send">发送</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { nextTick, ref } from 'vue'
import { Close, Service } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { aiApi } from '../api'

const open = ref(false)
const loading = ref(false)
const text = ref('')
const bodyRef = ref()
const messages = ref([
  { role: 'assistant', content: '您好，我是 AI 客服。您可以问我商品、购物车、收藏、下单和订单相关问题。' }
])

const scrollBottom = async () => {
  await nextTick()
  if (bodyRef.value) {
    bodyRef.value.scrollTop = bodyRef.value.scrollHeight
  }
}

const buildHistory = () => {
  return messages.value
    .filter((msg) => msg.content)
    .slice(-10)
    .map((msg) => ({
      role: msg.role,
      content: msg.content
    }))
}

const send = async () => {
  const content = text.value.trim()
  if (!content || loading.value) return

  const history = buildHistory()
  messages.value.push({ role: 'user', content })
  messages.value.push({ role: 'assistant', content: '' })
  const assistantIndex = messages.value.length - 1
  text.value = ''
  loading.value = true
  await scrollBottom()

  try {
    const reply = await aiApi.chatStream(content, history, (_chunk, fullText) => {
      messages.value[assistantIndex].content = fullText
      scrollBottom()
    })
    if (!reply) {
      messages.value[assistantIndex].content = '我暂时没有理解您的问题，请换个说法试试。'
    }
  } catch (e) {
    ElMessage.error('AI 客服暂时不可用')
    messages.value[assistantIndex].content = '客服暂时不可用，您可以稍后再试。'
  } finally {
    loading.value = false
    scrollBottom()
  }
}
</script>

<style scoped>
.ai-chat { position: fixed; right: 24px; bottom: 24px; z-index: 2000; }
.ai-fab { width: 52px; height: 52px; font-size: 22px; box-shadow: 0 10px 24px rgba(46, 125, 50, 0.24); }
.ai-panel { width: 360px; height: 480px; background: #fff; border: 1px solid #e8eee8; border-radius: 10px; box-shadow: 0 18px 45px rgba(31,45,61,.18); display: flex; flex-direction: column; overflow: hidden; }
.ai-head { height: 64px; padding: 12px 14px; background: #f4faf4; border-bottom: 1px solid #e8eee8; display: flex; align-items: center; justify-content: space-between; }
.ai-title { font-size: 15px; font-weight: 700; color: #1f2d3d; }
.ai-sub { margin-top: 2px; font-size: 12px; color: #7a8a7a; }
.ai-body { flex: 1; padding: 14px; overflow-y: auto; background: #fbfdfb; }
.ai-msg { max-width: 82%; margin-bottom: 10px; padding: 9px 11px; border-radius: 8px; font-size: 13px; line-height: 1.5; white-space: pre-wrap; }
.ai-msg.assistant { background: #eef7ee; color: #213321; }
.ai-msg.user { margin-left: auto; background: #2e7d32; color: #fff; }
.ai-input { padding: 10px; border-top: 1px solid #e8eee8; display: flex; gap: 8px; }

@media (max-width: 520px) {
  .ai-chat { right: 12px; bottom: 12px; }
  .ai-panel { width: calc(100vw - 24px); height: min(480px, calc(100vh - 24px)); }
}
</style>
