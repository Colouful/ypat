<!--
 * @Author: shawn
 * @LastEditTime: 2020-04-15 15:27:07
 -->
<template>
  <view class="page-container animation-fade">
    <view class="upload-box">
      <form>
        <view class="uni-textarea">
          <!-- #ifdef MP-WEIXIN -->
          <textarea
            placeholder="请输入约拍描述...(出于信息安全考虑，请勿预留联系方式等个人信息，可在个人中心进行添加)"
            v-model="describe"
            maxlength="200"
          />
          <!-- #endif -->
          <!-- #ifdef MP-BAIDU -->
          <textarea
            placeholder="请输入约拍描述...(出于信息安全考虑，请勿预留联系方式等个人信息，可在个人中心进行添加)"
            @input="getdescribe"
            maxlength="200"
          />
          <!-- #endif -->
        </view>
        <view class="uni-list list-pd">
          <view class="uni-list-cell cell-pd">
            <view class="uni-uploader">
              <view class="uni-uploader-head">
                <view class="uni-uploader-title"></view>
                <view class="uni-uploader-info">{{describe&&describe.length || 0}}/200字</view>
              </view>
              <view class="uni-uploader-body">
                <view class="uni-uploader__files">
                  <block v-for="(image,index) in imageList" :key="index">
                    <view class="uni-uploader__file" style="position: relative;">
                      <image
                        class="uni-uploader__img"
                        mode="aspectFill"
                        :src="image"
                        :data-src="image"
                        @tap="previewImage"
                      />
                      <view class="close-view" @tap="close(index)">×</view>
                    </view>
                  </block>
                  <view class="uni-uploader__input-box" v-show="imageList.length < 9">
                    <view class="uni-uploader__input" @tap="chooseImage"></view>
                  </view>
                </view>
                <view class="upload-desc">请上传想要拍摄的风格</view>
              </view>
            </view>
          </view>
        </view>
      </form>
    </view>
    <view class="cell-box">
      <tui-list-cell padding="0" :hover="false">
        <view class="tui-list-cell" style="padding-right:30upx">
          <view>约拍对象</view>
          <view class="item-btn-box">
            <view
              @tap="selectGender('约摄影师')"
              class="item-btn"
              :class="{'active':resultInfo.target==='约摄影师'}"
            >
              <text>约摄影师</text>
            </view>
            <view
              @tap="selectGender('约模特')"
              class="item-btn"
              :class="{'active':resultInfo.target==='约模特'}"
            >
              <text>约模特</text>
            </view>
          </view>
        </view>
      </tui-list-cell>
      <picker
        :mode="item.mode"
        :key="item.key"
        v-for="(item) in tabList"
        :data-list="item.list"
        :range="item.list"
        @change="bindPickerChange"
        :value="resultInfo[item.key] || []"
        :data-key="item.key"
      >
        <tui-list-cell padding="0" :arrow="true">
          <view class="tui-list-cell">
            <view>{{item.name}}</view>
            <view
              v-if="item.key==='address'?resultInfo2[item.key]:resultInfo[item.key]"
              class="tui-content-active"
            >{{item.key==='address'?resultInfo2[item.key]:resultInfo[item.key]}}</view>
            <view v-else class="tui-content">请选择</view>
          </view>
        </tui-list-cell>
      </picker>
      <tui-list-cell
        v-if="resultInfo.chargeway==='我要收费'||resultInfo.chargeway==='可付费'"
        padding="0"
        :arrow="true"
      >
        <view class="tui-list-cell">
          <view>收费金额</view>
          <input
            placeholder-style="color:#b4b9b9;font-weight:normal;font-size:24upx"
            v-model="resultInfo.chargeamt"
            class="tui-input"
            name="name"
            placeholder="请输入收费金额"
            maxlength="8"
            type="digit"
          />
          <view class="dw">元</view>
        </view>
      </tui-list-cell>
    </view>
    <view class="cell-box">
      <tui-collapse :index="0" :current="current" :arrow="false" @click="change">
        <template v-slot:title>
          <tui-list-cell padding="0">
            <view :class="['tui-list-cell','no-right',current===0?'active':'']">
              <view>更多选项</view>
              <image mode="widthFix" class="tui-content-more" src="/static/images/home/more.png" />
            </view>
          </tui-list-cell>
        </template>
        <template v-slot:content>
          <tui-list-cell padding="0" :arrow="true">
            <view class="tui-list-cell">
              <view>约拍成片</view>
              <input
                v-model="resultInfo.patslice"
                placeholder-style="color:#b4b9b9;font-weight:normal;font-size:24upx"
                class="tui-input"
                name="name"
                placeholder="如原片100张，精修15张（选填）"
                maxlength="50"
                type="text"
              />
            </view>
          </tui-list-cell>
          <tui-list-cell padding="0" :arrow="true">
            <view class="tui-list-cell">
              <view>约拍地点</view>
              <input
                v-model="resultInfo.patarea"
                placeholder-style="color:#b4b9b9;font-weight:normal;font-size:24upx"
                class="tui-input"
                name="name"
                placeholder="请输入"
                maxlength="100"
                type="text"
              />
            </view>
          </tui-list-cell>
          <picker
            :mode="item.mode"
            :key="item.key"
            v-for="(item) in tabList2"
            :data-list="item.list"
            :range="item.list"
            @change="bindPickerChange"
            :value="resultInfo[item.key] || []"
            :data-key="item.key"
          >
            <tui-list-cell padding="0" :arrow="true">
              <view class="tui-list-cell">
                <view>{{item.name}}</view>
                <view
                  v-if="resultInfo[item.key]"
                  class="tui-content-active"
                >{{resultInfo[item.key]}}</view>
                <view v-else class="tui-content">请选择</view>
              </view>
            </tui-list-cell>
          </picker>
          <view class="tag-box">
            <view class="title">
              拍摄风格
              <text class="subtitle">（个人喜好擅长）</text>
            </view>
            <view class="tag-list">
              <view
                @tap="checkItem(item,index)"
                :class="[item.check?'item active':'item']"
                v-for="(item,index) in patstyle"
                :key="item.key"
              >{{item.name}}</view>
            </view>
          </view>
        </template>
      </tui-collapse>
    </view>
    <view @tap="publish">
      <uni-button
        class="btn-primary-fixed"
        hover-class="btn-hover"
        formType="submit"
        type="primary"
      >发布约拍</uni-button>
    </view>
    <!--加载框 start-->
    <tui-loading :visible="visible"></tui-loading>
    <!--加载框 end-->
    <confirm-alert
      @sure="sure"
      :title="pathName==='isNeedRealName'?'实名安全信息保障':'提示'"
      @cancle="cancle"
      :visiable="visiableAlert"
      :button="button"
    >
      <view v-if="pathName==='/pages/mine/ppd/index'">
        发布约拍将会使用
        <text class="activeText">3</text>个拍拍豆，您当前剩余拍拍豆暂不充足，赶紧前去获取吧
      </view>
      <view v-if="pathName==='isNeedRealName'" class="text-left">为确保双方约拍信息服务安全及信誉保障，请再次确认是否要求对方实名认证？</view>
      <view v-if="pathName==='/pages/home/success/index?status=99'">
        发布约拍将会使用
        <text class="activeText">3</text>个拍拍豆，是否确定使用拍拍豆？
      </view>
    </confirm-alert>
  </view>
</template>

<script src="./index.js">
</script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
