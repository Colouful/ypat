/*
 * @Author: shawn
 * @LastEditTime: 2020-05-15 20:56:51
 */
import tuiListCell from "@/components/thor-ui/list-cell/list-cell";
import { user_upd } from "@/common/vmeitime-http";
import { filter } from "@/mixins/filter";
// import WPicker from "@/components/custom/picker/picker.vue";
import { isMobile, getUserInfo } from "@/common/utils";
import cropper from "@/components/custom/cropper/index.vue";
import { professMap } from "@/common/dataMap";
import WPicker from "@/components/custom/picker/picker.vue";
import { authorize, getLocation } from "@/common/utils";
import { getCity } from "@/common/getCity";
export default {
  components: {
    tuiListCell,
    WPicker,
    cropper,
  },
  mixins: [filter],
  data() {
    return {
      defaultRegion: [],
      imgurl: "/static/logo.png",
      resultInfo: {},
      professList: [],
      areaObj: {},
      defaultProps: {
        label: "label",
        value: "value",
      },
      genderList: [
        {
          label: "男",
          value: 1,
        },
        {
          label: "女",
          value: 2,
        },
      ],
      tabList: [
        {
          mode: "selector",
          key: "profess",
          name: "职业",
        },
        {
          mode: "selector",
          key: "gender",
          name: "性别",
        },
        {
          mode: "date",
          key: "birthday",
          name: "生日",
        },
        {
          mode: "region",
          key: "area",
          name: "城市",
        },
      ],
      genderDefaultValue: "",
      professDefaultValue: "",
      userInfo: {},
      show: false,
      imgpath: "",
      defaultValTime: "",
      defaultValImg: "/static/images/mine/mine_def_touxiang_3x.png",
    };
  },
  created() {},
  onShow() {
    //  页面公用方法
    this.tui.commonFunc();
    //  页面公用方法 end
  },
  mounted() {
    this.professList = professMap.map((element, index) => {
      return { label: element, value: index };
    });
    this.user_get();
  },

  methods: {
    //上传返回图片
    myUpload(rsp) {
      const self = this;
      self.imgurl = rsp.path; //更新头像方式一
      console.log(rsp.path);
      uni.getFileSystemManager().readFile({
        filePath: rsp.path, //选择图片返回的相对路径
        encoding: "base64", //编码格式
        success: (res) => {
          //成功的回调
          let base64 = "data:image/jpeg;base64," + res.data; //不加上这串字符，在页面无法显示的哦
          this.imgpath = base64;
          this.userInfo.imgpath = base64;
        },
      });
      // rsp.avatar.imgSrc = rsp.path; //更新头像方式二
    },
    getname(num, type) {
      let name = "";
      for (let index = 0; index < this[`${type}List`].length; index++) {
        if (this[`${type}List`][index].value === Number(num)) {
          name = this[`${type}List`][index].label;
          break;
        }
      }
      console.log(name);
      return name;
    },
    getError() {
      if (!this.userInfo.nickname) {
        this.tui.toast("请输入昵称");
        return;
      }

      if (!this.resultInfo.profess) {
        this.tui.toast("请选择职业");
        return;
      }
      if (!this.resultInfo.gender) {
        this.tui.toast("请选择性别");
        return;
      }
      if (!this.resultInfo.birthday) {
        this.tui.toast("请选择生日");
        return;
      }

      if (!this.userInfo.mobile) {
        this.tui.toast("请输入手机号");
        return;
      }
      if (this.userInfo.mobile && !isMobile(this.userInfo.mobile)) {
        this.tui.toast("请输入正确的手机号");
        return;
      }
      if (!this.userInfo.wx) {
        this.tui.toast("请输入微信号");
        return;
      }
      return true;
    },
    fSelect() {
      this.$refs.myUpload.fSelect();
    },
    async formSubmit() {
      if (!this.getError()) {
        return;
      }
      if (this.resultInfo && this.resultInfo.gender) {
        this.userInfo.gender = this.resultInfo.gender === "男" ? "1" : "2";
      }
      console.log(this.resultInfo, "--------------");
      if (this.resultInfo && this.resultInfo.profess) {
        professMap.forEach((ele, index) => {
          if (ele === this.resultInfo.profess) {
            this.userInfo.profess = index;
          }
        });
      }
      console.log(this.userInfo);
      let param = {
        gender: this.userInfo.gender || "",
        nickname: this.userInfo.nickname || "",
        profess: this.userInfo.profess + "" || "",
        mobile: this.userInfo.mobile || "",
        wx: this.userInfo.wx || "",
        qq: this.userInfo.qq || "",
        wb: this.userInfo.wb || "",
        pics: this.userInfo.imgpath || "",
        province: this.areaObj.province || "",
        city: this.areaObj.city || "",
        area: this.areaObj.area || "",
        birthday: this.resultInfo.birthday || "",
      };
      console.log(param);
      let res = await user_upd(param);
      if (res && res.code === 200) {
        this.tui.toast("更新成功");
        setTimeout(() => {
          uni.navigateBack();
        }, 2000);
      } else {
        this.tui.toast(res.msg);
      }
    },
    onCancel() {},
    onConfirm(val) {
      console.log(val, this.key);
      //如果页面需要调用多个mode类型，可以根据mode处理结果渲染到哪里;
      this.resultInfo = {
        ...this.resultInfo,
        [this.key]: val.result,
      };
      if (this.key === "area") {
        this.areaObj = {
          province: val.obj.province.label,
          city: val.obj.city.label,
          area: val.obj.area.label,
        };
      }
    },
    toggleTab(item) {
      this.key = item.key;
      this.defaultVal = item.value;
      this.$refs[item.key || item.mode].show();
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
    async user_get() {
      let res = await getUserInfo();
      this.userInfo = res;
      if (this.userInfo.gender) {
        this.genderDefaultValue =
          this.getname(this.userInfo.gender, "gender") + "";
        this.resultInfo.gender = this.gender(this.userInfo.gender);
      }
      if (this.userInfo.birthday) {
        this.defaultValTime = this.userInfo.birthday;
        this.resultInfo.birthday = this.userInfo.birthday;
      } else {
        this.getDefaultValTime();
      }
      if (this.userInfo.profess) {
        this.professDefaultValue =
          this.getname(this.userInfo.profess, "profess") + "";
        this.resultInfo.profess = this.profess(this.userInfo.profess);
      } else {
        this.professDefaultValue = this.getname("0", "profess") + "";
      }
      console.log(this.userInfo.province)
      // 回显地址
      if (this.userInfo.province && this.userInfo.city) {
        this.areaObj = {
          province: this.userInfo.province,
          city: this.userInfo.city,
          area: this.userInfo.area,
        };
        this.resultInfo.area = this.userInfo.area
          ? this.userInfo.province + this.userInfo.city + this.userInfo.area
          : this.userInfo.province + this.userInfo.city;
      } else {
        // #ifdef MP
        let location = await authorize(
          "scope.userLocation",
          () => getLocation(true),
          "请开启定位权限"
        ).catch((error) => {});
        if (location) {
          let address = await getCity(location, "area").catch(() => {});
          console.log(address);
          if (address) {
            this.areaObj = {
              province: address[0],
              city: address[1],
              area: address[2],
            };
            this.userInfo.province = address[0];
            this.userInfo.city = address[1];
            this.userInfo.area = address[2];
            this.resultInfo.area =
              this.userInfo.province + this.userInfo.city + this.userInfo.area;
          }
        }
        // #endif
      }
      this.imgpath = this.userInfo.imgpath;
      this.show = true;
    },
  },
};
