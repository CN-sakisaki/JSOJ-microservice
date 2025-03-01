import router from "@/router";
import store from "@/store";
import ACCESS_ENUM from "@/access/accessEnum";
import checkAccess from "@/access/checkAccess";

router.beforeEach(async (to, from, next) => {
  // 1. 获取当前登录用户信息
  let loginUser = store.state.user.loginUser;
  console.log("登录用户信息：", loginUser);
  // 2. 自动登录机制
  if (!loginUser || loginUser.userRole === ACCESS_ENUM.NOT_LOGIN) {
    console.log("没有登录，将自动登录");
    try {
      // 等待异步获取用户信息完成
      await store.dispatch("user/getLoginUser");
      loginUser = store.state.user.loginUser; // 更新用户信息
      console.log("登录成功，目前登录的用户：", loginUser);
    } catch (error) {
      console.error("自动登录失败:", error);
      // 清除可能的无效 token
      store.commit("user/clearUser");
    }
  }

  // 3. 获取目标路由所需权限
  const needAccess = (to.meta?.access as string) ?? ACCESS_ENUM.NOT_LOGIN;

  // 4. 权限校验逻辑
  if (needAccess !== ACCESS_ENUM.NOT_LOGIN) {
    // 情况 1: 未登录
    if (!loginUser?.userRole) {
      next({
        path: "/user/login",
        query: { redirect: to.fullPath },
      });
      return;
    }

    // 情况 2: 已登录但权限不足
    if (!checkAccess(loginUser, needAccess)) {
      next("/noAuth");
      return;
    }
  }
  // 5. 放行访问
  next();
});
