package com.ypat;

public class SysException extends RuntimeException  {

    private int code;
    private String msg;
    private ResponseCode responseCode;

    public SysException(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public SysException(ResponseCode responseCode) {
        super(responseCode.toString());
        this.code = responseCode.getCode();
        this.msg = responseCode.getMsg();
        this.responseCode = responseCode;
    }

    public SysException(ResponseCode responseCode, String msg) {
        super("{code=" + responseCode.getCode() + ", msg='" + msg + "'}");
        this.code = responseCode.getCode();
        this.msg = msg;
        this.responseCode = responseCode;
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
