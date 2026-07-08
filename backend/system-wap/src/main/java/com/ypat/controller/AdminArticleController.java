package com.ypat.controller;

import com.ypat.ArticleQo;
import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.service.ArticleServiceClient;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - 文章管理 Controller。
 *
 * <p>对应旧后台：审核系统-文章列表。</p>
 */
@RestController
@RequestMapping("/admin/article")
public class AdminArticleController {

    private static final Logger logger = LoggerFactory.getLogger(AdminArticleController.class);

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;

    @Autowired
    private ArticleServiceClient articleServiceClient;

    /**
     * 文章列表分页查询。
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

        ArticleQo qo = new ArticleQo();
        qo.setPage(page);
        qo.setSize(size);
        if (StringUtils.isNotBlank(name)) {
            qo.setTitle(name);
        }
        if (StringUtils.isNotBlank(status)) {
            qo.setStatus(status);
        }

        String json = articleServiceClient.findPage(qo);
        JsonElement pageData = JsonParser.parseString(json);
        return ResponseApiBody.success(pageData);
    }

    /**
     * 文章详情。
     */
    @GetMapping("/detail")
    public ResponseApiBody detail(@RequestParam("id") Long id) {
        if (id == null) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        String json = articleServiceClient.get(id);
        JsonElement data = JsonParser.parseString(json);
        return ResponseApiBody.success(data);
    }

    /**
     * 新增/编辑保存。
     */
    @PostMapping("/save")
    public ResponseApiBody save(@RequestBody ArticleQo articleQo) {
        if (articleQo == null || StringUtils.isBlank(articleQo.getTitle())) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "文章标题不能为空");
        }
        if (StringUtils.isNotBlank(articleQo.getEditorValue()) && StringUtils.isBlank(articleQo.getContent())) {
            articleQo.setContent(articleQo.getEditorValue());
        }

        logger.info("管理端文章保存：id={}, title={}", articleQo.getId(), articleQo.getTitle());
        articleServiceClient.add(articleQo);
        return ResponseApiBody.success(null);
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

        logger.info("管理端文章上下架：id={}, status={}", id, status);
        ArticleQo qo = new ArticleQo();
        qo.setId(id);
        qo.setStatus(status);

        articleServiceClient.upDown(qo);
        return ResponseApiBody.success(null);
    }
}
