package com.ypat.service;

import com.ypat.PubEventQo;
import com.ypat.entity.PubEvent;
import com.ypat.repository.PubEventRepository;
import com.ypat.util.CommonUtils;
import com.ypat.util.CopyUtil;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.DateFormat;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class PubEventService {

    @Autowired
    private PubEventRepository pubEventRepository;

    public void save(PubEvent pubEvent){
        String currDate = DateFormatUtils.format(new Date(),"yyyy-MM-dd");
        PubEvent currPubEvent = pubEventRepository.findByDateStrAndEventAndEventKey(currDate, pubEvent.getEvent(), pubEvent.getEventKey());
        if(currPubEvent!=null) {
            int times = currPubEvent.getMsgTimes()+1;
            currPubEvent.setMsgTimes(times);
            pubEventRepository.save(currPubEvent);
        } else {
            pubEvent.setDateStr(currDate);
            pubEvent.setMsgTimes(1);
            pubEventRepository.save(pubEvent);
        }
    }

    public Map<String, Object> findPage(PubEventQo queryQo) {
        Page<PubEvent> pubEventPage = findPageByPredicate(queryQo);
        List<PubEventQo> content = new ArrayList<>();
        if(!CollectionUtils.isEmpty(pubEventPage.getContent())){
            for (PubEvent pubEvent : pubEventPage.getContent()) {
                PubEventQo qo = CopyUtil.copy(pubEvent, PubEventQo.class);
                content.add(qo);
            }
        }

        Map<String, Object> page = new HashMap<>();
        page.put("content", content);
        page.put("totalPages", pubEventPage.getTotalPages());
        page.put("totalElements", pubEventPage.getTotalElements());
        return page;
    }

    /**
     * 分页
     * @param queryQo
     * @return
     */
    public Page<PubEvent> findPageByPredicate(PubEventQo queryQo){
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable  = new PageRequest(queryQo.getPage(), queryQo.getSize(), sort);
        return pubEventRepository.findAll(new Specification<PubEvent>() {
            @Override
            public Predicate toPredicate(Root<PubEvent> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicatesList = new ArrayList<Predicate>();
                if(CommonUtils.isNotNull(queryQo.getEventKey())){
                    predicatesList.add(criteriaBuilder.equal(root.get("eventKey"), queryQo.getEventKey()));
                }
                if(CommonUtils.isNotNull(queryQo.getEvent())){
                    predicatesList.add(criteriaBuilder.equal(root.get("event"), queryQo.getEvent()));
                }
                if(CommonUtils.isNotNull(queryQo.getDateStrStart())){
                    predicatesList.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateStr"), queryQo.getDateStrStart()));
                }
                if(CommonUtils.isNotNull(queryQo.getDateStrEnd())){
                    predicatesList.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateStr"), queryQo.getDateStrEnd()));
                }
                query.where(predicatesList.toArray(new Predicate[predicatesList.size()]));
                return query.getRestriction();
            }
        }, pageable);
    }
}
