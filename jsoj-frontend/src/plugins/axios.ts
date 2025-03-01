import store from "@/store";
import router from "@/router";
import axios, { AxiosError } from "axios";
import { getToken, removeToken, setToken } from "@/utils/cookie";

// 扩展 axios 类型
declare module "axios" {
  interface AxiosRequestConfig {
    _retry?: boolean;
  }
}

const createService = () => {
  const instance = axios.create({
    baseURL: "http://localhost:8101",
    timeout: 15000,
    withCredentials: true,
  });

  // 请求拦截器
  instance.interceptors.request.use((config) => {
    const token = getToken();
    if (token) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });

  // 刷新状态管理
  let isRefreshing = false;
  const requestQueue: Array<(token: string) => void> = [];

  // 响应拦截器
  instance.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
      const originalConfig = error.config;

      if (
        error.response?.status === 401 &&
        originalConfig &&
        !originalConfig._retry
      ) {
        originalConfig._retry = true;

        if (!isRefreshing) {
          isRefreshing = true;
          try {
            const newToken = await refreshToken(instance);
            setToken(newToken);
            processQueue(newToken);
            return instance(originalConfig);
          } catch (refreshError) {
            processQueue(null, refreshError as Error);
            handleAuthError();
            return Promise.reject(refreshError);
          } finally {
            isRefreshing = false;
          }
        }

        return new Promise((resolve, reject) => {
          requestQueue.push((token: string) => {
            originalConfig.headers = originalConfig.headers || {};
            originalConfig.headers.Authorization = `Bearer ${token}`;
            resolve(instance(originalConfig));
          });
        });
      }

      return Promise.reject(error);
    }
  );

  const processQueue = (token?: string | null, error?: Error) => {
    while (requestQueue.length) {
      const callback = requestQueue.shift();
      if (token && callback) {
        callback(token);
      } else if (error && callback) {
        callback(""); // 触发请求失败
      }
    }
  };

  const refreshToken = async (instance: any): Promise<string> => {
    try {
      // 使用你生成的服务方法调用刷新接口
      const response = await instance.get("/get/token", {
        params: { id: store.state.user.id },
      });
      return response.data.data; // 根据实际返回结构调整
    } catch (error) {
      throw new Error("Token refresh failed");
    }
  };

  const handleAuthError = () => {
    store.commit("user/clearUser");
    removeToken();
    const redirect = encodeURIComponent(router.currentRoute.value.fullPath);
    router.push(`/user/login?redirect=${redirect}`);
  };

  return instance;
};

export const request = createService();
