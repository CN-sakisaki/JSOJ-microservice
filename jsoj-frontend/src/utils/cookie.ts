// src/utils/cookie.ts
import Cookies from "js-cookie";

const TOKEN_KEY = "access_token";

// 设置 Token 到 Cookie
export const setToken = (token: string) => {
  Cookies.set(TOKEN_KEY, token, {
    expires: 1, // 1天过期
    secure: process.env.NODE_ENV === "production",
    sameSite: "Lax", // 允许安全跨站请求
  });
};

// 从 Cookie 获取 Token
export const getToken = () => Cookies.get(TOKEN_KEY);

// 移除 Token
export const removeToken = () => {
  Cookies.remove(TOKEN_KEY);
};
