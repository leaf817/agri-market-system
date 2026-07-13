<template>
  <div class="stats-page" @mousemove="onPageMove">
    <!-- 总览卡片 -->
    <el-row :gutter="16" class="overview">
      <el-col :xs="12" :sm="12" :md="6" v-for="card in cards" :key="card.title" class="overview-col">
        <el-card shadow="hover" class="stat-card" :body-style="{ padding: '18px 20px' }">
          <div class="stat-inner">
            <div class="stat-icon" :style="{ background: card.bg, color: card.color }">
              <el-icon :size="24"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-title">{{ card.title }}</div>
              <div class="stat-value"><span v-if="card.prefix">{{ card.prefix }}</span>{{ card.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="block-row">
      <el-col :xs="24" :md="14">
        <el-card shadow="never" class="panel">
          <template #header>
            <div class="panel-head">
              <span>各品类销量对比</span>
              <span class="hint">鼠标悬停查看详情</span>
            </div>
          </template>
          <el-empty v-if="!byCategory.length" description="暂无销售数据" :image-size="64" />
          <div v-else class="bar-chart">
            <div
              v-for="(item, idx) in byCategory"
              :key="item.categoryName"
              class="bar-col"
              @mouseenter="showTip($event, categoryTip(item))"
              @mousemove="moveTip"
              @mouseleave="hideTip"
            >
              <div class="bar-value">{{ item.quantity }}</div>
              <div class="bar-track">
                <div
                  class="bar-fill"
                  :class="{ active: tip?.title === item.categoryName }"
                  :style="{ height: barHeight(item.quantity) + '%', background: palette[idx % palette.length] }"
                ></div>
              </div>
              <div class="bar-name">{{ item.categoryName }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="10">
        <el-card shadow="never" class="panel">
          <template #header>
            <div class="panel-head">
              <span>品类销量占比</span>
              <span class="hint">鼠标悬停扇区查看</span>
            </div>
          </template>
          <el-empty v-if="!pieSlices.length" description="暂无销售数据" :image-size="64" />
          <div v-else class="pie-wrap">
            <svg class="pie-svg" viewBox="0 0 120 120" @mouseleave="hideTip">
              <circle cx="60" cy="60" r="28" fill="#fff" />
              <path
                v-for="(slice, idx) in pieSlices"
                :key="slice.name"
                :d="slice.d"
                :fill="palette[idx % palette.length]"
                class="pie-slice"
                :class="{ active: tip?.title === slice.name }"
                @mouseenter="showTip($event, slice.tip)"
                @mousemove="moveTip"
                @mouseleave="hideTip"
              />
              <circle cx="60" cy="60" r="22" fill="#fff" />
              <text x="60" y="58" text-anchor="middle" class="pie-center-label">合计</text>
              <text x="60" y="72" text-anchor="middle" class="pie-center-value">{{ totalCategoryQty }}</text>
            </svg>
            <div class="pie-legend">
              <div
                v-for="(item, idx) in categoryShares"
                :key="item.categoryName"
                class="legend-item"
                :class="{ active: tip?.title === item.categoryName }"
                @mouseenter="showTip($event, categoryTip(item))"
                @mousemove="moveTip"
                @mouseleave="hideTip"
              >
                <span class="dot" :style="{ background: palette[idx % palette.length] }"></span>
                <span class="l-name">{{ item.categoryName }}</span>
                <span class="l-pct">{{ item.percent }}%</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="panel top-panel">
      <template #header>
        <div class="panel-head">
          <span>销量 TOP 商品</span>
          <span class="hint">鼠标悬停查看详情</span>
        </div>
      </template>
      <el-empty v-if="!topProducts.length" description="暂无销售数据" :image-size="64" />
      <div v-else class="rank-list">
        <div
          v-for="(row, idx) in topProducts"
          :key="row.name"
          class="rank-row"
          :class="{ active: tip?.title === row.name }"
          @mouseenter="showTip($event, topTip(row, idx))"
          @mousemove="moveTip"
          @mouseleave="hideTip"
        >
          <span class="rank" :class="`rank-${idx + 1}`">{{ idx + 1 }}</span>
          <div class="rank-main">
            <div class="rank-top">
              <span class="rank-name">{{ row.name }}</span>
              <span class="rank-meta">销量 {{ row.sales }}</span>
            </div>
            <div class="progress">
              <div class="progress-fill" :style="{ width: topWidth(row.sales) + '%', background: topColor(idx) }"></div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 跟随鼠标的浮层提示 -->
    <div v-if="tip" class="chart-tip" :style="{ left: tip.x + 'px', top: tip.y + 'px' }">
      <div class="tip-title">{{ tip.title }}</div>
      <div v-for="line in tip.lines" :key="line" class="tip-line">{{ line }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, markRaw } from 'vue'
import { Goods, Tickets, Histogram, Money } from '@element-plus/icons-vue'
import { statsApi } from '../api'

const overview = ref({ productCount: 0, orderCount: 0, totalQuantity: 0, totalAmount: 0 })
const byCategory = ref([])
const topProducts = ref([])
const tip = ref(null)
const pageOffset = ref({ left: 0, top: 0 })

const palette = ['#2e7d32', '#43a047', '#66bb6a', '#fb8c00', '#42a5f5', '#8e24aa']

const cards = computed(() => [
  { title: '在售商品', value: overview.value.productCount, icon: markRaw(Goods), bg: '#e8f5e9', color: '#2e7d32' },
  { title: '订单总数', value: overview.value.orderCount, icon: markRaw(Tickets), bg: '#e3f2fd', color: '#1565c0' },
  { title: '累计销量', value: overview.value.totalQuantity, icon: markRaw(Histogram), bg: '#fff3e0', color: '#ef6c00' },
  { title: '累计销售额', value: Number(overview.value.totalAmount), prefix: '¥', icon: markRaw(Money), bg: '#f3e5f5', color: '#7b1fa2' }
])

const maxCategoryQty = computed(() =>
  Math.max(1, ...byCategory.value.map((i) => Number(i.quantity) || 0))
)
const maxTopSales = computed(() =>
  Math.max(1, ...topProducts.value.map((i) => Number(i.sales) || 0))
)
const totalCategoryQty = computed(() =>
  byCategory.value.reduce((s, i) => s + (Number(i.quantity) || 0), 0)
)

const categoryShares = computed(() => {
  const total = totalCategoryQty.value || 1
  return byCategory.value.map((i) => ({
    ...i,
    percent: ((Number(i.quantity) || 0) / total * 100).toFixed(1)
  }))
})

/** SVG 环形扇区 */
const pieSlices = computed(() => {
  const total = totalCategoryQty.value
  if (!total) return []
  const cx = 60
  const cy = 60
  const r = 48
  let angle = -Math.PI / 2
  return byCategory.value.map((item) => {
    const value = Number(item.quantity) || 0
    const sweep = (value / total) * Math.PI * 2
    const start = angle
    const end = angle + sweep
    angle = end
    const x1 = cx + r * Math.cos(start)
    const y1 = cy + r * Math.sin(start)
    const x2 = cx + r * Math.cos(end)
    const y2 = cy + r * Math.sin(end)
    const large = sweep > Math.PI ? 1 : 0
    const d = `M ${cx} ${cy} L ${x1} ${y1} A ${r} ${r} 0 ${large} 1 ${x2} ${y2} Z`
    const percent = ((value / total) * 100).toFixed(1)
    return {
      name: item.categoryName,
      d,
      tip: {
        title: item.categoryName,
        lines: [`销量：${value}`, `占比：${percent}%`, `销售额：¥${formatAmount(item.amount)}`]
      }
    }
  })
})

const barHeight = (qty) => Math.max(8, ((Number(qty) || 0) / maxCategoryQty.value) * 100)
const topWidth = (sales) => Math.max(6, ((Number(sales) || 0) / maxTopSales.value) * 100)
const topColor = (idx) => {
  if (idx === 0) return 'linear-gradient(90deg, #ffd54f, #f9a825)'
  if (idx === 1) return 'linear-gradient(90deg, #cfd8dc, #90a4ae)'
  if (idx === 2) return 'linear-gradient(90deg, #ffccbc, #ff8a65)'
  return 'linear-gradient(90deg, #a5d6a7, #2e7d32)'
}
const formatAmount = (v) => Number(v || 0).toLocaleString('zh-CN', { maximumFractionDigits: 1 })

const categoryTip = (item) => ({
  title: item.categoryName,
  lines: [
    `销量：${item.quantity}`,
    `销售额：¥${formatAmount(item.amount)}`,
    `占比：${item.percent || ((Number(item.quantity) || 0) / (totalCategoryQty.value || 1) * 100).toFixed(1)}%`
  ]
})

const topTip = (row, idx) => ({
  title: row.name,
  lines: [`排名：第 ${idx + 1} 名`, `销量：${row.sales}`, `销售额：¥${formatAmount(row.amount)}`]
})

const showTip = (e, payload) => {
  tip.value = {
    ...payload,
    x: e.clientX - pageOffset.value.left + 14,
    y: e.clientY - pageOffset.value.top + 14
  }
}

const moveTip = (e) => {
  if (!tip.value) return
  tip.value = {
    ...tip.value,
    x: e.clientX - pageOffset.value.left + 14,
    y: e.clientY - pageOffset.value.top + 14
  }
}

const hideTip = () => { tip.value = null }

const onPageMove = (e) => {
  const el = e.currentTarget
  if (!el) return
  const rect = el.getBoundingClientRect()
  pageOffset.value = { left: rect.left, top: rect.top }
}

onMounted(async () => {
  const [o, c, t] = await Promise.all([statsApi.overview(), statsApi.byCategory(), statsApi.topProducts(5)])
  overview.value = o
  byCategory.value = c || []
  topProducts.value = t || []
})
</script>

<style scoped>
.stats-page { position: relative; }
.overview-col { margin-bottom: 16px; }
.stat-card { border-radius: 12px; transition: transform 0.18s ease, box-shadow 0.18s ease; }
.stat-card:hover { transform: translateY(-3px); }
.stat-inner { display: flex; align-items: center; gap: 16px; }
.stat-icon { width: 52px; height: 52px; flex: none; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.stat-title { font-size: 13px; color: #8a97a0; }
.stat-value { font-size: 26px; font-weight: 800; color: #1f2d3d; margin-top: 4px; }

.block-row { margin-top: 4px; }
.block-row .el-col { margin-bottom: 16px; }
.panel { border-radius: 12px; min-height: 340px; }
.top-panel { margin-top: 0; }
.panel-head { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.hint { font-size: 12px; color: #aab4c0; font-weight: 400; }

.bar-chart {
  display: flex; align-items: flex-end; justify-content: space-around;
  gap: 8px; min-height: 260px; padding: 8px 4px 0;
}
.bar-col {
  flex: 1; min-width: 0; display: flex; flex-direction: column; align-items: center;
  cursor: pointer;
}
.bar-value { font-size: 12px; font-weight: 700; color: #2e7d32; margin-bottom: 6px; }
.bar-track {
  width: 100%; max-width: 42px; height: 180px;
  background: #f1f5f1; border-radius: 10px 10px 4px 4px;
  display: flex; align-items: flex-end; overflow: hidden;
}
.bar-fill {
  width: 100%; border-radius: 10px 10px 4px 4px;
  transition: height 0.45s ease, filter 0.2s ease, transform 0.2s ease;
  transform-origin: bottom;
}
.bar-fill.active { filter: brightness(1.08); transform: scaleX(1.08); }
.bar-name {
  margin-top: 8px; font-size: 12px; color: #4a5a4a; font-weight: 600;
  max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; text-align: center;
}

.pie-wrap { display: flex; flex-direction: column; align-items: center; gap: 14px; padding: 4px 0; }
.pie-svg { width: 200px; height: 200px; }
.pie-slice { cursor: pointer; transition: opacity 0.2s ease, filter 0.2s ease; }
.pie-slice:hover, .pie-slice.active { filter: brightness(1.08); opacity: 0.92; }
.pie-center-label { font-size: 8px; fill: #8a97a0; }
.pie-center-value { font-size: 11px; font-weight: 700; fill: #2e7d32; }
.pie-legend { width: 100%; display: flex; flex-direction: column; gap: 6px; }
.legend-item {
  display: flex; align-items: center; gap: 8px; font-size: 13px;
  padding: 6px 8px; border-radius: 8px; cursor: pointer; transition: background 0.2s ease;
}
.legend-item:hover, .legend-item.active { background: #eef5ee; }
.dot { width: 10px; height: 10px; border-radius: 50%; flex: none; }
.l-name { flex: 1; color: #4a5a4a; }
.l-pct { color: #2e7d32; font-weight: 700; }

.rank-list { display: flex; flex-direction: column; gap: 12px; padding: 4px 0; }
.rank-row {
  display: flex; align-items: center; gap: 12px;
  padding: 8px 10px; border-radius: 10px; cursor: pointer;
  transition: background 0.2s ease, transform 0.2s ease;
}
.rank-row:hover, .rank-row.active { background: #f4faf4; transform: translateX(2px); }
.rank {
  display: inline-flex; align-items: center; justify-content: center;
  width: 26px; height: 26px; border-radius: 50%; flex: none;
  font-size: 12px; font-weight: 700; background: #eef0ee; color: #8a97a0;
}
.rank-1 { background: #ffd54f; color: #8a5a00; }
.rank-2 { background: #cfd8dc; color: #546e7a; }
.rank-3 { background: #ffccbc; color: #bf360c; }
.rank-main { flex: 1; min-width: 0; }
.rank-top { display: flex; justify-content: space-between; gap: 10px; margin-bottom: 6px; }
.rank-name { font-weight: 700; color: #1f2d3d; }
.rank-meta { font-size: 12px; color: #8a97a0; }
.progress { height: 12px; background: #eef3ee; border-radius: 999px; overflow: hidden; }
.progress-fill { height: 100%; border-radius: 999px; transition: width 0.45s ease; }

.chart-tip {
  position: absolute; z-index: 20; pointer-events: none;
  min-width: 140px; max-width: 240px;
  padding: 10px 12px; border-radius: 10px;
  background: rgba(33, 47, 33, 0.92); color: #fff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.18);
  backdrop-filter: blur(4px);
}
.tip-title { font-weight: 700; margin-bottom: 6px; font-size: 13px; }
.tip-line { font-size: 12px; opacity: 0.92; line-height: 1.7; }
</style>
