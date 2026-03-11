<template>
  <div class="shell">
    <aside class="shell-sidebar">
      <div class="shell-brand">
        <div class="brand-logo">🛠</div>
        <div>
          <div class="brand-title">管理员后台</div>
          <div class="brand-sub">Admin Console</div>
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
        <el-menu-item index="/admin">
          <span>📊 系统看板</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <section class="shell-main">
      <header class="shell-header">
        <div>
          <div class="shell-title">系统管理视角</div>
          <div class="shell-tip">统计评估学习效果，不进行教学干预</div>
        </div>

        <div class="shell-actions">
          <el-tag type="danger" effect="light">管理员</el-tag>
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
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { getInfo } from "@/api/user";

const router = useRouter();
const username = ref("管理员");

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
    console.error("加载管理员信息失败", error);
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
  background: linear-gradient(180deg, #1f2937 0%, #334155 100%);
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
  background: rgba(248, 113, 113, 0.28);
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
}

.shell-menu :deep(.el-menu-item) {
  margin-bottom: 4px;
  border-radius: 10px;
}

.shell-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #ef4444 0%, #f97316 100%);
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

