<template>
  <div class="page">
    <el-card class="hero" shadow="never">
      <div class="hero-title">智能复习</div>
      <div class="hero-sub">基于艾宾浩斯遗忘曲线安排复习，系统按统一口径自动区分当前、今日计划与逾期任务。</div>

      <div class="hero-stats">
        <div class="stat-item">
          <div class="stat-label">当前待复习</div>
          <div class="stat-value">{{ summary.dueNowCount }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">逾期未复习</div>
          <div class="stat-value danger">{{ summary.overdueCount }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">今日计划总量</div>
          <div class="stat-value">{{ summary.dueTodayCount }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">今日稍后任务</div>
          <div class="stat-value warning">{{ summary.dueLaterTodayCount }}</div>
        </div>
      </div>

      <div class="logic-tip">
        <el-tag size="small" effect="dark" type="primary">口径说明</el-tag>
        <span>当前待复习：截至现在已到期（含逾期）；今日计划：今天 00:00~23:59 内计划复习；逾期：早于今天的未完成任务。</span>
      </div>
    </el-card>

    <div class="main-grid">
      <el-card class="due-card" shadow="never" v-loading="loadingDue">
        <template #header>
          <div class="card-header">
            <span>{{ listTitle }}</span>
            <div class="header-actions">
              <el-radio-group v-model="listMode" size="small">
                <el-radio-button label="current">当前待复习</el-radio-button>
                <el-radio-button label="today">今日计划</el-radio-button>
              </el-radio-group>
              <el-button link type="primary" @click="refreshAll">刷新</el-button>
            </div>
          </div>
        </template>

        <el-empty
          v-if="activeList.length === 0"
          :description="listMode === 'current' ? '当前没有到期任务' : '今天暂无计划任务'"
        />

        <div v-else class="due-list">
          <div
            v-for="item in activeList"
            :key="item.questionId"
            class="due-item"
            :class="{ active: currentQuestion && currentQuestion.questionId === item.questionId }"
            @click="selectQuestion(item.questionId)"
          >
            <div class="due-head">
              <div class="due-subject">{{ item.subject || "未分类" }}</div>
              <el-tag size="small" :type="queueTagType(item.nextReviewTime)">{{ queueTagText(item.nextReviewTime) }}</el-tag>
            </div>
            <div class="due-point">{{ item.knowledgePoint || "未标注知识点" }}</div>
            <div class="due-time">计划时间：{{ formatTime(item.nextReviewTime) }}</div>
          </div>
        </div>
      </el-card>

      <el-card class="question-card" shadow="never" v-loading="loadingQuestion">
        <template #header>
          <div class="card-header">
            <span>错题重做</span>
            <div class="header-tags" v-if="currentQuestion">
              <el-tag :type="statusType(currentQuestion.status)">{{ currentQuestion.status || "未设置" }}</el-tag>
              <el-tag effect="light">掌握度 {{ currentQuestion.masteryLevel ?? 0 }}/5</el-tag>
            </div>
          </div>
        </template>

        <el-empty v-if="!currentQuestion" description="当前没有可复习的错题" />

        <div v-else class="question-detail">
          <el-alert :title="currentQuestionTip" type="info" :closable="false" show-icon />

          <el-descriptions :column="2" border>
            <el-descriptions-item label="科目">{{ currentQuestion.subject || "未分类" }}</el-descriptions-item>
            <el-descriptions-item label="知识点">{{ currentQuestion.knowledgePoint || "未标注" }}</el-descriptions-item>
            <el-descriptions-item label="错误原因">{{ currentQuestion.errorReason || "暂无" }}</el-descriptions-item>
            <el-descriptions-item label="复习次数">{{ currentQuestion.reviewCount ?? 0 }}</el-descriptions-item>
          </el-descriptions>

          <div class="block">
            <div class="label">题目内容</div>
            <div class="content-box">{{ currentQuestion.content || "暂无题目内容" }}</div>
          </div>

          <div class="block" v-if="currentQuestion.imageUrl">
            <div class="label">题图</div>
            <el-image class="preview-image" :src="currentQuestion.imageUrl" :preview-src-list="[currentQuestion.imageUrl]" fit="contain" />
          </div>

          <div class="block">
            <div class="label">重做答案（用于自检）</div>
            <el-input v-model="redoAnswer" type="textarea" :rows="4" placeholder="请输入你的重做答案..." />
          </div>

          <div class="actions">
            <el-button type="primary" plain @click="showAnalysis = !showAnalysis">
              {{ showAnalysis ? "隐藏解析" : "查看解析" }}
            </el-button>
            <el-button type="success" :loading="submitting" @click="submitResult(true)">我答对了</el-button>
            <el-button type="danger" :loading="submitting" @click="submitResult(false)">我答错了</el-button>
            <el-button @click="nextQuestion">下一题推荐</el-button>
          </div>

          <el-alert
            v-if="reviewResult.message"
            :title="reviewResult.message"
            :type="reviewResult.correct ? 'success' : 'warning'"
            show-icon
            :closable="false"
            class="result-alert"
          />

          <div class="block" v-if="showAnalysis">
            <div class="label">正确答案</div>
            <div class="content-box">{{ currentQuestion.answer || "暂无答案" }}</div>

            <div class="label mt">解析</div>
            <div class="content-box">{{ currentQuestion.analysis || "暂无解析" }}</div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import {
  getNextReviewQuestion,
  getReviewQuestion,
  getReviewSummary,
  listDueNowReviewQuestions,
  listTodayPlanReviewQuestions,
  submitReview,
} from "@/api/review";

const STATUS_UNMASTERED = "未掌握";
const STATUS_REVIEWING = "复习中";
const STATUS_MASTERED = "已掌握";

const summary = reactive({
  dueNowCount: 0,
  dueTodayCount: 0,
  overdueCount: 0,
  dueLaterTodayCount: 0,
  nextReviewTime: null,
});

const listMode = ref("current");
const currentDueList = ref([]);
const todayPlanList = ref([]);
const currentQuestion = ref(null);
const redoAnswer = ref("");
const showAnalysis = ref(false);

const loadingDue = ref(false);
const loadingQuestion = ref(false);
const submitting = ref(false);

const reviewResult = reactive({
  message: "",
  correct: false,
});

const activeList = computed(() => {
  return listMode.value === "today" ? todayPlanList.value : currentDueList.value;
});

const listTitle = computed(() => {
  if (listMode.value === "today") {
    return "今日计划列表（今日 00:00-23:59）";
  }
  return "当前待复习列表（截至当前时刻）";
});

const currentQuestionTip = computed(() => {
  if (!currentQuestion.value) {
    return "";
  }

  const nextTime = currentQuestion.value.nextReviewTime;
  if (isDueNow(nextTime)) {
    return "该题已到复习时间（含逾期任务），建议优先完成。";
  }
  if (isTodayPlan(nextTime)) {
    return "该题属于今日稍后计划，可提前复习。";
  }
  return "该题属于未来计划，可预先练习，但不计入今日待复习。";
});

onMounted(async () => {
  await refreshAll();
});

function statusType(status) {
  if (status === STATUS_MASTERED) return "success";
  if (status === STATUS_REVIEWING) return "warning";
  return "info";
}

function formatTime(value) {
  if (!value) return "暂无";
  return String(value).replace("T", " ");
}

function toDate(value) {
  if (!value) return null;
  const text = String(value).replace(" ", "T");
  const date = new Date(text);
  if (Number.isNaN(date.getTime())) {
    return null;
  }
  return date;
}

function getTodayRange() {
  const now = new Date();
  const start = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0, 0);
  const end = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59, 999);
  return { start, end };
}

function isDueNow(timeValue) {
  const date = toDate(timeValue);
  if (!date) return true;
  return date.getTime() <= Date.now();
}

function isTodayPlan(timeValue) {
  const date = toDate(timeValue);
  if (!date) return false;
  const { start, end } = getTodayRange();
  return date >= start && date <= end;
}

function queueTagText(timeValue) {
  if (isDueNow(timeValue)) {
    return "当前待复习";
  }
  if (isTodayPlan(timeValue)) {
    return "今日稍后";
  }
  return "未来计划";
}

function queueTagType(timeValue) {
  if (isDueNow(timeValue)) {
    return "warning";
  }
  if (isTodayPlan(timeValue)) {
    return "info";
  }
  return "success";
}

async function refreshSummary() {
  const res = await getReviewSummary();
  if (res.code !== 200) {
    throw new Error(res.message || "获取复习汇总失败");
  }

  const data = res.data || {};
  summary.dueNowCount = Number(data.dueNowCount || 0);
  summary.dueTodayCount = Number(data.dueTodayCount || 0);
  summary.overdueCount = Number(data.overdueCount || 0);
  summary.dueLaterTodayCount = Number(data.dueLaterTodayCount || 0);
  summary.nextReviewTime = data.nextReviewTime || null;
}

async function refreshDueLists() {
  loadingDue.value = true;
  try {
    const [currentRes, todayRes] = await Promise.all([
      listDueNowReviewQuestions(),
      listTodayPlanReviewQuestions(),
    ]);

    if (currentRes.code !== 200) {
      throw new Error(currentRes.message || "获取当前待复习列表失败");
    }
    if (todayRes.code !== 200) {
      throw new Error(todayRes.message || "获取今日计划列表失败");
    }

    currentDueList.value = currentRes.data || [];
    todayPlanList.value = todayRes.data || [];
  } finally {
    loadingDue.value = false;
  }
}

async function refreshCurrentQuestion() {
  const preferred = currentDueList.value[0] || todayPlanList.value[0];
  if (preferred?.questionId) {
    await selectQuestion(preferred.questionId);
    return;
  }

  loadingQuestion.value = true;
  try {
    const res = await getNextReviewQuestion();
    if (res.code !== 200) {
      throw new Error(res.message || "获取下一题失败");
    }
    currentQuestion.value = res.data || null;
    showAnalysis.value = false;
    redoAnswer.value = "";
    reviewResult.message = "";
  } finally {
    loadingQuestion.value = false;
  }
}

async function refreshAll() {
  try {
    await refreshSummary();
    await refreshDueLists();
    await refreshCurrentQuestion();
  } catch (error) {
    ElMessage.error(error?.message || "加载复习数据失败");
  }
}

async function selectQuestion(questionId) {
  if (!questionId) return;

  loadingQuestion.value = true;
  try {
    const res = await getReviewQuestion(questionId);
    if (res.code !== 200) {
      throw new Error(res.message || "获取错题失败");
    }

    currentQuestion.value = res.data || null;
    redoAnswer.value = "";
    showAnalysis.value = false;
    reviewResult.message = "";
  } catch (error) {
    ElMessage.error(error?.message || "获取错题失败");
  } finally {
    loadingQuestion.value = false;
  }
}

async function submitResult(correct) {
  if (!currentQuestion.value?.questionId) {
    ElMessage.warning("当前没有可提交的错题");
    return;
  }

  submitting.value = true;
  try {
    const payload = {
      correct,
      redoAnswer: redoAnswer.value,
    };

    const res = await submitReview(currentQuestion.value.questionId, payload);
    if (res.code !== 200) {
      throw new Error(res.message || "提交复习结果失败");
    }

    const data = res.data || {};
    currentQuestion.value = data.question || currentQuestion.value;
    showAnalysis.value = true;
    reviewResult.correct = Boolean(data.correct);
    reviewResult.message = reviewResult.correct
      ? "本题判定为答对：掌握度提升，下次复习按遗忘曲线顺延。"
      : "本题判定为答错：掌握度下降，下次复习提前到 12 小时后。";

    await refreshSummary();
    await refreshDueLists();
  } catch (error) {
    ElMessage.error(error?.message || "提交复习结果失败");
  } finally {
    submitting.value = false;
  }
}

async function nextQuestion() {
  const list = activeList.value || [];
  if (list.length === 0) {
    await refreshCurrentQuestion();
    return;
  }

  if (currentQuestion.value?.questionId) {
    const index = list.findIndex((item) => item.questionId === currentQuestion.value.questionId);
    if (index >= 0 && index < list.length - 1) {
      await selectQuestion(list[index + 1].questionId);
      return;
    }
  }

  await selectQuestion(list[0].questionId);
}
</script>

<style scoped>
.page {
  max-width: 1500px;
  margin: 0 auto;
  padding: 18px 16px 36px;
  background: linear-gradient(180deg, rgba(14, 116, 144, 0.06), rgba(255, 255, 255, 0) 260px);
}

.hero {
  border: none;
  border-radius: 14px;
  margin-bottom: 12px;
  color: #fff;
  background: linear-gradient(135deg, #164e63 0%, #0ea5e9 55%, #22d3ee 100%);
}

.hero :deep(.el-card__body) {
  padding: 18px;
}

.hero-title {
  font-size: 22px;
  font-weight: 900;
}

.hero-sub {
  margin-top: 6px;
  opacity: 0.92;
  font-size: 13px;
}

.hero-stats {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.logic-tip {
  margin-top: 12px;
  border-radius: 10px;
  padding: 8px 10px;
  background: rgba(15, 23, 42, 0.2);
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  line-height: 1.5;
}

.stat-item {
  border-radius: 12px;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.18);
}

.stat-label {
  font-size: 12px;
  opacity: 0.9;
}

.stat-value {
  margin-top: 6px;
  font-size: 22px;
  font-weight: 800;
}

.stat-value.warning {
  color: #fde68a;
}

.stat-value.danger {
  color: #fecaca;
}

.stat-value.small {
  font-size: 14px;
  font-weight: 700;
}

.main-grid {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 12px;
}

.due-card,
.question-card {
  border-radius: 14px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  font-weight: 800;
}

.header-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.header-tags {
  display: flex;
  gap: 8px;
}

.due-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 640px;
  overflow-y: auto;
}

.due-item {
  border-radius: 12px;
  border: 1px solid rgba(15, 23, 42, 0.12);
  padding: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.due-item:hover {
  border-color: #0ea5e9;
  background: #f0f9ff;
}

.due-item.active {
  border-color: #0284c7;
  background: #e0f2fe;
}

.due-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.due-subject {
  font-weight: 800;
  color: #0f172a;
}

.due-point {
  margin-top: 4px;
  color: #475569;
  font-size: 13px;
}

.due-time {
  margin-top: 6px;
  color: #64748b;
  font-size: 12px;
}

.question-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.block {
  border-radius: 12px;
  border: 1px solid rgba(15, 23, 42, 0.12);
  padding: 12px;
  background: #fff;
}

.label {
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 8px;
}

.label.mt {
  margin-top: 14px;
}

.content-box {
  white-space: pre-wrap;
  color: #334155;
  line-height: 1.6;
}

.preview-image {
  width: 100%;
  max-height: 300px;
  border-radius: 10px;
  background: #fff;
}

.actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.result-alert {
  margin-top: 4px;
}

@media (max-width: 1200px) {
  .main-grid {
    grid-template-columns: 1fr;
  }

  .due-list {
    max-height: 260px;
  }

  .hero-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .hero-stats {
    grid-template-columns: 1fr;
  }

  .logic-tip {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
