<template>
  <div class="dashboard">
    <el-card class="hero" shadow="never">
      <div class="hero-title">欢迎回来，{{ userInfo.username || "同学" }}</div>
      <div class="hero-sub">今天也要坚持复习错题，掌握每一个薄弱点。</div>

      <div class="hero-actions">
        <el-button type="primary" size="large" @click="goReview">开始复习</el-button>
        <el-button plain size="large" @click="goAddQuestion">新增错题</el-button>
      </div>
    </el-card>

    <el-row :gutter="12" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card" shadow="never">
          <div class="stat-label">总错题数</div>
          <div class="stat-value">{{ stats.total }}</div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card" shadow="never">
          <div class="stat-label">当前待复习（到期）</div>
          <div class="stat-value warning">{{ reviewSummary.dueNowCount }}</div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card" shadow="never">
          <div class="stat-label">已掌握</div>
          <div class="stat-value success">{{ stats.mastered }}</div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card" shadow="never">
          <div class="stat-label">掌握率</div>
          <div class="stat-value">{{ stats.rate }}%</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="panel-row">
      <el-col :xs="24" :lg="14">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="panel-title">今日复习提醒</div>
          </template>

          <el-alert
            v-if="reviewSummary.dueNowCount > 0"
            type="warning"
            show-icon
            :closable="false"
            :title="`当前有 ${reviewSummary.dueNowCount} 道错题已到复习时间`"
            class="alert"
          />

          <el-alert
            v-else
            type="success"
            show-icon
            :closable="false"
            title="当前没有待复习错题，继续保持！"
            class="alert"
          />

          <div class="summary-lines">
            <div class="summary-line">
              <span>今日计划总量</span>
              <strong>{{ reviewSummary.dueTodayCount }}</strong>
            </div>
            <div class="summary-line">
              <span>逾期未复习</span>
              <strong class="danger">{{ reviewSummary.overdueCount }}</strong>
            </div>
          </div>

          <div class="next-time">
            下一次复习提醒：{{ formatTime(reviewSummary.nextReviewTime) }}
          </div>

          <div class="preview-list" v-if="reviewSummary.todayPreview.length > 0">
            <div class="preview-item" v-for="item in reviewSummary.todayPreview" :key="item.questionId">
              <div>
                <div class="preview-subject">{{ item.subject || "未分类" }}</div>
                <div class="preview-point">{{ item.knowledgePoint || "未标注知识点" }}</div>
              </div>
              <el-tag size="small" type="warning">待复习</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="10">
        <el-card shadow="never" class="panel-card quick-card">
          <template #header>
            <div class="panel-title">快捷操作</div>
          </template>

          <div class="quick-actions">
            <el-button type="primary" @click="goReview">开始复习</el-button>
            <el-button type="success" @click="goAddQuestion">录入错题</el-button>
            <el-button @click="goManage">错题管理</el-button>
            <el-button @click="goTags">标签管理</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive } from "vue";
import { useRouter } from "vue-router";
import request from "@/utils/request";
import { getReviewSummary } from "@/api/review";

const router = useRouter();

const userInfo = reactive({
  username: "",
});

const stats = reactive({
  total: 0,
  todayReview: 0,
  mastered: 0,
  rate: 0,
});

const reviewSummary = reactive({
  dueNowCount: 0,
  dueTodayCount: 0,
  overdueCount: 0,
  dueLaterTodayCount: 0,
  nextReviewTime: null,
  todayPreview: [],
});

onMounted(async () => {
  const user = JSON.parse(localStorage.getItem("user") || "null");
  if (user?.username) {
    userInfo.username = user.username;
  }

  await Promise.allSettled([loadStats(), loadReviewSummary()]);
});

async function loadStats() {
  try {
    const res = await request.get("/analysis/student/overview");
    Object.assign(stats, res || {});
  } catch (error) {
    console.error("加载统计失败", error);
  }
}

async function loadReviewSummary() {
  try {
    const res = await getReviewSummary();
    if (res.code !== 200) {
      return;
    }

    reviewSummary.dueNowCount = Number(res.data?.dueNowCount || 0);
    reviewSummary.dueTodayCount = Number(res.data?.dueTodayCount || 0);
    reviewSummary.overdueCount = Number(res.data?.overdueCount || 0);
    reviewSummary.dueLaterTodayCount = Number(res.data?.dueLaterTodayCount || 0);
    reviewSummary.nextReviewTime = res.data?.nextReviewTime || null;
    reviewSummary.todayPreview = res.data?.todayPreview || [];
  } catch (error) {
    console.error("加载复习提醒失败", error);
  }
}

function formatTime(value) {
  if (!value) return "暂无";
  return String(value).replace("T", " ");
}

function goReview() {
  router.push("/student/review");
}

function goAddQuestion() {
  router.push("/student/add");
}

function goManage() {
  router.push("/student/manage");
}

function goTags() {
  router.push("/student/tags");
}
</script>

<style scoped>
.dashboard {
  max-width: 1500px;
  margin: 0 auto;
  padding: 18px 16px 36px;
  background: linear-gradient(180deg, rgba(59, 130, 246, 0.06), rgba(255, 255, 255, 0) 260px);
}

.hero {
  border: none;
  border-radius: 14px;
  color: #fff;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 55%, #38bdf8 100%);
}

.hero :deep(.el-card__body) {
  padding: 20px;
}

.hero-title {
  font-size: 24px;
  font-weight: 900;
}

.hero-sub {
  margin-top: 8px;
  font-size: 13px;
  opacity: 0.92;
}

.hero-actions {
  margin-top: 16px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.stats-row,
.panel-row {
  margin-top: 12px;
}

.stat-card,
.panel-card {
  border-radius: 14px;
}

.stat-label {
  color: #64748b;
  font-size: 13px;
}

.stat-value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 800;
  color: #1d4ed8;
}

.stat-value.success {
  color: #16a34a;
}

.stat-value.warning {
  color: #f59e0b;
}

.panel-title {
  font-size: 16px;
  font-weight: 800;
}

.alert {
  margin-bottom: 10px;
}

.next-time {
  color: #475569;
  margin-bottom: 10px;
}

.summary-lines {
  margin-bottom: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.summary-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #334155;
  font-size: 14px;
}

.summary-line .danger {
  color: #dc2626;
}

.preview-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.preview-item {
  border: 1px solid rgba(15, 23, 42, 0.1);
  border-radius: 10px;
  padding: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.preview-subject {
  font-weight: 800;
  color: #0f172a;
}

.preview-point {
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
}

.quick-card {
  height: 100%;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

@media (max-width: 900px) {
  .quick-actions {
    grid-template-columns: 1fr;
  }
}
</style>
