package com.ypat.controller;

import com.ypat.PubEventQo;
import com.ypat.ResponseApiBody;
import com.ypat.service.PubEventServiceClient;
import com.ypat.third.baidu.ai.GsonUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 - 公众号关注统计 Controller。
 *
 * <p>对应旧后台：查询系统-公众号关注。</p>
 */
@RestController
@RequestMapping("/admin/pubevent")
public class AdminPubEventController {

    private static final Logger logger = LoggerFactory.getLogger(AdminPubEventController.class);

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;

    @Autowired
    private PubEventServiceClient pubEventServiceClient;

    /**
     * 公众号关注统计列表。
     */
    @GetMapping("/list")
    public ResponseApiBody list(
            @RequestParam(value = "dateStrStart", required = false) String dateStrStart,
            @RequestParam(value = "dateStrEnd", required = false) String dateStrEnd,
            @RequestParam(value = "eventKey", required = false) String eventKey,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {

        if (page == null || page < 0) {
            page = DEFAULT_PAGE;
        }
        if (size == null || size <= 0) {
            size = DEFAULT_SIZE;
        }

        PubEventQo qo = new PubEventQo();
        qo.setPage(page);
        qo.setSize(size);
        qo.setEvent("SCAN");
        if (StringUtils.isNotBlank(dateStrStart)) {
            qo.setDateStrStart(dateStrStart);
        }
        if (StringUtils.isNotBlank(dateStrEnd)) {
            qo.setDateStrEnd(dateStrEnd);
        }
        if (StringUtils.isNotBlank(eventKey)) {
            qo.setEventKey(eventKey);
        }

        String json = pubEventServiceClient.findPage(qo);
        Object pageData = GsonUtils.fromJson(json, Object.class);
        return ResponseApiBody.success(pageData);
    }
}
