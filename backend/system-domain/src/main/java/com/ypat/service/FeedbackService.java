package com.ypat.service;

import com.ypat.FeedbackQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.entity.Feedback;
import com.ypat.repository.FeedbackRepository;
import com.ypat.util.CopyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional(rollbackFor = Exception.class)
public class FeedbackService {
    @Autowired
    private FeedbackRepository feedbackRepository;

    public FeedbackQo add(FeedbackQo feedbackQo) {
        if (feedbackQo == null || feedbackQo.getUserid() == null) {
            throw new SysException(ResponseCode.FAIL_AUTH);
        }
        if (StringUtils.isBlank(feedbackQo.getContent())) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        Date now = new Date();
        Feedback feedback = CopyUtil.copy(feedbackQo, Feedback.class);
        feedback.setStatus("0");
        feedback.setCredate(now);
        feedback.setUpddate(now);
        feedbackRepository.save(feedback);
        return CopyUtil.copy(feedback, FeedbackQo.class);
    }
}
