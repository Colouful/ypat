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
    FAIL_SER(500, "内部错误");

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
