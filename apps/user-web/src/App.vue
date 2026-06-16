<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'

onMounted(() => {
  console.log('蓝血菁英平台 H5 已启动')
})
</script>

<style>
#app {
  width: 100%;
  max-width: var(--max-width);
  margin: 0 auto;
  min-height: 100vh;
  background-color: var(--bg-primary);
}
</style>