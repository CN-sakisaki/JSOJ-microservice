import { createRouter, createWebHistory } from "vue-router";
import { routes } from "@/router/routers";
import ACCESS_ENUM from "@/access/accessEnum";
import checkAccess from "@/access/checkAccess";
import { useStore } from "vuex";

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

router.beforeEach(async (to, from) => {
  const store = useStore();
  const loginUser = store.state.user.loginUser;

  // 自动登录逻辑
  if (!loginUser?.userRole) {
    await store.dispatch("user/getLoginUser");
  }

  const access = to.meta?.access;
  const needAccess =
    typeof access === "string" ? access : ACCESS_ENUM.NOT_LOGIN;

  // 如果目标页面需要登录
  if (needAccess !== ACCESS_ENUM.NOT_LOGIN) {
    // 未登录则跳转到登录页
    if (!loginUser?.userRole) {
      return `/user/login?redirect=${to.fullPath}`;
    }
    // 已登录但权限不足
    if (!checkAccess(loginUser, needAccess)) {
      return "/noAuth";
    }
  }
});

export default router;
