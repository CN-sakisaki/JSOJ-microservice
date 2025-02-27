<template>
  <a-row id="globalHeader" class="header-container" :wrap="false">
    <a-col flex="auto">
      <div class="header-content">
        <!-- Logo -->
        <div class="title-bar">
          <img class="logo" src="../assets/cubic.png" alt="" />
          <div class="title">Cubic OJ</div>
        </div>

        <!-- 菜单 -->
        <a-menu
          mode="horizontal"
          :selected-keys="selectedKeys"
          @menu-item-click="doMenuClick"
          class="nav-menu"
        >
          <a-menu-item v-for="item in visibleRoutes" :key="item.path"
            >{{ item.name }}
          </a-menu-item>
        </a-menu>

        <!-- 用户信息 -->
        <div class="user-info">
          <img class="avatar" src="../assets/cubic.png" alt="" />
          <div>{{ store.state.user?.loginUser?.userName ?? "未登录" }}</div>
        </div>
      </div>
    </a-col>
  </a-row>
</template>

<script setup lang="ts">
import { routes } from "@/router/routers";
import { useRouter } from "vue-router";
import { computed, ref } from "vue";
import { useStore } from "vuex";
import checkAccess from "@/access/checkAccess";

const router = useRouter();
// 默认主页
const selectedKeys = ref(["/home"]);
// 路由跳转后，更新选中的菜单项
router.afterEach((to, from, failure) => {
  selectedKeys.value = [to.path];
});
// 过滤只需要展示的元素数组
const visibleRoutes = computed(() => {
  // 1. 查找基础布局路由
  const layoutRoute = routes.find((route) => route.path === "/");

  // 2. 获取其子路由并进行过滤
  return (
    layoutRoute?.children?.filter((item) => {
      // 过滤隐藏菜单项
      if (item.meta?.hideInMenu) return false;
      // 权限检查
      return checkAccess(
        store.state.user.loginUser,
        item?.meta?.access as string
      );
    }) || []
  );
});
//路由跳转
const doMenuClick = (key: string) => {
  router.push({
    path: key,
  });
};

const store = useStore();
console.log("GlobalHeader查看store:", store.state.user);
</script>

<style scoped>
/* 全局头部容器样式 */
#globalHeader {
  /* 设置头部背景颜色为白色 */
  background-color: white;
  /* 为头部底部添加一条浅灰色的边框，增强视觉分割 */
  border-bottom: 1px solid #eee;
  /* 固定头部的高度为 64px，保证布局的一致性 */
  height: 64px;
}

/* 头部内容容器样式 */
.header-container {
  /* 设置头部内容容器的最大宽度为 1200px，使其在大屏幕上有合适的显示范围 */
  max-width: 1400px;
  /* 使头部内容容器在水平方向上居中显示 */
  margin: 0 auto;
  /* 为头部内容容器添加左右内边距，使内容不紧贴容器边缘 */
  padding: 0 20px;
}

/* 头部内容布局样式 */
.header-content {
  /* 使用 Flexbox 布局，方便子元素的对齐和排列 */
  display: flex;
  /* 使子元素在交叉轴上垂直居中对齐 */
  align-items: center;
  /* 设置子元素之间的间距为 40px，增强布局的空间感 */
  gap: 40px;
  /* 固定头部内容区域的高度为 64px，与头部容器高度一致 */
  height: 64px;
}

/* Logo 区域样式 */
.title-bar {
  /* 使用 Flexbox 布局，让 Logo 和标题在同一行显示 */
  display: flex;
  /* 使 Logo 和标题在交叉轴上垂直居中对齐 */
  align-items: center;
  /* 设置 Logo 和标题之间的间距为 16px */
  gap: 16px;
  /* 设置 Logo 区域与菜单之间的右外边距为 40px */
  margin-right: 40px;
}

/* Logo 图片样式 */
.logo {
  /* 设置 Logo 图片的高度为 36px，宽度根据高度自适应 */
  height: 48px;
  width: 48px;
}

/* 标题文字样式 */
.title {
  /* 设置标题文字的字体大小为 1.2em */
  font-size: 1.2em;
  /* 设置标题文字的字体粗细为 500，使其稍微加粗 */
  font-weight: 500;
  /* 设置标题文字的颜色为深灰色 */
  color: #444;
}

/* 导航菜单样式 */
.nav-menu {
  /* 让导航菜单在可用空间中占据剩余的全部宽度 */
  flex: 1;
  /* 使用 Flexbox 布局，使菜单项在主轴上水平居中对齐 */
  display: flex;
  /* 使菜单项在主轴上水平居中对齐 */
  justify-content: center;
  height: 64px;
}

/* 用户信息区域样式 */
.user-info {
  /* 使用 Flexbox 布局，让头像和用户名在同一行显示 */
  display: flex;
  /* 使头像和用户名在交叉轴上垂直居中对齐 */
  align-items: center;
  /* 设置头像和用户名之间的间距为 8px */
  gap: 8px;
}

/* 用户头像样式 */
.avatar {
  /* 设置头像的高度和宽度均为 32px，保证头像为正方形 */
  height: 35px;
  width: 35px;
  /* 将头像设置为圆形 */
  border-radius: 50%;
}

/* 深度选择器，用于修改 Arco Design 菜单项目的样式 */
:deep(.arco-menu-item) {
  /* 固定菜单项的高度为 36px */
  height: 36px !important;
  /* 设置菜单项内文字的行高为 36px，使文字垂直居中 */
  line-height: 36px !important;
  /* 设置菜单项的左右内边距为 20px */
  padding: 0 15px !important;
  /* 设置菜单项文字的颜色为深灰色 */
  color: rgba(0, 0, 0, 0.8) !important;
}
</style>
