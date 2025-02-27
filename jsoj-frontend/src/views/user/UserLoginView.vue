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
            field="account"
            :rules="[{ required: true, message: '账号为必填项' }]"
          >
            <template #label>账号：</template>

            <a-input
              v-model="accountLoginForm.account"
              placeholder="输入账号"
              allow-clear
            >
              <template #prefix>
                <icon-user />
              </template>
            </a-input>
          </a-form-item>
          <a-form-item
            field="password"
            :rules="[{ required: true, message: '密码为必填项' }]"
          >
            <template #label>密码：</template>
            <div class="input-container"></div>
            <a-input
              v-model="accountLoginForm.password"
              type="password"
              placeholder="输入密码"
              allow-clear
            >
              <template #prefix>
                <icon-lock />
              </template>
            </a-input>
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
      <a-form
        v-else
        class="register-form"
        :model="registerForm"
        @submit="handleRegister"
      >
        <a-form-item
          field="account"
          :rules="[{ required: true, message: '账号为必填项' }]"
        >
          <template #label>账号：</template>
          <a-input
            v-model="registerForm.account"
            placeholder="输入账号"
            allow-clear
          >
            <template #prefix>
              <icon-user />
            </template>
          </a-input>
        </a-form-item>
        <a-form-item
          field="password"
          :rules="[{ required: true, message: '密码为必填项' }]"
        >
          <template #label>密码：</template>
          <a-input
            v-model="registerForm.password"
            type="password"
            placeholder="输入密码"
            allow-clear
          >
            <template #prefix>
              <icon-lock />
            </template>
          </a-input>
        </a-form-item>
        <a-form-item
          field="confirmPassword"
          :rules="[
            { required: true, message: '确认密码为必填项' },
            { validator: validateConfirmPassword },
          ]"
        >
          <template #label>确认密码：</template>
          <a-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="再次输入密码"
            allow-clear
          >
            <template #prefix>
              <icon-lock />
            </template>
          </a-input>
        </a-form-item>
        <a-form-item field="email">
          <template #label>邮箱：</template>
          <a-input
            v-model="registerForm.email"
            placeholder="输入邮箱地址（可选）"
            allow-clear
          >
            <template #prefix>
              <icon-email />
            </template>
          </a-input>
        </a-form-item>
        <a-form-item field="phone">
          <template #label>手机号：</template>
          <a-input
            v-model="registerForm.phone"
            placeholder="+86 输入手机号（可选）"
            allow-clear
          >
            <template #prefix>
              <icon-phone />
            </template>
          </a-input>
        </a-form-item>
        <a-button type="primary" html-type="submit" long class="custom-button"
          >立即注册
        </a-button>
      </a-form>

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
  IconUser,
} from "@arco-design/web-vue/es/icon";

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
  account: "",
  password: "",
});

// 手机号登录表单
const loginForm = reactive({
  phone: "",
  code: "",
});
const loginCountdown = ref(0);

// 注册表单
const registerForm = reactive({
  account: "",
  password: "",
  confirmPassword: "",
  email: "",
  phone: "",
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
const validateConfirmPassword = (rule: any, value: string) => {
  if (value !== registerForm.password) {
    return Promise.reject(new Error("两次输入的密码不一致"));
  }
  return Promise.resolve();
};

// 提交处理
const handleAccountLogin = async () => {
  /* 账号密码登录逻辑 */
};
const handleLogin = async () => {
  /* 手机号登录逻辑 */
};
const handleRegister = async () => {
  const form = document.querySelector(".register-form") as HTMLFormElement;
  if (form) {
    form.reportValidity();
    if (form.checkValidity()) {
      // 表单验证通过，执行注册逻辑
      console.log("注册信息:", registerForm);
    }
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
