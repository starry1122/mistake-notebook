<template>
  <div class="shell">
    <aside class="shell-sidebar">
      <div class="shell-brand">
        <div class="brand-logo">📘</div>
        <div>
          <div class="brand-title">智能错题本</div>
          <div class="brand-sub">Student Workspace</div>
        </div>
      </div>

      <el-menu
        :default-active="$route.path"
        router
        class="shell-menu"
        background-color="transparent"
        text-color="#cbd5e1"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/student">
          <span>🏠 首页</span>
        </el-menu-item>
        <el-menu-item index="/student/add">
          <span>✍️ 错题采集</span>
        </el-menu-item>
        <el-menu-item index="/student/tags">
          <span>🏷️ 标签管理</span>
        </el-menu-item>
        <el-menu-item index="/student/manage">
          <span>🗂️ 错题管理</span>
        </el-menu-item>
        <el-menu-item index="/student/review">
          <span>🧠 智能复习</span>
        </el-menu-item>
        <el-menu-item index="/student/analysis">
          <span>📈 学情分析</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <section class="shell-main">
      <header class="shell-header">
        <div>
          <div class="shell-title">{{ pageTitle }}</div>
          <div class="shell-tip">持续记录 · 持续复习 · 持续进步</div>
        </div>

        <div class="shell-actions">
          <el-tag type="primary" effect="light">学生端</el-tag>
          <div class="user-box">欢迎，{{ username }}</div>
          <el-button type="danger" plain size="small" @click="logout">退出登录</el-button>
        </div>
      </header>

      <main class="shell-content">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getInfo } from "@/api/user";

const router = useRouter();
const route = useRoute();
const username = ref("同学");

const pageTitle = computed(() => {
  if (route.path.startsWith("/student/add")) return "错题采集";
  if (route.path.startsWith("/student/tags")) return "标签管理";
  if (route.path.startsWith("/student/manage")) return "错题管理";
  if (route.path.startsWith("/student/review")) return "智能复习";
  if (route.path.startsWith("/student/analysis")) return "学情分析";
  return "学习首页";
});

onMounted(() => {
  loadUsername();
});

async function loadUsername() {
  try {
    const localUser = JSON.parse(localStorage.getItem("user") || "null");
    if (localUser?.username) {
      username.value = localUser.username;
      return;
    }

    const res = await getInfo();
    const payload = res?.data?.code ? res.data : res;
    if (payload?.code === 200 && payload?.data?.username) {
      username.value = payload.data.username;
      localStorage.setItem("user", JSON.stringify(payload.data));
    }
  } catch (error) {
    console.error("加载用户信息失败", error);
  }
}

function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("user");
  router.push("/login");
}
</script>

<style scoped>
.shell {
  display: flex;
  min-height: 100vh;
}

.shell-sidebar {
  width: 244px;
  padding: 14px;
  background: linear-gradient(180deg, #0f172a 0%, #1e293b 100%);
  color: #fff;
}

.shell-brand {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 14px;
  padding: 12px;
  border-radius: 12px;
  background: rgba(148, 163, 184, 0.12);
}

.brand-logo {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(37, 99, 235, 0.3);
}

.brand-title {
  font-size: 16px;
  font-weight: 800;
}

.brand-sub {
  margin-top: 2px;
  font-size: 12px;
  color: #94a3b8;
}

.shell-menu {
  border-right: none;
  background: transparent;
}

.shell-menu :deep(.el-menu-item) {
  margin-bottom: 4px;
  border-radius: 10px;
}

.shell-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #2563eb 0%, #0ea5e9 100%);
}

.shell-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.shell-header {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 20px;
  border-bottom: 1px solid rgba(15, 23, 42, 0.07);
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(8px);
}

.shell-title {
  font-size: 20px;
  font-weight: 900;
  color: #0f172a;
}

.shell-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #64748b;
}

.shell-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-box {
  font-size: 13px;
  color: #334155;
  background: #f8fafc;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 999px;
  padding: 6px 10px;
}

.shell-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

@media (max-width: 1024px) {
  .shell-sidebar {
    width: 82px;
    padding: 10px 8px;
  }

  .shell-brand {
    justify-content: center;
  }

  .brand-title,
  .brand-sub,
  .shell-menu :deep(.el-menu-item span) {
    display: none;
  }

  .shell-menu :deep(.el-menu-item) {
    justify-content: center;
    padding: 0 !important;
  }
}

@media (max-width: 768px) {
  .shell-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .shell-actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
}
</style>

