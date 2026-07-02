package com.ypat.handler;

import com.ypat.ResponseApiBody;
import com.ypat.annotation.NotIntercept;
import com.ypat.third.baidu.ai.GsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;

@RestControllerAdvice
public class SysResultHandler implements ResponseBodyAdvice<Object> {
    private static Logger logger = LoggerFactory.getLogger(SysResultHandler.class);

    private static final Class[] annos = {
            RequestMapping.class,
            GetMapping.class,
            PostMapping.class,
            DeleteMapping.class,
            PutMapping.class
    };

    /**
     * 对所有RestController的接口方法进行拦截
     */
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
//        AnnotatedElement element = returnType.getAnnotatedElement();
//        return Arrays.stream(annos).anyMatch(anno -> anno.isAnnotation() && element.isAnnotationPresent(anno));
        if(returnType.hasMethodAnnotation(NotIntercept.class)) {
            return false;
        }
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if(body instanceof ResponseApiBody) {
            return body;
        } else if (body instanceof String) {
            String jsonStr = (String) body;
            Object resBody = GsonUtils.fromJson(jsonStr, Object.class);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
            // Feign 转发的响应本身已经是 restapi 包装的 {code, msg, res} 结构：直接透传，
            // 避免再包一层导致前端出现双层嵌套（原逻辑一直有这个 bug）
            if (resBody instanceof java.util.Map) {
                java.util.Map<?, ?> map = (java.util.Map<?, ?>) resBody;
                if (map.containsKey("code") && (map.containsKey("res") || map.containsKey("msg"))) {
                    return jsonStr;
                }
            }
            ResponseApiBody apiBody = ResponseApiBody.success(resBody);
            return GsonUtils.toJson(apiBody);
        } else {
            return body;
        }
    }
}
