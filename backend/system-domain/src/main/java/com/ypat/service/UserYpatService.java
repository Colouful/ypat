package com.ypat.service;

import com.ypat.UserQo;
import com.ypat.YpatInfoQo;
import com.ypat.entity.*;
import com.ypat.repository.UserYpatRepository;
import com.ypat.util.CopyUtil;
import com.ypat.util.MapUtil;
import com.ypat.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserYpatService {
    @Autowired
    private UserYpatRepository userYpatRepository;

    public Map<String, Object> myScList(YpatInfoQo queryQo) {
        Page<UserYpat> userYpatPage = findPageByPredicate(queryQo);
        List<YpatInfoQo> content = new ArrayList<>();
        if(!CollectionUtils.isEmpty(userYpatPage.getContent())){
            for (UserYpat userYpat : userYpatPage.getContent()) {
                YpatInfo ypatInfo = userYpat.getYpatInfo();
                YpatInfoQo qo = CopyUtil.copy(ypatInfo, YpatInfoQo.class);
                Long timeMillis = 0L;
                if(qo.getPubdate()!=null){
                    timeMillis = System.currentTimeMillis()-qo.getPubdate().getTime();
                }
                qo.setTimeStr(TimeUtil.getTimeStr(timeMillis));
                List<YpatImg> ypatImgs = ypatInfo.getYpatImgs();
                if(!CollectionUtils.isEmpty(ypatImgs)){
                    List<String> pics = new ArrayList<>();
                    for (YpatImg ypatImg : ypatImgs) {
                        pics.add(ypatImg.getImgpath());
                    }
                    qo.setPics(pics);
                }
                User user = ypatInfo.getUser();
                if (user!=null) {
                    List<UserImg> userImgs = user.getUserImgs();
                    if(!CollectionUtils.isEmpty(userImgs)){
                        UserQo userQo = new UserQo();
                        userQo.setId(user.getId());
                        userQo.setNickname(user.getNickname());
                        userQo.setProfess(user.getProfess());
                        userQo.setRealnameflag(user.getRealnameflag());
                        userQo.setCreditflag(user.getCreditflag());
                        userQo.setGender(user.getGender());
                        for (UserImg userImg : userImgs) {
                            if("0".equals(userImg.getType())){
                                userQo.setImgpath(userImg.getImgpath());
                                break;
                            }
                        }
                        qo.setUserQo(userQo);
                    }
                }
                content.add(qo);
            }
        }

        Map<String, Object> page = new HashMap<>();
        page.put("content", content);
        page.put("totalPages", userYpatPage.getTotalPages());
        page.put("totalElements", userYpatPage.getTotalElements());
        return page;
    }

    public Page<UserYpat> findPageByPredicate(YpatInfoQo queryQo){
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable  = new PageRequest(queryQo.getPage(), queryQo.getSize(), sort);

        return userYpatRepository.findAll(new Specification<UserYpat>(){
            @Override
            public Predicate toPredicate(Root<UserYpat> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicatesList = new ArrayList<Predicate>();
                predicatesList.add(criteriaBuilder.equal(root.join("user",  JoinType.LEFT).get("id"), queryQo.getUserQo().getId()));
                query.where(predicatesList.toArray(new Predicate[predicatesList.size()]));
                return query.getRestriction();
            }
        }, pageable);
    }
}
