package com.ypat.third.wxmess;

import com.ypat.comm.Const;
import com.ypat.enums.MessType;

import java.util.Map;

public class WxMess {
    private String touser;
    private String template_id;
    private String page;
    private WxMessData data;

    public static WxMess build(String touser, MessType messType, String page, Map<String, String> contentMap) {
        WxMess wxMess = new WxMess();
        WxMessData data = new WxMessData();
        wxMess.setData(data);
        wxMess.setTouser(touser);
        wxMess.setPage(page);
        switch (messType) {
            case send:
                wxMess.setTemplate_id(Const.TEMP_0);
                String area = contentMap.get("area");
                String time = contentMap.get("time");
                String note = contentMap.get("note");
                WxMessDateContent thing3 = new WxMessDateContent(area);
                WxMessDateContent date2 = new WxMessDateContent(time);
                WxMessDateContent thing4 = new WxMessDateContent(note);
                data.setThing3(thing3);
                data.setDate2(date2);
                data.setThing4(thing4);
                break;
            case oauth:
                wxMess.setTemplate_id(Const.TEMP_1);
                String type = contentMap.get("type");
                String result = contentMap.get("result");
                String note_ = contentMap.get("note");
                WxMessDateContent phrase3 = new WxMessDateContent(type);
                WxMessDateContent phrase1 = new WxMessDateContent(result);
                WxMessDateContent thing2 = new WxMessDateContent(note_);
                data.setPhrase3(phrase3);
                data.setPhrase1(phrase1);
                data.setThing2(thing2);
                break;
            case audit:
                wxMess.setTemplate_id(Const.TEMP_2);
                String content = contentMap.get("content");
                String result_ = contentMap.get("result");
                String time_ = contentMap.get("time");
                String note__ = contentMap.get("note");
                WxMessDateContent thing1 = new WxMessDateContent(content);
                WxMessDateContent phrase2 = new WxMessDateContent(result_);
                WxMessDateContent date3 = new WxMessDateContent(time_);
                WxMessDateContent thing4_ = new WxMessDateContent(note__);
                data.setThing1(thing1);
                data.setPhrase2(phrase2);
                data.setDate3(date3);
                data.setThing4(thing4_);
                break;
            case order:
                wxMess.setTemplate_id(Const.TEMP_3);
                String type_ = contentMap.get("type");
                String per = contentMap.get("per");
                WxMessDateContent thing2_ = new WxMessDateContent(type_);
                WxMessDateContent name3 = new WxMessDateContent(per);
                data.setThing2(thing2_);
                data.setName3(name3);
                break;
            default:
        }
        return wxMess;
    }

    public String getTouser() {
        return touser;
    }

    public void setTouser(String touser) {
        this.touser = touser;
    }

    public String getTemplate_id() {
        return template_id;
    }

    public void setTemplate_id(String template_id) {
        this.template_id = template_id;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public WxMessData getData() {
        return data;
    }

    public void setData(WxMessData data) {
        this.data = data;
    }
}

class WxMessData{
    /**
     * 审核内容, 审核理由
     */
    private WxMessDateContent thing1;
    private WxMessDateContent thing2;
    private WxMessDateContent thing3;
    private WxMessDateContent thing4;
    /**
     * 审核结果
     */
    private WxMessDateContent phrase1;
    private WxMessDateContent phrase2;
    private WxMessDateContent phrase3;
    /**
     * 审核时间
     */
    private WxMessDateContent date1;
    private WxMessDateContent date2;
    private WxMessDateContent date3;
    /**
     * 类型
     */
    private WxMessDateContent name1;
    private WxMessDateContent name2;
    private WxMessDateContent name3;

    public WxMessDateContent getThing1() {
        return thing1;
    }

    public void setThing1(WxMessDateContent thing1) {
        this.thing1 = thing1;
    }

    public WxMessDateContent getThing2() {
        return thing2;
    }

    public void setThing2(WxMessDateContent thing2) {
        this.thing2 = thing2;
    }

    public WxMessDateContent getThing3() {
        return thing3;
    }

    public void setThing3(WxMessDateContent thing3) {
        this.thing3 = thing3;
    }

    public WxMessDateContent getThing4() {
        return thing4;
    }

    public void setThing4(WxMessDateContent thing4) {
        this.thing4 = thing4;
    }

    public WxMessDateContent getPhrase1() {
        return phrase1;
    }

    public void setPhrase1(WxMessDateContent phrase1) {
        this.phrase1 = phrase1;
    }

    public WxMessDateContent getPhrase2() {
        return phrase2;
    }

    public void setPhrase2(WxMessDateContent phrase2) {
        this.phrase2 = phrase2;
    }

    public WxMessDateContent getPhrase3() {
        return phrase3;
    }

    public void setPhrase3(WxMessDateContent phrase3) {
        this.phrase3 = phrase3;
    }

    public WxMessDateContent getDate1() {
        return date1;
    }

    public void setDate1(WxMessDateContent date1) {
        this.date1 = date1;
    }

    public WxMessDateContent getDate2() {
        return date2;
    }

    public void setDate2(WxMessDateContent date2) {
        this.date2 = date2;
    }

    public WxMessDateContent getDate3() {
        return date3;
    }

    public void setDate3(WxMessDateContent date3) {
        this.date3 = date3;
    }

    public WxMessDateContent getName1() {
        return name1;
    }

    public void setName1(WxMessDateContent name1) {
        this.name1 = name1;
    }

    public WxMessDateContent getName2() {
        return name2;
    }

    public void setName2(WxMessDateContent name2) {
        this.name2 = name2;
    }

    public WxMessDateContent getName3() {
        return name3;
    }

    public void setName3(WxMessDateContent name3) {
        this.name3 = name3;
    }
}

class WxMessDateContent{

    private String value;
    public WxMessDateContent(String value) {
        this.value = value;
    }
}
