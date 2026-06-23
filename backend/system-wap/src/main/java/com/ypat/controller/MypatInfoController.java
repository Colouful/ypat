package com.ypat.controller;

import com.ypat.MessInfoQo;
import com.ypat.RecordQo;
import com.ypat.UserQo;
import com.ypat.YpatInfoQo;
import com.ypat.comm.Const;
import com.ypat.enums.MessType;
import com.ypat.service.MessServiceClient;
import com.ypat.service.RecordServiceClient;
import com.ypat.service.UserServiceClient;
import com.ypat.service.YpatServiceClient;
import com.ypat.third.baidu.ai.GsonUtils;
import com.ypat.third.wxmess.WxMessClient;
import com.ypat.util.UserUtil;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@Validated
public class MypatInfoController {

    private static Logger logger = LoggerFactory.getLogger(MypatInfoController.class);
    @Autowired
    private UserServiceClient systemServiceClient;
    @Autowired
    private MessServiceClient messServiceClient;
    @Autowired
    private RecordServiceClient recordServiceClient;
    @Autowired
    private WxMessClient wxMessClient;
    @Autowired
    private YpatServiceClient ypatServiceClient;

    @GetMapping(value = {"/my/ypat/pub/list"})
    public String myPubList(YpatInfoQo ypatInfoQo) {
        if(StringUtils.isEmpty(ypatInfoQo.getUserid())){
            ypatInfoQo.setUserid(Long.parseLong(UserUtil.getUserId()));
        }
        return messServiceClient.myPubList(ypatInfoQo);
    }

    @GetMapping(value = {"/my/ypat/app/list"})
    public String myAppList(MessInfoQo messInfoQo, Long userid) {
        if(StringUtils.isEmpty(userid)){
            messInfoQo.setSendperid(Long.parseLong(UserUtil.getUserId()));;
        }else {
            messInfoQo.setSendperid(userid);
        }
        messInfoQo.setType(MessType.send.value);
        return messServiceClient.myAppList(messInfoQo);
    }

    @GetMapping(value = {"/my/ypat/unread/count"})
    public String myUnreadCount(MessInfoQo messInfoQo) {
        if(UserUtil.getUserId()==null) {
            return null;
        }
        Long userId = Long.parseLong(UserUtil.getUserId());
        return messServiceClient.myUnreadCount(userId);
    }

    @GetMapping(value = {"/my/ypat/rec/unread/count"})
    public String myRecUnreadCount(MessInfoQo messInfoQo) {
        messInfoQo.setRecperid(Long.parseLong(UserUtil.getUserId()));
        messInfoQo.setType(MessType.send.value);//我收到约拍消息-未读
        return messServiceClient.myRecUnreadCount(messInfoQo.getType(), messInfoQo.getRecperid());
    }

    @GetMapping(value = {"/my/ypat/send/unread/count"})
    public String mySendUnreadCount(MessInfoQo messInfoQo) {
        messInfoQo.setRecperid(Long.parseLong(UserUtil.getUserId()));
        messInfoQo.setType(MessType.view.value);//我发送约拍消息-未读
        return messServiceClient.myRecUnreadCount(messInfoQo.getType(), messInfoQo.getRecperid());
    }

    @GetMapping(value = {"/my/ypat/rec/list"})
    public String myRecList(MessInfoQo messInfoQo) {
        messInfoQo.setRecperid(Long.parseLong(UserUtil.getUserId()));
        messInfoQo.setType(MessType.send.value);//我收到约拍消息
        return messServiceClient.myRecList(messInfoQo);
    }

    @GetMapping(value = {"/my/ypat/send/list"})
    public String mySendList(MessInfoQo messInfoQo) {
        messInfoQo.setRecperid(Long.parseLong(UserUtil.getUserId()));
        messInfoQo.setType(MessType.view.value);//我发送约拍消息
        return messServiceClient.mySendList(messInfoQo);
    }

    @GetMapping(value = {"/my/ypat/head/list"})
    public String myHeadList(@NotNull(message = "ypatid不能为空") Long ypatid) {
        MessInfoQo messInfoQo = new MessInfoQo();
        messInfoQo.setType(MessType.send.value);
        messInfoQo.setYpatid(ypatid);
        return messServiceClient.myRecList(messInfoQo);
    }

    @GetMapping(value = {"/my/ypat/sc/list"})
    public String myScList(YpatInfoQo ypatInfoQo) {
        UserQo userQo = ypatInfoQo.getUserQo();
        if(userQo==null) {
            userQo = new UserQo();
            ypatInfoQo.setUserQo(userQo);
        }
        userQo.setId(Long.parseLong(UserUtil.getUserId()));
        return messServiceClient.myScList(ypatInfoQo);
    }

    @RequestMapping(value = "/my/ypat/rec/add", method = {RequestMethod.POST, RequestMethod.PUT})
    public String myRecAdd(MessInfoQo messInfoQo){
        logger.info("发起约拍输入："+messInfoQo);
        long userid = Long.parseLong(UserUtil.getUserId());
        messInfoQo.setSendperid(userid);
        String res = systemServiceClient.myRecAdd(messInfoQo);
        //推送消息
        try {
            String accessToken = wxMessClient.getAccessToken();
            if(accessToken != null) {
                String page = Const.PAGE_MESS;
                String userJson = systemServiceClient.get(userid);
                UserQo userQo = GsonUtils.fromJson(userJson, UserQo.class);
                String ypatJson = ypatServiceClient.get(messInfoQo.getYpatid(), null);
                YpatInfoQo ypatInfoQo = GsonUtils.fromJson(ypatJson, YpatInfoQo.class);

                Map<String,String> contentMap = new HashMap<>();
                contentMap.put("area", ypatInfoQo.getCity());
                contentMap.put("time", DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
                contentMap.put("note", userQo.getNickname()+" 向您发起了约拍");
                wxMessClient.sendMsg(accessToken, ypatInfoQo.getUserQo().getOpenid(), MessType.send, page, contentMap);
            }
        } catch (Exception e) {
            logger.error("消息推送失败：", e);
        }
        return res;
    }

    @RequestMapping(value = "/my/ypat/sc/add", method = {RequestMethod.POST, RequestMethod.PUT})
    public String myScAdd(Long userid, Long ypatid){
        logger.info("收藏输入："+userid+", "+ypatid);
        if(userid==null){
            userid = Long.parseLong(UserUtil.getUserId());
        }
        return systemServiceClient.myScAdd(userid, ypatid);
    }

    @RequestMapping(value = "/my/ppd/sub", method = {RequestMethod.POST, RequestMethod.PUT})
    public String myPpdSub(Long userid){
        return null;
    }

    @GetMapping(value = "/my/ppd/list")
    public String myPpdList(RecordQo recordQo){
        recordQo.setUserid(Long.parseLong(UserUtil.getUserId()));
        return recordServiceClient.findPage(recordQo);
    }

    @GetMapping(value = {"/my/frd/list"})
    public String myFrdList(UserQo userQo) {
        String userStr = systemServiceClient.get(Long.parseLong(UserUtil.getUserId()));
        UserQo userQo1 = GsonUtils.fromJson(userStr, UserQo.class);
        userQo.setRecmobile(userQo1.getMobile());
        return systemServiceClient.findPage(userQo);
    }
}
