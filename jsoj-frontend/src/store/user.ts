import { StoreOptions } from "vuex";
import { UserControllerService } from "../../generated";
import { getToken, removeToken } from "@/utils/cookie";
import ACCESS_ENUM from "@/access/accessEnum";
import router from "@/router";

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
        const res = await UserControllerService.getLoginUserUsingGet();
        if (res.code === 0) {
          commit("updateUser", res.data);
        }
      } catch (error) {
        commit("clearUser");
      }
    },
    // // Vuex action
    async logout({ commit }) {
      try {
        await UserControllerService.userLogoutUsingPost();
      } finally {
        commit("clearUser");
        removeToken();
        router.push("/login");
      }
    },
  },
} as StoreOptions<any>;

// export default {
//   // 使用命名空间
//   namespace: true,
//   state: () => ({
//     loginUser: {
//       userName: "未登录",
//     },
//   }),
//   actions: {
//     async getLoginUser({ commit, state }, payload) {
//       // 从远程获取登录信息;
//       const res = await UserControllerService.getLoginUserUsingGet();
//       if (res.code === 0) {
//         commit("updateUser", res.data);
//         console.log("从远程获取的登录信息为：", res.data);
//       } else {
//         commit("updateUser", {
//           ...state.loginUser,
//           userRole: ACCESS_ENUM.NOT_LOGIN,
//         });
//       }
//     },
//   },
//   mutations: {
//     updateUser(state, payload) {
//       state.loginUser = payload;
//     },
//   },
// } as StoreOptions<any>;
