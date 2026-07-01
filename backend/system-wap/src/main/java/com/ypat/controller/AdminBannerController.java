package com.ypat.controller;

import com.ypat.BannerQo;
import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.service.BannerServiceClient;
import com.ypat.third.baidu.ai.GsonUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - 横幅管理 Controller。
 *
 * <p>对应旧后台：审核系统-横幅列表。</p>
 */
@RestController
@RequestMapping("/admin/banner")
public class AdminBannerController {

    private static final Logger logger = LoggerFactory.getLogger(AdminBannerController.class);

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;

    @Autowired
    private BannerServiceClient bannerServiceClient;

    /**
     * 横幅列表分页查询。
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

        BannerQo qo = new BannerQo();
        qo.setPage(page);
        qo.setSize(size);
        if (StringUtils.isNotBlank(name)) {
            qo.setTitle(name);
        }
        if (StringUtils.isNotBlank(status)) {
            qo.setStatus(status);
        }

        String json = bannerServiceClient.findPage(qo);
        Object pageData = GsonUtils.fromJson(json, Object.class);
        return ResponseApiBody.success(pageData);
    }

    /**
     * 横幅详情。
     */
    @GetMapping("/detail")
    public ResponseApiBody detail(@RequestParam("id") Long id) {
        if (id == null) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        String json = bannerServiceClient.get(id);
        Object data = GsonUtils.fromJson(json, Object.class);
        return ResponseApiBody.success(data);
    }

    /**
     * 新增/编辑保存。
     */
    @PostMapping("/save")
    public ResponseApiBody save(@RequestBody BannerQo bannerQo) {
        if (bannerQo == null || StringUtils.isBlank(bannerQo.getTitle())) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "横幅标题不能为空");
        }
        if (StringUtils.isBlank(bannerQo.getImgpath())) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "请上传横幅图片");
        }

        logger.info("管理端横幅保存：id={}, title={}", bannerQo.getId(), bannerQo.getTitle());
        String res = bannerServiceClient.add(bannerQo);
        Object resData = GsonUtils.fromJson(res, Object.class);
        return ResponseApiBody.success(resData);
    }

    /**
     * 发布 / 撤回。
     */
    @PostMapping("/upDown")
    public ResponseApiBody upDown(
            @RequestParam("id") Long id,
            @RequestParam("status") String status) {

        if (id == null || StringUtils.isBlank(status)) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }

        logger.info("管理端横幅上下架：id={}, status={}", id, status);
        BannerQo qo = new BannerQo();
        qo.setId(id);
        qo.setStatus(status);

        String res = bannerServiceClient.upDown(qo);
        Object resData = GsonUtils.fromJson(res, Object.class);
        return ResponseApiBody.success(resData);
    }
}
