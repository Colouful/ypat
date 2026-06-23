package com.ypat.config;

import com.google.gson.Gson;
import com.ypat.SysException;
import com.ypat.handler.SysExceptionHandler;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientErrorDecoder implements ErrorDecoder {

    private static Logger logger = LoggerFactory.getLogger(FeignClientErrorDecoder.class);
    @Override
    public Exception decode(String methodKey, Response response) {
        String body = null;
        try {
            body = Util.toString(response.body().asReader());
            ExceptionInfo exceptionInfo = new Gson().fromJson(body, ExceptionInfo.class);
            String message = exceptionInfo.getMessage();
            logger.error("feign body :",body);
            if(message.indexOf("code")>0){
                SysException ex = new Gson().fromJson(message, SysException.class);
                logger.error("feign ex :",ex);
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