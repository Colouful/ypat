package com.ypat.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.comm.Const;
import com.ypat.config.ParamConfig;
import com.ypat.third.baidu.ai.GsonUtils;
import com.ypat.util.JsonReaderUtil;
import com.ypat.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ParamController {

    @Autowired
    private ParamConfig paramConfig;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @GetMapping("/area/list")
    public String areaList() {
        String provinceData = JsonReaderUtil.readJsonData("province.json");
        String cityData = JsonReaderUtil.readJsonData("city.json");
        String areaData = JsonReaderUtil.readJsonData("area.json");
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("provinceData", GsonUtils.fromJson(provinceData, JsonArray.class));
        jsonObject.add("cityData", GsonUtils.fromJson(cityData, JsonArray.class));
        jsonObject.add("areaData", GsonUtils.fromJson(areaData, JsonArray.class));
        return GsonUtils.toJson(jsonObject);
    }

    @GetMapping("/area/list/a")
    public String areaListAll() {
        String areaData = JsonReaderUtil.readJsonData("all.json");
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("areaData", GsonUtils.fromJson(areaData, JsonArray.class));
        return GsonUtils.toJson(jsonObject);
    }

    @GetMapping("/tmplid/list")
    public String tmplidList() {
        JsonArray jsonArray = new JsonArray();
        JsonObject jsonObject0 = new JsonObject();
        jsonObject0.addProperty("id", 0);
        jsonObject0.addProperty("name", "拍摄模板");
        jsonObject0.addProperty("value", Const.TEMP_0);

        JsonObject jsonObject1 = new JsonObject();
        jsonObject1.addProperty("id", 1);
        jsonObject1.addProperty("name", "实名认证审核模板");
        jsonObject1.addProperty("value", Const.TEMP_1);

        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("id", 2);
        jsonObject2.addProperty("name", "发布信息审核模板");
        jsonObject2.addProperty("value", Const.TEMP_2);

        JsonObject jsonObject3 = new JsonObject();
        jsonObject3.addProperty("id", 3);
        jsonObject3.addProperty("name", "新订单通知模板");
        jsonObject3.addProperty("value", Const.TEMP_3);

        jsonArray.add(jsonObject0);
        jsonArray.add(jsonObject1);
        jsonArray.add(jsonObject2);
        jsonArray.add(jsonObject3);
        return GsonUtils.toJson(jsonArray);
    }

    @GetMapping("/param/list")
    public String paramList() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("realname", paramConfig.getRealName());
        return GsonUtils.toJson(jsonObject);
    }

    @GetMapping("/param/set")
    public void setRealName(String flag, String token) {
        if(jwtTokenUtil.validateToken(token)) {
            paramConfig.setRealName(flag);
        }else{
            throw new SysException(ResponseCode.FAIL_NET);
        }
    }
}
