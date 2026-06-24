/*
 * @Author: shawn
 * @LastEditTime: 2020-03-26 10:44:58
 */
import tuiListCell from "@/components/thor-ui/list-cell/list-cell";
import tuiListView from "@/components/thor-ui/list-view/list-view";
import badge from "@/components/thor-ui/badge/badge";
import tuiCollapse from "@/components/thor-ui/tui-collapse/tui-collapse";

export default {
  components: {
    tuiListCell,
    tuiListView,
    badge,
    tuiCollapse
  },
  data() {
    return {
      current: -1,
      dataList: [
        {
          current: 0,
          title: "爱去拍是什么？",
          desc: ["爱去拍是个人与摄影师的摄影约拍平台。"]
        },
        {
          current: -1,
          title: "发布约拍信息时，图片为什么上传不上去？",
          desc: [
            "当前这种情况，可能由于当前图片过大导致，请尽量选择单张图片大小3M以内进行上传， 或也可尝试上传6张图片数量以内图片。"
          ]
        },
        {
          current: -1,
          title: "为什么我发布的信息会审核失败？",
          desc: [
            "1、图片过于模糊不清晰。",
            "2、约拍的内容不宜传播、存在虚假信息或错误信息等情况。",
            "3、该平台用户账号被他人实名举报，均会导致审核失败。"
          ]
        },

        {
          current: -1,
          title: "平台拍拍豆是做什么的？",
          desc: [
            "拍拍豆是可用于约拍信息发布，及查看约拍者联系方式或向其他人发起约拍申请时使用。"
          ]
        },
        {
          current: -1,
          title: "如何获取拍拍豆？",
          desc: [
            "方式一、可通过平台直接进行购买。",
            "方式二、可通过邀请好友获取更多拍拍豆。"
          ]
        },
        {
          current: -1,
          title: "订阅消息是干什么的？",
          desc: [
            "订阅消息是用户来接收消息通知，请用户务必允许，方便能及时第一时间收到约拍消息、平台审核等消息通知。"
          ]
        },
        {
          current: -1,
          title: "怎么向其他人发起约拍，怎么联系对方？",
          desc: [
            "发起方，选择想要约拍的用户信息，直接进行约拍，平台将会把发起方预留在平台的联系方式（包括但不仅限于手机号、微信号）提供给约拍接受方，约拍接收方收到发起方约拍请求后，会根据发起方当前资质条件，可自由选择是否对发起方（通过预留平台的联系方式）进行联系 。"
          ]
        },
        {
          current: -1,
          title: "怎么才能提高自己的约拍成功率？",
          desc: [
            "1、在平台进行实名认证、信用担保，加强自己的约拍资质信息及个人信誉度。",
            "2、发布信息尽量约拍主题明确，约拍信息描述详细、真实、真诚，发布图片尽可能质量高、大胆有创意、风格各异，更容易获得摄影师或模特信赖认可。",
            "3、尽可能多发布约拍信息，利于加强平台活跃度，利于他人有更多约拍信息了解。"
          ]
        }
      ]
    };
  },
  onShow() {
    //  页面公用方法
    this.tui.commonFunc();
    //  页面公用方法 end
  },
  mounted() {},
  methods: {
    change3(e) {
      this.current = this.current == e.index ? -1 : e.index;
    }
  }
};
