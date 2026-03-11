<template>
  <div class="login-page">
    <div class="login-panel">
      <div class="panel-left">
        <div class="slogan-tag">Mistake Notebook</div>
        <h1>智能错题本系统</h1>
        <p>围绕错题采集、复习计划与学情分析，打造清晰高效的个人学习闭环。</p>

        <div class="feature-list">
          <div class="feature-item">✅ 错题采集与标签管理</div>
          <div class="feature-item">✅ 艾宾浩斯智能复习</div>
          <div class="feature-item">✅ 学情可视化分析报告</div>
        </div>

        <div class="tip-box">
          <div><strong>管理员测试账号：</strong>admin</div>
          <div><strong>默认密码：</strong>admin123</div>
        </div>
      </div>

      <div class="panel-right">
        <div class="form-header">
          <h2>{{ isLogin ? "欢迎回来" : "创建账号" }}</h2>
          <p>{{ isLogin ? "请输入账号密码登录系统" : "创建学生账号后即可开始使用" }}</p>
        </div>

        <el-form :model="form" class="login-form" @keyup.enter="handleSubmit">
          <el-form-item>
            <el-input v-model="form.username" placeholder="请输入用户名" size="large" clearable />
          </el-form-item>

          <el-form-item>
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              clearable
            />
          </el-form-item>

          <el-button type="primary" class="submit-btn" size="large" :loading="loading" @click="handleSubmit">
            {{ isLogin ? "登录" : "注册" }}
          </el-button>
        </el-form>

        <div class="switch-row">
          <span>{{ isLogin ? "还没有账号？" : "已有账号？" }}</span>
          <el-button type="primary" link @click="isLogin = !isLogin">
            {{ isLogin ? "去注册" : "去登录" }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { getInfo, login, register } from "@/api/user";

const router = useRouter();

const loading = ref(false);
const isLogin = ref(true);
const form = ref({
  username: "",
  password: "",
});

function unwrapResult(res) {
  if (res && typeof res === "object" && "data" in res && res.data && typeof res.data === "object" && "code" in res.data) {
    return res.data;
  }
  return res;
}

async function handleSubmit() {
  const username = form.value.username?.trim();
  const password = form.value.password?.trim();

  if (!username || !password) {
    ElMessage.error("请输入完整的用户名和密码");
    return;
  }

  loading.value = true;
  try {
    if (isLogin.value) {
      const loginRes = await login({ username, password });
      const loginResult = unwrapResult(loginRes);

      if (!loginResult || loginResult.code !== 200 || !loginResult.data) {
        ElMessage.error(loginResult?.message || "登录失败");
        return;
      }

      const token = loginResult.data;
      localStorage.setItem("token", token);

      let user = null;
      try {
        const infoRes = await getInfo();
        const infoResult = unwrapResult(infoRes);
        if (infoResult?.code === 200 && infoResult?.data) {
          user = infoResult.data;
          localStorage.setItem("user", JSON.stringify(infoResult.data));
        }
      } catch (error) {
        console.error("获取用户信息失败", error);
      }

      ElMessage.success("登录成功");
      if (user?.role === "admin") {
        router.push("/admin");
      } else {
        router.push("/student");
      }
      return;
    }

    const registerRes = await register({ username, password });
    const registerResult = unwrapResult(registerRes);
    if (!registerResult || registerResult.code !== 200) {
      ElMessage.error(registerResult?.message || "注册失败");
      return;
    }

    ElMessage.success("注册成功，请登录");
    isLogin.value = true;
  } catch (error) {
    console.error(error);
    ElMessage.error("请求失败，请稍后重试");
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background:
    radial-gradient(circle at 15% 20%, rgba(37, 99, 235, 0.3) 0%, transparent 35%),
    radial-gradient(circle at 90% 80%, rgba(14, 165, 233, 0.26) 0%, transparent 35%),
    linear-gradient(135deg, #1e3a8a 0%, #312e81 35%, #0f172a 100%);
}

.login-panel {
  width: min(1080px, 100%);
  min-height: 610px;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  border-radius: 22px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.18);
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.45);
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(16px);
}

.panel-left {
  padding: 46px 40px;
  color: #fff;
}

.slogan-tag {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  color: #dbeafe;
  background: rgba(148, 163, 184, 0.2);
}

.panel-left h1 {
  margin-top: 16px;
  margin-bottom: 12px;
  font-size: 36px;
  line-height: 1.2;
}

.panel-left p {
  margin: 0;
  color: rgba(226, 232, 240, 0.95);
  line-height: 1.8;
}

.feature-list {
  margin-top: 26px;
  display: grid;
  gap: 12px;
}

.feature-item {
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(148, 163, 184, 0.16);
  border: 1px solid rgba(226, 232, 240, 0.16);
}

.tip-box {
  margin-top: 24px;
  padding: 12px 14px;
  border-radius: 12px;
  line-height: 1.9;
  color: #f8fafc;
  background: rgba(14, 116, 144, 0.35);
}

.panel-right {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 42px 36px;
  background: rgba(255, 255, 255, 0.96);
}

.form-header h2 {
  margin: 0;
  font-size: 28px;
  color: #0f172a;
}

.form-header p {
  margin-top: 8px;
  color: #64748b;
  font-size: 14px;
}

.login-form {
  margin-top: 22px;
}

.submit-btn {
  width: 100%;
  margin-top: 8px;
}

.switch-row {
  margin-top: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  color: #64748b;
  font-size: 13px;
}

@media (max-width: 980px) {
  .login-panel {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .panel-left {
    padding: 30px 24px;
  }

  .panel-left h1 {
    font-size: 28px;
  }

  .panel-right {
    padding: 28px 24px;
  }
}
</style>

