<template>
  <div class="page">
    <el-card class="hero" shadow="never">
      <div class="hero-title">标签管理</div>
      <div class="hero-sub">新增/删除标签；删除会级联移除错题关联</div>
    </el-card>

    <el-card class="main" shadow="never">
      <div class="toolbar">
        <el-input v-model="name" placeholder="输入新标签名，如：易错 / 计算 / 概念" clearable />
        <el-button type="primary" :loading="loading" @click="add">新增</el-button>
        <el-button plain @click="back">返回错题采集</el-button>
      </div>

      <el-table :data="tags" style="width: 100%" v-loading="loading" empty-text="暂无标签">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="name" label="标签名" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button type="danger" plain size="small" @click="del(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { createTag, deleteTag, listMyTags } from "@/api/tag";
import { useRouter } from "vue-router";

const router = useRouter();

const loading = ref(false);
const name = ref("");
const tags = ref([]);

async function fetchTags() {
  const res = await listMyTags();
  if (res.code !== 200) throw new Error(res.message || "加载失败");
  tags.value = res.data || [];
}

onMounted(async () => {
  try {
    await fetchTags();
  } catch (e) {
    ElMessage.error(e.message || "标签加载失败");
  }
});

async function add() {
  const n = name.value.trim();
  if (!n) {
    ElMessage.warning("请输入标签名");
    return;
  }

  loading.value = true;
  try {
    const res = await createTag({ name: n });
    if (res.code !== 200) {
      ElMessage.error(res.message || "新增失败");
      return;
    }
    name.value = "";
    await fetchTags();
    ElMessage.success("新增成功");
  } finally {
    loading.value = false;
  }
}

async function del(id) {
  await ElMessageBox.confirm("确认删除该标签？删除后将同时移除错题关联。", "提示", {
    confirmButtonText: "删除",
    cancelButtonText: "取消",
    type: "warning",
  });

  loading.value = true;
  try {
    const res = await deleteTag(id);
    if (res.code !== 200) {
      ElMessage.error(res.message || "删除失败");
      return;
    }
    await fetchTags();
    ElMessage.success("删除成功");
  } finally {
    loading.value = false;
  }
}

function back() {
  router.push("/student/add");
}
</script>

<style scoped>
.page {
  max-width: 960px;
  margin: 0 auto;
  padding: 18px 16px 40px;
  background: linear-gradient(180deg, rgba(20, 184, 166, 0.06), rgba(255, 255, 255, 0) 260px);
}

.hero {
  border-radius: 14px;
  border: none;
  background: linear-gradient(135deg, #0f766e 0%, #14b8a6 55%, #67e8f9 100%);
  color: #fff;
  margin-bottom: 12px;
}
.hero :deep(.el-card__body) {
  padding: 18px 18px;
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

.main {
  border-radius: 14px;
}

.toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.toolbar :deep(.el-input) {
  width: 360px;
  max-width: 100%;
}
</style>
