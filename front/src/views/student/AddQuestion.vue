<template>
  <div class="page">
    <el-card class="hero" shadow="never">
      <div class="hero-title">错题采集</div>
      <div class="hero-sub">手动录入 / 图片上传OCR / 标签标注，一页完成</div>
    </el-card>

    <el-card class="main" shadow="never">
      <el-tabs v-model="activeTab" class="tabs">
        <el-tab-pane label="手动录入" name="manual">
          <el-form label-position="top" :model="manual" class="form">
            <el-row :gutter="14">
              <el-col :xs="24" :md="12">
                <el-form-item label="科目">
                  <el-input v-model="manual.subject" placeholder="如：数学 / 英语" clearable />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-form-item label="知识点">
                  <el-input v-model="manual.knowledgePoint" placeholder="如：函数 / 定语从句" clearable />
                </el-form-item>
              </el-col>

              <el-col :xs="24" :md="12">
                <el-form-item label="错误原因">
                  <el-input v-model="manual.errorReason" placeholder="如：审题不清 / 计算失误" clearable />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-form-item label="难度（1~5）">
                  <div class="inline">
                    <el-rate v-model="manual.difficulty" :max="5" />
                    <span class="muted" v-if="manual.difficulty">({{ manual.difficulty }})</span>
                    <span class="muted" v-else>(未选择)</span>
                  </div>
                </el-form-item>
              </el-col>

              <el-col :span="24">
                <el-form-item label="题目内容（必填）">
                  <el-input
                      v-model="manual.content"
                      type="textarea"
                      :rows="6"
                      placeholder="输入题干内容..."
                      maxlength="5000"
                      show-word-limit
                  />
                </el-form-item>
              </el-col>

              <el-col :xs="24" :md="12">
                <el-form-item label="正确答案（可选）">
                  <el-input v-model="manual.answer" type="textarea" :rows="4" placeholder="输入答案..." />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-form-item label="解析（可选）">
                  <el-input v-model="manual.analysis" type="textarea" :rows="4" placeholder="输入解析..." />
                </el-form-item>
              </el-col>

              <el-col :span="24">
                <el-form-item label="标签">
                  <div class="tag-wrap">
                    <div class="tag-list">
                      <el-check-tag
                          v-for="t in tags"
                          :key="t.id"
                          :checked="manual.tagIds.includes(t.id)"
                          @change="(checked) => onToggleTag('manual', t.id, checked)"
                      >
                        {{ t.name }}
                      </el-check-tag>
                      <span v-if="tags.length === 0" class="muted">暂无标签，可右侧新增</span>
                    </div>

                    <div class="tag-create">
                      <el-input v-model="newTagName" placeholder="新增标签..." clearable />
                      <el-button type="primary" plain @click="handleCreateTag('manual')">
                        新增并选中
                      </el-button>
                    </div>
                  </div>
                </el-form-item>
              </el-col>

              <el-col :span="24">
                <el-form-item>
                  <div class="inline">
                    <el-switch v-model="manual.favorite" />
                    <span class="muted">收藏此错题</span>
                  </div>
                </el-form-item>
              </el-col>
            </el-row>

            <div class="actions">
              <el-button type="primary" :loading="loading" @click="submitManual">提交错题</el-button>
              <el-button :disabled="loading" @click="resetManual">重置</el-button>
            </div>

            <el-alert
                v-if="manualResult.questionId"
                type="success"
                :closable="false"
                show-icon
                class="result"
                title="创建成功"
                :description="`错题ID：#${manualResult.questionId}`"
            />
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="图片上传 + OCR" name="image">
          <el-form label-position="top" :model="imageMeta" class="form">
            <el-row :gutter="14">
              <el-col :xs="24" :md="12">
                <el-form-item label="科目">
                  <el-input v-model="imageMeta.subject" placeholder="如：物理 / 化学" clearable />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-form-item label="知识点">
                  <el-input v-model="imageMeta.knowledgePoint" placeholder="如：电场 / 氧化还原" clearable />
                </el-form-item>
              </el-col>

              <el-col :xs="24" :md="12">
                <el-form-item label="错误原因">
                  <el-input v-model="imageMeta.errorReason" placeholder="如：公式记错 / 概念混淆" clearable />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-form-item label="难度（1~5）">
                  <div class="inline">
                    <el-rate v-model="imageMeta.difficulty" :max="5" />
                    <span class="muted" v-if="imageMeta.difficulty">({{ imageMeta.difficulty }})</span>
                    <span class="muted" v-else>(未选择)</span>
                  </div>
                </el-form-item>
              </el-col>

              <el-col :span="24">
                <el-form-item label="上传图片">
                  <el-upload
                      class="upload"
                      :auto-upload="false"
                      :limit="1"
                      accept="image/*"
                      list-type="picture-card"
                      :on-change="onUploadChange"
                      :on-remove="onUploadRemove"
                  >
                    <el-icon><Plus /></el-icon>
                    <template #tip>
                      <div class="muted" style="margin-top: 8px">
                        支持 jpg/png/webp；图片越清晰 OCR 越准
                      </div>
                    </template>
                  </el-upload>

                  <div v-if="imagePreview" class="preview">
                    <img :src="imagePreview" alt="preview" />
                  </div>
                </el-form-item>
              </el-col>

              <el-col :span="24">
                <el-form-item label="标签">
                  <div class="tag-wrap">
                    <div class="tag-list">
                      <el-check-tag
                          v-for="t in tags"
                          :key="t.id"
                          :checked="imageMeta.tagIds.includes(t.id)"
                          @change="(checked) => onToggleTag('image', t.id, checked)"
                      >
                        {{ t.name }}
                      </el-check-tag>
                      <span v-if="tags.length === 0" class="muted">暂无标签，可右侧新增</span>
                    </div>

                    <div class="tag-create">
                      <el-input v-model="newTagName" placeholder="新增标签..." clearable />
                      <el-button type="primary" plain @click="handleCreateTag('image')">
                        新增并选中
                      </el-button>
                    </div>
                  </div>
                </el-form-item>
              </el-col>

              <el-col :span="24">
                <el-form-item>
                  <div class="inline">
                    <el-switch v-model="imageMeta.favorite" />
                    <span class="muted">收藏此错题</span>
                  </div>
                </el-form-item>
              </el-col>
            </el-row>

            <div class="actions">
              <el-button type="primary" :loading="loading" @click="submitImage">
                上传并识别(OCR)
              </el-button>
              <el-button :disabled="loading" @click="resetImage">重置</el-button>
              <el-button type="primary" plain @click="goTagManage">标签管理</el-button>
            </div>

            <el-card v-if="imageResult.questionId" class="ocr-card" shadow="never">
              <template #header>
                <div class="ocr-header">
                  <span>识别结果</span>
                  <el-tag :type="imageResult.ocrStatus === 'success' ? 'success' : 'warning'">
                    {{ imageResult.ocrStatus || 'unknown' }}
                  </el-tag>
                </div>
              </template>

              <el-descriptions :column="1" border>
                <el-descriptions-item label="错题ID">
                  #{{ imageResult.questionId }}
                </el-descriptions-item>
                <el-descriptions-item label="图片URL" v-if="imageResult.imageUrl">
                  <a :href="imageResult.imageUrl" target="_blank">{{ imageResult.imageUrl }}</a>
                </el-descriptions-item>
              </el-descriptions>

              <div style="margin-top: 12px" v-if="imageResult.ocrText">
                <div class="label">OCR文本</div>
                <el-input type="textarea" :rows="8" :model-value="imageResult.ocrText" readonly />
              </div>
              <el-alert
                  v-else
                  type="warning"
                  :closable="false"
                  show-icon
                  style="margin-top: 12px"
                  title="未识别到文本"
                  description="可尝试更清晰的图片；或确认后端 mistake.ocr.mode 为 mock/tesseract"
              />
            </el-card>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import { listMyTags, createTag } from "@/api/tag";
import { createManualQuestion, createImageQuestion } from "@/api/question";
import { useRouter } from "vue-router";

const router = useRouter();

const activeTab = ref("manual");
const loading = ref(false);

const tags = ref([]);
const newTagName = ref("");

async function fetchTags() {
  const res = await listMyTags(); // res = Result
  if (res.code !== 200) throw new Error(res.message || "加载标签失败");
  tags.value = res.data || [];
}

onMounted(async () => {
  try {
    await fetchTags();
  } catch (e) {
    ElMessage.warning(e.message || "标签加载失败");
  }
});

// ----------------- manual -----------------
const manual = reactive({
  subject: "",
  knowledgePoint: "",
  errorReason: "",
  difficulty: 0, // el-rate 默认用 0 表示未选
  favorite: false,
  content: "",
  answer: "",
  analysis: "",
  tagIds: [],
});

const manualResult = reactive({ questionId: null });

function resetManual() {
  manual.subject = "";
  manual.knowledgePoint = "";
  manual.errorReason = "";
  manual.difficulty = 0;
  manual.favorite = false;
  manual.content = "";
  manual.answer = "";
  manual.analysis = "";
  manual.tagIds = [];
  manualResult.questionId = null;
}

async function submitManual() {
  if (!manual.content?.trim()) {
    ElMessage.warning("请先填写题目内容");
    return;
  }

  loading.value = true;
  try {
    const payload = {
      subject: manual.subject,
      knowledgePoint: manual.knowledgePoint,
      errorReason: manual.errorReason,
      difficulty: manual.difficulty || null,
      favorite: manual.favorite,
      content: manual.content,
      answer: manual.answer,
      analysis: manual.analysis,
      tagIds: manual.tagIds,
    };

    const res = await createManualQuestion(payload);
    if (res.code !== 200) {
      ElMessage.error(res.message || "提交失败");
      return;
    }
    manualResult.questionId = res.data?.questionId || null;
    ElMessage.success("提交成功");
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e.message || "提交失败");
  } finally {
    loading.value = false;
  }
}

// ----------------- image -----------------
const imageMeta = reactive({
  subject: "",
  knowledgePoint: "",
  errorReason: "",
  difficulty: 0,
  favorite: false,
  tagIds: [],
});

const pickedFile = ref(null);
const imagePreview = ref("");

const imageResult = reactive({
  questionId: null,
  imageUrl: "",
  ocrStatus: "",
  ocrText: "",
});

function onUploadChange(file) {
  pickedFile.value = file?.raw || null;
  if (pickedFile.value) {
    imagePreview.value = URL.createObjectURL(pickedFile.value);
  }
}

function onUploadRemove() {
  pickedFile.value = null;
  imagePreview.value = "";
}

function resetImage() {
  imageMeta.subject = "";
  imageMeta.knowledgePoint = "";
  imageMeta.errorReason = "";
  imageMeta.difficulty = 0;
  imageMeta.favorite = false;
  imageMeta.tagIds = [];

  pickedFile.value = null;
  imagePreview.value = "";

  imageResult.questionId = null;
  imageResult.imageUrl = "";
  imageResult.ocrStatus = "";
  imageResult.ocrText = "";
}

async function submitImage() {
  if (!pickedFile.value) {
    ElMessage.warning("请先选择一张图片");
    return;
  }

  loading.value = true;
  try {
    const meta = {
      subject: imageMeta.subject,
      knowledgePoint: imageMeta.knowledgePoint,
      errorReason: imageMeta.errorReason,
      difficulty: imageMeta.difficulty || null,
      favorite: imageMeta.favorite,
      tagIds: imageMeta.tagIds,
    };

    const res = await createImageQuestion({ file: pickedFile.value, meta });
    if (res.code !== 200) {
      ElMessage.error(res.message || "上传失败");
      return;
    }

    const data = res.data || {};
    imageResult.questionId = data.questionId || null;
    imageResult.imageUrl = data.imageUrl || "";
    imageResult.ocrStatus = data.ocrStatus || "";
    imageResult.ocrText = data.ocrText || "";

    ElMessage.success("上传完成");
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e.message || "上传失败");
  } finally {
    loading.value = false;
  }
}

// ----------------- tags -----------------
function onToggleTag(which, tagId, checked) {
  const arr = which === "manual" ? manual.tagIds : imageMeta.tagIds;
  const idx = arr.indexOf(tagId);

  if (checked && idx < 0) arr.push(tagId);
  if (!checked && idx >= 0) arr.splice(idx, 1);
}

async function handleCreateTag(which) {
  const name = newTagName.value?.trim();
  if (!name) {
    ElMessage.warning("请输入标签名");
    return;
  }

  try {
    const res = await createTag({ name });
    if (res.code !== 200) {
      ElMessage.error(res.message || "创建标签失败");
      return;
    }
    const created = res.data;

    newTagName.value = "";
    await fetchTags();

    if (created?.id) {
      const arr = which === "manual" ? manual.tagIds : imageMeta.tagIds;
      if (!arr.includes(created.id)) arr.push(created.id);
    }

    ElMessage.success("标签已创建");
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e.message || "创建标签失败");
  }
}

function goTagManage() {
  router.push("/student/tags");
}
</script>

<style scoped>
.page {
  max-width: 1120px;
  margin: 0 auto;
  padding: 18px 16px 40px;
  background: linear-gradient(180deg, rgba(37, 99, 235, 0.06), rgba(255, 255, 255, 0) 260px);
}

.hero {
  border-radius: 14px;
  border: none;
  background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 50%, #38bdf8 100%);
  color: #fff;
  margin-bottom: 12px;
}
.hero :deep(.el-card__body) {
  padding: 18px 18px;
}
.hero-title {
  font-size: 22px;
  font-weight: 900;
  letter-spacing: 0.4px;
}
.hero-sub {
  margin-top: 6px;
  opacity: 0.92;
  font-size: 13px;
}

.main {
  border-radius: 14px;
}
.form {
  margin-top: 6px;
}

.inline {
  display: inline-flex;
  gap: 10px;
  align-items: center;
}
.muted {
  color: #64748b;
  font-size: 13px;
}

.actions {
  display: flex;
  gap: 10px;
  margin-top: 6px;
  flex-wrap: wrap;
}

.tag-wrap {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 12px;
}
.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 10px;
  border-radius: 12px;
  background: #fbfdff;
  border: 1px dashed rgba(15, 23, 42, 0.18);
  min-height: 48px;
  align-items: center;
}
.tag-create {
  display: flex;
  gap: 10px;
}

.upload :deep(.el-upload--picture-card) {
  border-radius: 14px;
}
.preview {
  margin-top: 10px;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid rgba(15, 23, 42, 0.10);
}
.preview img {
  width: 100%;
  max-height: 360px;
  object-fit: contain;
  display: block;
  background: #fff;
}

.result {
  margin-top: 14px;
}

.ocr-card {
  margin-top: 14px;
  border-radius: 14px;
}
.ocr-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 800;
}
.label {
  font-weight: 800;
  margin-bottom: 8px;
  color: #0f172a;
}

@media (max-width: 900px) {
  .tag-wrap {
    grid-template-columns: 1fr;
  }
}
</style>
