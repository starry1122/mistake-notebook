import { createRouter, createWebHistory } from "vue-router";

import Login from "@/views/Login.vue";
import StudentLayout from "@/layouts/StudentLayout.vue";
import AdminLayout from "@/layouts/AdminLayout.vue";

import StudentDashboard from "@/views/student/Dashboard.vue";
import StudentAnalysis from "@/views/student/Analysis.vue";
import AdminDashboard from "@/views/admin/Dashboard.vue";

const routes = [
  { path: "/", redirect: "/login" },
  { path: "/login", component: Login },

  {
    path: "/student",
    component: StudentLayout,
    children: [
      {
        path: "",
        name: "StudentDashboard",
        component: StudentDashboard,
      },
      {
        path: "add",
        name: "StudentAddQuestion",
        component: () => import("@/views/student/AddQuestion.vue"),
        alias: ["add-question"],
      },
      {
        path: "tags",
        name: "StudentTagManage",
        component: () => import("@/views/student/TagManage.vue"),
      },
      {
        path: "manage",
        name: "StudentQuestionManage",
        component: () => import("@/views/student/QuestionManage.vue"),
      },
      {
        path: "review",
        name: "StudentReview",
        component: () => import("@/views/student/Review.vue"),
      },
      {
        path: "analysis",
        name: "StudentAnalysis",
        component: StudentAnalysis,
      },
    ],
  },

  {
    path: "/admin",
    component: AdminLayout,
    children: [
      {
        path: "",
        name: "AdminDashboard",
        component: AdminDashboard,
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  if (to.path === "/login") {
    next();
    return;
  }

  const token = localStorage.getItem("token");
  if (!token) {
    next("/login");
    return;
  }

  const role = getCurrentRole(token);
  if (!role) {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    next("/login");
    return;
  }

  if (to.path.startsWith("/admin")) {
    if (role === "admin") {
      next();
    } else {
      next("/student");
    }
    return;
  }

  if (to.path.startsWith("/student")) {
    if (role === "admin") {
      next("/admin");
    } else {
      next();
    }
    return;
  }

  next();
});

function getCurrentRole(token) {
  try {
    const localUser = JSON.parse(localStorage.getItem("user") || "null");
    if (localUser?.role) {
      return localUser.role;
    }
  } catch (error) {
    console.error("读取本地用户信息失败", error);
  }

  return parseRoleFromToken(token);
}

function parseRoleFromToken(token) {
  try {
    const segments = String(token || "").split(".");
    if (segments.length < 2) {
      return "";
    }

    let payload = segments[1].replace(/-/g, "+").replace(/_/g, "/");
    while (payload.length % 4 !== 0) {
      payload += "=";
    }

    const decoded = JSON.parse(window.atob(payload));
    return decoded?.role || "";
  } catch (error) {
    console.error("解析 Token 角色失败", error);
    return "";
  }
}

export default router;

