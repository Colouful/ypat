package com.ypat.controller;

import com.ypat.ResponseCode;
import com.ypat.ResponseApiBody;
import com.ypat.SysException;
import com.ypat.service.WorkMediaService;
import com.ypat.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 作品媒体上传 Controller（multipart/form-data）
 * 注意：本 Controller 不通过 Feign，直接调用同模块的 WorkMediaService
 * （因为 FastDFS 客户端只在 system-wap 引入）
 */
@RestController
public class WorkMediaController {

    @Autowired
    private WorkMediaService workMediaService;

    @PostMapping("/work/upload/image")
    public ResponseApiBody uploadImage(@RequestPart("file") MultipartFile file) {
        String userId = UserUtil.getUserId();
        if (userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
        if (file == null || file.isEmpty()) throw new SysException(ResponseCode.FAIL_PARA, "请选择要上传的文件");
        Map<String, Object> res = workMediaService.uploadImage(file, Long.parseLong(userId));
        return ResponseApiBody.success(res);
    }

    @PostMapping("/work/upload/video")
    public ResponseApiBody uploadVideo(@RequestPart("file") MultipartFile file) {
        String userId = UserUtil.getUserId();
        if (userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
        if (file == null || file.isEmpty()) throw new SysException(ResponseCode.FAIL_PARA, "请选择要上传的文件");
        Map<String, Object> res = workMediaService.uploadVideo(file, Long.parseLong(userId));
        return ResponseApiBody.success(res);
    }

    @DeleteMapping("/work/upload/media")
    public ResponseApiBody deleteMedia(@RequestParam("id") String id) {
        String userId = UserUtil.getUserId();
        if (userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
        if (id == null) throw new SysException(ResponseCode.FAIL_PARA);
        workMediaService.deleteMedia(Long.parseLong(id), Long.parseLong(userId));
        return ResponseApiBody.success("已删除");
    }
}
