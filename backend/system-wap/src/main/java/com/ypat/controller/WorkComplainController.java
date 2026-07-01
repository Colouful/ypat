package com.ypat.controller;

import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.WorkComplainQo;
import com.ypat.service.WorkComplainServiceClient;
import com.ypat.util.RedisClient;
import com.ypat.util.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkComplainController {
    private static final Logger logger = LoggerFactory.getLogger(WorkComplainController.class);
    private static final long RATE_LIMIT_SECONDS = 60L;

    @Autowired private WorkComplainServiceClient workComplainServiceClient;
    @Autowired private RedisClient redisClient;

    @PostMapping("/work/complain")
    public String complain(WorkComplainQo qo) {
        String userId = UserUtil.getUserId();
        if (userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
        qo.setUserId(userId);
        // 60s 频控
        String rateKey = "work:complain:" + userId;
        try {
            if (redisClient.get(rateKey) != null) {
                throw new SysException(1006, "提交过于频繁，请稍后再试");
            }
            redisClient.put(rateKey, "1", RATE_LIMIT_SECONDS);
        } catch (SysException e) {
            throw e;
        } catch (RuntimeException e) {
            logger.warn("投诉限频 Redis 不可用，userid={}, error={}", userId, e.toString());
        }
        return workComplainServiceClient.complain(qo);
    }
}
