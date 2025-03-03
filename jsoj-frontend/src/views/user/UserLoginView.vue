<template>
  <div class="login-container">
    <div class="login-card">
      <!-- Logo 和标题 -->
      <div class="logo-title">
        <img class="logo" src="@/assets/cubic.png" alt="Logo" />
        <div class="title">Cubic OJ</div>
      </div>
      <!-- 扫码登录固定在右上角 -->
      <div class="qrcode-corner">
        <icon-scan class="scan-icon" />
        <div class="qrcode-tip">扫码登录</div>
      </div>

      <!-- 登录/注册切换标签 -->
      <div class="auth-tabs">
        <div
          class="tab-item"
          :class="{ active: isLogin }"
          @click="isLogin = true"
        >
          登录
        </div>
        <div
          class="tab-item"
          :class="{ active: !isLogin }"
          @click="isLogin = false"
        >
          注册
        </div>
      </div>

      <!-- 登录表单 -->
      <template v-if="isLogin">
        <!-- 登录方式切换 -->
        <div class="login-methods">
          <a-button
            v-for="method in loginMethods"
            :key="method.value"
            :type="activeLoginMethod === method.value ? 'primary' : 'text'"
            @click="activeLoginMethod = method.value"
          >
            {{ method.label }}
          </a-button>
        </div>

        <!-- 账号密码登录 -->
        <a-form
          v-show="activeLoginMethod === 'account'"
          class="login-form"
          :model="accountLoginForm"
          @submit="handleAccountLogin"
        >
          <a-form-item
            field="userPhone"
            :rules="[{ required: true, message: '手机号为必填项' }]"
          >
            <template #label>手机号：</template>

            <a-input
              v-model="accountLoginForm.userPhone"
              placeholder="+86 输入手机号"
              allow-clear
            >
              <template #prefix>
                <icon-phone />
              </template>
            </a-input>
          </a-form-item>
          <a-form-item
            field="userPassword"
            :rules="[{ required: true, message: '密码为必填项' }]"
          >
            <template #label>密码：</template>
            <div class="input-container"></div>
            <a-input-password
              invisible-button
              v-model="accountLoginForm.userPassword"
              placeholder="输入密码"
              allow-clear
            >
              <template #prefix>
                <icon-lock />
              </template>
            </a-input-password>
          </a-form-item>
          <a-button type="primary" html-type="submit" long class="custom-button"
            >账号登录
          </a-button>
        </a-form>

        <!-- 手机号登录 -->
        <a-form
          v-show="activeLoginMethod === 'phone'"
          class="login-form"
          :model="loginForm"
          @submit="handleLogin"
        >
          <a-form-item
            field="phone"
            :rules="[{ required: true, message: '手机号为必填项' }]"
          >
            <template #label>手机号：</template>
            <a-input
              v-model="loginForm.phone"
              placeholder="+86 输入手机号"
              allow-clear
            >
              <template #prefix>
                <icon-phone />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item
            field="code"
            :rules="[{ required: true, message: '验证码为必填项' }]"
          >
            <template #label>验证码：</template>
            <div class="code-input-group">
              <a-input
                v-model="loginForm.code"
                placeholder="验证码"
                allow-clear
              />
              <a-button
                type="text"
                class="get-code-btn"
                :disabled="loginCountdown > 0"
                @click="handleLoginCode"
              >
                {{
                  loginCountdown > 0 ? `${loginCountdown}s后获取` : "获取验证码"
                }}
              </a-button>
            </div>
          </a-form-item>

          <a-button type="primary" html-type="submit" long class="custom-button"
            >登录
          </a-button>
        </a-form>
      </template>

      <!-- 注册表单 -->
      <template v-else>
        <a-form
          class="register-form"
          :model="registerForm"
          @submit="handleRegister"
        >
          <a-form-item
            field="userPhone"
            :rules="[{ required: true, message: '手机号为必填项' }]"
          >
            <template #label>手机号：</template>
            <a-input
              v-model="registerForm.userPhone"
              placeholder="+86 输入手机号"
              allow-clear
            >
              <template #prefix>
                <icon-phone />
              </template>
            </a-input>
          </a-form-item>
          <a-form-item
            field="userPassword"
            :rules="[{ required: true, message: '密码为必填项' }]"
          >
            <template #label>密码：</template>
            <a-input-password
              invisible-button
              v-model="registerForm.userPassword"
              placeholder="输入密码"
              allow-clear
            >
              <template #prefix>
                <icon-lock />
              </template>
            </a-input-password>
          </a-form-item>
          <a-form-item
            field="checkPassword"
            :rules="[
              { required: true, message: '确认密码为必填项' },
              { validator: validateConfirmPassword },
            ]"
          >
            <template #label>确认密码：</template>
            <a-input-password
              invisible-button
              v-model="registerForm.checkPassword"
              placeholder="再次输入密码"
              allow-clear
            >
              <template #prefix>
                <icon-lock />
              </template>
            </a-input-password>
          </a-form-item>
          <a-form-item field="userEmail">
            <template #label>邮箱：</template>
            <a-input
              v-model="registerForm.userEmail"
              placeholder="输入邮箱地址（可选）"
              allow-clear
            >
              <template #prefix>
                <icon-email />
              </template>
            </a-input>
          </a-form-item>
          <a-button type="primary" html-type="submit" long class="custom-button"
            >立即注册
          </a-button>
        </a-form>
      </template>

      <!-- 协议声明 -->
      <div class="agreement">
        注册或登录即代表您同意
        <a-link href="#">《用户协议》</a-link>
        和
        <a-link href="#">《隐私协议》</a-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, Ref, ref } from "vue";
import {
  IconEmail,
  IconLock,
  IconPhone,
  IconScan,
} from "@arco-design/web-vue/es/icon";
import { Message } from "@arco-design/web-vue";
import store from "@/store";
import {
  BaseResponseLong,
  BaseResponseUserVO,
  Service,
  UserLoginRequest,
  UserRegisterRequest,
} from "@/userService";
import { setToken } from "@/utils/cookie";
import { useRouter } from "vue-router";

const router = useRouter();

// 登录/注册状态
const isLogin = ref(true);

// 登录方式配置
const loginMethods = [
  { label: "账号密码登录", value: "account" },
  { label: "手机验证码登录", value: "phone" },
];
const activeLoginMethod = ref("account");

// 账号密码登录表单
const accountLoginForm = reactive({
  userPhone: "",
  userPassword: "",
});

// 手机号登录表单
const loginForm = reactive({
  userPhone: "",
  code: "",
});
const loginCountdown = ref(0);

// 注册表单
const registerForm = reactive({
  userPhone: "",
  userPassword: "",
  checkPassword: "",
  userEmail: "",
});

// 验证码处理逻辑
const handleCountdown = (countdownRef: Ref<number>) => {
  countdownRef.value = 60;
  const timer = setInterval(() => {
    if (countdownRef.value <= 0) {
      clearInterval(timer);
      return;
    }
    countdownRef.value--;
  }, 1000);
};

const handleLoginCode = () => handleCountdown(loginCountdown);

// 自定义验证函数，用于验证确认密码和密码是否相同
const validateConfirmPassword = (value: string) => {
  if (value !== registerForm.userPassword) {
    Message.error("两次输入的密码不一致");
    return { valid: false, error: "两次输入的密码不一致" };
  }
  return { valid: true, error: null };
};

// 登录
const handleAccountLogin = async () => {
  try {
    // 从表单中获取用户输入的账号和密码
    const { userPhone, userPassword } = accountLoginForm;
    // 检查账号和密码是否为空
    if (!userPhone || !userPassword) {
      Message.error("账号和密码不能为空");
      return;
    }
    // 构建登录请求体
    const requestBody: UserLoginRequest = {
      userPhone,
      userPassword,
    };
    // 调用登录接口
    const response = await Service.userLogin(requestBody);
    await handleLoginSuccess(response);
  } catch (error) {
    Message.error("登录请求出错，请稍后重试");
  }
};

const handleLogin = async () => {
  /* 手机号登录逻辑 */
};

const handleRegister = async () => {
  try {
    // 从表单中获取用户输入的账号和密码
    const { userPhone, userPassword, checkPassword, userEmail } = registerForm;
    // 检查账号和密码是否为空
    if (!userPhone || !userPassword || !checkPassword) {
      Message.error("账号和密码不能为空");
      return;
    }
    // 构建登录请求体
    const requestBody: UserRegisterRequest = {
      userPhone,
      userPassword,
      checkPassword,
      userEmail,
    };
    // 调用登录接口
    const response = await Service.userRegister(requestBody);
    await handleRegisterSuccess(response);
  } catch (error) {
    Message.error("登录请求出错，请稍后重试");
  }
};

// 登录成功后的处理逻辑
const handleLoginSuccess = async (response: BaseResponseUserVO) => {
  if (response.code === 200 && response.data?.token) {
    const token = response.data.token;
    setToken(token);
    await store.dispatch("user/getLoginUser");
    // 处理跳转逻辑
    const redirectPath = router.currentRoute.value.query?.redirect;
    const isValidPath = (path: unknown): path is string =>
      typeof path === "string" && path.startsWith("/");

    if (isValidPath(redirectPath)) {
      // 跳转到来源页面
      await router.replace(redirectPath);
    } else {
      // 跳转到默认首页
      await router.replace("/home");
    }
    Message.success("登录成功");
  }
};

// 注册成功后的处理逻辑
const handleRegisterSuccess = async (response: BaseResponseLong) => {
  if (response.code === 200) {
    Message.success("注册成功");
    isLogin.value = true;
  } else {
    Message.error("注册失败，请稍后再试～");
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 75vh;
}

.login-card {
  position: relative;
  width: 475px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.logo-title {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 30px;
  justify-content: center; /* 让 logo 和标题居中 */
}

.logo {
  height: 48px;
  width: auto;
}

.title {
  font-size: 1.5em;
  font-weight: 500;
  color: #444;
}

.qrcode-corner {
  position: absolute;
  top: 20px;
  right: 20px;
  text-align: center;
}

.auth-tabs {
  display: flex;
  margin-bottom: 30px;
  border-bottom: 1px solid #e5e6eb;
}

.tab-item {
  flex: 1;
  padding: 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.tab-item.active {
  color: #165dff;
  border-bottom: 2px solid #165dff;
}

.login-methods {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
  justify-content: center;
}

.login-form,
.register-form {
  margin-top: 24px;
}

.code-input-group {
  display: flex;
  gap: 12px;
}

.get-code-btn {
  flex-shrink: 0;
}

.agreement {
  margin-top: 24px;
  font-size: 12px;
  color: #86909c;
  text-align: center;
}

.custom-button {
  height: 30px; /* 设置按钮的高度 */
  width: 377px;
  font-size: 15px; /* 设置按钮文字的大小 */
  display: block; /* 将按钮设置为块级元素 */
  margin: 0 0 0 98px; /* 设置左右外边距为 auto 实现水平居中 */
}
</style>
