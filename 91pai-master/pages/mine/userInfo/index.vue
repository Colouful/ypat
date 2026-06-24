<!--
 * @Author: shawn
 * @LastEditTime: 2020-05-07 22:54:33
 -->
<template>
  <view class="tui-userinfo-box fadeIn">
    <form>
      <tui-list-cell
        @tap="fSelect"
        padding="0"
        :arrow="true"
      >
        <view class="tui-list-cell">
          <view>头像</view>
          <cropper
            ref="myUpload"
            selWidth="660rpx"
            selHeight="660rpx"
            @upload="myUpload"
            :avatarSrc="imgpath || defaultValImg"
            class="tui-avatar"
            avatarStyle="width:70rpx;height:70rpx;"
          >
          </cropper>
        </view>
      </tui-list-cell>
      <tui-list-cell
        padding="0"
        :arrow="true"
      >
        <view class="tui-list-cell">
          <view>昵称</view>
          <input
            placeholder-style="color:#b4b9b9;font-weight:normal;font-size:24upx"
            class="tui-input"
            name="name"
            v-model="userInfo.nickname"
            placeholder="请输入昵称(必填)"
            maxlength="50"
            type="text"
          />
          <!-- <view class="tui-content">echo.</view> -->
        </view>
      </tui-list-cell>
      <view class="cell-title">
        职业信息
      </view>
      <block
        v-for="(item,index) in tabList"
        :key="index"
      >
        <tui-list-cell
          padding="0"
          :arrow="true"
          @tap="toggleTab(item,index)"
        >
          <view class="tui-list-cell">
            <view>{{item.name}}</view>
            <view
              v-if="resultInfo[item.key]"
              class="tui-content-active"
            >{{resultInfo[item.key]}}</view>
            <view
              v-else
              class="tui-content"
            >请选择(必填)</view>
          </view>
        </tui-list-cell>
      </block>
      <view class="cell-title">
        联系方式
      </view>
      <tui-list-cell
        padding="0"
        :arrow="false"
      >
        <view class="tui-list-cell">
          <view>手机号</view>
          <input
            disabled
            v-model="userInfo.mobile"
            placeholder-style="color:#b4b9b9;font-weight:normal;font-size:24upx"
            class="tui-input"
            name="mobile"
            placeholder="请输入手机号"
            maxlength="11"
            type="text"
          />
          <!-- <view class="tui-content">echo.</view> -->
        </view>
      </tui-list-cell>
      <tui-list-cell
        padding="0"
        :arrow="true"
      >
        <view class="tui-list-cell">
          <view>微信号</view>
          <input
            v-model="userInfo.wx"
            placeholder-style="color:#b4b9b9;font-weight:normal;font-size:24upx"
            class="tui-input"
            name="name"
            placeholder="请输入微信号(必填)"
            maxlength="50"
            type="text"
          />
          <!-- <view class="tui-content">echo.</view> -->
        </view>
      </tui-list-cell>
      <tui-list-cell
        padding="0"
        :arrow="true"
      >
        <view class="tui-list-cell">
          <view>QQ号</view>
          <input
            v-model="userInfo.qq"
            placeholder-style="color:#b4b9b9;font-weight:normal;font-size:24upx"
            class="tui-input"
            name="name"
            placeholder="请输入QQ号(选填)"
            maxlength="50"
            type="text"
          />
          <!-- <view class="tui-content">echo.</view> -->
        </view>
      </tui-list-cell>
      <tui-list-cell
        padding="0"
        :arrow="true"
      >
        <view class="tui-list-cell">
          <view>微博号</view>
          <input
            v-model="userInfo.wb"
            placeholder-style="color:#b4b9b9;font-weight:normal;font-size:24upx"
            class="tui-input"
            name="name"
            placeholder="请输入微博号(选填)"
            maxlength="50"
            type="text"
          />
          <!-- <view class="tui-content">echo.</view> -->
        </view>
      </tui-list-cell>
      <view @tap="formSubmit">
        <uni-button
          class="btn-primary-fixed"
          hover-class="btn-hover"
        >保存</uni-button>
      </view>
    </form>
    <!--
        约拍对象
        -->
    <w-picker
      mode="selector"
      :value="professDefaultValue"
      v-if="show"
      themeColor="#1BB6B6"
      defaultType="label"
      :default-props="defaultProps"
      :options="professList"
      @confirm="onConfirm"
      @cancel="onCancel"
      ref="profess"
    ></w-picker>
    <w-picker
      mode="selector"
      :value="genderDefaultValue"
      v-if="show"
      themeColor="#1BB6B6"
      defaultType="label"
      :default-props="defaultProps"
      :options="genderList"
      @confirm="onConfirm"
      @cancel="onCancel"
      ref="gender"
    ></w-picker>
    <!-- 约拍对象 -->
    <!-- 约拍时间 -->
    <w-picker
      mode="date"
      endYear="2040"
      :value="defaultValTime"
      :current="false"
      fields="day"
      @confirm="onConfirm"
      @cancel="onCancel"
      themeColor="#1BB6B6"
      disabled-after
      ref="birthday"
    ></w-picker>
    <!-- 约拍时间 -->
    <w-picker
      mode="region"
      :value="defaultRegion"
      default-type="value"
      :hide-area="false"
      @confirm="onConfirm($event,'region')"
      @cancel="onCancel"
      ref="area"
    ></w-picker>
  </view>
</template>

<script src="./index.js">

</script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
