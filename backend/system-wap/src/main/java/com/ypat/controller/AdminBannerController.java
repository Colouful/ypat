package com.ypat.controller;

import com.ypat.BannerQo;
import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.service.BannerServiceClient;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
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
    private static final String JUMP_DISABLED = "0";
    private static final String JUMP_ENABLED = "1";
    private static final String JUMP_TYPE_MINIAPP = "miniapp";
    private static final String JUMP_TYPE_WEB = "web";
    private static final int MAX_JUMP_URL_LENGTH = 500;

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
        JsonElement pageData = JsonParser.parseString(json);
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
        JsonElement data = JsonParser.parseString(json);
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
        normalizeAndValidateJump(bannerQo);

        logger.info("管理端横幅保存：id={}, title={}", bannerQo.getId(), bannerQo.getTitle());
        bannerServiceClient.add(bannerQo);
        return ResponseApiBody.success(null);
    }

    private void normalizeAndValidateJump(BannerQo bannerQo) {
        String jumpflag = StringUtils.defaultIfBlank(bannerQo.getJumpflag(), JUMP_DISABLED).trim();
        bannerQo.setJumpflag(jumpflag);

        if (!JUMP_ENABLED.equals(jumpflag)) {
            bannerQo.setJumpflag(JUMP_DISABLED);
            bannerQo.setJumptype(null);
            bannerQo.setJumpurl(null);
            return;
        }

        String jumptype = StringUtils.trimToEmpty(bannerQo.getJumptype());
        String jumpurl = StringUtils.trimToEmpty(bannerQo.getJumpurl());

        if (!JUMP_TYPE_MINIAPP.equals(jumptype) && !JUMP_TYPE_WEB.equals(jumptype)) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "跳转类型不正确");
        }
        if (StringUtils.isBlank(jumpurl)) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "请输入跳转目标");
        }
        if (jumpurl.length() > MAX_JUMP_URL_LENGTH) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "跳转目标不能超过500个字符");
        }
        if (JUMP_TYPE_MINIAPP.equals(jumptype) && !isMiniappPath(jumpurl)) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "请输入 /pages 或 /pages-sub 开头的小程序页面路径");
        }
        if (JUMP_TYPE_WEB.equals(jumptype) && !isHttpUrl(jumpurl)) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "请输入 http 或 https 开头的外部地址");
        }

        bannerQo.setJumptype(jumptype);
        bannerQo.setJumpurl(jumpurl);
    }

    private boolean isMiniappPath(String value) {
        return value.startsWith("/pages/") || value.startsWith("/pages-sub/");
    }

    private boolean isHttpUrl(String value) {
        return value.startsWith("http://") || value.startsWith("https://");
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

        bannerServiceClient.upDown(qo);
        return ResponseApiBody.success(null);
    }
}
