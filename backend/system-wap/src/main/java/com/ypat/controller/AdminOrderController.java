package com.ypat.controller;

import com.ypat.OrderQo;
import com.ypat.ResponseApiBody;
import com.ypat.service.OrderServiceClient;
import com.ypat.third.baidu.ai.GsonUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 - 订单查询 Controller。
 *
 * <p>对应旧后台：订单系统-订单列表。</p>
 */
@RestController
@RequestMapping("/admin/order")
public class AdminOrderController {

    private static final Logger logger = LoggerFactory.getLogger(AdminOrderController.class);

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;

    @Autowired
    private OrderServiceClient orderServiceClient;

    /**
     * 订单列表分页查询。
     */
    @GetMapping("/list")
    public ResponseApiBody list(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {

        if (page == null || page < 0) {
            page = DEFAULT_PAGE;
        }
        if (size == null || size <= 0) {
            size = DEFAULT_SIZE;
        }

        OrderQo qo = new OrderQo();
        qo.setPage(page);
        qo.setSize(size);
        if (StringUtils.isNotBlank(status)) {
            qo.setStatus(status);
        }
        if (StringUtils.isNotBlank(type)) {
            qo.setType(type);
        }

        String json = orderServiceClient.findPage(qo);
        JsonElement pageData = JsonParser.parseString(json);
        return ResponseApiBody.success(pageData);
    }
}
