package com.ypat.controller;

import com.ypat.annotation.NotIntercept;
import com.ypat.util.FastDFSClient;
import com.ypat.util.GsonUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ueditor")
public class UeditorController {

    @Autowired
    private FastDFSClient fastDFSClient;

    @RequestMapping("/config")
    @NotIntercept
    public String getConfig() throws Exception {
        //File file = ResourceUtils.getFile("classpath:conf/config.json");
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("conf/config.json");
        String json = IOUtils.toString(inputStream,"utf-8");
        return json;
    }

    @RequestMapping("/uploadImage")
    @NotIntercept
    public String uploadImage(@RequestParam("upfile") MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String extraName = fileName.substring(fileName.lastIndexOf("."));
        String fileId = fastDFSClient.uploanFile1(file.getInputStream(), "jpg");
        //String fileId = "group1/M00/00/06/rBEPS14QNXWAI86XAAEUuLLnVnY640.jpg";
        Map<String, Object> resultMap = resultMap("SUCCESS", fileId, file.getSize(), "", fileName, extraName);
        return GsonUtils.toJson(resultMap);
    }

    private Map<String,Object> resultMap(String state,String url,long size,String title,String original,String type){
        Map<String ,Object> result = new HashMap();
        result.put("state",state);
        result.put("original",original);
        result.put("size",size);
        result.put("title",title);
        result.put("type",type);
        result.put("url", url);
        return result;
    }
}
