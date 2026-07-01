package com.ypat;

public enum ResponseCode {
    SUCCESS(200, "成功"),
    FAIL_AUTH(1001, "鉴权失败"),
    FAIL_PARA(1002, "参数错误"),
    FAIL_JSON(1003, "json错误"),
    FAIL_ERRS(1004, "非法字符串"),
    FAIL_NOT(1005, "数据不存在"),
    FAIL_EXIST(1006, "数据已存在"),
    FAIL_OCR(1008, "识别失败"),
    FAIL_BALANCE(1009, "余额不足"),
    FAIL_NOREAL(1010, "未实名"),
    FAIL_NOCRED(1011, "未缴纳保证金"),
    FAIL_PASSWORD(1012, "密码错误"),
    FAIL_REALNAME(1013, "实名失败"),
    FAIL_LIMIT(1014, "识别超限"),
    FAIL_MARK(1015, "水印失败"),
    FAIL_WX(2001, "通信失败"),
    FAIL_ORDER(2002, "下单失败"),
    FAIL_PAY(2003, "支付失败"),
    FAIL_NET(401, "token无效"),
    FAIL_VAL(403, "无权限"),
    FAIL_NOTFOUND(404, "未找到"),
    FAIL_SER(500, "内部错误"),
    // 4001~4011 作品模块错误码
    FAIL_VIDEO_OVERSIZE(4001, "视频大小不能超过 200MB"),
    FAIL_IMG_OVERCOUNT(4002, "最多只能上传九张图片"),
    FAIL_IMG_VIDEO_MIX(4003, "图片和视频不能同时上传"),
    FAIL_FILE_TYPE(4004, "文件格式不支持"),
    FAIL_UPLOAD(4005, "文件上传失败，请重试"),
    FAIL_IMG_TOTAL_OVERSIZE(4006, "图片总大小不能超过 100MB"),
    FAIL_VIDEO_COUNT(4007, "视频只能上传一个"),
    FAIL_TAG_OVERCOUNT(4008, "主题标签最多选择 5 个"),
    FAIL_DESC_SENSITIVE(4009, "描述包含敏感联系方式"),
    FAIL_WORK_NOT_FOUND(4010, "作品不存在或已下架"),
    FAIL_WORK_FORBIDDEN(4011, "无权操作该作品");

    private int code;
    private String msg;

    ResponseCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
