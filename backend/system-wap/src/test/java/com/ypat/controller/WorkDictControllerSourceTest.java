package com.ypat.controller;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class WorkDictControllerSourceTest {

    @Test
    public void restapiSeedsDefaultWorkTagsWhenDictionaryIsEmpty() throws Exception {
        String source = read("backend/system-restapi/src/main/java/com/ypat/controller/WorkDictController.java");

        assertTrue(source.contains("ensureDefaultWorkTags()"));
        assertTrue(source.contains("findByCode(defaultTag.code)"));
        assertTrue(source.contains("tag.setStatus(1)"));
        assertTrue(source.contains("workTagRepository.save(tag)"));
        assertTrue(source.contains("qinglv"));
        assertTrue(source.contains("fugu"));
    }

    @Test
    public void defaultWorkTagsContainTwentyNineStableCodes() throws Exception {
        String source = read("backend/system-restapi/src/main/java/com/ypat/controller/WorkDictController.java");
        String[] codes = new String[] {
                "qinglv", "shangwu", "minguo", "hanfu", "yunzhao",
                "ertong", "anhei", "qingxu", "yejing", "xiaoyuan",
                "zhuangrong", "gufeng", "taobao", "shishang", "hefu",
                "qipao", "hanxi", "oumei", "senxi", "shaonv",
                "baolilai", "qingxin", "hunli", "cosplay", "jiaopian",
                "heibai", "jishi", "rixi", "fugu"
        };
        int lastIndex = -1;
        for (String code : codes) {
            int index = source.indexOf("new DefaultWorkTag(\"" + code + "\"");
            assertTrue("missing code " + code, index >= 0);
            assertTrue("code order changed for " + code, index > lastIndex);
            lastIndex = index;
        }
    }

    @Test
    public void wapResponseAdviceSetsUtf8ForUnifiedApiBodies() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/handler/SysResultHandler.java");

        assertTrue(source.contains("body instanceof ResponseApiBody"));
        assertTrue(source.contains("response.getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8)"));
    }

    private String read(String file) throws Exception {
        Path path = Paths.get(file);
        if (!Files.exists(path)) {
            path = Paths.get("..", file.replace("backend/", ""));
        }
        if (!Files.exists(path)) {
            path = Paths.get(file.replace("backend/system-wap/", ""));
        }
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
