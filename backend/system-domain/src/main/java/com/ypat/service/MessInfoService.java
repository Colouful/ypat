package com.ypat.service;

import com.ypat.*;
import com.ypat.entity.*;
import com.ypat.entity.Record;
import com.ypat.enums.MessagePushEventType;
import com.ypat.enums.MessType;
import com.ypat.enums.RecordType;
import com.ypat.enums.UserImgType;
import com.ypat.enums.YesNo;
import com.ypat.repository.MessInfoRepository;
import com.ypat.repository.RecordRepository;
import com.ypat.repository.UserRepository;
import com.ypat.repository.YpatInfoRepository;
import com.ypat.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.*;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class MessInfoService {
    private static Logger logger = LoggerFactory.getLogger(MessInfoService.class);

    @Autowired
    private MessInfoRepository messInfoRepository;
    @Autowired
    private YpatInfoRepository ypatInfoRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RecordRepository recordRepository;
    @Autowired
    private MessagePushLogService messagePushLogService;

    public void myRecAdd(MessInfoQo messInfoQo){
        Long userid = messInfoQo.getSendperid();
        Long ypatid = messInfoQo.getYpatid();
        if(userid == null){
            throw new SysException(ResponseCode.FAIL_AUTH);
        }
        if(ypatid == null){
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        User user = userRepository.findByIdForUpdate(userid);
        if(user == null){
            throw new SysException(ResponseCode.FAIL_AUTH);
        }
        YpatInfo ypatInfo = ypatInfoRepository.findById(ypatid);
        if(ypatInfo == null){
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        User recper = ypatInfo.getUser();
        if(recper == null){
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        if(userid.equals(recper.getId())){
            throw new SysException(ResponseCode.FAIL_VAL, "不能给自己约拍");
        }
        //对方要求是否实名
        if(YesNo.yes.value.equals(ypatInfo.getRealnameflag())){
            if(!YesNo.yes.value.equals(user.getRealnameflag())){
                throw new SysException(ResponseCode.FAIL_NOREAL);
            }
        }
        //对方要是是否保证金
        if(YesNo.yes.value.equals(ypatInfo.getCreditflag())){
            if(!YesNo.yes.value.equals(user.getCreditflag())){
                throw new SysException(ResponseCode.FAIL_NOCRED);
            }
        }

        //是否重复约拍
        Long hasSend = messInfoRepository.countSend(MessType.send.value, userid, ypatid);
        if(hasSend > 0) {
            throw new SysException(ResponseCode.FAIL_EXIST);
        }

        //拍拍豆是否充足
        if(user.getPpd()< Constant.APPLY_NEED_PPD){
            throw new SysException(ResponseCode.FAIL_BALANCE);
        }

        user.setPpd(user.getPpd()- Constant.APPLY_NEED_PPD);
        userRepository.save(user);

        //增加收支记录
        Record record0 = new Record();
        record0.setCredate(new Date());
        record0.setPpd(-1*Constant.APPLY_NEED_PPD);
        record0.setUserid(user.getId());
        record0.setType(RecordType.APP.value);
        recordRepository.save(record0);

        MessInfo messInfo = new MessInfo();
        messInfo.setCredate(new Date());
        messInfo.setMessviewflag(YesNo.no.value);
        messInfo.setLinkwayflag(YesNo.no.value);
        messInfo.setStatus(YesNo.no.value);
        messInfo.setSendper(user);
        messInfo.setRecper(recper);
        messInfo.setYpatInfo(ypatInfo);
        messInfo.setType(MessType.send.value);
        messInfo.setContent(messInfoQo.getContent());
        messInfoRepository.save(messInfo);
        recordInAppCreated(messInfo);

        //作品的约拍次数+1
        int times = ypatInfo.getPattimes()+1;
        ypatInfo.setPattimes(times);
        ypatInfoRepository.save(ypatInfo);

        //用户的收到的约拍次数+1
        int recTimes = recper.getRectimes()+1;
        recper.setRectimes(recTimes);
        userRepository.save(recper);

    }

    private void recordInAppCreated(MessInfo messInfo) {
        try {
            if (messInfo == null) {
                return;
            }
            MessagePushLogQo qo = new MessagePushLogQo();
            qo.setEventType(MessagePushEventType.IN_APP_CREATED.value);
            qo.setBusinessType(messInfo.getType());
            qo.setMessageId(messInfo.getId());
            if (messInfo.getYpatInfo() != null) {
                qo.setYpatid(messInfo.getYpatInfo().getId());
            }
            if (messInfo.getSendper() != null) {
                qo.setSendperid(messInfo.getSendper().getId());
            }
            if (messInfo.getRecper() != null) {
                qo.setRecperid(messInfo.getRecper().getId());
            }
            qo.setSuccess(YesNo.yes.value);
            qo.setRemark("站内消息创建");
            messagePushLogService.record(qo);
        } catch (Exception e) {
            logger.error("站内消息创建日志记录失败：", e);
        }
    }

    public MessInfo get(Long id){
        return messInfoRepository.findById(id);
    }

    public MessInfoQo findById(Long id, Long userid){
        MessInfo messInfo = get(id);
        if(messInfo==null){
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        MessInfoQo messInfoQo = CopyUtil.copy(messInfo, MessInfoQo.class);
        Long timeMillis = 0L;
        if(messInfoQo.getCredate()!=null){
            timeMillis = System.currentTimeMillis()-messInfoQo.getCredate().getTime();
        }
        messInfoQo.setTimeStr(TimeUtil.getTimeStr(timeMillis));
        if(messInfo.getSendper()!=null) {
            messInfoQo.setSendperid(messInfo.getSendper().getId());
            User sendper = messInfo.getSendper();
            List<UserImg> sendperImgs = sendper.getUserImgs();
            if(!CollectionUtils.isEmpty(sendperImgs)){
                for (UserImg sendperImg : sendperImgs) {
                    if(sendperImg.getType().equals(UserImgType.head.value)){
                        messInfoQo.setImgpath(sendperImg.getImgpath());
                        messInfoQo.setNickname(sendper.getNickname());
                        messInfoQo.setCity(sendper.getCity());
                        break;
                    }
                }
            }
        }
        if(messInfo.getRecper()!=null) {
            messInfoQo.setRecperid(messInfo.getRecper().getId());
        }
        if(messInfo.getYpatInfo()!=null) {
            messInfoQo.setYpatid(messInfo.getYpatInfo().getId());
        }
        if(YesNo.no.value.equals(messInfo.getMessviewflag())) {
            //未读变成已读
            messInfoRepository.updateMessviewflag(YesNo.yes.value, id);
            messInfoQo.setMessviewflag(YesNo.yes.value);
        }
        return messInfoQo;
    }

    public Long count(String type, Long userid) {
        return messInfoRepository.countRec(type, userid);
    }
    public Long countUnread(Long userid) {
        Long recCount = messInfoRepository.countRecUnread(MessType.send.value, userid, YesNo.no.value);
        Long sendCount = messInfoRepository.countRecUnread(MessType.view.value, userid, YesNo.no.value);
        return recCount+sendCount;
    }
    public Long countRecUnread(String type, Long userid) {
        return messInfoRepository.countRecUnread(type, userid, YesNo.no.value);
    }
    public Long countSendUnread(String type, Long userid) {
        return messInfoRepository.countSendUnread(type, userid, YesNo.no.value);
    }

    public Map<String, Object> myAppList(MessInfoQo queryQo) {
        Page<MessInfo> messInfoPage = findPageByPredicate(queryQo);
        List<YpatInfoQo> content = new ArrayList<>();
        if(!CollectionUtils.isEmpty(messInfoPage.getContent())){
            for (MessInfo messInfo : messInfoPage.getContent()) {
                //查询接收人列表
                User recper = messInfo.getRecper();
                UserQo userQo = CopyUtil.copy(recper, UserQo.class);
                List<UserImg> recperImgs = recper.getUserImgs();
                if(!CollectionUtils.isEmpty(recperImgs)){
                    for (UserImg recperImg : recperImgs) {
                        if(recperImg.getType().equals(UserImgType.head.value)){
                            userQo.setImgpath(recperImg.getImgpath());
                            break;
                        }
                    }
                }
                //查询接收人发布的信息
                YpatInfo ypatInfo = messInfo.getYpatInfo();
                YpatInfoQo qo = CopyUtil.copy(ypatInfo, YpatInfoQo.class);
                Long timeMillis = 0L;
                if(qo.getPubdate()!=null){
                    timeMillis = System.currentTimeMillis()-qo.getPubdate().getTime();
                }
                qo.setTimeStr(TimeUtil.getTimeStr(timeMillis));
                qo.setUserQo(userQo);
                //发布的作品
                List<YpatImg> ypatImgs = ypatInfo.getYpatImgs();
                if(!CollectionUtils.isEmpty(ypatImgs)){
                    List<String> pics = new ArrayList<>();
                    for (YpatImg ypatImg : ypatImgs) {
                        pics.add(ypatImg.getImgpath());
                    }
                    qo.setPics(pics);
                }
                content.add(qo);
            }
        }

        Map<String, Object> page = new HashMap<>();
        page.put("content", content);
        page.put("pages", messInfoPage.getTotalPages());
        page.put("totals", messInfoPage.getTotalElements());
        return page;
    }

    public Map<String, Object> findPage(MessInfoQo queryQo) {
        Page<MessInfo> messInfoPage = findPageByPredicate(queryQo);
        List<MessInfoQo> content = new ArrayList<>();
        if(!CollectionUtils.isEmpty(messInfoPage.getContent())){
            for (MessInfo messInfo : messInfoPage.getContent()) {
                MessInfoQo qo = CopyUtil.copy(messInfo, MessInfoQo.class);
                Long timeMillis = 0L;
                if(qo.getCredate()!=null){
                    timeMillis = System.currentTimeMillis()-qo.getCredate().getTime();
                }
                qo.setTimeStr(TimeUtil.getTimeStr(timeMillis));
                qo.setSendperid(messInfo.getSendper().getId());
                qo.setRecperid(messInfo.getRecper().getId());
                qo.setYpatid(messInfo.getYpatInfo().getId());
                //接收人查发送人的列表
                if(!StringUtils.isEmpty(queryQo.getRecperid())){
                    User sendper = messInfo.getSendper();
                    List<UserImg> sendperImgs = sendper.getUserImgs();
                    if(!CollectionUtils.isEmpty(sendperImgs)){
                        for (UserImg sendperImg : sendperImgs) {
                            if("0".equals(sendperImg.getType())){
                                qo.setImgpath(sendperImg.getImgpath());
                                qo.setNickname(sendper.getNickname());
                                qo.setCity(sendper.getCity());
                                break;
                            }
                        }
                    }
                }
                //发送人查接收人的列表
                else if(!StringUtils.isEmpty(queryQo.getSendperid())){
                    User recper = messInfo.getRecper();
                    List<UserImg> recperImgs = recper.getUserImgs();
                    if(!CollectionUtils.isEmpty(recperImgs)){
                        for (UserImg recperImg : recperImgs) {
                            if("0".equals(recperImg.getType())){
                                qo.setImgpath(recperImg.getImgpath());
                                qo.setNickname(recper.getNickname());
                                qo.setCity(recper.getCity());
                                break;
                            }
                        }
                    }
                }
                //作品查询发送人列表
                else if(!StringUtils.isEmpty(queryQo.getYpatid())){
                    User sendper = messInfo.getSendper();
                    List<UserImg> sendperImgs = sendper.getUserImgs();
                    if(!CollectionUtils.isEmpty(sendperImgs)){
                        for (UserImg sendperImg : sendperImgs) {
                            if("0".equals(sendperImg.getType())){
                                qo.setImgpath(sendperImg.getImgpath());
                                qo.setNickname(sendper.getNickname());
                                qo.setCity(sendper.getCity());
                                break;
                            }
                        }
                    }
                }
                content.add(qo);
            }
        }

        Map<String, Object> page = new HashMap<>();
        page.put("content", content);
        page.put("pages", messInfoPage.getTotalPages());
        page.put("totals", messInfoPage.getTotalElements());
        return page;
    }

    /**
     * 分页
     * @param queryQo
     * @return
     */
    public Page<MessInfo> findPageByPredicate(MessInfoQo queryQo){
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable  = new PageRequest(queryQo.getPage(), queryQo.getSize(), sort);
        return messInfoRepository.findAll(new Specification<MessInfo>() {
            @Override
            public Predicate toPredicate(Root<MessInfo> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicatesList = new ArrayList<Predicate>();
                if(CommonUtils.isNotNull(queryQo.getRecperid())){
                    predicatesList.add(criteriaBuilder.equal(root.join("recper",  JoinType.LEFT).get("id"), queryQo.getRecperid()));
                }
                if(CommonUtils.isNotNull(queryQo.getSendperid())){
                    predicatesList.add(criteriaBuilder.equal(root.join("sendper", JoinType.LEFT).get("id"), queryQo.getSendperid()));
                }
                if(CommonUtils.isNotNull(queryQo.getType())){
                    predicatesList.add(criteriaBuilder.equal(root.get("type"), queryQo.getType()));
                }
                if(CommonUtils.isNotNull(queryQo.getYpatid())){
                    predicatesList.add(criteriaBuilder.equal(root.join("ypatInfo",  JoinType.LEFT).get("id"), queryQo.getYpatid()));
                }
                query.where(predicatesList.toArray(new Predicate[predicatesList.size()]));
                return query.getRestriction();
            }
        }, pageable);
    }
}
