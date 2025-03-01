// 封装工具函数
import { useRouter } from "vue-router";

export const safeRedirect = (defaultPath = "/home") => {
  const router = useRouter();
  const currentRoute = router.currentRoute.value;

  const redirect = currentRoute.query.redirect;
  const path = validateRedirectPath(redirect) || defaultPath;

  return router.replace(path);
};

// 验证跳转路径合法性（防止开放重定向）
const validateRedirectPath = (path: unknown): string => {
  const defaultPath = "/home";

  if (typeof path !== "string") return defaultPath;
  if (!path.startsWith("/")) return defaultPath;
  if (path.includes("//") || path.includes("\\")) return defaultPath;

  return path;
};
