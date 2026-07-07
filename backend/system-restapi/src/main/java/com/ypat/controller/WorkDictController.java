package com.ypat.controller;

import com.ypat.ResponseApiBody;
import com.ypat.entity.WorkTag;
import com.ypat.repository.WorkTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/service/dict")
public class WorkDictController {

    @Autowired
    private WorkTagRepository workTagRepository;

    @GetMapping("/work-tag")
    @Transactional
    public ResponseApiBody listWorkTags() {
        ensureDefaultWorkTags();
        List<WorkTag> tags = workTagRepository.findByStatusOrderBySortNoAsc(1);
        return ResponseApiBody.success(tags);
    }

    private void ensureDefaultWorkTags() {
        for (DefaultWorkTag defaultTag : DEFAULT_WORK_TAGS) {
            WorkTag tag = workTagRepository.findByCode(defaultTag.code);
            if (tag == null) {
                tag = new WorkTag();
                tag.setCode(defaultTag.code);
            } else if (Integer.valueOf(1).equals(tag.getStatus())
                    && defaultTag.name.equals(tag.getName())
                    && defaultTag.sortNo.equals(tag.getSortNo())) {
                continue;
            }
            tag.setName(defaultTag.name);
            tag.setSortNo(defaultTag.sortNo);
            tag.setStatus(1);
            workTagRepository.save(tag);
        }
    }

    private static final DefaultWorkTag[] DEFAULT_WORK_TAGS = new DefaultWorkTag[] {
            new DefaultWorkTag("qinglv", "情侣", 1),
            new DefaultWorkTag("shangwu", "商务", 2),
            new DefaultWorkTag("minguo", "民国", 3),
            new DefaultWorkTag("hanfu", "汉服", 4),
            new DefaultWorkTag("yunzhao", "孕照", 5),
            new DefaultWorkTag("ertong", "儿童摄影", 6),
            new DefaultWorkTag("anhei", "暗黑", 7),
            new DefaultWorkTag("qingxu", "情绪", 8),
            new DefaultWorkTag("yejing", "夜景", 9),
            new DefaultWorkTag("xiaoyuan", "校园", 10),
            new DefaultWorkTag("zhuangrong", "妆容", 11),
            new DefaultWorkTag("gufeng", "古风", 12),
            new DefaultWorkTag("taobao", "淘宝", 13),
            new DefaultWorkTag("shishang", "时尚", 14),
            new DefaultWorkTag("hefu", "和服", 15),
            new DefaultWorkTag("qipao", "旗袍", 16),
            new DefaultWorkTag("hanxi", "韩系", 17),
            new DefaultWorkTag("oumei", "欧美", 18),
            new DefaultWorkTag("senxi", "森系", 19),
            new DefaultWorkTag("shaonv", "少女", 20),
            new DefaultWorkTag("baolilai", "宝丽来", 21),
            new DefaultWorkTag("qingxin", "清新", 22),
            new DefaultWorkTag("hunli", "婚礼", 23),
            new DefaultWorkTag("cosplay", "cosplay", 24),
            new DefaultWorkTag("jiaopian", "胶片", 25),
            new DefaultWorkTag("heibai", "黑白", 26),
            new DefaultWorkTag("jishi", "纪实", 27),
            new DefaultWorkTag("rixi", "日系", 28),
            new DefaultWorkTag("fugu", "复古", 29)
    };

    private static class DefaultWorkTag {
        private final String code;
        private final String name;
        private final Integer sortNo;

        private DefaultWorkTag(String code, String name, Integer sortNo) {
            this.code = code;
            this.name = name;
            this.sortNo = sortNo;
        }
    }
}
