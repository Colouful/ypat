package com.ypat.third.baidu.ai;

public class IdcardResponse {
    private String image_status;
    private String cert_words;
    private String name_words;
    private int error_code = 0;
    private String error_msg;

    public String getImage_status() {
        return image_status;
    }

    public void setImage_status(String image_status) {
        this.image_status = image_status;
    }

    public String getCert_words() {
        return cert_words;
    }

    public void setCert_words(String cert_words) {
        this.cert_words = cert_words;
    }

    public String getName_words() {
        return name_words;
    }

    public void setName_words(String name_words) {
        this.name_words = name_words;
    }

    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    public String getError_msg() {
        return error_msg;
    }

    public void setError_msg(String error_msg) {
        this.error_msg = error_msg;
    }
}
