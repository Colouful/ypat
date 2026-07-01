package com.ypat.service;

import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.WorkComplainQo;
import com.ypat.entity.Work;
import com.ypat.enums.WorkStatus;
import com.ypat.repository.WorkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 作品投诉服务
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class WorkComplainService {

    @Autowired private WorkRepository workRepository;
    @Autowired private WorkService workService;

    public void complain(WorkComplainQo qo) {
        if (qo == null) throw new SysException(ResponseCode.FAIL_PARA);
        if (qo.getWorkId() == null) throw new SysException(ResponseCode.FAIL_PARA);
        Long workId = Long.parseLong(qo.getWorkId());
        Long userId = Long.parseLong(qo.getUserId());
        Work work = workRepository.findOne(workId);
        if (work == null || WorkStatus.xj.value.equals(work.getStatus()) || work.getDeletedFlag() != null && work.getDeletedFlag() == 1) {
            throw new SysException(ResponseCode.FAIL_WORK_NOT_FOUND);
        }
        if (userId.equals(work.getUserid())) {
            throw new SysException(ResponseCode.FAIL_VAL, "不能投诉自己的作品");
        }
        workService.complain(work, userId, qo.getReason(), qo.getContact());
    }
}
