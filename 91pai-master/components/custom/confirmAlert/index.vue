<!--
 * @Author: shawn
 * @LastEditTime : 2019-12-18 22:41:53
 -->
<!--
 * @Author: shawn
 * @LastEditTime : 2019-12-18 22:37:15
 -->
<!--
 * @Author: shawn
 * @LastEditTime : 2019-12-18 22:36:34
 -->
<template>
  <tui-modal
    :title="title"
    :show="visiable"
    @click="sure"
    :button="button"
  >
    <slot></slot>
  </tui-modal>
</template>

<script src="./index.js">

</script>


<style lang="scss" scoped>
@import "./index.scss";
</style>
