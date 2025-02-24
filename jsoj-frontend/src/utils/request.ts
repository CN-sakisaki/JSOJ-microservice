import axios from "axios";
import { getToken, removeToken, setToken } from "./cookie";
import store from "@/store";
import router from "@/router";

const service = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL,
  timeout: 15000,
  withCredentials: true, // 关键配置：允许跨域携带 Cookie
});

// 请求拦截器：自动携带 Token
service.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 用于标记是否正在刷新 token
let isRefreshing = false;
// 存储因 token 过期而被挂起的请求
let requests: ((newToken: string) => Promise<any>)[] = [];

// 响应拦截器：处理 Token 过期
service.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalConfig = error.config;
    if (error.response?.status === 401) {
      if (!originalConfig._retry) {
        if (!isRefreshing) {
          isRefreshing = true;
          try {
            // 调用刷新 token 的接口
            const id = store.state.user.id; // 假设用户 id 存储在 store 中
            const refreshResponse = await service.get("/get/token", {
              params: { id },
            });
            const newToken = refreshResponse.data.data;
            // 更新本地存储的 token
            setToken(newToken);
            // 更新请求头中的 token
            originalConfig.headers.Authorization = `Bearer ${newToken}`;
            // 重新发起原请求
            requests.forEach((callback) => callback(newToken));
            requests = [];
            return service(originalConfig);
          } catch (refreshError) {
            // 刷新 token 失败，清除前端存储的 Token 并跳转到登录页
            store.commit("user/clearUser");
            removeToken();
            const redirect = encodeURIComponent(
              router.currentRoute.value.fullPath
            );
            router.push(`/user/login?redirect=${redirect}`);
            return Promise.reject(refreshError);
          } finally {
            isRefreshing = false;
          }
        } else {
          // 正在刷新 token，将原请求挂起
          return new Promise((resolve) => {
            requests.push((newToken: string) => {
              originalConfig.headers.Authorization = `Bearer ${newToken}`;
              // 直接返回 Promise
              return service(originalConfig);
            });
          });
        }
      }
    }
    return Promise.reject(error);
  }
);

export default service;
