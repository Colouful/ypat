package com.ypat.config;

import com.google.gson.Gson;
import com.ypat.SysException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Configuration;
/**
 * @Auther dingyinxin
 * @Date 2021/12/6 16:52
 * @Version 1.0
 */
@Configuration
public class FeignClientErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        String body = null;
        try {
            body = Util.toString(response.body().asReader());
            ExceptionInfo exceptionInfo = new Gson().fromJson(body, ExceptionInfo.class);
            String message = exceptionInfo.getMessage();
            if(message.indexOf("code")>0){
                SysException ex = new Gson().fromJson(message, SysException.class);
                return ex;
            }
        } catch (Exception e) {
            return e;
        }
        return new Exception("系统异常");
    }
}

class ExceptionInfo {
    private String timestamp;
    private String status;
    private String error;
    private String exception;
    private String message;
    private String path;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
