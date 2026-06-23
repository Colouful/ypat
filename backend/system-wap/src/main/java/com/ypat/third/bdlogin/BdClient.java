package com.ypat.third.bdlogin;

import com.ypat.config.SystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class BdClient {

    private static Logger logger = LoggerFactory.getLogger(BdClient.class);
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private SystemConfig systemConfig;

    public BdClient() {

    }

    public String code2Session(String code) {
        String url = "https://spapi.baidu.com/oauth/jscode2sessionkey";
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<String, Object>();
        paramMap.add("code", code);
        paramMap.add("client_id", systemConfig.getBd_key());
        paramMap.add("sk", systemConfig.getBd_secret());
        ResponseEntity<String> entity = restTemplate.postForEntity(url,paramMap, String.class);
        logger.info("百度根据code获取用户open：" + entity);
        if (entity.getStatusCode().equals(HttpStatus.OK)){
            return entity.getBody();
        }
        return null;
    }

}
