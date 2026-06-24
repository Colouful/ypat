<template>
  <view @touchmove.stop.prevent>
    <view
      class="tui-modal-box"
      :class="[(fadein || show)?'tui-modal-normal':'tui-modal-scale',show?'tui-modal-show':'']"
    >
      <view v-if="!custom">
        <view
          class="tui-modal-title"
          v-if="title"
        >{{title}}</view>
        <tui-icon
          name="close"
          v-if="closeIcon"
          :size="24"
          @tap="handleClickCancel"
          class="tui-modal-close"
          color="#B4B9B9"
        ></tui-icon>
        <view
          class="tui-modal-content"
          :class="[title?'':'tui-mtop']"
          :style="{color:color,fontSize:size+'rpx'}"
        >
          <!-- {{content}} -->
          <slot></slot>
        </view>
        <view class="tui-modalBtn-box">
          <block
            v-for="(item,index) in button"
            :key="index"
          >
            <button
              class="tui-modal-btn"
              :class="['tui-'+(item.type || 'primary')+(item.plain?'-outline':''),button.length!=2?'tui-btn-width':'',button.length>2?'tui-mbtm':'',shape=='circle'?'tui-circle-btn':'']"
              :hover-class="'tui-'+(item.plain?'outline':(item.type || 'primary'))+'-hover'"
              :data-index="index"
              @tap="handleClick"
            >{{item.text || "确定"}}</button>
          </block>
        </view>
      </view>
      <view v-else>
        <slot></slot>
      </view>
    </view>
    <view
      class="tui-modal-mask"
      :class="[show?'tui-mask-show':'']"
      @tap="handleClickCancel"
    ></view>

  </view>
</template>

<script>
export default {
  name: "tuiModal",
  props: {
    //是否显示
    show: {
      type: Boolean,
      default: false
    },
    //标题
    title: {
      type: String,
      default: ""
    },
    //内容
    content: {
      type: String,
      default: ""
    },
    //内容字体颜色
    color: {
      type: String,
      default: "#999"
    },
    //内容字体大小 rpx
    size: {
      type: Number,
      default: 28
    },
    //形状 circle, square
    shape: {
      type: String,
      default: 'square'
    },
    button: {
      type: Array,
      default: function () {
        return [{
          text: "取消",
          type: "red",
          plain: true //是否空心
        }, {
          text: "确定",
          type: "red",
          plain: false
        }]
      }
    },
    //点击遮罩 是否可关闭
    maskClosable: {
      type: Boolean,
      default: true
    },
    closeIcon: {
      type: Boolean,
      default: false
    },
    //自定义弹窗内容
    custom: {
      type: Boolean,
      default: false
    },
    //淡入效果，自定义弹框插入input输入框时传true
    fadein: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {

    };
  },
  methods: {
    handleClick(e) {
      if (!this.show) return;
      const dataset = e.currentTarget.dataset;
      this.$emit('click', {
        index: Number(dataset.index)
      });
    },
    handleClickCancel() {
      if (!this.maskClosable) return;
      this.$emit('cancel');
    }
  }
}
</script>

<style>
.tui-modal-close{
    position: absolute;
    right: 20upx;
    top: 20upx;
}
.tui-modal-box {
  position: fixed;
  width: 84%;
  left: 50%;
  top: 50%;
  margin: auto;
  background: #fff;
  z-index: 998;
  transition: all 0.3s ease-in-out;
  opacity: 0;
  border-radius: 8rpx;
  box-sizing: border-box;
  padding: 30rpx 44rpx;
  visibility: hidden;
}

.tui-modal-scale {
  transform: translate(-50%, -50%) scale(0);
}

.tui-modal-normal {
  transform: translate(-50%, -50%) scale(1);
}

.tui-modal-show {
  opacity: 1;
  visibility: visible;
}

.tui-modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 996;
  transition: all 0.3s ease-in-out;
  opacity: 0;
  visibility: hidden;
}

.tui-mask-show {
  visibility: visible;
  opacity: 1;
}

.tui-modal-title {
  text-align: center;
  font-size: 34rpx;
  color: #2c2d2d;
  font-weight: bold;
}

.tui-modal-content {
  text-align: center;
  color: #999;
  font-size: 28rpx;
  padding-top: 20rpx;
  padding-bottom: 60rpx;
}

.tui-mtop {
  margin-top: 30rpx;
}

.tui-mbtm {
  margin-bottom: 30rpx;
}

.tui-modalBtn-box {
  width: 100%;
  flex-direction: row;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tui-flex-column {
  flex-direction: column;
}

.tui-modal-btn {
  width: 46%;
  height: 68rpx;
  line-height: 68rpx;
  position: relative;
  border-radius: 10rpx;
  font-size: 24rpx;
  overflow: visible;
  margin-left: 0;
  margin-right: 0;
}

.tui-modal-btn::after {
  content: "";
  position: absolute;
  width: 200%;
  height: 200%;
  -webkit-transform-origin: 0 0;
  transform-origin: 0 0;
  -webkit-transform: scale(0.5, 0.5);
  transform: scale(0.5, 0.5);
  left: 0;
  top: 0;
  border-radius: 20rpx;
}

.tui-btn-width {
  width: 80% !important;
}

.tui-primary {
  background: #5677fc;
  color: #fff;
}

.tui-primary-hover {
  background: #4a67d6;
  color: #e5e5e5;
}

.tui-primary-outline {
  color: #5677fc;
  background: none;
}

.tui-primary-outline::after {
  border: 1px solid #5677fc;
}

.tui-danger {
  background: #ed3f14;
  color: #fff;
}

.tui-danger-hover {
  background: #d53912;
  color: #e5e5e5;
}

.tui-danger-outline {
  color: #ed3f14;
  background: none;
}

.tui-danger-outline::after {
  border: 1px solid #ed3f14;
}

.tui-red {
  background: #21c2c2;
  color: #fff;
}

.tui-red-hover {
  background: #21c2c2;
  opacity: 0.6;
  color: #e5e5e5;
}

.tui-red-outline {
  color: #21c2c2;
  background: none;
}

.tui-red-outline::after {
  border: 1px solid #21c2c2;
}

.tui-warning {
  background: #ff7900;
  color: #fff;
}

.tui-warning-hover {
  background: #e56d00;
  color: #e5e5e5;
}

.tui-warning-outline {
  color: #ff7900;
  background: none;
}

.tui-warning-outline::after {
  border: 1px solid #ff7900;
}

.tui-green {
  background: #19be6b;
  color: #fff;
}

.tui-green-hover {
  background: #16ab60;
  color: #e5e5e5;
}

.tui-green-outline {
  color: #19be6b;
  background: none;
}

.tui-green-outline::after {
  border: 1px solid #19be6b;
}

.tui-white {
  background: #fff;
  color: #2c2d2d;
}

.tui-white-hover {
  background: #f7f7f9;
  color: #666;
}

.tui-white-outline {
  color: #2c2d2d;
  background: none;
}

.tui-white-outline::after {
  border: 1px solid #2c2d2d;
}

.tui-gray {
  background: #ededed;
  color: #999;
}

.tui-gray-hover {
  background: #d5d5d5;
  color: #898989;
}

.tui-gray-outline {
  color: #999;
  background: none;
}

.tui-gray-outline::after {
  border: 1px solid #999;
}

.tui-outline-hover {
  opacity: 0.6;
}

.tui-circle-btn {
  border-radius: 40rpx !important;
}

.tui-circle-btn::after {
  border-radius: 80rpx !important;
}
</style>
