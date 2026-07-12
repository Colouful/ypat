package com.ypat;

public class SysException extends RuntimeException  {

    private int code;
    private String msg;
    private ResponseCode responseCode;

    public SysException(int code, String msg) {
        super(buildMessage(code, msg));
        this.code = code;
        this.msg = msg;
    }

    public SysException(ResponseCode responseCode) {
        super(buildMessage(responseCode.getCode(), responseCode.getMsg()));
        this.code = responseCode.getCode();
        this.msg = responseCode.getMsg();
        this.responseCode = responseCode;
    }

    public SysException(ResponseCode responseCode, String msg) {
        super(buildMessage(responseCode.getCode(), msg));
        this.code = responseCode.getCode();
        this.msg = msg;
        this.responseCode = responseCode;
    }

    private static String buildMessage(int code, String msg) {
        StringBuilder message = new StringBuilder();
        message.append("{\"code\":")
                .append(code)
                .append(",\"msg\":");
        if (msg == null) {
            message.append("null");
        } else {
            message.append('"').append(escapeJson(msg)).append('"');
        }
        return message.append('}').toString();
    }

    private static String escapeJson(String value) {
        StringBuilder escaped = new StringBuilder(value.length());
        String hexDigits = "0123456789ABCDEF";
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (ch <= 0x1F) {
                        escaped.append("\\u00")
                                .append(hexDigits.charAt((ch >> 4) & 0x0F))
                                .append(hexDigits.charAt(ch & 0x0F));
                    } else {
                        escaped.append(ch);
                    }
            }
        }
        return escaped.toString();
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
        return "SysException{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
