<template>
  <div class="admin-page">
    <el-card class="hero" shadow="never">
      <div class="hero-content">
        <div>
          <div class="hero-title">系统级学情管理看板</div>
          <div class="hero-sub">支持学生学情查看与全局统计分析，不涉及教学干预。</div>
        </div>
        <el-button type="primary" :icon="Refresh" :loading="loadingAll" @click="refreshAll">刷新数据</el-button>
      </div>
    </el-card>

    <el-row :gutter="14" class="stats-row">
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card" shadow="never">
          <div class="stat-label">学生总数</div>
          <div class="stat-value">{{ systemStats.studentCount }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card" shadow="never">
          <div class="stat-label">系统错题总量</div>
          <div class="stat-value warning">{{ systemStats.totalWrongCount }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card" shadow="never">
          <div class="stat-label">系统平均掌握率</div>
          <div class="stat-value success">{{ systemStats.masteryRate }}%</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="14" class="panel-row">
      <el-col :xs="24" :lg="11">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="panel-head">
              <span class="panel-title">学生列表浏览</span>
              <el-tag type="info">{{ students.length }} 人</el-tag>
            </div>
          </template>

          <el-table
            :data="students"
            border
            stripe
            highlight-current-row
            row-key="studentId"
            height="430"
            v-loading="loadingStudents"
            @current-change="handleStudentSelect"
          >
            <el-table-column prop="username" label="学生" min-width="120" />
            <el-table-column prop="wrongCount" label="错题数" width="88" />
            <el-table-column label="掌握率" width="120">
              <template #default="{ row }">
                <el-tag type="success" effect="light">{{ row.masteryRate }}%</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="highFrequencyKnowledgePoint" label="高频错误知识点" min-width="150" show-overflow-tooltip />
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="13">
        <el-card class="panel-card" shadow="never" v-loading="loadingStudentOverview">
          <template #header>
            <div class="panel-head">
              <span class="panel-title">学生学情概览</span>
              <el-tag v-if="studentOverview.username" type="primary">{{ studentOverview.username }}</el-tag>
            </div>
          </template>

          <el-empty v-if="!selectedStudentId" description="请先在左侧选择学生" :image-size="90" />

          <div v-else>
            <div class="overview-cards">
              <div class="overview-item">
                <div class="overview-label">错题数量</div>
                <div class="overview-value">{{ studentOverview.wrongCount }}</div>
              </div>
              <div class="overview-item">
                <div class="overview-label">掌握率</div>
                <div class="overview-value success">{{ studentOverview.masteryRate }}%</div>
              </div>
              <div class="overview-item">
                <div class="overview-label">高频错误知识点</div>
                <div class="overview-value text">{{ studentOverview.highFrequencyKnowledgePoint || "暂无" }}</div>
              </div>
            </div>

            <div class="sub-section">
              <div class="sub-title">该学生科目错题分布</div>
              <div v-if="studentOverview.subjectStats.length > 0" class="bar-list">
                <div v-for="item in studentOverview.subjectStats" :key="item.name" class="bar-item">
                  <div class="bar-head">
                    <span>{{ item.name }}</span>
                    <span>{{ item.count }}（{{ fixedPercent(item.percent) }}%）</span>
                  </div>
                  <div class="bar-track">
                    <div class="bar-fill subject-fill" :style="{ width: `${fixedPercent(item.percent)}%` }" />
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无数据" :image-size="70" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="panel-card panel-row" shadow="never" v-loading="loadingSystemStats">
      <template #header>
        <div class="panel-head">
          <span class="panel-title">系统级统计分析</span>
          <el-select v-model="knowledgeTopN" size="small" style="width: 130px" @change="loadSystemStats">
            <el-option :value="5" label="Top 5" />
            <el-option :value="8" label="Top 8" />
            <el-option :value="10" label="Top 10" />
            <el-option :value="15" label="Top 15" />
          </el-select>
        </div>
      </template>

      <el-row :gutter="14">
        <el-col :xs="24" :lg="8">
          <div class="sub-title">各学科错题数量统计</div>
          <div class="bar-list" v-if="systemStats.subjectStats.length > 0">
            <div v-for="item in systemStats.subjectStats" :key="item.name" class="bar-item">
              <div class="bar-head">
                <span>{{ item.name }}</span>
                <span>{{ item.count }}</span>
              </div>
              <el-progress :percentage="fixedPercent(item.percent)" :stroke-width="10" color="#3b82f6" />
            </div>
          </div>
          <el-empty v-else description="暂无统计数据" :image-size="70" />
        </el-col>

        <el-col :xs="24" :lg="8">
          <div class="sub-title">错误原因分布统计</div>
          <div class="bar-list" v-if="systemStats.errorReasonStats.length > 0">
            <div v-for="item in systemStats.errorReasonStats" :key="item.name" class="bar-item">
              <div class="bar-head">
                <span>{{ item.name }}</span>
                <span>{{ item.count }}</span>
              </div>
              <el-progress :percentage="fixedPercent(item.percent)" :stroke-width="10" color="#f59e0b" />
            </div>
          </div>
          <el-empty v-else description="暂无统计数据" :image-size="70" />
        </el-col>

        <el-col :xs="24" :lg="8">
          <div class="sub-title">知识点错误集中度分析</div>
          <div class="bar-list" v-if="systemStats.knowledgePointConcentration.length > 0">
            <div v-for="item in systemStats.knowledgePointConcentration" :key="item.name" class="bar-item">
              <div class="bar-head">
                <span>{{ item.name }}</span>
                <span>{{ item.count }}</span>
              </div>
              <el-progress :percentage="fixedPercent(item.percent)" :stroke-width="10" color="#ef4444" />
            </div>
          </div>
          <el-empty v-else description="暂无统计数据" :image-size="70" />
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { Refresh } from "@element-plus/icons-vue";
import {
  getAdminStudentOverview,
  getAdminSystemStats,
  listAdminStudents,
} from "@/api/admin";

const students = ref([]);
const selectedStudentId = ref(null);
const knowledgeTopN = ref(8);

const loadingStudents = ref(false);
const loadingStudentOverview = ref(false);
const loadingSystemStats = ref(false);

const loadingAll = computed(() => {
  return loadingStudents.value || loadingStudentOverview.value || loadingSystemStats.value;
});

const studentOverview = reactive({
  username: "",
  wrongCount: 0,
  masteryRate: 0,
  highFrequencyKnowledgePoint: "",
  subjectStats: [],
});

const systemStats = reactive({
  studentCount: 0,
  totalWrongCount: 0,
  masteryRate: 0,
  subjectStats: [],
  errorReasonStats: [],
  knowledgePointConcentration: [],
});

onMounted(async () => {
  await refreshAll();
});

async function refreshAll() {
  await Promise.allSettled([loadStudents(), loadSystemStats()]);
}

async function loadStudents() {
  loadingStudents.value = true;
  try {
    const res = await listAdminStudents();
    if (res?.code !== 200) {
      ElMessage.error(res?.message || "加载学生列表失败");
      return;
    }

    students.value = Array.isArray(res.data) ? res.data : [];
    if (students.value.length === 0) {
      selectedStudentId.value = null;
      resetStudentOverview();
      return;
    }

    const exists = students.value.some((item) => item.studentId === selectedStudentId.value);
    if (!exists) {
      selectedStudentId.value = students.value[0].studentId;
    }
    await loadStudentOverview();
  } catch (error) {
    console.error("加载学生列表失败", error);
    ElMessage.error("加载学生列表失败，请稍后重试");
  } finally {
    loadingStudents.value = false;
  }
}

async function loadStudentOverview() {
  if (!selectedStudentId.value) {
    return;
  }

  loadingStudentOverview.value = true;
  try {
    const res = await getAdminStudentOverview(selectedStudentId.value);
    if (res?.code !== 200) {
      ElMessage.error(res?.message || "加载学生概览失败");
      return;
    }

    const data = res.data || {};
    studentOverview.username = data.username || "";
    studentOverview.wrongCount = Number(data.wrongCount || 0);
    studentOverview.masteryRate = fixedPercent(data.masteryRate);
    studentOverview.highFrequencyKnowledgePoint = data.highFrequencyKnowledgePoint || "";
    studentOverview.subjectStats = Array.isArray(data.subjectStats) ? data.subjectStats : [];
  } catch (error) {
    console.error("加载学生概览失败", error);
    ElMessage.error("加载学生概览失败，请稍后重试");
  } finally {
    loadingStudentOverview.value = false;
  }
}

async function loadSystemStats() {
  loadingSystemStats.value = true;
  try {
    const res = await getAdminSystemStats({ topN: knowledgeTopN.value });
    if (res?.code !== 200) {
      ElMessage.error(res?.message || "加载系统统计失败");
      return;
    }

    const data = res.data || {};
    systemStats.studentCount = Number(data.studentCount || 0);
    systemStats.totalWrongCount = Number(data.totalWrongCount || 0);
    systemStats.masteryRate = fixedPercent(data.masteryRate);
    systemStats.subjectStats = Array.isArray(data.subjectStats) ? data.subjectStats : [];
    systemStats.errorReasonStats = Array.isArray(data.errorReasonStats) ? data.errorReasonStats : [];
    systemStats.knowledgePointConcentration = Array.isArray(data.knowledgePointConcentration)
      ? data.knowledgePointConcentration
      : [];
  } catch (error) {
    console.error("加载系统统计失败", error);
    ElMessage.error("加载系统统计失败，请稍后重试");
  } finally {
    loadingSystemStats.value = false;
  }
}

async function handleStudentSelect(row) {
  if (!row?.studentId) {
    return;
  }
  selectedStudentId.value = row.studentId;
  await loadStudentOverview();
}

function fixedPercent(value) {
  const number = Number(value || 0);
  if (Number.isNaN(number)) {
    return 0;
  }
  return Math.max(0, Math.min(100, Math.round(number)));
}

function resetStudentOverview() {
  studentOverview.username = "";
  studentOverview.wrongCount = 0;
  studentOverview.masteryRate = 0;
  studentOverview.highFrequencyKnowledgePoint = "";
  studentOverview.subjectStats = [];
}
</script>

<style scoped>
.admin-page {
  max-width: 1500px;
  margin: 0 auto;
  padding: 2px 0 18px;
}

.hero {
  border: none;
  border-radius: 14px;
  margin-bottom: 12px;
  color: #fff;
  background: linear-gradient(135deg, #334155 0%, #1e40af 55%, #0ea5e9 100%);
}

.hero :deep(.el-card__body) {
  padding: 20px;
}

.hero-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
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

.stat-value.warning {
  color: #f59e0b;
}

.stat-value.success {
  color: #16a34a;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.panel-title {
  font-size: 16px;
  font-weight: 800;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.overview-item {
  border: 1px solid rgba(15, 23, 42, 0.1);
  border-radius: 10px;
  padding: 10px;
  background: #fff;
}

.overview-label {
  color: #64748b;
  font-size: 13px;
}

.overview-value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
}

.overview-value.success {
  color: #16a34a;
}

.overview-value.text {
  font-size: 16px;
  line-height: 1.4;
}

.sub-section {
  margin-top: 14px;
}

.sub-title {
  font-size: 14px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 10px;
}

.bar-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.bar-item {
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 10px;
  padding: 10px;
  background: #fff;
}

.bar-head {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 13px;
}

.bar-track {
  height: 8px;
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
  background: linear-gradient(90deg, #3b82f6 0%, #06b6d4 100%);
}

@media (max-width: 1200px) {
  .overview-cards {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .hero-content {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

