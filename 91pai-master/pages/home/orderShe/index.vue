<!--
 * @Author: shawn
 * @LastEditTime: 2020-03-29 17:36:14
 -->
<!--
 * @Author: shawn
 * @LastEditTime : 2019-12-19 01:36:03
 -->
<template>
  <view class="page-container">
    <view class="upload-box">
      <form>
        <view class="uni-textarea">
          <textarea
            placeholder-class="uni-textarea-placeholder"
            placeholder="请输入约拍理由，有利于提高您的约拍成功率哦～"
            v-model="content"
            maxlength="100"
          />
          <view class="uni-list list-pd">
				<view class="uni-list-cell cell-pd">
					<view class="uni-uploader">
						<view class="uni-uploader-head header">
							<view class="uni-uploader-title"></view>
							<view class="uni-uploader-info">{{content.length}}/100字</view>
						</view>
					</view>
				</view>
			</view>
          </view>
		</form>
	</view>
    <view class="contact-box">
        <view class="contact-title">联系方式</view>
         <view class="cell-box">
             <view class="cell-item">
                 <view class="cell-label">
                     手机号
                     <text class="cell-value">{{userInfo.mobile}}</text>
                 </view>
             </view>
              <view class="cell-item">
                 <view class="cell-label" >
                     微信号
                      <text  v-if="userInfo.wx" class="cell-value">{{userInfo.wx}}</text>
                     <input v-model="wx" v-else placeholder-style="color:#b4b9b9;font-weight:normal;font-size:24upx" class="tui-input" name="name" placeholder="请输入您的微信号" maxlength="50" type="text" />
                 </view>
             </view>
              
         </view>
    </view>
    <view @tap="my_ypat_rec_add">
			<uni-button :class="['btn-primary-fixed',((userInfo.wx||wx)&&content)?'':'disactive']" hover-class="btn-hover"  >提交约拍</uni-button>
            </view>
<!--加载框 start-->
    <tui-loading :visible="visible"></tui-loading>
    <!--加载框 end-->
    
        <confirm-alert
      @sure="sure"
      @cancle="cancle"
      :visiable="visiableAlert"
      :button="button"
    >
       <view v-if="pathName==='/pages/mine/ppd/index'">
        申请约拍将会使用<text class="activeText">3</text>个拍拍豆，您当前剩余拍拍豆暂不充足，赶紧前去获取吧
      </view>
       <view v-if="pathName==='/pages/home/success/index?status=98'">
        申请约拍将会使用<text class="activeText">3</text>个拍拍豆，是否确定使用拍拍豆？
      </view>
    </confirm-alert>
 </view>
</template>

<script src="./index.js">

</script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
