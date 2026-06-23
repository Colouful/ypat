package com.ypat.controller;

import com.ypat.*;
import com.ypat.comm.Const;
import com.ypat.enums.MessType;
import com.ypat.enums.UserStatus;
import com.ypat.enums.YesNo;
import com.ypat.enums.YpatStatus;
import com.ypat.model.SecurityUserDetails;
import com.ypat.service.*;
import com.ypat.third.wxmess.WxMessClient;
import com.ypat.util.GsonUtils;
import com.ypat.util.MapUtils;
import com.ypat.util.SmsUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/manage")
public class ManageController {
    private static Logger logger = LoggerFactory.getLogger(ManageController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private YpatServiceClient ypatServiceClient;
    @Autowired
    private ProductServiceClient productServiceClient;
    @Autowired
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private UserServiceClient userServiceClient;
    @Autowired
    private WxMessClient wxMessClient;
    @Autowired
    private OrderServiceClient orderServiceClient;
    @Autowired
    private MessServiceClient messServiceClient;
    @Resource(name="thymeleafViewResolver")
    ThymeleafViewResolver thymeleafViewResolver;

    @RequestMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/")
    public String loginPage(HttpServletRequest request){
        SecurityUserDetails user = (SecurityUserDetails) request.getSession().getAttribute(Const.USER_SESSION_KEY);
        if (user!=null) {
            return "home";
        }
        return "login";
    }

    @PostMapping("/login")
    public ModelAndView login(UserQo userQo, Model model, HttpServletRequest request) {
        try{
            SecurityUserDetails userDetails = userService.manageLogin(userQo);
            request.getSession().setAttribute(Const.USER_SESSION_KEY, userDetails);
            return new ModelAndView("home");
        }catch (SysException e) {
            model.addAttribute("errorMsg", e.getMsg());
            return new ModelAndView("login");
        }
    }

    @RequestMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/manage/";
    }

    /**
     * 审核系统首页
     * @return
     */
    @RequestMapping("/index")
    public String index() {
        setSystemFlag(Const.SYSFLAG_AUDIT);
        return "manage/index";
    }

    @RequestMapping("/detail")
    public ModelAndView detail(Long id, ModelMap model) {
        String json = ypatServiceClient.get(id, null);
        YpatInfoQo qo = GsonUtils.fromJson(json, YpatInfoQo.class);
        model.addAttribute("detail", qo);
        return new ModelAndView("manage/detail");
    }

    @RequestMapping("/list")
    @ResponseBody
    public String list(YpatInfoQo ypatInfoQo) {
        return ypatServiceClient.findPage(ypatInfoQo);
    }

    @RequestMapping("/audit")
    @ResponseBody
    public String audit(Long id, String flag, String recomflag, String reason, String messflag){
        String res = ypatServiceClient.audit(id, flag, recomflag, reason);
        try {
            String accessToken = wxMessClient.getAccessToken();
            if(accessToken != null) {
                String page = "";
                String ypatJson = ypatServiceClient.get(id, null);
                YpatInfoQo ypatInfoQo = GsonUtils.fromJson(ypatJson, YpatInfoQo.class);
                Map<String,String> contentMap = new HashMap<>();
                contentMap.put("time", DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
                String content = ypatInfoQo.getDescrib();
                if(!StringUtils.isEmpty(content)){
                    if(content.length()>=10){
                        content = content.substring(0,10)+"...";
                    }
                }
                contentMap.put("content", content);
                if(YpatStatus.shtg.value.equals(flag)) {
                    page = Const.PAGE_PUB_TG;
                    contentMap.put("result", YpatStatus.shtg.name);
                    contentMap.put("note", "无");
                } else {
                    page = Const.PAGE_PUB_BTG;
                    contentMap.put("result", YpatStatus.shbtg.name);
                    if(StringUtils.isEmpty(reason)){
                        contentMap.put("note", "发布信息有误，请重新填写");
                    }else{
                        if(reason.length()>=20){
                            reason = reason.substring(0,17)+"...";
                        }
                        contentMap.put("note", reason);
                    }
                }
                //微信消息推送
                wxMessClient.sendMsg(accessToken, ypatInfoQo.getUserQo().getOpenid(), MessType.audit, page, contentMap);

                //短信推送
                if(YpatStatus.shtg.value.equals(flag) && YesNo.yes.value.equals(messflag)) {
                    String mobileJson = userServiceClient.findByCityAndProfess(ypatInfoQo.getUserQo().getId(), ypatInfoQo.getCity());
                    logger.info("发送短信："+mobileJson);
                    Map<String, Object> mobileMap = GsonUtils.fromJson(mobileJson, Map.class);
                    SmsUtils.sendMsg(MapUtils.mapKey2Str(mobileMap), ypatInfoQo.getTargetTxt());
                }
            }
        } catch (Exception e) {
            logger.error("消息推送失败：", e);
        }
        return res;
    }

    @PostMapping("/upRecom")
    @ResponseBody
    public String upRecom(Long id, String recomflag) {
        return ypatServiceClient.upRecom(id, recomflag);
    }

    @RequestMapping("/product/index")
    public String productIndex(Model model) {
        return "manage/product/index";
    }

    @RequestMapping("/product/edit")
    public ModelAndView productEdit(Long id, Model model) {
        String json = productServiceClient.get(id);
        ProductQo qo = GsonUtils.fromJson(json, ProductQo.class);
        model.addAttribute("product", qo!=null ? qo : new ProductQo());
        return new ModelAndView("manage/product/edit");
    }

    @RequestMapping("/product/get")
    @ResponseBody
    public String get(Long id) {
        return productServiceClient.get(id);
    }

    @PostMapping("/product/save")
    @ResponseBody
    public String add(ProductQo productQo) {
        return productServiceClient.add(productQo);
    }

    @PostMapping("/product/upDown")
    @ResponseBody
    public String upDown(ProductQo productQo) {
        return productServiceClient.upDown(productQo);
    }

    @RequestMapping("/product/list")
    @ResponseBody
    public String findPage(ProductQo productQo) {
        return productServiceClient.findPage(productQo);
    }

    @RequestMapping("/user/index")
    public String userIndex(Model model) {
        return "manage/user/index";
    }

    @RequestMapping("/user/detail")
    public ModelAndView userDetail(Long id, ModelMap model) {
        String json = oauthServiceClient.getAuth(id);
        OauthQo qo = GsonUtils.fromJson(json, OauthQo.class);
        model.addAttribute("user", qo!=null ? qo : new OauthQo());
        return new ModelAndView("manage/user/detail");
    }

    @RequestMapping("/user/list")
    @ResponseBody
    public String findPage(UserQo userQo) {
        return userServiceClient.findPage(userQo);
    }

    @RequestMapping("/user/audit")
    @ResponseBody
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

    /**
     * 查询系统首页
     * @return
     */
    @RequestMapping("/query/index")
    public String userindex() {
        setSystemFlag(Const.SYSFLAG_QUERY);
        return "manage/query/userindex";
    }

    @RequestMapping("/query/ypat/appindex")
    public String ypatappindex() {
        return "manage/query/ypatappindex";
    }

    @RequestMapping("/query/mess/messindex")
    public String messindex() {
        return "manage/query/messindex";
    }

    @RequestMapping("/mess/list")
    @ResponseBody
    public String messList(MessInfoQo messInfoQo) {
        return messServiceClient.findPage(messInfoQo);
    }

    /**
     * 订单系统首页
     * @return
     */
    @RequestMapping("/order/index")
    public String orderIndex() {
        setSystemFlag(Const.SYSFLAG_ORDER);
        return "manage/order/index";
    }

    @RequestMapping("/order/list")
    @ResponseBody
    public String orderList(OrderQo orderQo) {
        return orderServiceClient.findPage(orderQo);
    }

    private void setSystemFlag(String flag) {
        Map<String, Object> vars = new HashMap<>();
        vars.put(Const.SYSFLAG, flag);
        thymeleafViewResolver.setStaticVariables(vars);
    }
}
