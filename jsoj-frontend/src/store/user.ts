import { StoreOptions } from "vuex";
import { getToken } from "@/utils/cookie";
import ACCESS_ENUM from "@/access/accessEnum";
import { Service } from "@/userService";

export default {
  namespaced: true,
  state: () => ({
    loginUser: {
      userName: "未登录",
      userRole: ACCESS_ENUM.NOT_LOGIN,
      token: getToken() || "", // 初始化时读取 Cookie
    },
  }),
  mutations: {
    updateUser(state: any, payload: any) {
      state.loginUser = {
        ...payload,
        token: payload.token || state.loginUser.token,
      };
    },
    clearUser(state: any) {
      state.loginUser = {
        userName: "未登录",
        token: "",
        userRole: ACCESS_ENUM.NOT_LOGIN,
      };
    },
  },
  actions: {
    async getLoginUser({ commit }) {
      try {
        const res = await Service.getLoginUser();
        if (res.code === 200) {
          commit("updateUser", res.data);
        }
      } catch (error) {
        commit("clearUser");
      }
    },
  },
} as StoreOptions<any>;
