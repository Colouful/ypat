<!--
 * @Author: shawn
 * @LastEditTime : 2020-01-05 13:31:20
 -->
<template>
  <view class="fadeIn container">
    <view class="title">
      <view>
        常见问题
      </view>
      <badge
        direction="bottom-left"
        class="dot-msg"
        type="danger"
        size="small"
      >Hot</badge>
    </view>
    <view>
      <block
        v-for="(item,index) in dataList"
        :key="index"
      >
        <tui-collapse
          :index="index"
          :current="current"
          @click="change3"
        >
          <template v-slot:title>
            <tui-list-cell class="tui-title">{{item.title}}</tui-list-cell>
          </template>
          <template v-slot:content>
            <view class="tui-content">
              <view
                class="item-cell"
                v-for="(item2,index2) in item.desc"
                :key="index2"
              >{{item2}}</view>
            </view>
          </template>
        </tui-collapse>
      </block>
    </view>
  </view>
</template>

<script src="./index.js">

</script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
