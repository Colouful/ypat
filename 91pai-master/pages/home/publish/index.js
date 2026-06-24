/*
 * @Author: shawn
 * @LastEditTime: 2020-04-15 15:06:59
 */
import tuiListCell from "@/components/thor-ui/list-cell/list-cell";
import tuiCollapse from "@/components/thor-ui/tui-collapse/tui-collapse";
// import WPicker from "@/components/custom/picker/picker.vue";
import { authorize, getLocation, getUserInfo, sendMsg } from "@/common/utils";
import { ypat_submit, tmplid_list, param_list } from "@/common/vmeitime-http";
import { patstyleMap, chargewayMap, targetMap } from "@/common/dataMap";
import confirmAlert from "@/components/custom/confirmAlert/index.vue";
import { isNendUserInfo } from "@/common/getNextUrl";
import { getCity } from "@/common/getCity";
import localStorageObj from "@/common/localStorage";

var sourceType = [["camera"], ["album"], ["camera", "album"]];
var sizeType = [["compressed"], ["original"], ["compressed", "original"]];
export default {
  components: {
    tuiListCell,
    tuiCollapse,
    // WPicker,
    confirmAlert,
  },
  data() {
    return {
      visible: false,
      unload: false,
      defaultValTime: "",
      resultInfoArray: {},
      describe: "",
      patstyle: [],
      index: 0,
      tabList2: [
        {
          mode: "selector",
          key: "credit",
          name: "是否要求对方信用担保",
          list: ["不需要", "需要"],
        },
      ],
      tabList: [
        {
          mode: "date",
          key: "patdate",
          name: "约拍时间",
        },
        {
          mode: "region",
          key: "address",
          name: "约拍地区",
        },
        {
          mode: "selector",
          key: "chargeway",
          name: "收费方式",
          list: chargewayMap,
        },
      ],
      chargeList: [],
      realnameList: [
        {
          label: "不需要",
          value: 0,
        },
        {
          label: "需要",
          value: 1,
        },
      ],

      input_content: "",
      imageList: [],
      sourceTypeIndex: 2,
      sourceType: ["拍照", "相册", "拍照或相册"],
      sizeTypeIndex: 1,
      sizeType: ["压缩", "原图", "压缩或原图"],
      countIndex: 8,
      count: [1, 2, 3, 4, 5, 6, 7, 8, 9],
      //侧滑返回start
      startX: 0, //点击屏幕起始位置
      movedX: 0, //横向移动的距离
      endX: 0, //接触屏幕后移开时的位置
      //end
      current: -1,
      resultInfo: {
        target: "约摄影师",
      },
      visiableAlert: false,
      button: [],
      pathName: "",
      param: {},
      userInfo: {},
      getPatstyleArrayStr: "",
      resultInfo2: {},
      tmplidList: [],
      showRealName: {},
    };
  },
  created() {
    this.getDefaultValTime();
    this.getPatstyle();
    this.getChargeList();
  },
  onShow() {
    //  页面公用方法
    this.tui.commonFunc();
    //  页面公用方法 end
    this.getUserInfo();
  },
  async mounted() {
    let locationArray = "-1,-1";
    // #ifdef MP
    locationArray = await authorize(
      "scope.userLocation",
      () => getLocation(true),
      "请开启定位权限"
    );
    // #endif
    let address = await getCity(locationArray, "area");
    this.resultInfo2.province = address[0];
    this.resultInfo2.city = address[1];
    this.resultInfo2.area = address[2];
    this.$set(
      this.resultInfo2,
      "address",
      address[0] + address[1] + address[2]
    );
    // this.$set(this.resultInfo, "address", address[0] + address[1] + address[2]);
  },
  onLoad() {
    this.param_list();
    const _uni = uni;
    const _this = this;
    const publishData = _uni.getStorageSync(localStorageObj.publishData);
    const publishDataAddress = uni.getStorageSync(
      localStorageObj.publishDataAddress
    );

    if (
      publishData &&
      ((publishData.imageList && publishData.imageList.length > 0) ||
        publishData.describe ||
        publishData.getPatstyleArrayStr ||
        JSON.stringify(publishData.resultInfo) !== "{}" ||
        publishDataAddress)
    ) {
      _uni.showModal({
        title: "提示",
        content: "是否继续上次编辑?",
        confirmText: "继续",
        cancelText: "取消",
        confirmColor: "#1BB6B6",
        cancelColor: "#B4B9B9",
        success: function (res) {
          if (res.confirm) {
            _this.resultInfo = publishData.resultInfo;
            if (publishDataAddress && publishDataAddress) {
              _this.$set(_this.resultInfo2, "address", publishDataAddress);
            }
            _this.describe = publishData.describe;
            _this.imageList = publishData.imageList;
            _this.patstyle = publishData.patstyle;
            _this.resultInfoArray = publishData.resultInfoArray;
          } else if (res.cancel) {
            _uni.removeStorageSync(localStorageObj.publishDataAddress);
            _uni.removeStorageSync(localStorageObj.publishData);
          }
        },
      });
    }
  },
  onUnload() {
    if (
      (!this.unload && this.imageList && this.imageList.length > 0) ||
      this.describe ||
      this.getPatstyleStr() ||
      this.resultInfo
    ) {
      uni.setStorageSync(
        localStorageObj.publishDataAddress,
        this.resultInfo2.address
      );
      uni.setStorageSync(localStorageObj.publishData, {
        resultInfo: this.resultInfo,
        describe: this.describe,
        imageList: this.imageList,
        patstyle: this.patstyle,
        getPatstyleStr: this.getPatstyleStr(),
        resultInfoArray: this.resultInfoArray,
      });
    }
    if (this.unload) {
      uni.removeStorageSync(localStorageObj.publishData);
    }
    this.unload = false;
    this.imageList = [];
    this.sourceTypeIndex = 2;
    this.sourceType = ["拍照", "相册", "拍照或相册"];
    this.sizeTypeIndex = 1;
    this.sizeType = ["压缩", "原图", "压缩或原图"];
    this.countIndex = 8;
  },

  methods: {
    selectGender(type) {
      this.resultInfo.target = type;
    },
    async param_list() {
      let res = await param_list();
      if (res && res.code === 200) {
        this.showRealName = res.res;
        if (this.showRealName.realname === "1") {
          this.tabList2.unshift({
            mode: "selector",
            key: "realname",
            name: "是否要求对方实名",
            list: ["不需要", "需要"],
          });
        }
      }
    },
    bindPickerChange: function (e) {
      const { key, list } = e.currentTarget.dataset;
      console.log(e);
      if (key === "address") {
        this.resultInfo2.province = e.target.value[0];
        this.resultInfo2.city = e.target.value[1];
        this.resultInfo2.area = e.target.value[2];
        this.$set(
          this.resultInfo2,
          [key],
          e.target.value[0] + e.target.value[1] + e.target.value[2]
        );
      } else {
        this.$set(
          this.resultInfo,
          key,
          list ? list[e.target.value] : e.target.value
        );
      }
    },
    async getUserInfo() {
      let res = await getUserInfo();
      this.userInfo = res;
    },
    getPatstyle() {
      let patstyle = [];
      for (let index = 0; index < patstyleMap.length; index++) {
        const element = patstyleMap[index];
        patstyle.push({ name: element, key: index });
      }
      this.patstyle = patstyle;
    },
    getChargeList() {
      let chargeList = [];
      for (let index = 0; index < chargewayMap.length; index++) {
        const element = chargewayMap[index];
        chargeList.push({ label: element, value: index });
      }
      this.chargeList = chargeList;
    },
    getDefaultValTime() {
      let date = new Date();
      let dateYear = date.getFullYear(); //获取年
      let dateMonth =
        date.getMonth() + 1 > 9
          ? date.getMonth() + 1
          : "0" + (date.getMonth() + 1); //获取月
      let dateDate = date.getDate() > 9 ? date.getDate() : "0" + date.getDate(); //获取当日
      this.defaultValTime = dateYear + "-" + dateMonth + "-" + dateDate;
      console.log(this.defaultValTime);
    },
    checkItem(item, index) {
      this.patstyle.forEach((element, index2) => {
        if (index2 === index) {
          element.check = !item.check;
        }
      });
      this.patstyle = [...this.patstyle];
    },
    toggleTab(item) {
      this.key = item.key;
      const value =
        this.resultInfoArray && this.resultInfoArray[item.key || item.mode];
      console.log(value, "++++++++++++");
      this.$refs[item.key || item.mode].show(value);
    },
    onCancel(val) {
      console.log(val);
    },
    onConfirm(val) {
      console.log(val, this.key);
      //如果页面需要调用多个mode类型，可以根据mode处理结果渲染到哪里;
      if (this.key === "address") {
        this.resultInfo2.province = val.checkArr[0];
        this.resultInfo2.city =
          val.checkArr[1] === "市辖区" ? val.checkArr[0] : val.checkArr[1];
        this.resultInfo2.area = val.checkArr[2];
        this.$set(this.resultInfo2, [this.key], val.result);
      } else {
        this.$set(this.resultInfo, [this.key], val.result);
      }
      this.$set(this.resultInfoArray, [this.key], val.defaultVal);
    },
    change(e) {
      if (this.current === -1) {
        this.current = 0;
      } else {
        this.current = -1;
      }
    },
    getPatstyleStr() {
      let patstyle = "";
      this.patstyle.forEach((element) => {
        if (element.check) {
          patstyle += element.key + ",";
        }
      });
      if (patstyle.indexOf(",") > -1) {
        patstyle = patstyle.substring(0, patstyle.lastIndexOf(","));
      }
      this.getPatstyleArrayStr = patstyle;
      return patstyle;
    },
    getError() {
      if (!this.describe || this.describe.length < 6) {
        this.tui.toast("约拍描述不少于6个字");
        return;
      }
      if (!this.imageList || this.imageList.length < 1) {
        this.tui.toast("请上传想要拍摄的风格");
        return;
      }
      if (!this.resultInfo.target) {
        this.tui.toast("请选择约拍对象");
        return;
      }
      if (!this.resultInfo.patdate) {
        this.tui.toast("请选择约拍时间");
        return;
      }
      const start = new Date(
        new Date(new Date().toLocaleDateString()).getTime()
      );
      if (+new Date(this.resultInfo.patdate) < +new Date(start)) {
        this.tui.toast("请选择正确约拍时间");
        return;
      }
      if (!this.resultInfo2.province || !this.resultInfo2.city) {
        this.tui.toast("请选择约拍地区");
        return;
      }
      if (!this.resultInfo.chargeway) {
        this.tui.toast("请选择收费方式");
        return;
      }
      if (
        this.resultInfo.chargeway &&
        (this.resultInfo.chargeway === "我要收费" ||
          this.resultInfo.chargeway === "可付费") &&
        !this.resultInfo.chargeamt
      ) {
        this.tui.toast("请输入收费金额");
        return;
      }
      return true;
    },
    cancle() {
      this.visiableAlert = false;
      if (this.pathName === "isNeedRealName") {
        this.publish_data();
        this.pathName = "/pages/home/success/index?status=99";
      }
    },
    sure() {
      this.visiableAlert = false;
      //   if (this.pathName === "isNeedRealName") {
      //     this.$set(this.param, "realnameflag", "1");
      //     this.$set(this.resultInfo, "realname", "需要");
      //     this.pathName = "/pages/home/success/index?status=99";
      //     this.publish_data();
      //     return;
      //   }
      if (this.pathName === "/pages/home/success/index?status=99") {
        // if (this.resultInfo.realname !== "需要") {
        //   this.visiableAlert = true;
        //   this.pathName = "isNeedRealName";
        //   this.button = [
        //     {
        //       text: "不要求，继续发布",
        //       type: "red",
        //       plain: true //是否空心
        //     },
        //     {
        //       text: "要求，继续发布",
        //       type: "red",
        //       plain: false
        //     }
        //   ];
        //   return;
        // }
        this.publish_data();
        return;
      }
      uni.navigateTo({ url: this.pathName });
    },
    async publish() {
      if (!this.tmplidList || this.tmplidList.length === 0) {
        this.tmplid_list();
      }
      let infoStatus = await isNendUserInfo(this);
      if (infoStatus) {
        if (this.userInfo && this.userInfo.ppd < 3) {
          this.visiableAlert = true;
          this.pathName = "/pages/mine/ppd/index";
          this.button = [
            {
              text: "取消",
              type: "red",
              plain: true, //是否空心
            },
            {
              text: "去获取",
              type: "red",
              plain: false,
            },
          ];
          return;
        }
        let param = {};
        if (!this.getError()) {
          return;
        }
        console.log(this.resultInfo);
        param.target = this.resultInfo.target === "约摄影师" ? "0" : "1";
        param.realnameflag = this.resultInfo.realname === "需要" ? "1" : "0";
        param.creditflag = this.resultInfo.credit === "需要" ? "1" : "0";
        chargewayMap.forEach((ele, index) => {
          if (ele === this.resultInfo.chargeway) {
            param.chargeway = index;
          }
        });
        if (param.chargeway !== 0 && !param.chargeway) {
          param.chargeway = "";
        }
        this.getPatstyleStr();
        param.describ = this.describe || "";

        param.patdate = this.resultInfo.patdate || "";
        param.province = this.resultInfo2.province || "";
        param.city = this.resultInfo2.city || "";
        param.area = this.resultInfo2.area || "";
        param.patarea = this.resultInfo.patarea || "";
        param.patslice = this.resultInfo.patslice || "";
        param.chargeamt = Number(this.resultInfo.chargeamt) || 0;
        param.patstyle = this.getPatstyleArrayStr || "";
        param.pics = this.imageList || "";
        console.log(param, "============");
        this.param = param;
        this.visiableAlert = true;
        this.pathName = "/pages/home/success/index?status=99";
        this.button = [
          {
            text: "取消",
            type: "red",
            plain: true, //是否空心
          },
          {
            text: "确认发布",
            type: "red",
            plain: false,
          },
        ];
      }
    },
    async tmplid_list() {
      let res = await tmplid_list();
      if (res && res.code === 200) {
        this.tmplidList = res.res;
      }
    },
    async publish_data() {
      console.log(this.param, "----------");
      let message = await sendMsg(this.tmplidList, [0, 1, 2]).catch((err) => {
        if (err) {
          this.tui.toast(err);
        }
      });
      if (message) {
        this.visible = true;
        let res = await ypat_submit(this.param).catch(() => {
          this.visible = false;
        });
        if (res && res.code === 200) {
          this.unload = true;
          this.visible = false;
          uni.redirectTo({ url: this.pathName });
        } else {
          this.visible = false;
          this.tui.toast(res.msg);
        }
      } else {
        this.visible = false;
      }
    },
    close(e) {
      this.imageList.splice(e, 1);
    },
    chooseImage: async function () {
      if (this.imageList.length === 9) {
        let isContinue = await this.isFullImg();
        console.log("是否继续?", isContinue);
        if (!isContinue) {
          return;
        }
      }
      uni.chooseImage({
        sourceType: sourceType[this.sourceTypeIndex],
        sizeType: ["compressed"],
        count:
          this.imageList.length + this.count[this.countIndex] > 9
            ? 9 - this.imageList.length
            : this.count[this.countIndex],
        success: (res1) => {
          for (let index = 0; index < res1.tempFilePaths.length; index++) {
            uni.compressImage({
              src: res1.tempFilePaths[index],
              quality: 50,
              success: (res) => {
                // #ifdef MP
                this.toBase64(res.tempFilePath);
                // #endif
              },
            });
          }
        },
      });
    },
    toBase64(data) {
      uni.getFileSystemManager().readFile({
        filePath: data, //选择图片返回的相对路径
        encoding: "base64", //编码格式
        success: (res) => {
          var strLen = res.data.length;
          var fileSize = strLen - (strLen / 8) * 2;
          console.log("文件大小:" + fileSize);
          //成功的回调
          let base64 = "data:image/jpeg;base64," + res.data; //不加上这串字符，在页面无法显示的哦
          if (this.imageList.length < 9) {
            // #ifndef APP-PLUS
            this.imageList = this.imageList.concat(base64); //非APP平台不支持自定义压缩,暂时没有处理,可通过uni-app上传组件的sizeType属性压缩
            // #endif
          }
        },
      });
    },
    urlTobase64(url) {
      uni.request({
        url: url,
        method: "GET",
        responseType: "arraybuffer",
        success: (ress) => {
          let base64 = wx.arrayBufferToBase64(ress.data); //把arraybuffer转成base64
          base64 = "data:image/jpeg;base64," + base64; //不加上这串字符，在页面无法显示的哦
          if (this.imageList.length < 9) {
            this.imageList.push(base64); //非APP平台不支持自定义压缩,暂时没有处理,可通过uni-app上传组件的sizeType属性压缩
          }
        },
      });
    },
    isFullImg: function () {
      return new Promise((res) => {
        uni.showModal({
          confirmColor: "#1BB6B6",
          cancelColor: "#B4B9B9",
          content: "已经有9张图片了,是否清空现有图片？",
          success: (e) => {
            if (e.confirm) {
              this.imageList = [];
              res(true);
            } else {
              res(false);
            }
          },
          fail: () => {
            res(false);
          },
        });
      });
    },
    previewImage: function (e) {
      var current = e.target.dataset.src;
      uni.previewImage({
        current: current,
        urls: this.imageList,
      });
    },
    getdescribe: function (e) {
      this.describe = e.detail.value;
    },
    touchStart: function (e) {
      this.startX = e.mp.changedTouches[0].pageX;
    },
    touchEnd: function (e) {
      this.endX = e.mp.changedTouches[0].pageX;
      if (this.endX - this.startX > 200) {
        uni.navigateBack();
      }
    },
  },
};
