<template>
  <div class="page">
    <el-card class="hero" shadow="never">
      <div class="hero-title">错题管理</div>
      <div class="hero-sub">
        支持按科目、知识点、状态、难度、时间等维度筛选；可直接编辑、删除、收藏。
      </div>
    </el-card>

    <el-card class="filter-card" shadow="never">
      <el-form :model="query" label-position="top" class="filter-form">
        <el-form-item label="科目">
          <el-select v-model="query.subject" placeholder="全部科目" clearable filterable>
            <el-option v-for="subject in subjectOptions" :key="subject" :label="subject" :value="subject" />
          </el-select>
        </el-form-item>

        <el-form-item label="知识点">
          <el-select v-model="query.knowledgePoint" placeholder="全部知识点" clearable filterable>
            <el-option v-for="point in knowledgeOptions" :key="point" :label="point" :value="point" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable>
            <el-option
              v-for="statusItem in statusOptions"
              :key="statusItem.value"
              :label="statusItem.label"
              :value="statusItem.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="难度">
          <el-select v-model="query.difficulty" placeholder="全部难度" clearable>
            <el-option v-for="level in 5" :key="level" :label="`${level} 星`" :value="level" />
          </el-select>
        </el-form-item>

        <el-form-item label="收藏状态">
          <el-select v-model="query.favorite" placeholder="全部" clearable>
            <el-option label="已收藏" :value="true" />
            <el-option label="未收藏" :value="false" />
          </el-select>
        </el-form-item>

        <el-form-item class="wide-2" label="创建时间">
          <el-date-picker
            v-model="query.timeRange"
            type="datetimerange"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item class="wide-2" label="关键词">
          <el-input v-model="query.keyword" placeholder="可输入科目 / 知识点 / 错误原因" clearable />
        </el-form-item>
      </el-form>

      <div class="filter-actions">
        <el-button type="primary" :icon="Search" :loading="loading" @click="handleSearch">筛选</el-button>
        <el-button :icon="Refresh" @click="resetFilters">重置</el-button>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="rows" border stripe row-key="id" v-loading="loading" empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="80" />

        <el-table-column label="分类信息" min-width="190">
          <template #default="{ row }">
            <div class="meta-title">{{ row.subject || "未分类" }}</div>
            <div class="meta-sub">{{ row.knowledgePoint || "未标注知识点" }}</div>
          </template>
        </el-table-column>

        <el-table-column label="题目摘要" min-width="280">
          <template #default="{ row }">
            <div class="text-ellipsis">{{ row.content || "暂无题目内容" }}</div>
          </template>
        </el-table-column>

        <el-table-column label="难度" width="130">
          <template #default="{ row }">
            <el-rate :model-value="row.difficulty || 0" disabled :max="5" />
          </template>
        </el-table-column>

        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ row.status || "未设置" }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="标签" min-width="180">
          <template #default="{ row }">
            <el-space wrap>
              <el-tag v-for="tag in row.tags || []" :key="tag.id" effect="light">{{ tag.name }}</el-tag>
              <span v-if="!row.tags || row.tags.length === 0" class="muted">无标签</span>
            </el-space>
          </template>
        </el-table-column>

        <el-table-column label="收藏" width="90" align="center">
          <template #default="{ row }">
            <el-button link :type="row.favorite ? 'warning' : 'info'" @click="toggleFavoriteRow(row)">
              <el-icon><Star /></el-icon>
            </el-button>
          </template>
        </el-table-column>

        <el-table-column prop="updateTime" label="更新时间" min-width="180" />

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-space>
              <el-button link type="primary" :icon="EditPen" @click="openEdit(row)">编辑</el-button>
              <el-button link type="danger" :icon="Delete" @click="removeQuestion(row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :current-page="query.pageNo"
          :page-size="query.pageSize"
          :page-sizes="[8, 12, 20, 30]"
          :total="total"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-drawer v-model="editVisible" title="编辑错题" size="62%" destroy-on-close>
      <div v-loading="editLoading">
        <el-form label-position="top" :model="editForm" class="edit-form">
          <el-row :gutter="14">
            <el-col :xs="24" :md="12">
              <el-form-item label="科目">
                <el-input v-model="editForm.subject" clearable />
              </el-form-item>
            </el-col>

            <el-col :xs="24" :md="12">
              <el-form-item label="知识点">
                <el-input v-model="editForm.knowledgePoint" clearable />
              </el-form-item>
            </el-col>

            <el-col :xs="24" :md="12">
              <el-form-item label="错误原因">
                <el-input v-model="editForm.errorReason" clearable />
              </el-form-item>
            </el-col>

            <el-col :xs="24" :md="12">
              <el-form-item label="状态">
                <el-select v-model="editForm.status" clearable>
                  <el-option
                    v-for="statusItem in statusOptions"
                    :key="statusItem.value"
                    :label="statusItem.label"
                    :value="statusItem.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :xs="24" :md="12">
              <el-form-item label="难度">
                <el-rate v-model="editForm.difficulty" :max="5" />
              </el-form-item>
            </el-col>

            <el-col :xs="24" :md="12">
              <el-form-item label="收藏">
                <el-switch v-model="editForm.favorite" />
              </el-form-item>
            </el-col>

            <el-col :span="24">
              <el-form-item label="标签">
                <el-select v-model="editForm.tagIds" multiple collapse-tags filterable placeholder="请选择标签">
                  <el-option v-for="tag in allTags" :key="tag.id" :label="tag.name" :value="tag.id" />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="24">
              <el-form-item label="题目内容">
                <el-input v-model="editForm.content" type="textarea" :rows="5" maxlength="5000" show-word-limit />
              </el-form-item>
            </el-col>

            <el-col :xs="24" :md="12">
              <el-form-item label="答案">
                <el-input v-model="editForm.answer" type="textarea" :rows="4" />
              </el-form-item>
            </el-col>

            <el-col :xs="24" :md="12">
              <el-form-item label="解析">
                <el-input v-model="editForm.analysis" type="textarea" :rows="4" />
              </el-form-item>
            </el-col>

            <el-col :span="24" v-if="editForm.imageUrl">
              <el-form-item label="题图预览">
                <el-image
                  class="preview-image"
                  :src="editForm.imageUrl"
                  :preview-src-list="[editForm.imageUrl]"
                  fit="contain"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <template #footer>
        <div class="drawer-footer">
          <el-button @click="editVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitEdit">保存</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Delete, EditPen, Refresh, Search, Star } from "@element-plus/icons-vue";
import {
  deleteQuestion,
  getQuestionDetail,
  listQuestionKnowledgePoints,
  listQuestionSubjects,
  listQuestions,
  updateFavorite,
  updateQuestion,
} from "@/api/question";
import { listMyTags } from "@/api/tag";

const STATUS_UNMASTERED = "未掌握";
const STATUS_REVIEWING = "复习中";
const STATUS_MASTERED = "已掌握";

const statusOptions = [
  { label: "未掌握", value: STATUS_UNMASTERED },
  { label: "复习中", value: STATUS_REVIEWING },
  { label: "已掌握", value: STATUS_MASTERED },
];

const loading = ref(false);
const total = ref(0);
const subjectOptions = ref([]);
const knowledgeOptions = ref([]);
const rows = ref([]);
const allTags = ref([]);

const query = reactive({
  subject: "",
  knowledgePoint: "",
  status: "",
  difficulty: null,
  favorite: null,
  keyword: "",
  timeRange: [],
  pageNo: 1,
  pageSize: 8,
});

const editVisible = ref(false);
const editLoading = ref(false);
const submitting = ref(false);
const editForm = reactive({
  id: null,
  subject: "",
  knowledgePoint: "",
  errorReason: "",
  difficulty: 0,
  status: STATUS_UNMASTERED,
  favorite: false,
  content: "",
  answer: "",
  analysis: "",
  tagIds: [],
  imageUrl: "",
});

watch(
  () => query.subject,
  async () => {
    query.knowledgePoint = "";
    await fetchKnowledgePoints();
  }
);

onMounted(async () => {
  await Promise.allSettled([fetchSubjects(), fetchKnowledgePoints(), fetchAllTags()]);
  await fetchList();
});

async function fetchSubjects() {
  try {
    const res = await listQuestionSubjects();
    if (res.code === 200) {
      subjectOptions.value = res.data || [];
    }
  } catch (error) {
    subjectOptions.value = [];
  }
}

async function fetchKnowledgePoints() {
  try {
    const res = await listQuestionKnowledgePoints(query.subject || undefined);
    if (res.code === 200) {
      knowledgeOptions.value = res.data || [];
    }
  } catch (error) {
    knowledgeOptions.value = [];
  }
}

async function fetchAllTags() {
  try {
    const res = await listMyTags();
    if (res.code === 200) {
      allTags.value = res.data || [];
    }
  } catch (error) {
    allTags.value = [];
  }
}

function buildQueryParams() {
  const params = {
    pageNo: query.pageNo,
    pageSize: query.pageSize,
  };

  if (query.subject) params.subject = query.subject;
  if (query.knowledgePoint) params.knowledgePoint = query.knowledgePoint;
  if (query.status) params.status = query.status;
  if (query.difficulty) params.difficulty = query.difficulty;
  if (query.favorite !== null) params.favorite = query.favorite;
  if (query.keyword?.trim()) params.keyword = query.keyword.trim();
  if (query.timeRange?.length === 2) {
    params.startTime = query.timeRange[0];
    params.endTime = query.timeRange[1];
  }

  return params;
}

async function fetchList() {
  loading.value = true;
  try {
    const res = await listQuestions(buildQueryParams());
    if (res.code !== 200) {
      ElMessage.error(res.message || "加载错题失败");
      return;
    }

    const data = res.data || {};
    rows.value = data.records || [];
    total.value = Number(data.total || 0);
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error.message || "加载错题失败");
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  query.pageNo = 1;
  fetchList();
}

function resetFilters() {
  query.subject = "";
  query.knowledgePoint = "";
  query.status = "";
  query.difficulty = null;
  query.favorite = null;
  query.keyword = "";
  query.timeRange = [];
  query.pageNo = 1;
  query.pageSize = 8;
  fetchKnowledgePoints();
  fetchList();
}

function handlePageChange(pageNo) {
  query.pageNo = pageNo;
  fetchList();
}

function handleSizeChange(pageSize) {
  query.pageSize = pageSize;
  query.pageNo = 1;
  fetchList();
}

function statusType(status) {
  if (status === STATUS_MASTERED) return "success";
  if (status === STATUS_REVIEWING) return "warning";
  return "info";
}

async function toggleFavoriteRow(row) {
  try {
    const nextValue = !Boolean(row.favorite);
    const res = await updateFavorite(row.id, nextValue);
    if (res.code !== 200) {
      ElMessage.error(res.message || "更新收藏状态失败");
      return;
    }

    row.favorite = nextValue;
    ElMessage.success(nextValue ? "已收藏" : "已取消收藏");
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error.message || "更新收藏状态失败");
  }
}

function fillEditForm(data) {
  editForm.id = data.id;
  editForm.subject = data.subject || "";
  editForm.knowledgePoint = data.knowledgePoint || "";
  editForm.errorReason = data.errorReason || "";
  editForm.difficulty = data.difficulty || 0;
  editForm.status = data.status || STATUS_UNMASTERED;
  editForm.favorite = Boolean(data.favorite);
  editForm.content = data.content || "";
  editForm.answer = data.answer || "";
  editForm.analysis = data.analysis || "";
  editForm.tagIds = (data.tags || []).map((tag) => tag.id);
  editForm.imageUrl = data.imageUrl || "";
}

async function openEdit(row) {
  editVisible.value = true;
  editLoading.value = true;

  try {
    await fetchAllTags();
    const res = await getQuestionDetail(row.id);
    if (res.code !== 200) {
      ElMessage.error(res.message || "获取详情失败");
      editVisible.value = false;
      return;
    }

    fillEditForm(res.data || {});
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error.message || "获取详情失败");
    editVisible.value = false;
  } finally {
    editLoading.value = false;
  }
}

async function submitEdit() {
  if (!editForm.id) return;
  if (!editForm.content?.trim()) {
    ElMessage.warning("题目内容不能为空");
    return;
  }

  submitting.value = true;
  try {
    const payload = {
      subject: editForm.subject,
      knowledgePoint: editForm.knowledgePoint,
      errorReason: editForm.errorReason,
      difficulty: editForm.difficulty || null,
      status: editForm.status,
      favorite: editForm.favorite,
      content: editForm.content,
      answer: editForm.answer,
      analysis: editForm.analysis,
      tagIds: editForm.tagIds,
    };

    const res = await updateQuestion(editForm.id, payload);
    if (res.code !== 200) {
      ElMessage.error(res.message || "保存失败");
      return;
    }

    ElMessage.success("保存成功");
    editVisible.value = false;
    await fetchList();
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error.message || "保存失败");
  } finally {
    submitting.value = false;
  }
}

async function removeQuestion(row) {
  try {
    await ElMessageBox.confirm("确认删除这条错题吗？删除后不可恢复。", "删除确认", {
      type: "warning",
      confirmButtonText: "删除",
      cancelButtonText: "取消",
    });

    const res = await deleteQuestion(row.id);
    if (res.code !== 200) {
      ElMessage.error(res.message || "删除失败");
      return;
    }

    ElMessage.success("删除成功");
    if (rows.value.length === 1 && query.pageNo > 1) {
      query.pageNo -= 1;
    }
    await fetchList();
  } catch (error) {
    if (error !== "cancel" && error !== "close") {
      ElMessage.error(error?.response?.data?.message || error.message || "删除失败");
    }
  }
}
</script>

<style scoped>
.page {
  max-width: 1560px;
  margin: 0 auto;
  padding: 18px 16px 36px;
  background: linear-gradient(180deg, rgba(59, 130, 246, 0.05), rgba(255, 255, 255, 0) 260px);
}

.hero {
  border: none;
  border-radius: 14px;
  margin-bottom: 12px;
  color: #fff;
  background: linear-gradient(135deg, #0f172a 0%, #2563eb 55%, #38bdf8 100%);
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

.filter-card,
.table-card {
  border-radius: 14px;
}

.filter-card {
  margin-bottom: 12px;
}

.filter-form {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px 14px;
}

.filter-form :deep(.el-form-item) {
  margin: 0;
}

.filter-form :deep(.el-select),
.filter-form :deep(.el-input),
.filter-form :deep(.el-date-editor) {
  width: 100%;
}

.wide-2 {
  grid-column: span 2;
}

.filter-actions {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
}

.meta-title {
  font-weight: 800;
  color: #0f172a;
}

.meta-sub {
  color: #64748b;
  margin-top: 4px;
  font-size: 12px;
}

.text-ellipsis {
  color: #1e293b;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.muted {
  color: #94a3b8;
  font-size: 12px;
}

.pager {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.edit-form {
  padding: 4px 4px 16px;
}

.preview-image {
  width: 100%;
  max-height: 320px;
  border-radius: 12px;
  border: 1px solid rgba(15, 23, 42, 0.1);
  background: #fff;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 1500px) {
  .filter-form {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 1200px) {
  .filter-form {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .filter-form {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .wide-2 {
    grid-column: span 2;
  }
}

@media (max-width: 640px) {
  .filter-form {
    grid-template-columns: 1fr;
  }

  .wide-2 {
    grid-column: span 1;
  }
}
</style>

