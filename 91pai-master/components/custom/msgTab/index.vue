<!--
 * @Author: shawn
 * @LastEditTime: 2019-11-07 19:52:39
 -->
<template>
  <view>
    <View
      v-for="(item,index) in tabList"
      :key="index"
    >{{item.name}}</View>
  </view>
</template>

<script src="./index.js">

</script>


<style lang="scss" scoped>
@import "./index.scss";
</style>
