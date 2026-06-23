package com.ypat.service;

import com.ypat.RecordQo;
import com.ypat.entity.Record;
import com.ypat.repository.RecordRepository;
import com.ypat.util.CommonUtils;
import com.ypat.util.CopyUtil;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dingyinxin
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class RecordService {

    @Autowired
    private RecordRepository recordRepository;

    public void save(Record record){
        recordRepository.save(record);
    }

    public Record get(Long id){
        return recordRepository.findById(id);
    }

    public RecordQo findById(Long id){
        Record record = get(id);
        return CopyUtil.copy(record, RecordQo.class);
    }

    public Map<String, Object> findPage(RecordQo queryQo) {
        Page<Record> recordPage = findPageByPredicate(queryQo);
        List<RecordQo> content = new ArrayList<>();
        if(!CollectionUtils.isEmpty(recordPage.getContent())){
            for (Record order : recordPage.getContent()) {
                RecordQo qo = CopyUtil.copy(order, RecordQo.class);
                content.add(qo);
            }
        }

        Map<String, Object> page = new HashMap<>();
        page.put("content", content);
        page.put("pages", recordPage.getTotalPages());
        page.put("totals", recordPage.getTotalElements());
        return page;
    }

    /**
     * 分页
     * @param queryQo
     * @return
     */
    public Page<Record> findPageByPredicate(RecordQo queryQo){
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable  = new PageRequest(queryQo.getPage(), queryQo.getSize(), sort);
        return recordRepository.findAll(new Specification<Record>() {
            @Override
            public Predicate toPredicate(Root<Record> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicatesList = new ArrayList<Predicate>();
                predicatesList.add(criteriaBuilder.equal(root.get("userid"), queryQo.getUserid()));
                if(CommonUtils.isNotNull(queryQo.getType())){
                    predicatesList.add(criteriaBuilder.equal(root.get("type"), queryQo.getType()));
                }
                query.where(predicatesList.toArray(new Predicate[predicatesList.size()]));
                return query.getRestriction();
            }
        }, pageable);
    }
}
