package com.ypat.service;


import com.ypat.*;
import com.ypat.entity.*;
import com.ypat.entity.Record;
import com.ypat.enums.*;
import com.ypat.repository.*;
import com.ypat.util.CommonUtils;
import com.ypat.util.Constant;
import com.ypat.util.CopyUtil;
import com.ypat.util.InviteCodeCodec;
import com.ypat.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {
    private static Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final int REALNAME_PHOTO_COUNT = 3;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserYpatRepository userYpatRepository;
    @Autowired
    private UserImgRepository userImgRepository;
    @Autowired
    private MessInfoRepository messInfoRepository;
    @Autowired
    private YpatInfoRepository ypatInfoRepository;
    @Autowired
    private RecordRepository recordRepository;
    @Autowired
    private UserOrigRepository userOrigRepository;
    @Autowired
    private InviteService inviteService;
    @Autowired
    private MessagePushLogService messagePushLogService;

    /**
     *
     * @param id
     * @param flag
     */
    public void audit(Long id, String flag){
        User info = get(id);
        if(info==null){
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        info.setStatus(flag);
        if(flag.equals(UserStatus.shtg.value)){
            info.setRealnameflag(YesNo.yes.value);
        }
        userRepository.save(info);
    }

    /**
     * 实名
     * @param oauthQo
     */
    public void oauth(OauthQo oauthQo){
        User old = userRepository.findByIdForUpdate(oauthQo.getUserid());
        if(old==null){
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        if(YesNo.yes.value.equals(old.getRealnameflag())){
            throw new SysException(1007,"已经实名");
        }
        List<String> pics = oauthQo.getPics();
        if(pics == null || pics.size() != REALNAME_PHOTO_COUNT){
            throw new SysException(ResponseCode.FAIL_REALNAME);
        }
        boolean canSubmit = UserStatus.zfcg.value.equals(old.getStatus()) || UserStatus.shbtg.value.equals(old.getStatus());
        if(!canSubmit){
            throw new SysException(ResponseCode.FAIL_NOREAL);
        }
        //保存信息
        old.setName(oauthQo.getName());
        old.setCertcode(oauthQo.getCertcode());
        old.setRealnameflag(YesNo.no.value);
        old.setStatus(UserStatus.ytj.value);
        old.setRealnameSubmitAt(new Date());
        userRepository.save(old);
        //删除历史证件
        List<UserImg> oldUserImgs = old.getUserImgs();
        if(!CollectionUtils.isEmpty(oldUserImgs)){
            for (UserImg userImg : oldUserImgs) {
                if(userImg.getType().equals(UserImgType.head.value)){
                    continue;
                }else{
                    userImgRepository.delete(userImg);
                }
            }
        }
        //保存证件信息
        if(pics!=null){
            List<UserImg> userImgs = new ArrayList<>();
            for (int i = 0,j=1; i < pics.size(); i++) {
                UserImg userImg = new UserImg();
                userImg.setUserid(oauthQo.getUserid());
                userImg.setType((j++)+"");
                userImg.setImgpath(pics.get(i));
                userImgs.add(userImg);
            }
            userImgRepository.save(userImgs);
        }

    }

    /**
     * 更新
     * @param userQo
     */
    public UserQo save(UserQo userQo){
        if(userQo.getId()==null){
            User user = CopyUtil.copy(userQo, User.class);
            if(StringUtils.isEmpty(user.getMobile())){
                throw new SysException(ResponseCode.FAIL_PARA);
            }
            UserQo oldUser = findByMobile(user.getMobile());
            if(oldUser!=null) {
                throw new SysException(ResponseCode.FAIL_EXIST);
            }

            user.setPubtimes(0);
            user.setRectimes(0);
            user.setColtimes(0);
            user.setPpd(Constant.SYS_NEED_PPD);
            user.setRealnameflag(YesNo.no.value);
            user.setCreditflag(YesNo.no.value);
            user.setRegisdate(new Date());
            user.setStatus(UserStatus.zc.value);
            userRepository.save(user);

            //增加收支记录
            Record record0 = new Record();
            record0.setCredate(new Date());
            record0.setPpd(Constant.SYS_NEED_PPD);
            record0.setUserid(user.getId());
            record0.setType(RecordType.SYS.value);
            recordRepository.save(record0);

            //保存头像信息
            String imgpath = userQo.getImgpath();
            if(!StringUtils.isEmpty(imgpath)){
                UserImg userImg = new UserImg();
                userImg.setUserid(user.getId());
                userImg.setType(UserImgType.head.value);
                userImg.setImgpath(imgpath);
                userImgRepository.save(userImg);
            }

            //增加来源
            String channel = userQo.getChannel();
            if(StringUtils.isEmpty(channel)) {
                channel = UserOrigType.wx.value;
            }
            UserOrig userOrig = new UserOrig();
            userOrig.setUserid(user.getId());
            userOrig.setOpenid(user.getOpenid());
            userOrig.setType(channel);
            userOrig.setCredate(new Date());
            userOrigRepository.save(userOrig);

            //推荐人增加拍拍豆 - 优先解析 inviteCode（新版安全邀请码），退化到 recmobile（旧版兼容）
            Long inviterId = null;
            String inviteCodeUsed = null;
            String inviteSource = userQo.getInviteSource();
            if(!StringUtils.isEmpty(userQo.getInviteCode())){
                Long decoded = InviteCodeCodec.decode(userQo.getInviteCode());
                if(decoded != null && !decoded.equals(user.getId())){
                    User inviter = userRepository.findOne(decoded);
                    if(inviter != null){
                        inviterId = inviter.getId();
                        inviteCodeUsed = userQo.getInviteCode();
                    }
                }
            }
            if(inviterId == null && !StringUtils.isEmpty(userQo.getRecmobile())){
                UserQo recUserQo = findByMobile(userQo.getRecmobile());
                if(recUserQo != null && !recUserQo.getId().equals(user.getId())){
                    inviterId = recUserQo.getId();
                    if(StringUtils.isEmpty(inviteSource)) inviteSource = "recmobile";
                }
            }
            if(inviterId != null){
                InviteConfigQo inviteConfig = inviteService.getConfig();
                Integer rewardPpd = inviteConfig.getRewardPpd() == null ? Constant.INVITE_NEED_PPD : inviteConfig.getRewardPpd();
                boolean inviteEnabled = InviteService.INVITE_ENABLED.equals(inviteConfig.getEnabled());
                if(inviteEnabled){
                    InviteService.BindRelationResult bindResult =
                            inviteService.bindRelationIfAbsent(inviterId, user.getId(), inviteCodeUsed, inviteSource, rewardPpd);
                    if(bindResult.isCreated() && rewardPpd > 0){
                        User recUser = get(inviterId);
                        recUser.setPpd(recUser.getPpd() + rewardPpd);
                        userRepository.save(recUser);
                        //增加收支记录
                        Record record = new Record();
                        record.setCredate(new Date());
                        record.setPpd(rewardPpd);
                        record.setUserid(recUser.getId());
                        record.setType(RecordType.FRI.value);
                        recordRepository.save(record);
                    }
                }
            }
            userQo.setId(user.getId());
            return userQo;
        }else{
            User old = get(userQo.getId());
            if(old==null){
                throw new SysException(ResponseCode.FAIL_NOT);
            }
            CopyUtil.copyIgnoreNull(userQo,old);
            userRepository.save(old);

            //保存头像信息
            String imgpath = userQo.getImgpath();
            if(!StringUtils.isEmpty(imgpath)){
                List<UserImg> userImgs = old.getUserImgs();
                if(!CollectionUtils.isEmpty(userImgs)){
                    for (UserImg userImg : userImgs) {
                        if(userImg.getType().equals(UserImgType.head.value)){
                            userImg.setImgpath(imgpath);
                            userImgRepository.save(userImg);
                            break;
                        }
                    }
                }else {
                    UserImg userImg = new UserImg();
                    userImg.setUserid(userQo.getId());
                    userImg.setType(UserImgType.head.value);
                    userImg.setImgpath(imgpath);
                    userImgRepository.save(userImg);
                }
            }
            return userQo;
        }
    }

    /**
     * 收到约拍、请求约拍
     * @param id
     */
    public void myRecAdd(Long id){
        User user = get(id);
        if(user==null){
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        user.setRectimes(user.getRectimes()+1);
        userRepository.save(user);
    }

    /**
     * 收藏
     */
    public void myColAdd(Long userid, Long ypatid){
        //我的收藏+1
        User user = get(userid);
        if(user==null){
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        int count = userYpatRepository.countByUseridAndYpatid(userid, ypatid);
        if(count>0){
            throw new SysException(ResponseCode.FAIL_EXIST);
        }
        user.setColtimes(user.getColtimes()+1);
        userRepository.save(user);

        //作品被收藏+1
        YpatInfo ypatInfo = ypatInfoRepository.findById(ypatid);
        int times = ypatInfo.getColtimes()+1;
        ypatInfo.setColtimes(times);
        ypatInfoRepository.save(ypatInfo);

        //保存关系
        UserYpat userYpat = new UserYpat();
        userYpat.setUser(user);
        userYpat.setYpatInfo(ypatInfo);
        userYpatRepository.save(userYpat);
    }

    /**
     * 取消收藏
     */
    public void myColCancel(Long userid, Long ypatid){
        User user = get(userid);
        YpatInfo ypatInfo = ypatInfoRepository.findById(ypatid);
        if(user==null || ypatInfo==null){
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        if(userYpatRepository.countByUseridAndYpatid(userid, ypatid)<=0){
            throw new SysException(ResponseCode.FAIL_NOT, "未收藏");
        }

        userYpatRepository.deleteByUseridAndYpatid(userid, ypatid);
        user.setColtimes(Math.max(0, user.getColtimes()-1));
        userRepository.save(user);
        ypatInfo.setColtimes(Math.max(0, ypatInfo.getColtimes()-1));
        ypatInfoRepository.save(ypatInfo);
    }

    /**
     * 发布+1
     * @param id
    public void myPubAdd(Long id){
        User user = get(id);
        if(user==null){
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        user.setPubtimes(user.getPubtimes()+1);
        userRepository.save(user);
    }
     */

    /**
     * 删除
     * @param id
    public void delete(Long id){
        userRepository.delete(id);
    }
     */

    /**
     * 根据id获取
     * @param id
     * @return
     */
    public User get(Long id){
        return  userRepository.findById(id);
    }

    /**
     * 根据手机号查询
     * @param mobile
     * @return
     */
    public UserQo findByMobile(String mobile){
        User user = userRepository.findByMobile(mobile);
        return CopyUtil.copy(user, UserQo.class);
    }

    public Map<String, Object> findByCityAndProfess(Long userid, String city) {
        Map<String, Object> map = new HashMap();
        User pubUser = get(userid);
        if(StringUtils.isEmpty(pubUser.getProfess())) {
            return map;
        }
        if(StringUtils.isEmpty(city)) {
            return map;
        }

        city = city.replace("市","")+"%";
        String profess = "";
        if(UserProfess.sys.value.equals(pubUser.getProfess())) {
            profess = UserProfess.mt.value;
        }else{
            profess = UserProfess.sys.value;
        }
        List<User> users = userRepository.findByCityAndProfess(city, profess);
        for (User user : users) {
            map.put(user.getMobile(),"1");
        }
        map.remove(pubUser.getMobile());
        return map;
    }

    /**
     * 根据id获取
     * @param id
     * @return
     */
    public UserQo findById(Long id){
        User user = get(id);
        UserQo userQo = CopyUtil.copy(user, UserQo.class);
        List<UserImg> userImgs = user.getUserImgs();
        if(!CollectionUtils.isEmpty(userImgs)){
            for (UserImg userImg : userImgs) {
                if(userImg.getType().equals(UserImgType.head.value)){
                    userQo.setImgpath(userImg.getImgpath());
                    break;
                }
            }
        }
        return userQo;
    }

    /**
     * 根据id获取授权
     * @param id
     * @return
     */
    public OauthQo getAuth(Long id){
        User user = get(id);
        OauthQo oauthQo = new OauthQo();
        oauthQo.setUserid(user.getId());
        oauthQo.setName(user.getName());
        oauthQo.setCertcode(user.getCertcode());
        oauthQo.setStatus(user.getStatus());
        List<UserImg> userImgs = user.getUserImgs();
        if(!CollectionUtils.isEmpty(userImgs)){
            List<String> resImgs = new ArrayList<>();
            for (UserImg userImg : userImgs) {
                if(userImg.getType().equals(UserImgType.head.value)){
                    continue;
                }else{
                    resImgs.add(userImg.getImgpath());
                }
            }
            oauthQo.setPics(resImgs);
        }
        return oauthQo;
    }

    /**
     * 查看联系方式
     */
    public UserLinkWayQo getLinkWay(Long id, Long userid, Long messid){
        //查询拍拍逗
        User user = get(id);
        if(user==null){
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        //查看联系方式标记
        MessInfo messInfo = messInfoRepository.findById(messid);

        if(messInfo.getLinkwayflag().equals(YesNo.no.value)){
            if(user.getPpd()< Constant.VIEW_NEED_PPD){
                throw new SysException(ResponseCode.FAIL_BALANCE);
            }
            //余额扣除
            user.setPpd(user.getPpd()- Constant.VIEW_NEED_PPD);
            userRepository.save(user);
            messInfoRepository.updatelinkwayflag(YesNo.yes.value, messid);

            //查看了约拍申请人的联系方式, 生成新消息
            if(messInfo.getType().equals(MessType.send.value)){
                MessInfo messInfoNew = new MessInfo();
                messInfoNew.setCredate(new Date());
                messInfoNew.setMessviewflag(YesNo.no.value);
                messInfoNew.setLinkwayflag(YesNo.no.value);
                messInfoNew.setStatus(YesNo.no.value);
                messInfoNew.setSendper(messInfo.getRecper());
                messInfoNew.setRecper(messInfo.getSendper());
                messInfoNew.setYpatInfo(messInfo.getYpatInfo());
                messInfoNew.setType(MessType.view.value);
                messInfoNew.setContent(MessType.view.name);
                messInfoRepository.save(messInfoNew);
                recordInAppCreated(messInfoNew);
            }

            //增加收支记录
            Record record = new Record();
            record.setCredate(new Date());
            record.setPpd(-1*Constant.VIEW_NEED_PPD);
            record.setUserid(user.getId());
            record.setType(RecordType.VIEW.value);
            recordRepository.save(record);
        }
        //返回信息
        UserLinkWayQo userQo = new UserLinkWayQo();
        User userView = userRepository.findById(userid);
        userQo.setNickname(userView.getNickname());
        userQo.setMobile(userView.getMobile());
        userQo.setName(userView.getName());
        userQo.setWx(userView.getWx());
        return userQo;
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


    public Map<String, Object> findPage(UserQo queryQo) {
        Page<User> userPage = findPageByPredicate(queryQo);
        List<UserQo> content = new ArrayList<>();
        if(!CollectionUtils.isEmpty(userPage.getContent())){
            for (User user : userPage.getContent()) {
                UserQo userQo = CopyUtil.copy(user, UserQo.class);
                List<UserImg> userImgs = user.getUserImgs();
                if(!CollectionUtils.isEmpty(userImgs)){
                    for (UserImg userImg : userImgs) {
                        if("0".equals(userImg.getType())){
                            userQo.setImgpath(userImg.getImgpath());
                            break;
                        }
                    }
                }
                content.add(userQo);
            }
        }

        Map<String, Object> page = new HashMap<>();
        page.put("content", content);
        page.put("totalPages", userPage.getTotalPages());
        page.put("totalElements", userPage.getTotalElements());
        return page;
    }
    /**
     * 分页
     * @param queryQo
     * @return
     */
    public Page<User> findPageByPredicate(UserQo queryQo){
        Pageable pageable  = new PageRequest(queryQo.getPage(), queryQo.getSize());

        return userRepository.findAll(new Specification<User>(){
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicatesList = new ArrayList<Predicate>();

                if(CommonUtils.isNotNull(queryQo.getId())){
                    predicatesList.add(criteriaBuilder.equal(root.get("id"), queryQo.getId()));
                }
                if(CommonUtils.isNotNull(queryQo.getName())){
                    predicatesList.add(criteriaBuilder.like(root.get("name"), "%" + queryQo.getName() + "%"));
                }
                if(CommonUtils.isNotNull(queryQo.getGender())){
                    predicatesList.add(criteriaBuilder.equal(root.get("gender"), queryQo.getGender()));
                }
                if(CommonUtils.isNotNull(queryQo.getCity())){
                    predicatesList.add(criteriaBuilder.equal(root.get("city"), queryQo.getCity()));
                }
                if(CommonUtils.isNotNull(queryQo.getRecmobile())){
                    predicatesList.add(criteriaBuilder.equal(root.get("recmobile"), queryQo.getRecmobile()));
                }
                if(CommonUtils.isNotNull(queryQo.getStatus())){
                    predicatesList.add(criteriaBuilder.equal(root.get("status"), queryQo.getStatus()));
                }
                if(CommonUtils.isNotNull(queryQo.getNickname())){
                    predicatesList.add(criteriaBuilder.like(root.get("nickname"), "%" + queryQo.getNickname() + "%"));
                }
                if(CommonUtils.isNotNull(queryQo.getMobile())){
                    predicatesList.add(criteriaBuilder.equal(root.get("mobile"), queryQo.getMobile()));
                }
                if(CommonUtils.isNotNull(queryQo.getRegisdate())){
                    Date startDay = TimeUtil.getStartDay(queryQo.getRegisdate());
                    Date endDay = TimeUtil.getEndDay(queryQo.getRegisdate());
                    predicatesList.add(criteriaBuilder.between(root.get("regisdate"), startDay, endDay));
                }
                if(CommonUtils.isNotNull(queryQo.getRealnameSubmitAt())){
                    Date startDay = TimeUtil.getStartDay(queryQo.getRealnameSubmitAt());
                    Date endDay = TimeUtil.getEndDay(queryQo.getRealnameSubmitAt());
                    predicatesList.add(criteriaBuilder.between(root.get("realnameSubmitAt"), startDay, endDay));
                }
                applyDataFlagPredicate(predicatesList, criteriaBuilder, root, queryQo.getDataFlag());
                query.where(predicatesList.toArray(new Predicate[predicatesList.size()]));
                if (Boolean.TRUE.equals(queryQo.getRealnameAuditSort())) {
                    CriteriaBuilder.Case<Integer> pendingOrder = criteriaBuilder.selectCase();
                    pendingOrder.when(criteriaBuilder.equal(root.get("status"), UserStatus.ytj.value), 0).otherwise(1);
                    query.orderBy(
                            criteriaBuilder.asc(pendingOrder),
                            criteriaBuilder.desc(root.get("realnameSubmitAt")),
                            criteriaBuilder.desc(root.get("id"))
                    );
                } else {
                    query.orderBy(criteriaBuilder.desc(root.get("id")));
                }
                return query.getRestriction();
            }
        }, pageable);
    }

    private void applyDataFlagPredicate(List<Predicate> predicatesList, CriteriaBuilder criteriaBuilder, Root<User> root, String dataFlag) {
        if (StringUtils.isEmpty(dataFlag)) return;
        if (InternalTestDataFlag.internalTest.value.equals(dataFlag)) {
            predicatesList.add(criteriaBuilder.equal(root.get("dataFlag"), InternalTestDataFlag.internalTest.value));
        } else if (InternalTestDataFlag.real.value.equals(dataFlag)) {
            predicatesList.add(criteriaBuilder.or(
                    criteriaBuilder.isNull(root.get("dataFlag")),
                    criteriaBuilder.notEqual(root.get("dataFlag"), InternalTestDataFlag.internalTest.value)
            ));
        }
    }

}
