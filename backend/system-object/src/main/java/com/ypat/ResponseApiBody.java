package com.ypat;

public class ResponseApiBody {

    private int code;
    private String msg;
    private Object res;

    public ResponseApiBody() {
    }

    public ResponseApiBody(int code, String msg, Object res) {
        this.code = code;
        this.msg = msg;
        this.res = res;
    }

    public ResponseApiBody(ResponseCode responseCode, Object res) {
        this.code = responseCode.getCode();
        this.msg = responseCode.getMsg();
        this.res = res;
    }

    public static ResponseApiBody success(Object res){
        return new ResponseApiBody(ResponseCode.SUCCESS, res);
    }

    public static ResponseApiBody fail(ResponseCode responseCode, Object res){
        return new ResponseApiBody(responseCode, res);
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

    public Object getRes() {
        return res;
    }

    public void setRes(Object res) {
        this.res = res;
    }

    @Override
    public String toString() {
        return "ResponseApiBody{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", res=" + res +
                '}';
    }
}
