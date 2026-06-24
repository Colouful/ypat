/*
 * @Author: shawn
 * @LastEditTime: 2020-03-26 10:42:26
 */
import headerBox from "@/components/custom/headerBox/index.vue";
import { my_ypat_rec_add, user_upd, tmplid_list } from "@/common/vmeitime-http";
import confirmAlert from "@/components/custom/confirmAlert/index.vue";

import { getUserInfo, sendMsg } from "@/common/utils";
export default {
  components: {
    headerBox,
    confirmAlert
  },
  data() {
    return {
      content: "",
      userInfo: {},
      wx: "",
      ypatid: "",
      visible: false,
      visiableAlert: false,
      button: [],
      pathName: "",
      tmplidList: []
    };
  },
  onLoad(options) {
    this.ypatid = options.ypatid;
  },
  onShow() {
    //  页面公用方法
    this.tui.commonFunc();
    //  页面公用方法 end
    this.getUserInfo();
  },
  methods: {
    cancle() {
      this.visiableAlert = false;
    },
    sure() {
      this.visiableAlert = false;
      if (this.pathName === "/pages/home/success/index?status=98") {
        this.publish_data();
        return;
      }
      uni.navigateTo({ url: this.pathName });
    },
    async getUserInfo() {
      let res = await getUserInfo();
      this.userInfo = res;
    },
    async tmplid_list() {
      let res = await tmplid_list();
      if (res && res.code === 200) {
        this.tmplidList = res.res;
      }
    },

    async my_ypat_rec_add() {
      if (!this.tmplidList || this.tmplidList.length === 0) {
        this.tmplid_list();
      }
      if (this.userInfo && this.userInfo.ppd < 3) {
        this.visiableAlert = true;
        this.pathName = "/pages/mine/ppd/index";
        this.button = [
          {
            text: "取消",
            type: "red",
            plain: true //是否空心
          },
          {
            text: "去获取",
            type: "red",
            plain: false
          }
        ];
        return;
      }
      if (!this.content || this.content.length < 6) {
        this.tui.toast("约拍理由不少于六个字");
        return;
      }
      if (!this.userInfo || (!this.userInfo.wx && !this.wx)) {
        this.tui.toast("请输入您的微信号");
        return;
      }
      this.visiableAlert = true;
      this.pathName = "/pages/home/success/index?status=98";
      this.button = [
        {
          text: "取消",
          type: "red",
          plain: true //是否空心
        },
        {
          text: "确定申请",
          type: "red",
          plain: false
        }
      ];
      return;
    },
    async publish_data() {
      this.visible = true;
      let param = {
        content: this.content,
        ypatid: this.ypatid
      };
      let param2 = {
        gender: this.userInfo.gender,
        nickName: this.userInfo.nickname,
        profess: this.userInfo.profess,
        wx: this.wx || this.userInfo.wx,
        qq: this.userInfo.qq,
        wb: this.userInfo.wb,
        pics: this.userInfo.imgpath,
        province: this.userInfo.province,
        city: this.userInfo.city,
        area: this.userInfo.area
      };
      let message = await sendMsg(this.tmplidList, [0, 1, 2]).catch(err => {
        if (err) {
          this.tui.toast(err);
        }
      });
      if (message) {
        let res2 = await user_upd(param2).catch(() => {
          this.visible = false;
        });
        if (res2 && res2.code === 200) {
          let res = await my_ypat_rec_add(param).catch(() => {
            this.visible = false;
          });
          if (res && res.code === 200) {
            this.visible = false;
            uni.redirectTo({ url: this.pathName });
          } else {
            this.visible = false;
            this.tui.toast(res.msg);
          }
        } else {
          this.visible = false;
        }
      } else {
        this.visible = false;
      }
    }
  }
};
