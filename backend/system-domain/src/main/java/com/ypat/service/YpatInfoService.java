package com.ypat.service;


import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.MemberBenefitQuoteQo;
import com.ypat.UserQo;
import com.ypat.YpatInfoQo;
import com.ypat.entity.*;
import com.ypat.entity.Record;
import com.ypat.enums.RecordType;
import com.ypat.enums.UserImgType;
import com.ypat.enums.YesNo;
import com.ypat.enums.YpatPatstyle;
import com.ypat.enums.YpatStatus;
import com.ypat.repository.*;
import com.ypat.util.*;
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
import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class YpatInfoService {
    @Autowired
    private YpatInfoRepository ypatInfoRepository;
    @Autowired
    private YpatImgRepository ypatImgRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RecordRepository recordRepository;
    @Autowired
    private UserYpatRepository userYpatRepository;
    @Autowired
    private MemberService memberService;

    public YpatInfoQo save(YpatInfoQo ypatInfo){
        YpatInfo info = new YpatInfo();
        if(ypatInfo.getId()==null){
            info = CopyUtil.copy(ypatInfo, YpatInfo.class);
            info.setWorkId(parseWorkIdValue(ypatInfo.getWorkId()));
            info.setReadtimes(0);
            info.setPattimes(0);
            info.setColtimes(0);
            info.setStatus(YpatStatus.zc.value);
            info.setUser(userRepository.findById(ypatInfo.getUserid()));
            ypatInfoRepository.save(info);
            ypatInfo.setId(info.getId());
        }else{
            info = get(ypatInfo.getId());
            if(info==null){
                throw new SysException(ResponseCode.FAIL_NOT);
            }
            CopyUtil.copyIgnoreNull(ypatInfo,info);
            if(ypatInfo.getWorkId()!=null){
                info.setWorkId(parseWorkIdValue(ypatInfo.getWorkId()));
            }
            ypatInfoRepository.save(info);
        }
        //保存图片信息
        List<String> pics = ypatInfo.getPics();
        if(pics!=null){
            List<YpatImg> ypatImgs = new ArrayList<>();
            for (String pic : pics) {
                YpatImg ypatImg = new YpatImg();
                ypatImg.setYpatid(info.getId());
                ypatImg.setType("0");
                ypatImg.setImgpath(pic);
                ypatImgs.add(ypatImg);
            }
            ypatImgRepository.deleteByYpatid(info.getId());
            ypatImgRepository.save(ypatImgs);
        }
        return ypatInfo;
    }

    public void submit(YpatInfoQo ypatInfo) {

        //扣除拍拍逗
        User user = userRepository.findById(ypatInfo.getUserid());
        if(user==null){
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        MemberBenefitQuoteQo quote = memberService.quoteBenefit(ypatInfo.getUserid(), "SUBMIT_YPAT");
        int actualPpd = quote.getActualPpd() == null ? Constant.PUB_NEED_PPD : quote.getActualPpd();
        int userPpd = user.getPpd() == null ? 0 : user.getPpd();
        if(userPpd < actualPpd){
            throw new SysException(ResponseCode.FAIL_BALANCE);
        }

        //保存
        this.save(ypatInfo);
        YpatInfo info = get(ypatInfo.getId());
        if(info==null){
            throw new SysException(ResponseCode.FAIL_NOT);
        }

        //修改状态为已提交
        info.setStatus(YpatStatus.ytj.value);
        info.setPubdate(new Date());
        ypatInfoRepository.save(info);

        //发布次数+1, 余额扣除
        user.setPubtimes((user.getPubtimes() == null ? 0 : user.getPubtimes()) + 1);
        user.setPpd(userPpd - actualPpd);
        userRepository.save(user);

        //增加收支记录
        Record record = new Record();
        record.setCredate(new Date());
        record.setPpd(-1 * actualPpd);
        record.setUserid(user.getId());
        record.setType(RecordType.PUB.value);
        recordRepository.save(record);

    }

    public void audit(Long id, String flag, String recomflag, String reason){
        YpatInfo info = get(id);
        if(info==null){
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        info.setStatus(flag);
        if(recomflag!=null){
            info.setRecomflag(recomflag);
        }
        info.setReason(reason);
        ypatInfoRepository.save(info);
    }

    public void delete(Long id){
        ypatInfoRepository.delete(id);
    }

    /**
     * 阅读+1
     * @param ids
     */
    public void readAdd(List<Long> ids){
        if(ids==null){
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        ypatInfoRepository.updateByIds(ids);
    }

    /**
     * 阅读+1
     * @param id
     */
    public void readAdd(Long id){
        YpatInfo ypatInfo = get(id);
        if(ypatInfo==null){
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        int times = ypatInfo.getReadtimes()+1;
        ypatInfo.setReadtimes(times);
        ypatInfoRepository.save(ypatInfo);
    }

    /**
     * 约拍+1
     * @param id
     */
    public void ypatAdd(Long id){
        YpatInfo ypatInfo = get(id);
        int times = ypatInfo.getPattimes()+1;
        ypatInfo.setPattimes(times);
        ypatInfoRepository.save(ypatInfo);
    }

    /**
     * 收藏+1
     * @param id
    public void collAdd(Long id){
        YpatInfo ypatInfo = get(id);
        int times = ypatInfo.getColtimes()+1;
        ypatInfo.setColtimes(times);
        ypatInfoRepository.save(ypatInfo);
    }
     */

    /**
     * 上推荐
     * @param id
     * @param recomflag
     */
    public void upRecom(Long id, String recomflag){
        YpatInfo info = get(id);
        if(info==null){
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        info.setRecomflag(recomflag);
        ypatInfoRepository.save(info);
    }

    public YpatInfoQo findById(Long id, Long userid){
        YpatInfo ypatInfo = get(id);
        if(ypatInfo==null){
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        YpatInfoQo ypatInfoQo = CopyUtil.copy(ypatInfo, YpatInfoQo.class);
        User user = ypatInfo.getUser();
        if(user!=null){
            UserQo userQo = CopyUtil.copy(user, UserQo.class);
            userQo.setMobile("");
            userQo.setWx("");
            userQo.setQq("");
            userQo.setWb("");
            userQo.setCertcode("");
            userQo.setPassword("");
            userQo.setRecmobile("");
            List<UserImg> userImgs = user.getUserImgs();
            if(!CollectionUtils.isEmpty(userImgs)){
                for (UserImg userImg : userImgs) {
                    if(userImg.getType().equals(UserImgType.head.value)){
                        userQo.setImgpath(userImg.getImgpath());
                        break;
                    }
                }
            }
            ypatInfoQo.setUserQo(userQo);
        }
        List<YpatImg> ypatImgs = ypatInfo.getYpatImgs();
        if(!CollectionUtils.isEmpty(ypatImgs)){
            List<String> pics = new ArrayList<>();
            for (YpatImg ypatImg : ypatImgs) {
                pics.add(ypatImg.getImgpath());
            }
            ypatInfoQo.setPics(pics);
        }
        //时间处理
        Long timeMillis = 0L;
        if(ypatInfoQo.getPubdate()!=null){
            timeMillis = System.currentTimeMillis()-ypatInfoQo.getPubdate().getTime();
        }
        ypatInfoQo.setTimeStr(TimeUtil.getTimeStr(timeMillis));
        //是否收藏标识
        if(userid!=null) {
            int count = userYpatRepository.countByUseridAndYpatid(userid, ypatInfo.getId());
            if(count>0){
                ypatInfoQo.setColflag(YesNo.yes.value);
            }else {
                ypatInfoQo.setColflag(YesNo.no.value);
            }
        }
        return ypatInfoQo;
    }

    public YpatInfo get(Long id){
        return ypatInfoRepository.findById(id);
    }

    public Map<String, Object> findPage(YpatInfoQo queryQo) {
        Page<YpatInfo> ypatInfoPage = findPageByPredicate(queryQo);
        List<YpatInfoQo> content = new ArrayList<>();
        if(!CollectionUtils.isEmpty(ypatInfoPage.getContent())){
            List<Long> ids = new ArrayList<>(10);
            for (YpatInfo ypatInfo : ypatInfoPage.getContent()) {
                ids.add(ypatInfo.getId());
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
                    UserQo userQo = new UserQo();
                    userQo.setId(user.getId());
                    userQo.setNickname(user.getNickname());
                    userQo.setProfess(user.getProfess());
                    userQo.setRealnameflag(user.getRealnameflag());
                    userQo.setCreditflag(user.getCreditflag());
                    userQo.setGender(user.getGender());

                    List<UserImg> userImgs = user.getUserImgs();
                    if(!CollectionUtils.isEmpty(userImgs)){
                        for (UserImg userImg : userImgs) {
                            if(userImg.getType().equals(UserImgType.head.value)){
                                userQo.setImgpath(userImg.getImgpath());
                                break;
                            }
                        }
                    }
                    qo.setUserQo(userQo);
                }
                content.add(qo);
            }
            readAdd(ids);
        }

        Map<String, Object> page = new HashMap<>();
        page.put("content", content);
        page.put("totalPages", ypatInfoPage.getTotalPages());
        page.put("totalElements", ypatInfoPage.getTotalElements());
        return page;
    }

    public Page<YpatInfo> findPageByPredicate(YpatInfoQo queryQo){
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable  = new PageRequest(queryQo.getPage(), queryQo.getSize(), sort);
        final Long workId = parseWorkIdFilter(queryQo);

        return ypatInfoRepository.findAll(new Specification<YpatInfo>(){
            @Override
            public Predicate toPredicate(Root<YpatInfo> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicatesList = new ArrayList<Predicate>();
                if(queryQo!=null){
                    Join<YpatInfo, User> joinUser = root.join("user");
                    if(CommonUtils.isNotNull(queryQo.getGender())){
                        predicatesList.add(criteriaBuilder.equal(joinUser.get("gender"), queryQo.getGender()));
                    }
                    if(CommonUtils.isNotNull(queryQo.getRealnameflag())){
                        predicatesList.add(criteriaBuilder.equal(joinUser.get("realnameflag"), queryQo.getRealnameflag()));
                    }
                    if(CommonUtils.isNotNull(queryQo.getCreditflag())){
                        predicatesList.add(criteriaBuilder.equal(joinUser.get("creditflag"), queryQo.getCreditflag()));
                    }
                    if(CommonUtils.isNotNull(queryQo.getUserid())){
                        predicatesList.add(criteriaBuilder.equal(joinUser.get("id"), queryQo.getUserid()));
                    }
                    if(CommonUtils.isNotNull(queryQo.getMobile())){
                        predicatesList.add(criteriaBuilder.equal(joinUser.get("mobile"), queryQo.getMobile()));
                    }
                    if(CommonUtils.isNotNull(queryQo.getNickname())){
                        predicatesList.add(criteriaBuilder.like(joinUser.get("nickname"),"%"+queryQo.getNickname()+"%"));
                    }
                }
                if(CommonUtils.isNotNull(queryQo.getCity())){
                    predicatesList.add(criteriaBuilder.like(root.get("city"), queryQo.getCity()+"%"));
                }
                /**
                 *
                if(CommonUtils.isNotNull(queryQo.getLongitude())){
                    predicatesList.add(criteriaBuilder.ge(root.get("longitude"), queryQo.getLongitude().subtract(BigDecimal.ONE)));
                }
                if(CommonUtils.isNotNull(queryQo.getLatitude())){
                    predicatesList.add(criteriaBuilder.le(root.get("latitude"), queryQo.getLatitude().add(BigDecimal.ONE)));
                }
                 */
                if(CommonUtils.isNotNull(queryQo.getStatus())){
                    predicatesList.add(criteriaBuilder.equal(root.get("status"), queryQo.getStatus()));
                }
                if(CommonUtils.isNotNull(queryQo.getTarget())){
                    predicatesList.add(criteriaBuilder.equal(root.get("target"), queryQo.getTarget()));
                }
                if(CommonUtils.isNotNull(queryQo.getPatstyle())){
                    Set<String> patstyles = normalizePatstyleFilters(queryQo.getPatstyle());
                    List<Predicate> patstylePredicates = new ArrayList<Predicate>();
                    for (String patstyle : patstyles) {
                        patstylePredicates.add(criteriaBuilder.equal(root.get("patstyle"), patstyle));
                        patstylePredicates.add(criteriaBuilder.like(root.get("patstyle"), patstyle + ",%"));
                        patstylePredicates.add(criteriaBuilder.like(root.get("patstyle"), "%," + patstyle));
                        patstylePredicates.add(criteriaBuilder.like(root.get("patstyle"), "%," + patstyle + ",%"));
                    }
                    predicatesList.add(criteriaBuilder.or(patstylePredicates.toArray(new Predicate[patstylePredicates.size()])));
                }
                if(CommonUtils.isNotNull(queryQo.getChargeway())){
                    predicatesList.add(criteriaBuilder.equal(root.get("chargeway"), queryQo.getChargeway()));
                }
                if(CommonUtils.isNotNull(workId)){
                    predicatesList.add(criteriaBuilder.equal(root.get("workId"), workId));
                }
                if(CommonUtils.isNotNull(queryQo.getProfess())){
                    predicatesList.add(criteriaBuilder.notEqual(root.get("target"), queryQo.getProfess()));
                }
                if(CommonUtils.isNotNull(queryQo.getRecomflag())){
                    predicatesList.add(criteriaBuilder.equal(root.get("recomflag"), queryQo.getRecomflag()));
                }
                query.where(predicatesList.toArray(new Predicate[predicatesList.size()]));
                return query.getRestriction();
            }
        }, pageable);
    }

    Long parseWorkIdFilter(YpatInfoQo queryQo) {
        if(CommonUtils.isNotNull(queryQo.getWorkId())){
            return parseWorkIdValue(queryQo.getWorkId());
        }
        return null;
    }

    Long parseWorkIdValue(String rawWorkId) {
        if(CommonUtils.isNotNull(rawWorkId)){
            try {
                Long workId = Long.valueOf(rawWorkId);
                if(workId <= 0){
                    throw new SysException(ResponseCode.FAIL_PARA, "workId参数错误");
                }
                return workId;
            } catch (NumberFormatException e) {
                throw new SysException(ResponseCode.FAIL_PARA, "workId参数错误");
            }
        }
        return null;
    }

    Set<String> normalizePatstyleFilters(String patstyleFilter) {
        Set<String> patstyles = new LinkedHashSet<String>();
        if(CommonUtils.isNotNull(patstyleFilter)){
            for (String rawPatstyle : patstyleFilter.split(",")) {
                String patstyle = rawPatstyle.trim();
                if("".equals(patstyle)){
                    continue;
                }
                if(!patstyle.matches("\\d+") || "".equals(YpatPatstyle.getNameByCode(patstyle))){
                    throw new SysException(ResponseCode.FAIL_PARA, "patstyle参数错误");
                }
                patstyles.add(patstyle);
            }
        }
        if(CollectionUtils.isEmpty(patstyles)){
            throw new SysException(ResponseCode.FAIL_PARA, "patstyle参数错误");
        }
        return patstyles;
    }

}
