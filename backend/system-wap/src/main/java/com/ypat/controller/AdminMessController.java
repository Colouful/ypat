package com.ypat.controller;

import com.ypat.MessInfoQo;
import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.service.MessServiceClient;
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
 * 管理端 - 消息查询 Controller。
 *
 * <p>对应旧后台：查询系统-消息列表。</p>
 */
@RestController
@RequestMapping("/admin/mess")
public class AdminMessController {

    private static final Logger logger = LoggerFactory.getLogger(AdminMessController.class);

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;

    @Autowired
    private MessServiceClient messServiceClient;

    /**
     * 消息列表分页查询。
     */
    @GetMapping("/list")
    public ResponseApiBody list(
            @RequestParam(value = "ypatid", required = false) Long ypatid,
            @RequestParam(value = "sendperid", required = false) Long sendperid,
            @RequestParam(value = "recperid", required = false) Long recperid,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {

        if (page == null || page < 0) {
            page = DEFAULT_PAGE;
        }
        if (size == null || size <= 0) {
            size = DEFAULT_SIZE;
        }

        MessInfoQo qo = new MessInfoQo();
        qo.setPage(page);
        qo.setSize(size);
        if (ypatid != null) {
            qo.setYpatid(ypatid);
        }
        if (sendperid != null) {
            qo.setSendperid(sendperid);
        }
        if (recperid != null) {
            qo.setRecperid(recperid);
        }

        // 旧后台要求至少输入一个条件
        if (qo.getYpatid() == null && qo.getSendperid() == null && qo.getRecperid() == null) {
            logger.warn("消息列表查询缺少必要条件");
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "请至少输入一个查询条件（约拍ID、发送者ID、接收者ID）");
        }

        String json = messServiceClient.findPage(qo);
        Object pageData = GsonUtils.fromJson(json, Object.class);
        return ResponseApiBody.success(pageData);
    }
}
