package com.ypat.controller;

import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.UserQo;
import com.ypat.YpatInfoQo;
import com.ypat.comm.ImageConst;
import com.ypat.config.SystemConfig;
import com.ypat.enums.YpatPatstyle;
import com.ypat.service.UserServiceClient;
import com.ypat.service.YpatServiceClient;
import com.ypat.third.wxmess.WxMessClient;
import com.ypat.util.FastDFSClient;
import com.ypat.util.GsonUtils;
import com.ypat.util.ImageMarkUtil;
import com.ypat.util.TradeGenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping("/ypat")
public class YpatInfoController {

    private static Logger logger = LoggerFactory.getLogger(YpatInfoController.class);
    @Autowired
    private YpatServiceClient ypatServiceClient;
    @Autowired
    private FastDFSClient fastDFSClient;
    @Autowired
    private SystemConfig systemConfig;
    @Autowired
    private WxMessClient wxMessClient;
    @Autowired
    private ImageMarkUtil imageMarkUtil;
    @Autowired
    private UserServiceClient userServiceClient;

    @RequestMapping("/edit")
    public ModelAndView ypatEdit(Long id, Model model) {
        model.addAttribute("patstyleList", YpatPatstyle.values());
        return new ModelAndView("manage/ypatinfo/edit");
    }

    @PostMapping("/submit")
    @ResponseBody
    public String submit(YpatInfoQo ypatInfoQo, MultipartFile file, MultipartFile[] files) throws IOException {
        logger.info("后台约拍申请输入："+ypatInfoQo);
        if(ypatInfoQo.getPatdate()==null){
            throw new SysException(ResponseCode.FAIL_PARA.getCode(),"patdate不能为空");
        }

        UserQo userQo = new UserQo();
        if (file != null) {
            String fileId = fastDFSClient.uploanFile1(file.getInputStream(), ImageConst.IMAGE_TYPE);
            userQo.setImgpath(SystemConfig.fdfs_path+fileId);
        }
        userQo.setNickname(ypatInfoQo.getNickname());
        userQo.setGender(ypatInfoQo.getGender());
        userQo.setProfess(ypatInfoQo.getProfess());
        userQo.setName("wm");
        userQo.setMobile(TradeGenUtil.genNum(10));
        String userJson = userServiceClient.add(userQo);
        UserQo user = GsonUtils.fromJson(userJson, UserQo.class);

        List<String> picsList = new ArrayList<>();
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                InputStream inputStream = files[i].getInputStream();
                InputStream waterStream = imageMarkUtil.waterMake(inputStream);
                String fileId = fastDFSClient.uploanFile1(waterStream, ImageConst.IMAGE_TYPE);
                picsList.add(SystemConfig.fdfs_path+fileId);
            }
        } else {
            throw new RuntimeException("未上传图片");
        }
        if(picsList.size()<=0) {
            throw new RuntimeException("上传图片格式错误");
        }

        ypatInfoQo.setUserid(user.getId());
        ypatInfoQo.setPics(picsList);
        String res = ypatServiceClient.submit(ypatInfoQo);
        return res;
    }


}
