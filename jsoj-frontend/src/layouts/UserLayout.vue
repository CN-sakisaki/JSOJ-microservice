<template>
  <div id="userLayout">
    <a-layout style="min-height: 100vh; position: relative">
      <!-- 背景视频 -->
      <video
        autoplay
        loop
        muted
        class="background-video"
        style="
          width: 100%;
          height: 100%;
          object-fit: cover;
          position: absolute;
          top: 0;
          left: 0;
          z-index: -1;
        "
      >
        <source
          src="https://gw.alipayobjects.com/v/huamei_gcee1x/afts/video/jXRBRK_VAwoAAAAAAAAAAAAAK4eUAQBr"
          type="video/mp4"
        />
        你的浏览器不支持视频播放。
      </video>
      <!--头部区域-->
      <a-layout-header class="header"></a-layout-header>
      <!--内容展示区-->
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
      <!--底部footer区域-->
      <a-layout-footer class="footer">
        <a href="https://github.com/TIG-prey" target="_blank">
          程序员 JianShang 出品
        </a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>
<script>
import { defineComponent } from "vue";

export default defineComponent({
  components: {},
});
</script>

<style scoped>
#userLayout {
  text-align: center;
}

#userLayout .header {
  margin-top: 16px;
}

#userLayout .content {
  margin-bottom: 16px;
  padding: 20px;
  min-height: 50vh;
  position: relative;
  z-index: 1;
}

#userLayout .footer {
  background: #efefef;
  padding: 16px;
  position: sticky;
  bottom: 0;
  left: 0;
  right: 0;
  text-align: center;
  position: relative;
  z-index: 1;
}

.background-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  position: absolute;
  top: 0;
  left: 0;
  z-index: -1;
}
</style>
