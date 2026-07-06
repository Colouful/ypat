package com.ypat.service;

import com.ypat.InternalTestBatchQo;
import com.ypat.InternalTestGenerateQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.entity.InternalTestResource;
import com.ypat.entity.User;
import com.ypat.entity.UserImg;
import com.ypat.entity.Work;
import com.ypat.entity.WorkMedia;
import com.ypat.entity.YpatImg;
import com.ypat.entity.YpatInfo;
import com.ypat.enums.InternalTestDataFlag;
import com.ypat.enums.InternalTestResourceMediaType;
import com.ypat.enums.InternalTestResourceStatus;
import com.ypat.enums.InternalTestResourceUsageType;
import com.ypat.enums.UserGender;
import com.ypat.enums.UserImgType;
import com.ypat.enums.UserProfess;
import com.ypat.enums.UserStatus;
import com.ypat.enums.WorkStatus;
import com.ypat.enums.YesNo;
import com.ypat.enums.YpatStatus;
import com.ypat.repository.InternalTestResourceRepository;
import com.ypat.repository.UserImgRepository;
import com.ypat.repository.UserRepository;
import com.ypat.repository.WorkMediaRepository;
import com.ypat.repository.WorkRepository;
import com.ypat.repository.YpatImgRepository;
import com.ypat.repository.YpatInfoRepository;
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
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@Service
@Transactional(rollbackFor = Exception.class)
public class InternalTestDataService {

    private static final String MODE_APPEND_TO_USERS = "append_to_users";
    private static final String CONTENT_YPAT = "ypat";
    private static final String CONTENT_WORK = "work";
    private static final String CLEANUP_REASON = "内测数据软清理";

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserImgRepository userImgRepository;
    @Autowired
    private YpatInfoRepository ypatInfoRepository;
    @Autowired
    private YpatImgRepository ypatImgRepository;
    @Autowired
    private WorkRepository workRepository;
    @Autowired
    private WorkMediaRepository workMediaRepository;
    @Autowired
    private InternalTestResourceRepository internalTestResourceRepository;

    public InternalTestBatchQo createUsers(InternalTestGenerateQo qo) {
        CreateUsersResult usersResult = createInternalUsers(qo, buildBatchNo());
        return buildBatch(usersResult.batchNo, usersResult.users.size(), 0, 0);
    }

    public InternalTestBatchQo generate(InternalTestGenerateQo qo) {
        if (qo == null) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        String batchNo = CommonUtils.isNotNull(qo.getBatchNo()) ? qo.getBatchNo() : buildBatchNo();
        List<User> users;
        if (MODE_APPEND_TO_USERS.equals(qo.getMode())) {
            users = loadInternalUsers(qo.getUserIds());
        } else {
            users = createInternalUsers(qo, batchNo).users;
        }

        int ypatCount = 0;
        int workCount = 0;
        if (shouldGenerateYpat(qo)) {
            List<InternalTestResource> ypatResources = ensureResources(
                    qo.getYpatResourceIds(),
                    InternalTestResourceUsageType.ypat.value,
                    InternalTestResourceMediaType.image.value,
                    qo.getStyleCode(),
                    1);
            for (int i = 0; i < users.size(); i++) {
                createYpat(users.get(i), pick(ypatResources, i), qo, batchNo);
                ypatCount++;
            }
        }
        if (shouldGenerateWork(qo)) {
            List<InternalTestResource> workResources = ensureResources(
                    qo.getWorkResourceIds(),
                    InternalTestResourceUsageType.work.value,
                    null,
                    qo.getStyleCode(),
                    1);
            for (int i = 0; i < users.size(); i++) {
                createWork(users.get(i), pick(workResources, i), qo, batchNo);
                workCount++;
            }
        }

        return buildBatch(batchNo, users.size(), ypatCount, workCount);
    }

    public Map<String, Object> listUsers(InternalTestGenerateQo qo) {
        if (qo == null) {
            qo = new InternalTestGenerateQo();
        }
        Pageable pageable = new PageRequest(0, 50, new Sort(Sort.Direction.DESC, "id"));
        Page<User> userPage = userRepository.findAll(userSpec(qo.getBatchNo()), pageable);

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("content", CopyUtil.copyList(userPage.getContent(), com.ypat.UserQo.class));
        result.put("totalPages", userPage.getTotalPages());
        result.put("totalElements", userPage.getTotalElements());
        return result;
    }

    public Map<String, Object> listBatches(InternalTestGenerateQo qo) {
        if (qo == null) {
            qo = new InternalTestGenerateQo();
        }
        List<User> users = userRepository.findAll(userSpec(qo.getBatchNo()));
        List<YpatInfo> ypats = ypatInfoRepository.findAll(ypatSpec(qo.getBatchNo()));
        List<Work> works = workRepository.findAll(workSpec(qo.getBatchNo()));

        Map<String, InternalTestBatchQo> batchMap = new LinkedHashMap<String, InternalTestBatchQo>();
        for (User user : users) {
            InternalTestBatchQo batch = ensureBatch(batchMap, user.getInternalBatchNo(), user.getRegisdate());
            batch.setUserCount(batch.getUserCount() + 1);
        }
        for (YpatInfo ypat : ypats) {
            InternalTestBatchQo batch = ensureBatch(batchMap, ypat.getInternalBatchNo(), ypat.getPubdate());
            batch.setYpatCount(batch.getYpatCount() + 1);
        }
        for (Work work : works) {
            InternalTestBatchQo batch = ensureBatch(batchMap, work.getInternalBatchNo(), work.getCreatedAt());
            batch.setWorkCount(batch.getWorkCount() + 1);
        }

        List<InternalTestBatchQo> content = new ArrayList<InternalTestBatchQo>(batchMap.values());
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("content", content);
        result.put("totalPages", 1);
        result.put("totalElements", content.size());
        return result;
    }

    public InternalTestBatchQo cleanup(InternalTestGenerateQo qo) {
        String batchNo = qo == null || CommonUtils.isNull(qo.getBatchNo()) ? null : qo.getBatchNo();
        if (!hasCleanupCondition(qo, batchNo)) {
            throw new SysException(ResponseCode.FAIL_PARA, "清理条件不能为空");
        }

        int users;
        int ypats;
        int works;
        int ignoredRealCount = 0;
        if (hasCleanupUserFilter(qo)) {
            CleanupUserScope scope = resolveCleanupUserScope(qo, batchNo);
            ignoredRealCount = scope.ignoredRealCount;
            if (CollectionUtils.isEmpty(scope.internalUserIds)) {
                users = 0;
                ypats = 0;
                works = 0;
            } else {
                users = userRepository.updateInternalTestUsersStatusByIds(scope.internalUserIds, batchNo, UserStatus.shbtg.value);
                ypats = ypatInfoRepository.updateInternalTestYpatStatusByUserIds(scope.internalUserIds, batchNo, YpatStatus.shbtg.value, CLEANUP_REASON);
                works = workRepository.updateInternalTestWorkStatusByUserIds(scope.internalUserIds, batchNo, WorkStatus.xj.value, CLEANUP_REASON);
            }
        } else {
            users = userRepository.updateInternalTestUsersStatus(batchNo, UserStatus.shbtg.value);
            ypats = ypatInfoRepository.updateInternalTestYpatStatus(batchNo, YpatStatus.shbtg.value, CLEANUP_REASON);
            works = workRepository.updateInternalTestWorkStatus(batchNo, WorkStatus.xj.value, CLEANUP_REASON);
        }
        InternalTestBatchQo batch = buildBatch(batchNo, users, ypats, works);
        batch.setIgnoredRealCount(ignoredRealCount);
        return batch;
    }

    private boolean hasCleanupCondition(InternalTestGenerateQo qo, String batchNo) {
        return qo != null && (batchNo != null || hasCleanupUserFilter(qo));
    }

    private boolean hasCleanupUserFilter(InternalTestGenerateQo qo) {
        return qo != null && (!CollectionUtils.isEmpty(qo.getUserIds())
                || CommonUtils.isNotNull(qo.getCity())
                || CommonUtils.isNotNull(qo.getArea())
                || CommonUtils.isNotNull(qo.getProfess())
                || CommonUtils.isNotNull(qo.getGender()));
    }

    private CleanupUserScope resolveCleanupUserScope(InternalTestGenerateQo qo, String batchNo) {
        List<User> matchedUsers = userRepository.findAll(cleanupUserSpec(qo, batchNo));
        List<Long> internalUserIds = new ArrayList<Long>();
        int ignoredRealCount = 0;
        for (User user : matchedUsers) {
            if (InternalTestDataFlag.internalTest.value.equals(user.getDataFlag())) {
                internalUserIds.add(user.getId());
            } else {
                ignoredRealCount++;
            }
        }
        return new CleanupUserScope(internalUserIds, ignoredRealCount);
    }

    private Specification<User> cleanupUserSpec(final InternalTestGenerateQo qo, final String batchNo) {
        return new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                if (!CollectionUtils.isEmpty(qo.getUserIds())) {
                    predicates.add(root.get("id").in(qo.getUserIds()));
                }
                if (CommonUtils.isNotNull(batchNo)) {
                    predicates.add(cb.equal(root.get("internalBatchNo"), batchNo));
                }
                if (CommonUtils.isNotNull(qo.getCity())) {
                    predicates.add(cb.equal(root.get("city"), qo.getCity()));
                }
                if (CommonUtils.isNotNull(qo.getArea())) {
                    predicates.add(cb.equal(root.get("area"), qo.getArea()));
                }
                if (CommonUtils.isNotNull(qo.getProfess())) {
                    predicates.add(cb.equal(root.get("profess"), qo.getProfess()));
                }
                if (CommonUtils.isNotNull(qo.getGender())) {
                    predicates.add(cb.equal(root.get("gender"), qo.getGender()));
                }
                query.where(predicates.toArray(new Predicate[predicates.size()]));
                return query.getRestriction();
            }
        };
    }

    private CreateUsersResult createInternalUsers(InternalTestGenerateQo qo, String batchNo) {
        if (qo == null || qo.getUserCount() == null || qo.getUserCount() < 1 || qo.getUserCount() > 50) {
            throw new SysException(ResponseCode.FAIL_PARA, "userCount必须在1到50之间");
        }
        List<InternalTestResource> avatarResources = ensureResources(
                qo.getAvatarResourceIds(),
                InternalTestResourceUsageType.avatar.value,
                InternalTestResourceMediaType.image.value,
                null,
                1);

        List<User> users = new ArrayList<User>();
        for (int i = 0; i < qo.getUserCount(); i++) {
            InternalTestResource avatar = pick(avatarResources, i);
            User user = new User();
            user.setGender(defaultString(qo.getGender(), UserGender.nv.value));
            user.setNickname(defaultString(qo.getNicknamePrefix(), "内测用户") + (i + 1));
            user.setProfess(defaultString(qo.getProfess(), UserProfess.mt.value));
            user.setMobile(buildUniqueMobile(i));
            user.setName(user.getNickname());
            user.setPpd(1000);
            user.setAvatarurl(avatar.getUrl());
            user.setRealnameflag(YesNo.yes.value);
            user.setCreditflag(YesNo.yes.value);
            user.setPubtimes(0);
            user.setRectimes(0);
            user.setColtimes(0);
            user.setStatus(UserStatus.shtg.value);
            user.setProvince(defaultString(qo.getProvince(), "浙江省"));
            user.setCity(defaultString(qo.getCity(), "杭州市"));
            user.setArea(defaultString(qo.getArea(), "西湖区"));
            user.setRegisdate(new Date());
            user.setDataFlag(InternalTestDataFlag.internalTest.value);
            user.setInternalBatchNo(batchNo);
            user = userRepository.save(user);
            saveUserAvatar(user, avatar);
            users.add(user);
        }
        return new CreateUsersResult(batchNo, users);
    }

    private void createYpat(User user, InternalTestResource resource, InternalTestGenerateQo qo, String batchNo) {
        Date now = new Date();
        YpatInfo ypat = new YpatInfo();
        ypat.setDescrib(defaultString(resource.getDescription(), "内测约拍内容"));
        ypat.setTarget(resolveTarget(qo.getTemplateType()));
        ypat.setPatdate(now);
        ypat.setPatarea(defaultString(user.getCity(), "杭州市"));
        ypat.setPatslice("全天");
        ypat.setChargeway("0");
        ypat.setChargeamt(BigDecimal.ZERO);
        ypat.setProvince(user.getProvince());
        ypat.setCity(user.getCity());
        ypat.setArea(user.getArea());
        ypat.setCreditflag(YesNo.yes.value);
        ypat.setRealnameflag(YesNo.yes.value);
        ypat.setPatstyle(defaultString(qo.getStyleCode(), "0"));
        ypat.setStatus(resolveYpatStatus(qo.getPublishStatus()));
        ypat.setPubdate(now);
        ypat.setReadtimes(0);
        ypat.setPattimes(0);
        ypat.setColtimes(0);
        ypat.setUser(user);
        ypat.setRecomflag(YesNo.no.value);
        ypat.setIsNationwide(0);
        ypat.setDataFlag(InternalTestDataFlag.internalTest.value);
        ypat.setInternalBatchNo(batchNo);
        ypat = ypatInfoRepository.save(ypat);

        YpatImg img = new YpatImg();
        img.setYpatid(ypat.getId());
        img.setType("0");
        img.setImgpath(resource.getUrl());
        ypatImgRepository.save(img);
    }

    private void createWork(User user, InternalTestResource resource, InternalTestGenerateQo qo, String batchNo) {
        Date now = new Date();
        Work work = new Work();
        work.setUserid(user.getId());
        work.setDescription(defaultString(resource.getDescription(), "内测作品内容"));
        work.setDevice("internal-test");
        work.setShootLocation(defaultString(user.getCity(), "杭州市"));
        work.setReturnPhotoFlag(1);
        work.setMediaType(toWorkMediaType(resource.getMediaType()));
        work.setIsNationwide(0);
        work.setStatus(resolveWorkStatus(qo.getPublishStatus()));
        work.setReadCount(0);
        work.setLikeCount(0);
        work.setFavoriteCount(0);
        work.setPublishTime(now);
        work.setCreatedAt(now);
        work.setUpdatedAt(now);
        work.setDeletedFlag(0);
        work.setCity(user.getCity());
        work.setArea(user.getArea());
        work.setDataFlag(InternalTestDataFlag.internalTest.value);
        work.setInternalBatchNo(batchNo);
        work = workRepository.save(work);

        WorkMedia media = new WorkMedia();
        media.setWorkId(work.getId());
        media.setUserId(user.getId());
        media.setType(toWorkMediaType(resource.getMediaType()));
        media.setUrl(resource.getUrl());
        media.setFileSize(0L);
        media.setMime(InternalTestResourceMediaType.video.value.equals(resource.getMediaType()) ? "video/mp4" : "image/jpeg");
        media.setSortNo(0);
        media.setUploadStatus("1");
        media.setCreatedAt(now);
        workMediaRepository.save(media);
    }

    private List<InternalTestResource> ensureResources(List<Long> ids, final String usageType, final String mediaType, final String styleCode, int minCount) {
        List<InternalTestResource> resources;
        if (!CollectionUtils.isEmpty(ids)) {
            resources = internalTestResourceRepository.findByIdInAndStatus(ids, InternalTestResourceStatus.enabled.value);
            resources = filterResources(resources, usageType, mediaType, styleCode);
            Set<Long> uniqueIds = new HashSet<Long>(ids);
            if (resources.size() != uniqueIds.size()) {
                throw new SysException(ResponseCode.FAIL_PARA, "启用资源不足：" + usageType);
            }
        } else {
            resources = internalTestResourceRepository.findAll(new Specification<InternalTestResource>() {
                @Override
                public Predicate toPredicate(Root<InternalTestResource> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    List<Predicate> predicates = new ArrayList<Predicate>();
                    predicates.add(cb.equal(root.get("status"), InternalTestResourceStatus.enabled.value));
                    predicates.add(cb.equal(root.get("usageType"), usageType));
                    if (CommonUtils.isNotNull(mediaType)) {
                        predicates.add(cb.equal(root.get("mediaType"), mediaType));
                    }
                    if (CommonUtils.isNotNull(styleCode)) {
                        predicates.add(cb.or(cb.equal(root.get("styleCode"), styleCode), cb.isNull(root.get("styleCode"))));
                    }
                    query.orderBy(cb.asc(root.get("sortNo")), cb.desc(root.get("id")));
                    query.where(predicates.toArray(new Predicate[predicates.size()]));
                    return query.getRestriction();
                }
            });
        }
        if (CollectionUtils.isEmpty(resources) || resources.size() < minCount) {
            throw new SysException(ResponseCode.FAIL_PARA, "启用资源不足：" + usageType);
        }
        return resources;
    }

    private List<User> loadInternalUsers(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            throw new SysException(ResponseCode.FAIL_PARA, "请选择已有内测用户");
        }
        List<User> users = userRepository.findAll(userIds);
        if (users.size() != userIds.size()) {
            throw new SysException(ResponseCode.FAIL_PARA, "用户不存在");
        }
        for (User user : users) {
            if (!InternalTestDataFlag.internalTest.value.equals(user.getDataFlag())) {
                throw new SysException(ResponseCode.FAIL_PARA, "只能给内测用户追加内容");
            }
        }
        return users;
    }

    private String buildBatchNo() {
        int suffix = new Random().nextInt(10000);
        return "IT" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + String.format("%04d", suffix);
    }

    private String buildUniqueMobile(int index) {
        long seed = System.currentTimeMillis() % 1000000L;
        for (int i = 0; i < 100; i++) {
            String mobile = "199" + String.format("%08d", (seed * 100 + index + i) % 100000000L);
            if (userRepository.findByMobile(mobile) == null) {
                return mobile;
            }
        }
        throw new SysException(ResponseCode.FAIL_EXIST, "内测手机号生成冲突");
    }

    private void saveUserAvatar(User user, InternalTestResource avatar) {
        UserImg userImg = new UserImg();
        userImg.setUserid(user.getId());
        userImg.setType(UserImgType.head.value);
        userImg.setImgpath(avatar.getUrl());
        userImgRepository.save(userImg);
    }

    private InternalTestResource pick(List<InternalTestResource> resources, int index) {
        return resources.get(index % resources.size());
    }

    private List<InternalTestResource> filterResources(List<InternalTestResource> resources, String usageType, String mediaType, String styleCode) {
        List<InternalTestResource> filtered = new ArrayList<InternalTestResource>();
        for (InternalTestResource resource : resources) {
            if (!usageType.equals(resource.getUsageType())) {
                continue;
            }
            if (CommonUtils.isNotNull(mediaType) && !mediaType.equals(resource.getMediaType())) {
                continue;
            }
            if (CommonUtils.isNotNull(styleCode)
                    && CommonUtils.isNotNull(resource.getStyleCode())
                    && !styleCode.equals(resource.getStyleCode())) {
                continue;
            }
            filtered.add(resource);
        }
        return filtered;
    }

    private Specification<User> userSpec(final String batchNo) {
        return new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                predicates.add(cb.equal(root.get("dataFlag"), InternalTestDataFlag.internalTest.value));
                if (CommonUtils.isNotNull(batchNo)) {
                    predicates.add(cb.equal(root.get("internalBatchNo"), batchNo));
                }
                query.where(predicates.toArray(new Predicate[predicates.size()]));
                return query.getRestriction();
            }
        };
    }

    private Specification<YpatInfo> ypatSpec(final String batchNo) {
        return new Specification<YpatInfo>() {
            @Override
            public Predicate toPredicate(Root<YpatInfo> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                predicates.add(cb.equal(root.get("dataFlag"), InternalTestDataFlag.internalTest.value));
                if (CommonUtils.isNotNull(batchNo)) {
                    predicates.add(cb.equal(root.get("internalBatchNo"), batchNo));
                }
                query.where(predicates.toArray(new Predicate[predicates.size()]));
                return query.getRestriction();
            }
        };
    }

    private Specification<Work> workSpec(final String batchNo) {
        return new Specification<Work>() {
            @Override
            public Predicate toPredicate(Root<Work> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                predicates.add(cb.equal(root.get("dataFlag"), InternalTestDataFlag.internalTest.value));
                if (CommonUtils.isNotNull(batchNo)) {
                    predicates.add(cb.equal(root.get("internalBatchNo"), batchNo));
                }
                query.where(predicates.toArray(new Predicate[predicates.size()]));
                return query.getRestriction();
            }
        };
    }

    private boolean shouldGenerateYpat(InternalTestGenerateQo qo) {
        return CommonUtils.isNull(qo.getContentType())
                || CONTENT_YPAT.equals(qo.getContentType())
                || "both".equals(qo.getContentType());
    }

    private boolean shouldGenerateWork(InternalTestGenerateQo qo) {
        return CommonUtils.isNull(qo.getContentType())
                || CONTENT_WORK.equals(qo.getContentType())
                || "both".equals(qo.getContentType());
    }

    private String resolveTarget(String templateType) {
        if ("photographer".equals(templateType)) {
            return "约摄影师";
        }
        if ("model".equals(templateType)) {
            return "约模特";
        }
        return "发布约拍";
    }

    private String resolveYpatStatus(String status) {
        if (CommonUtils.isNull(status)) {
            return YpatStatus.ytj.value;
        }
        if (YpatStatus.zc.value.equals(status) || YpatStatus.ytj.value.equals(status)
                || YpatStatus.shtg.value.equals(status) || YpatStatus.shbtg.value.equals(status)) {
            return status;
        }
        throw new SysException(ResponseCode.FAIL_PARA, "publishStatus参数错误");
    }

    private String resolveWorkStatus(String status) {
        if (CommonUtils.isNull(status)) {
            return WorkStatus.ytj.value;
        }
        if (!WorkStatus.isValid(status)) {
            throw new SysException(ResponseCode.FAIL_PARA, "publishStatus参数错误");
        }
        return status;
    }

    private String toWorkMediaType(String resourceMediaType) {
        return InternalTestResourceMediaType.video.value.equals(resourceMediaType) ? "2" : "1";
    }

    private String defaultString(String value, String defaultValue) {
        return CommonUtils.isNotNull(value) ? value : defaultValue;
    }

    private InternalTestBatchQo buildBatch(String batchNo, int userCount, int ypatCount, int workCount) {
        InternalTestBatchQo batch = new InternalTestBatchQo();
        batch.setBatchNo(batchNo);
        batch.setUserCount(userCount);
        batch.setYpatCount(ypatCount);
        batch.setWorkCount(workCount);
        batch.setIgnoredRealCount(0);
        batch.setStatus("success");
        batch.setCreatedAt(new Date());
        return batch;
    }

    private InternalTestBatchQo ensureBatch(Map<String, InternalTestBatchQo> batchMap, String batchNo, Date createdAt) {
        String key = CommonUtils.isNotNull(batchNo) ? batchNo : "";
        InternalTestBatchQo batch = batchMap.get(key);
        if (batch == null) {
            batch = buildBatch(batchNo, 0, 0, 0);
            batch.setCreatedAt(createdAt);
            batchMap.put(key, batch);
        }
        return batch;
    }

    private static class CreateUsersResult {
        private String batchNo;
        private List<User> users;

        private CreateUsersResult(String batchNo, List<User> users) {
            this.batchNo = batchNo;
            this.users = users;
        }
    }

    private static class CleanupUserScope {
        private List<Long> internalUserIds;
        private int ignoredRealCount;

        private CleanupUserScope(List<Long> internalUserIds, int ignoredRealCount) {
            this.internalUserIds = internalUserIds;
            this.ignoredRealCount = ignoredRealCount;
        }
    }
}
