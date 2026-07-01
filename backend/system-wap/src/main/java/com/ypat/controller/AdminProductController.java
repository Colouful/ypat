package com.ypat.controller;

import com.ypat.ProductQo;
import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.service.ProductServiceClient;
import com.ypat.third.baidu.ai.GsonUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - 产品管理 Controller。
 *
 * <p>对应旧后台：审核系统-产品列表。</p>
 */
@RestController
@RequestMapping("/admin/product")
public class AdminProductController {

    private static final Logger logger = LoggerFactory.getLogger(AdminProductController.class);

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;

    @Autowired
    private ProductServiceClient productServiceClient;

    /**
     * 产品列表分页查询。
     */
    @GetMapping("/list")
    public ResponseApiBody list(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {

        if (page == null || page < 0) {
            page = DEFAULT_PAGE;
        }
        if (size == null || size <= 0) {
            size = DEFAULT_SIZE;
        }

        ProductQo qo = new ProductQo();
        qo.setPage(page);
        qo.setSize(size);
        if (StringUtils.isNotBlank(name)) {
            qo.setName(name);
        }
        if (StringUtils.isNotBlank(status)) {
            qo.setStatus(status);
        }

        String json = productServiceClient.findPage(qo);
        JsonElement pageData = JsonParser.parseString(json);
        return ResponseApiBody.success(pageData);
    }

    /**
     * 产品详情。
     */
    @GetMapping("/detail")
    public ResponseApiBody detail(@RequestParam("id") Long id) {
        if (id == null) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        String json = productServiceClient.get(id);
        JsonElement data = JsonParser.parseString(json);
        return ResponseApiBody.success(data);
    }

    /**
     * 新增/编辑保存。
     */
    @PostMapping("/save")
    public ResponseApiBody save(@RequestBody ProductQo productQo) {
        if (productQo == null || StringUtils.isBlank(productQo.getName())) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "产品名称不能为空");
        }

        logger.info("管理端产品保存：id={}, name={}", productQo.getId(), productQo.getName());
        String res = productServiceClient.add(productQo);
        JsonElement resData = JsonParser.parseString(res);
        return ResponseApiBody.success(resData);
    }

    /**
     * 上架 / 下架。
     */
    @PostMapping("/upDown")
    public ResponseApiBody upDown(
            @RequestParam("id") Long id,
            @RequestParam("status") String status) {

        if (id == null || StringUtils.isBlank(status)) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }

        logger.info("管理端产品上下架：id={}, status={}", id, status);
        ProductQo qo = new ProductQo();
        qo.setId(id);
        qo.setStatus(status);

        String res = productServiceClient.upDown(qo);
        JsonElement resData = JsonParser.parseString(res);
        return ResponseApiBody.success(resData);
    }
}
