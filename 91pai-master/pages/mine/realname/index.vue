<!--
 * @Author: shawn
 * @LastEditTime: 2020-04-10 09:39:05
 -->
<template>
  <view class="container fadeIn">
    <view class="box">
      <view class='tui-notice-board'>
        <view class="tui-icon-bg">
          <tui-icon
            name="news-fill"
            :size='24'
            color='#FFCFA0'
          ></tui-icon>
        </view>
        <view class="tui-scorll-view">
          <view
            class="tui-notice"
            :class="[animation?'tui-animation':'']"
          >请安心上传您本人身份信息及证件，爱去拍平台将重视和保护您个人隐私</view>
        </view>
      </view>

    </view>
    <view class="item-boxs ">
      <view class="title">请上传您的身份证正反面照片</view>
      <view class="item-box">
        <view class="item-container">
          <view class="item">
            <block
              v-for="(image,index) in imageList"
              :key="index"
            >
              <view
                v-if="index===0 &&imageList[0]"
                class="uni-uploade"
                style="position: relative;"
              >
                <image
                  class="item-img"
                  mode="aspectFill"
                  :src="image"
                  :data-src="image"
                  @tap="previewImage"
                ></image>
                <view
                  class="close-view"
                  @tap="close(index)"
                >×</view>
              </view>
            </block>
            <image
              v-if="!imageList||!imageList[0]"
              mode="widthFix"
              @tap="chooseImage('image1')"
              class="item-img"
              src="/static/images/mine/zm@2x.png"
            ></image>
          </view>
          <view class="desc">请上传身份证<text class="desc-text">正面照</text></view>
        </view>
        <view class="item-container">
          <view class="item">
            <block
              v-for="(image,index) in imageList"
              :key="index"
            >
              <view
                v-if="index===1&&imageList[1]"
                class="uni-uploade"
                style="position: relative;"
              >
                <image
                  class="item-img"
                  mode="aspectFill"
                  :src="image"
                  :data-src="image"
                  @tap="previewImage"
                ></image>
                <view
                  class="close-view"
                  @tap="close(index)"
                >×</view>
              </view>
            </block>
            <image
              v-if="!imageList||!imageList[1]"
              mode="widthFix"
              @tap="chooseImage('image2')"
              class="item-img"
              src="/static/images/mine/fm@2x.png"
            ></image>
          </view>
          <view class="desc">请上传身份证<text class="desc-text">反面照</text></view>
        </view>
      </view>
    </view>
    <view class="mtb20">
      <tui-list-cell
        padding="0"
        :arrow="true"
      >
        <view class="tui-list-cell">
          <view>姓名</view>
          <input
            placeholder-style="color:#b4b9b9;font-weight:normal;font-size:24upx"
            class="tui-input"
            name="name"
            placeholder="请输入"
            maxlength="20"
            v-model="name"
            type="text"
          />
        </view>
      </tui-list-cell>
      <tui-list-cell
        padding="0"
        :bottomLine="false"
        :arrow="true"
      >
        <view class="tui-list-cell">
          <view>身份证号码</view>
          <input
            placeholder-style="color:#b4b9b9;font-weight:normal;font-size:24upx"
            class="tui-input"
            v-model="certcode"
            name="name"
            maxlength="18"
            placeholder="请输入"
            type="text"
          />
        </view>
      </tui-list-cell>
    </view>
    <view class="item-boxs">
      <view class="title">请上传您手持身份证照片</view>
      <view class="item-box">
        <view class="item-container">
          <view class="item sc">
            <image
              mode="widthFill"
              class="item-img header"
              src="/static/images/mine/sc@2x.png"
            ></image>
          </view>
          <view class="desc">手持身份证件示例图</view>
        </view>
        <view class="item-container">
          <view class="item">
            <block
              v-for="(image,index) in imageList"
              :key="index"
            >
              <view
                v-if="index===2&&imageList[2]"
                class="uni-uploade"
                style="position: relative;"
              >
                <image
                  class="item-img"
                  mode="aspectFill"
                  :src="image"
                  :data-src="image"
                  @tap="previewImage"
                ></image>
                <view
                  class="close-view"
                  @tap="close(index)"
                >×</view>
              </view>
            </block>
            <view
              class="item-sc"
              v-if="!imageList||!imageList[2]"
              @tap="chooseImage('image3')"
            >
              <image
                mode="widthFix"
                class="plus"
                src="/static/images/mine/plus@2x.png"
              ></image>
              <view class="desc">请上传身份证<text class="desc-text">手持照片</text></view>
            </view>
          </view>
        </view>
      </view>
    </view>
    <view class="warntext">* 填写时请确保信息真实有效，否则将会影响您的正常使用</view>
    <view class="tui-safearea-bottom"></view>
    <view @tap="showModal">
      <uni-button
        class="btn-primary-fixed"
        hover-class="btn-hover"
        formType="submit"
        type="primary"
      >提交</uni-button>
    </view>

    <tui-modal
      :show="modal"
      class="modal-box"
      :custom="true"
    >
      <view>
        <view class="content-box">
          <grid-four title="实名认证"></grid-four>
          <view class="bottom-text">
            * 实名信息需进行人工审核，将会收取<text class="money">29</text>元信息审核费用，是否同意？ 您的支持是我们最大的动力！
          </view>
          <view class="btn-container">

            <view
              @tap="hideModal"
              :plain="true"
               hover-class="opacity"
              class="cancle-btn"
            >取消</view>
            <view
              @tap="order_create"
               hover-class="opacity"
              class="confirm-btn"
            >确定</view>
          </view>

          <view class="bottom-warn">
            (若信息审核失败，重新提交无需再支付审核费用)
          </view>
        </view>
      </view>
    </tui-modal>
    <qrcode-alert :visiable="qrcodeAlertShow" @callback="qrcodeAlertShow=false" title="请到微信小程序实名认证"></qrcode-alert>
    <!--加载框 start-->
    <tui-loading :visible="visible"></tui-loading>
    <!--加载框 end-->
  </view>
</template>

<script src="./index.js">

</script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
