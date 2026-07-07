import localStorageObj from "@/common/localStorage";
import { getUserInfo } from "@/common/utils";
import {
  work_get,
  work_like_add,
  work_like_cancel,
  work_sc_add,
  work_sc_cancel,
} from "@/common/vmeitime-http";

export default {
  data() {
    return {
      id: "",
      loading: false,
      content: {},
      userInfo: {},
      liked: false,
      favorited: false,
    };
  },
  computed: {
    mediaUrls() {
      const mediaList = this.content.mediaList || this.content.medias || [];
      const fromList = mediaList.map((item) => item.url || item.mediaUrl || item.path).filter(Boolean);
      if (fromList.length) return fromList;
      if (this.content.coverUrl) return [this.content.coverUrl];
      if (this.content.pics && Array.isArray(this.content.pics)) return this.content.pics;
      return [];
    },
    author() {
      return this.content.user || this.content.userQo || {};
    },
    authorAvatar() {
      return this.author.imgpath || this.author.avatar || "/static/images/mine/mine_def_touxiang_3x.png";
    },
    authorName() {
      return this.author.nickname || this.author.name || "爱去拍用户";
    },
    authorMeta() {
      const profess = this.author.professTxt || this.author.professName || "创作者";
      const city = this.author.city || this.content.city || "";
      return city ? `${profess} | ${city}` : profess;
    },
    isSelf() {
      const authorId = `${this.author.id || this.content.userid || ""}`;
      return authorId && this.userInfo && `${this.userInfo.id}` === authorId;
    },
  },
  onLoad(options) {
    this.id = options.id || options.workId || "";
    this.loadDetail();
  },
  async onShow() {
    this.tui.commonFunc();
    this.userInfo = await getUserInfo().catch(() => ({}));
  },
  methods: {
    ensureLogin() {
      if (uni.getStorageSync(localStorageObj.token)) return true;
      uni.navigateTo({ url: "/pages/login/login/index" });
      return false;
    },
    async loadDetail() {
      if (!this.id) return;
      this.loading = true;
      const res = await work_get({ id: this.id }).catch(() => null);
      this.loading = false;
      if (res && res.code === 200) {
        this.content = res.res || {};
        this.liked = this.content.likeflag === "1" || this.content.liked === true;
        this.favorited = this.content.colflag === "1" || this.content.favoriteflag === "1" || this.content.favorited === true;
      } else if (res && res.msg) {
        this.tui.toast(res.msg);
      }
    },
    async toggleLike() {
      if (!this.ensureLogin()) return;
      const next = !this.liked;
      this.liked = next;
      const api = next ? work_like_add : work_like_cancel;
      const res = await api({ workId: this.id }).catch(() => null);
      if (!res || res.code !== 200) {
        this.liked = !next;
        this.tui.toast((res && res.msg) || "操作失败");
      }
    },
    async toggleFavorite() {
      if (!this.ensureLogin()) return;
      const next = !this.favorited;
      this.favorited = next;
      const api = next ? work_sc_add : work_sc_cancel;
      const res = await api({ workId: this.id }).catch(() => null);
      if (!res || res.code !== 200) {
        this.favorited = !next;
        this.tui.toast((res && res.msg) || "操作失败");
      }
    },
    goComplain() {
      if (!this.ensureLogin()) return;
      uni.navigateTo({ url: `/pages/work/complain/index?workId=${this.id}` });
    },
    goApply() {
      if (!this.ensureLogin()) return;
      uni.navigateTo({ url: `/pages/work/apply/index?workId=${this.id}` });
    },
    previewImage(e) {
      const index = e.currentTarget.dataset.index;
      uni.previewImage({
        current: this.mediaUrls[index],
        urls: this.mediaUrls,
      });
    },
  },
  onShareAppMessage() {
    return {
      title: `${this.authorName}的作品，快来爱去拍看看`,
      imageUrl: this.mediaUrls[0],
      path: `/pages/work/detail/index?id=${this.id}`,
    };
  },
};
