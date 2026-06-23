package com.ypat.controller;

import com.ypat.util.FastDFSClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@RestController
public class FileUploadController {

//    @Autowired
    private FastDFSClient fastDFSClient;

    @RequestMapping("/uploadfile")
    public String filesUpload(@RequestParam MultipartFile[] files,
                              HttpServletRequest request) throws Exception{
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                MultipartFile uploadfile = files[i];
                // 保存文件
                String fileId = fastDFSClient.uploanFile1(uploadfile.getInputStream(), uploadfile.getOriginalFilename());
                //文件路径更新到数据库
                //Myfile file=new Myfile();
                //file.setFileid(fileId);
                //file.setFilename(uploadfile.getOriginalFilename());
            }
        }
        return "index";
    }
}
