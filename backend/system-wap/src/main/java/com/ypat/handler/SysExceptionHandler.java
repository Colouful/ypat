package com.ypat.handler;

import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

@RestControllerAdvice
public class SysExceptionHandler {
    private static Logger logger = LoggerFactory.getLogger(SysExceptionHandler.class);

    @ExceptionHandler(value = Exception.class)
    public ResponseApiBody exception(HttpServletRequest request, Exception exception) {
        ResponseApiBody apiBody = null;
        logger.error("统一异常：", exception.getMessage());
        if (exception instanceof SysException) {
            SysException ex = (SysException) exception;
            apiBody = new ResponseApiBody(ex.getCode(), ex.getMsg(), null);
        } else if (exception instanceof BindException) {
            BindingResult bindResult = ((BindException) exception).getBindingResult();
            if (bindResult != null && bindResult.hasErrors()) {
                String msg = bindResult.getAllErrors().get(0).getDefaultMessage();
                apiBody = new ResponseApiBody(ResponseCode.FAIL_PARA.getCode(), msg, null);
            }
        } else if (exception instanceof MethodArgumentNotValidException) {
            BindingResult bindResult = ((MethodArgumentNotValidException) exception).getBindingResult();
            if (bindResult != null && bindResult.hasErrors()) {
                String msg = bindResult.getAllErrors().get(0).getDefaultMessage();
                apiBody = new ResponseApiBody(ResponseCode.FAIL_PARA.getCode(), msg, null);
            }
        } else if (exception instanceof ConstraintViolationException) {
            ConstraintViolationException e = (ConstraintViolationException) exception;
            String msg = e.getConstraintViolations().iterator().next().getMessage();
            apiBody = new ResponseApiBody(ResponseCode.FAIL_PARA.getCode(), msg, null);
        } else if (exception instanceof NoHandlerFoundException) {
            apiBody = new ResponseApiBody(ResponseCode.FAIL_NOTFOUND.getCode(), exception.getMessage(), null);
        } else {
            apiBody = new ResponseApiBody(ResponseCode.FAIL_PARA.getCode(), exception.getMessage(), null);
        }
        return apiBody;
    }
}
