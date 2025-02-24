<template>
  <div id="userLogin">
    <h2>用户登录</h2>
    <a-form
      class="loginForm"
      style="max-width: 480px"
      :model="form"
      @submit="handleSubmit"
    >
      <a-form-item field="userAccount" label="账号:">
        <a-input
          v-model="form.userAccount"
          tooltip="密码不少于 8 位"
          placeholder="请输入账号..."
        />
      </a-form-item>
      <a-form-item field="userPassword" label="密码:">
        <a-input-password
          v-model="form.userPassword"
          placeholder="请输入密码..."
        />
      </a-form-item>
      <a-form-item>
        <a-button
          class="arco-form-item-content-flex"
          type="primary"
          html-type="submit"
          >登录
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useStore } from "vuex";
import { UserControllerService, UserLoginRequest } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { setToken } from "@/utils/cookie";

/**
 * 表单信息
 */
const form = reactive({
  userAccount: "",
  userPassword: "",
} as UserLoginRequest);

const router = useRouter();
const store = useStore();
const route = useRoute();

/**
 * 提交表单
 */
const handleSubmit = async () => {
  const res = await UserControllerService.userLoginUsingPost(form);
  if (res.code === 0) {
    // 存储 Token 到 Cookie 和 Vuex
    setToken(res.data.token);
    store.commit("user/updateUser", res.data);
    await store.dispatch("getLoginUser");
    // 3. 跳转到首页或重定向页
    const redirect = route.query.redirect || "/";
    router.push(redirect as string);
  } else {
    message.error("登录失败，" + res.message);
  }
};
</script>
<style>
#userLogin {
}

#userLogin .loginForm {
  margin: auto;
}

#userLogin .arco-form-item-content-flex {
  justify-content: right;
}
</style>
