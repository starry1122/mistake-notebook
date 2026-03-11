<template>
  <div class="analysis-page">
    <el-card class="hero" shadow="never">
      <div class="hero-content">
        <div>
          <div class="hero-title">学情分析</div>
          <div class="hero-sub">
            从科目分布、错误原因与知识点薄弱项三个维度进行统计，并自动生成文本分析报告。
          </div>
        </div>
        <el-button type="primary" :icon="Refresh" :loading="loading" @click="loadDashboard">
          刷新分析
        </el-button>
      </div>
    </el-card>

    <el-card class="filter-card" shadow="never">
      <div class="filter-row">
        <el-form :inline="true" class="filter-form">
          <el-form-item label="分析科目">
            <el-select
              v-model="query.subject"
              placeholder="全部科目"
              clearable
              filterable
              style="width: 190px"
            >
              <el-option label="全部科目" value="" />
              <el-option v-for="subject in subjectOptions" :key="subject" :label="subject" :value="subject" />
            </el-select>
          </el-form-item>

          <el-form-item label="薄弱项数量">
            <el-select v-model="query.topN" style="width: 140px">
              <el-option :value="3" label="前 3 个" />
              <el-option :value="5" label="前 5 个" />
              <el-option :value="8" label="前 8 个" />
              <el-option :value="10" label="前 10 个" />
            </el-select>
          </el-form-item>
        </el-form>

        <div class="filter-actions">
          <el-button type="primary" :loading="loading" @click="loadDashboard">开始分析</el-button>
          <el-button @click="resetFilters">重置条件</el-button>
        </div>
      </div>

      <div class="summary-row">
        <div class="summary-item">分析范围：{{ query.subject || "全部科目" }}</div>
        <div class="summary-item">错题总量：{{ dashboard.totalWrongCount }}</div>
        <div class="summary-item">报告时间：{{ formatTime(dashboard.report.createTime) }}</div>
      </div>
    </el-card>

    <el-row :gutter="14" class="panel-row">
      <el-col :xs="24" :lg="12">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">各科错题数量统计</div>
          </template>

          <div v-if="dashboard.subjectStats.length > 0" class="bar-list">
            <div v-for="item in dashboard.subjectStats" :key="item.name" class="bar-item">
              <div class="bar-head">
                <span class="bar-name">{{ item.name }}</span>
                <span class="bar-count">{{ item.count }} 题（{{ barWidth(item.percent) }}%）</span>
              </div>
              <div class="bar-track">
                <div class="bar-fill subject-fill" :style="{ width: `${barWidth(item.percent)}%` }" />
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无统计数据" :image-size="90" />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">错误原因分布分析</div>
          </template>

          <div v-if="dashboard.errorReasonStats.length > 0" class="reason-list">
            <div v-for="item in dashboard.errorReasonStats" :key="item.name" class="reason-item">
              <div class="reason-title">
                <span>{{ item.name }}</span>
                <span>{{ item.count }} 次</span>
              </div>
              <el-progress
                :stroke-width="12"
                :percentage="barWidth(item.percent)"
                :format="(value) => `${value}%`"
                color="#f59e0b"
              />
            </div>
          </div>
          <el-empty v-else description="暂无统计数据" :image-size="90" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="14" class="panel-row">
      <el-col :xs="24" :lg="14">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-title">知识点薄弱项识别（Top {{ query.topN }}）</div>
          </template>

          <div v-if="dashboard.weakKnowledgePoints.length > 0" class="weak-list">
            <div
              v-for="(item, index) in dashboard.weakKnowledgePoints"
              :key="`${item.name}-${index}`"
              class="weak-item"
            >
              <div class="weak-rank">{{ index + 1 }}</div>
              <div class="weak-main">
                <div class="weak-name">{{ item.name }}</div>
                <div class="weak-meta">
                  未掌握 {{ item.unmasteredCount }} / {{ item.totalCount }} · 平均难度 {{ item.avgDifficulty || 0 }}
                </div>
                <el-progress
                  :percentage="barWidth(item.masteryRate)"
                  :stroke-width="10"
                  :format="(value) => `掌握率 ${value}%`"
                  color="#22c55e"
                />
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无薄弱知识点" :image-size="90" />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="10">
        <el-card class="panel-card report-card" shadow="never">
          <template #header>
            <div class="panel-title">学情分析文本报告</div>
          </template>

          <div class="report-content">
            {{ dashboard.report.content || "暂无报告内容" }}
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { Refresh } from "@element-plus/icons-vue";
import { listQuestionSubjects } from "@/api/question";
import { getStudentAnalysisDashboard } from "@/api/analysis";

const loading = ref(false);
const subjectOptions = ref([]);

const query = reactive({
  subject: "",
  topN: 5,
});

const dashboard = reactive({
  totalWrongCount: 0,
  subjectStats: [],
  errorReasonStats: [],
  weakKnowledgePoints: [],
  report: {
    id: null,
    subject: null,
    content: "",
    createTime: null,
  },
});

onMounted(async () => {
  await Promise.allSettled([loadSubjectOptions(), loadDashboard()]);
});

async function loadSubjectOptions() {
  try {
    const res = await listQuestionSubjects();
    if (res?.code === 200 && Array.isArray(res.data)) {
      subjectOptions.value = res.data;
    }
  } catch (error) {
    console.error("加载科目列表失败", error);
  }
}

async function loadDashboard() {
  loading.value = true;
  try {
    const params = {
      topN: query.topN,
    };
    if (query.subject) {
      params.subject = query.subject;
    }

    const res = await getStudentAnalysisDashboard(params);
    const payload = normalizePayload(res);

    dashboard.totalWrongCount = Number(payload.totalWrongCount || 0);
    dashboard.subjectStats = Array.isArray(payload.subjectStats) ? payload.subjectStats : [];
    dashboard.errorReasonStats = Array.isArray(payload.errorReasonStats) ? payload.errorReasonStats : [];
    dashboard.weakKnowledgePoints = Array.isArray(payload.weakKnowledgePoints)
      ? payload.weakKnowledgePoints
      : [];

    const report = payload.report || {};
    dashboard.report.id = report.id || null;
    dashboard.report.subject = report.subject || null;
    dashboard.report.content = report.content || "";
    dashboard.report.createTime = report.createTime || null;
  } catch (error) {
    console.error("加载学情分析失败", error);
    ElMessage.error("加载学情分析失败，请稍后重试");
  } finally {
    loading.value = false;
  }
}

function resetFilters() {
  query.subject = "";
  query.topN = 5;
  loadDashboard();
}

function barWidth(value) {
  const number = Number(value || 0);
  return Math.max(0, Math.min(100, Math.round(number)));
}

function formatTime(value) {
  if (!value) {
    return "暂无";
  }
  return String(value).replace("T", " ");
}

function normalizePayload(res) {
  if (res && typeof res === "object" && "code" in res && "data" in res) {
    return res.data || {};
  }
  return res || {};
}
</script>

<style scoped>
.analysis-page {
  max-width: 1500px;
  margin: 0 auto;
  padding: 18px 16px 36px;
  background: linear-gradient(180deg, rgba(37, 99, 235, 0.07), rgba(255, 255, 255, 0) 300px);
}

.hero {
  border: none;
  border-radius: 14px;
  margin-bottom: 12px;
  color: #fff;
  background: linear-gradient(120deg, #1e3a8a 0%, #2563eb 55%, #22d3ee 100%);
}

.hero :deep(.el-card__body) {
  padding: 20px;
}

.hero-content {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
}

.hero-title {
  font-size: 24px;
  font-weight: 900;
}

.hero-sub {
  margin-top: 8px;
  max-width: 760px;
  font-size: 13px;
  opacity: 0.92;
  line-height: 1.6;
}

.filter-card,
.panel-card {
  border-radius: 14px;
}

.filter-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
}

.filter-actions {
  display: flex;
  gap: 10px;
}

.summary-row {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.summary-item {
  font-size: 13px;
  color: #334155;
  background: #f8fafc;
  border-radius: 999px;
  padding: 6px 12px;
}

.panel-row {
  margin-top: 12px;
}

.panel-title {
  font-size: 16px;
  font-weight: 800;
}

.bar-list,
.reason-list,
.weak-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.bar-item,
.reason-item {
  padding: 10px;
  border-radius: 10px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: #ffffff;
}

.bar-head,
.reason-title {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
  font-size: 13px;
}

.bar-name {
  font-weight: 700;
  color: #0f172a;
}

.bar-count {
  color: #475569;
}

.bar-track {
  height: 10px;
  border-radius: 999px;
  overflow: hidden;
  background: #e2e8f0;
}

.bar-fill {
  height: 100%;
  border-radius: inherit;
  transition: width 0.3s ease;
}

.subject-fill {
  background: linear-gradient(90deg, #3b82f6 0%, #0ea5e9 100%);
}

.weak-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 12px;
  padding: 12px;
  background: #fff;
}

.weak-rank {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #e0e7ff;
  color: #1d4ed8;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.weak-main {
  flex: 1;
}

.weak-name {
  font-size: 15px;
  color: #0f172a;
  font-weight: 800;
}

.weak-meta {
  margin-top: 5px;
  margin-bottom: 8px;
  color: #64748b;
  font-size: 13px;
}

.report-card {
  height: 100%;
}

.report-content {
  min-height: 320px;
  border-radius: 12px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  padding: 12px;
  line-height: 1.75;
  color: #1e293b;
  background: #f8fafc;
  white-space: pre-wrap;
}

@media (max-width: 900px) {
  .analysis-page {
    padding: 14px 6px 22px;
  }

  .hero-content {
    align-items: flex-start;
    flex-direction: column;
  }

  .filter-actions {
    width: 100%;
  }

  .filter-actions .el-button {
    flex: 1;
  }
}
</style>

