package com.ypat.controller;

import com.ypat.OauthQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.UserQo;
import com.ypat.comm.Const;
import com.ypat.comm.ImageConst;
import com.ypat.config.SystemConfig;
import com.ypat.enums.MessType;
import com.ypat.enums.UserStatus;
import com.ypat.service.OauthServiceClient;
import com.ypat.service.UserServiceClient;
import com.ypat.third.baidu.ai.GsonUtils;
import com.ypat.third.baidu.ai.Idcard;
import com.ypat.third.baidu.ai.IdcardResponse;
import com.ypat.third.baidu.ai.Idmatch;
import com.ypat.third.wxmess.WxMessClient;
import com.ypat.util.FastDFSClient;
import com.ypat.util.UserUtil;
import org.apache.commons.codec.binary.Base64;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Validated
public class OauthController {

    private static Logger logger = LoggerFactory.getLogger(OauthController.class);
    @Autowired
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private FastDFSClient fastDFSClient;
    @Autowired
    private Idcard idcard;
    @Autowired
    private Idmatch idmatch;
    @Autowired
    private SystemConfig systemConfig;
    @Autowired
    private WxMessClient wxMessClient;
    @Autowired
    private UserServiceClient userServiceClient;

    @PostMapping("/oauth/ocr")
    public String ocr(String cardfront) {
        if (!StringUtils.isEmpty(cardfront)) {
            final String[] picsArr = cardfront.split(",");
            IdcardResponse response = idcard.idcard(picsArr[1], "front");
            if(response ==null){
                throw new SysException(ResponseCode.FAIL_OCR);
            }
            if(response.getError_code()==17){
                throw new SysException(ResponseCode.FAIL_LIMIT);
            }
            if(!"normal".equals(response.getImage_status())){
                throw new SysException(ResponseCode.FAIL_OCR);
            }
            OauthQo qo = new OauthQo();
            qo.setUserid(Long.parseLong(UserUtil.getUserId()));
            qo.setCertcode(response.getCert_words());
            qo.setName(response.getName_words());
            return GsonUtils.toJson(qo);
        } else {
            throw new RuntimeException("未上传证件照");
        }
    }

    @PostMapping("/oauth/add")
    public String add(@Valid OauthQo oauthQo) {
        logger.info("实名认证申请输入："+oauthQo);
        List<String> picsList = new ArrayList<>();
        List<String> pics = oauthQo.getPics();
        if (pics != null && pics.size() > 0) {
            for (int i = 0; i < pics.size(); i++) {
                String fileBase64 = pics.get(i);
                // 保存文件
                String[] picsArr = fileBase64.split(",", 2);
                String imageBody = picsArr.length == 2 ? picsArr[1] : fileBase64;
                byte[] bytes = Base64.decodeBase64(imageBody);
                String fileId = fastDFSClient.uploanFile1(new ByteArrayInputStream(bytes), ImageConst.IMAGE_TYPE);
                picsList.add(systemConfig.getFdfs_path()+fileId);
            }
        } else {
            throw new RuntimeException("未上传证件照");
        }

        if(picsList.size()<=0) {
            throw new RuntimeException("上传证件照格式错误");
        }
        OauthQo qo = new OauthQo();
        qo.setPics(picsList);
        qo.setUserid(Long.parseLong(UserUtil.getUserId()));
        qo.setCertcode(oauthQo.getCertcode());
        qo.setName(oauthQo.getName());
        oauthServiceClient.add(qo);
        //推送消息
        try {
            String accessToken = wxMessClient.getAccessToken();
            if(accessToken != null) {
                String page = "";
                Map<String,String> contentMap = new HashMap<>();
                contentMap.put("type", "实名待审批");
                contentMap.put("per",  "去拍");
                wxMessClient.sendMsg(accessToken, Const.SYS_ADMIN, MessType.order, page, contentMap);
            }
        } catch (Exception e) {
            logger.error("消息推送失败：", e);
        }
        return GsonUtils.toJson(qo);
    }

    @RequestMapping(value = "/oauth/add2", method = {RequestMethod.POST, RequestMethod.PUT})
    public void add2(@RequestParam MultipartFile[] pics) throws Exception{
        List<String> picsList = new ArrayList<>();
        if (pics != null && pics.length > 0) {
            IdcardResponse response = idcard.idcard(pics[0].getInputStream(), "front");
            if(response ==null || !"normal".equals(response.getImage_status())){
                throw new SysException(ResponseCode.FAIL_REALNAME);
            }
            for (int i = 0; i < pics.length; i++) {
                MultipartFile uploadfile = pics[i];
                // 保存文件
                String fileId = fastDFSClient.uploanFile1(uploadfile.getInputStream(), uploadfile.getOriginalFilename());
                picsList.add(fileId);
            }
            OauthQo qo = new OauthQo();
            qo.setPics(picsList);
            qo.setUserid(Long.parseLong(UserUtil.getUserId()));
            qo.setCertcode(response.getCert_words());
            qo.setName(response.getName_words());
            oauthServiceClient.add(qo);
        }
    }

    @GetMapping("/oauth/get")
    public String get() {
        Long id = Long.parseLong(UserUtil.getUserId());
        return oauthServiceClient.get(id);
    }

    @GetMapping("/oauth/getById")
    public String getById(@NotEmpty(message = "id不能为空") Long id) {
        return oauthServiceClient.get(id);
    }

    @GetMapping("/oauth/detail")
    public String userDetail(Long id) {
        return oauthServiceClient.getAuth(id);
    }

    @PostMapping("/oauth/audit")
    public String userAudit(Long id, String flag) {
        String res = oauthServiceClient.audit(id, flag);
        try {
            String accessToken = wxMessClient.getAccessToken();
            if(accessToken != null) {
                String page = "";
                String userJson = userServiceClient.get(id);
                UserQo userQo = GsonUtils.fromJson(userJson, UserQo.class);
                Map<String,String> contentMap = new HashMap<>();
                contentMap.put("type", "实名认证");
                if(UserStatus.shtg.value.equals(flag)) {
                    page = Const.PAGE_REALNAME_TG;
                    contentMap.put("result",UserStatus.shtg.name);
                    contentMap.put("note","赶紧找到心仪的小伙伴拍起来吧~");
                } else {
                    page = Const.PAGE_REALNAME_BTG;
                    contentMap.put("result",UserStatus.shbtg.name);
                    contentMap.put("note","填写信息有误，请认证填写哦~");
                }
                wxMessClient.sendMsg(accessToken, userQo.getOpenid(), MessType.oauth, page, contentMap);
            }
        } catch (Exception e) {
            logger.error("消息推送失败：", e);
        }
        return res;
    }

}
