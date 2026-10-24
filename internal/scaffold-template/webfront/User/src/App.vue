<script setup>
// 用户端（User）应用根组件
// 教师、教研室主任、教学院长共用此前端，通过登录后角色权限展示不同页面
</script>

<template>
  <router-view />
</template>

<style>
html, body, #app {
  height: 100%;
  margin: 0;
  font-family: system-ui, -apple-system, "Microsoft YaHei", sans-serif;
}
</style>
