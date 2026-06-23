package com.ypat.model;

public class SecurityResponse {
    private String code;
    private String msg;
    private String token;

    public SecurityResponse(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public SecurityResponse(String code, String msg, String token) {
        this.code = code;
        this.msg = msg;
        this.token = token;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
