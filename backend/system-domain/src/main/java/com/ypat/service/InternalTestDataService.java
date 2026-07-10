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
import com.ypat.entity.WorkTag;
import com.ypat.entity.WorkTagRel;
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
import com.ypat.enums.YpatPatstyle;
import com.ypat.enums.YpatStatus;
import com.ypat.enums.YpatTarget;
import com.ypat.repository.InternalTestResourceRepository;
import com.ypat.repository.UserImgRepository;
import com.ypat.repository.UserRepository;
import com.ypat.repository.WorkMediaRepository;
import com.ypat.repository.WorkRepository;
import com.ypat.repository.WorkTagRelRepository;
import com.ypat.repository.WorkTagRepository;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private static final int CHUNK_SIZE = 500;
    private static final int STYLE_LIMIT = 5;

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
    private WorkTagRepository workTagRepository;
    @Autowired
    private WorkTagRelRepository workTagRelRepository;
    @Autowired
    private InternalTestResourceRepository internalTestResourceRepository;
    @Autowired
    private InternalTestResourceService internalTestResourceService;

    public InternalTestBatchQo createUsers(InternalTestGenerateQo qo) {
        CreateUsersResult usersResult = createInternalUsers(qo, buildBatchNo());
        return buildBatch(usersResult.batchNo, usersResult.users.size(), 0, 0);
    }

    public InternalTestBatchQo generateUsers(InternalTestGenerateQo qo) {
        return createUsers(qo);
    }

    public InternalTestBatchQo generateWorks(InternalTestGenerateQo qo) {
        validateWorkGeneration(qo);
        User user = loadInternalUser(qo.getUserId());
        String batchNo = CommonUtils.isNotNull(qo.getBatchNo()) ? qo.getBatchNo() : buildBatchNo();
        int workCount = 0;
        for (String groupNo : qo.getGroupNos()) {
            List<InternalTestResource> group = loadAvailableWorkGroup(groupNo);
            Work work = createWorkFromGroup(user, group, qo, batchNo);
            internalTestResourceService.markResourcesUsed(group, batchNo, "work", work.getId());
            workCount++;
        }
        return buildBatch(batchNo, 0, 0, workCount);
    }

    public InternalTestBatchQo generateYpats(InternalTestGenerateQo qo) {
        validateYpatGeneration(qo);
        User user = loadInternalUser(qo.getUserId());
        updateInternalUserContact(user, qo);
        String batchNo = CommonUtils.isNotNull(qo.getBatchNo()) ? qo.getBatchNo() : buildBatchNo();
        List<InternalTestResource> resources = ensureResources(
                qo.getYpatResourceIds(),
                InternalTestResourceUsageType.ypat.value,
                InternalTestResourceMediaType.image.value,
                qo.getStyleCode(),
                1);
        InternalTestResource resource = pick(resources, 0);
        YpatInfo ypat = createYpat(user, resource, qo, batchNo);
        List<InternalTestResource> usedResources = new ArrayList<InternalTestResource>();
        usedResources.add(resource);
        internalTestResourceService.markResourcesUsed(usedResources, batchNo, "ypat", ypat.getId());
        return buildBatch(batchNo, 0, 1, 0);
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
                    users.size());
            for (int i = 0; i < users.size(); i++) {
                InternalTestResource resource = ypatResources.get(i);
                YpatInfo ypat = createYpat(users.get(i), resource, qo, batchNo);
                markSingleResourceUsed(resource, batchNo, "ypat", ypat.getId());
                ypatCount++;
            }
        }
        if (shouldGenerateWork(qo)) {
            List<InternalTestResource> workResources = ensureResources(
                    qo.getWorkResourceIds(),
                    InternalTestResourceUsageType.work.value,
                    null,
                    qo.getStyleCode(),
                    users.size());
            for (int i = 0; i < users.size(); i++) {
                InternalTestResource resource = workResources.get(i);
                Work work = createWork(users.get(i), resource, qo, batchNo);
                markSingleResourceUsed(resource, batchNo, "work", work.getId());
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
        Page<User> userPage = userRepository.findAll(userSpec(qo.getBatchNo(), null), pageable);

        return userPageResult(userPage);
    }

    public Map<String, Object> searchUsers(InternalTestGenerateQo qo) {
        if (qo == null) {
            qo = new InternalTestGenerateQo();
        }
        int page = qo.getPage() == null || qo.getPage() < 0 ? 0 : qo.getPage();
        int size = qo.getSize() == null || qo.getSize() <= 0 ? 20 : Math.min(qo.getSize(), 50);
        Pageable pageable = new PageRequest(page, size, new Sort(Sort.Direction.DESC, "id"));
        Page<User> userPage = userRepository.findAll(userSpec(null, qo.getKeyword()), pageable);

        return userPageResult(userPage);
    }

    private Map<String, Object> userPageResult(Page<User> userPage) {
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
        Map<String, InternalTestBatchQo> batchMap = new LinkedHashMap<String, InternalTestBatchQo>();
        mergeBatchAggregates(batchMap, userRepository.aggregateInternalTestBatches(qo.getBatchNo()), "user");
        mergeBatchAggregates(batchMap, ypatInfoRepository.aggregateInternalTestBatches(qo.getBatchNo()), "ypat");
        mergeBatchAggregates(batchMap, workRepository.aggregateInternalTestBatches(qo.getBatchNo()), "work");

        List<InternalTestBatchQo> content = new ArrayList<InternalTestBatchQo>(batchMap.values());
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("content", content);
        result.put("totalPages", 1);
        result.put("totalElements", content.size());
        return result;
    }

    public InternalTestBatchQo cleanup(InternalTestGenerateQo qo) {
        String batchNo = qo == null || CommonUtils.isNull(qo.getBatchNo()) ? null : qo.getBatchNo();
        boolean cleanupAll = qo != null && Boolean.TRUE.equals(qo.getCleanupAll());
        if (!hasCleanupCondition(qo, batchNo)) {
            throw new SysException(ResponseCode.FAIL_PARA, "清理条件不能为空");
        }
        if (cleanupAll && (batchNo != null || hasCleanupUserFilter(qo))) {
            throw new SysException(ResponseCode.FAIL_PARA, "清除全部不能与其他清理条件同时使用");
        }

        int users;
        int ypats;
        int works;
        int ignoredRealCount = 0;
        int releasedResources = 0;
        if (hasCleanupUserFilter(qo)) {
            ignoredRealCount = countRealUsersForCleanup(qo, batchNo);
            int[] counts = cleanupInternalUsersByFilter(qo, batchNo);
            users = counts[0];
            ypats = counts[1];
            works = counts[2];
            releasedResources = counts[3];
        } else {
            users = userRepository.updateInternalTestUsersStatus(batchNo, UserStatus.shbtg.value);
            ypats = ypatInfoRepository.updateInternalTestYpatStatus(batchNo, YpatStatus.shbtg.value, CLEANUP_REASON);
            works = workRepository.updateInternalTestWorkStatus(batchNo, WorkStatus.xj.value, CLEANUP_REASON);
            releasedResources = cleanupAll
                    ? internalTestResourceService.releaseAllUsedResources()
                    : internalTestResourceService.releaseResourcesByBatch(batchNo);
        }
        InternalTestBatchQo batch = buildBatch(batchNo, users, ypats, works);
        batch.setIgnoredRealCount(ignoredRealCount);
        batch.setReleasedResourceCount(releasedResources);
        return batch;
    }

    private boolean hasCleanupCondition(InternalTestGenerateQo qo, String batchNo) {
        return qo != null && (Boolean.TRUE.equals(qo.getCleanupAll()) || batchNo != null || hasCleanupUserFilter(qo));
    }

    private boolean hasCleanupUserFilter(InternalTestGenerateQo qo) {
        return qo != null && (!CollectionUtils.isEmpty(qo.getUserIds())
                || CommonUtils.isNotNull(qo.getCity())
                || CommonUtils.isNotNull(qo.getArea())
                || CommonUtils.isNotNull(qo.getProfess())
                || CommonUtils.isNotNull(qo.getGender()));
    }

    private int countRealUsersForCleanup(InternalTestGenerateQo qo, String batchNo) {
        if (!CollectionUtils.isEmpty(qo.getUserIds())) {
            int total = 0;
            for (List<Long> userIds : partition(qo.getUserIds())) {
                total += safeLong(userRepository.countRealUsersForCleanupByIds(userIds, batchNo,
                        cleanupCity(qo), cleanupArea(qo), cleanupProfess(qo), cleanupGender(qo))).intValue();
            }
            return total;
        }
        return safeLong(userRepository.countRealUsersForCleanup(batchNo,
                cleanupCity(qo), cleanupArea(qo), cleanupProfess(qo), cleanupGender(qo))).intValue();
    }

    private int[] cleanupInternalUsersByFilter(InternalTestGenerateQo qo, String batchNo) {
        int[] counts = new int[]{0, 0, 0, 0};
        if (!CollectionUtils.isEmpty(qo.getUserIds())) {
            for (List<Long> userIds : partition(qo.getUserIds())) {
                List<Long> internalUserIds = userRepository.findInternalTestUserIdsForCleanupByIds(userIds, batchNo,
                        cleanupCity(qo), cleanupArea(qo), cleanupProfess(qo), cleanupGender(qo));
                addCounts(counts, cleanupInternalUserIdChunk(internalUserIds, batchNo));
            }
            return counts;
        }

        Long afterId = 0L;
        while (true) {
            Pageable pageable = new PageRequest(0, CHUNK_SIZE, new Sort(Sort.Direction.ASC, "id"));
            List<Long> internalUserIds = userRepository.findInternalTestUserIdsForCleanup(afterId, batchNo,
                    cleanupCity(qo), cleanupArea(qo), cleanupProfess(qo), cleanupGender(qo), pageable);
            if (CollectionUtils.isEmpty(internalUserIds)) {
                break;
            }
            addCounts(counts, cleanupInternalUserIdChunk(internalUserIds, batchNo));
            afterId = internalUserIds.get(internalUserIds.size() - 1);
            if (internalUserIds.size() < CHUNK_SIZE) {
                break;
            }
        }
        return counts;
    }

    private int[] cleanupInternalUserIdChunk(List<Long> internalUserIds, String batchNo) {
        int[] counts = new int[]{0, 0, 0, 0};
        if (CollectionUtils.isEmpty(internalUserIds)) {
            return counts;
        }
        for (List<Long> userIds : partition(internalUserIds)) {
            List<Long> ypatIds = ypatInfoRepository.findInternalTestYpatIdsByUserIds(userIds, batchNo);
            List<Long> workIds = workRepository.findInternalTestWorkIdsByUserIds(userIds, batchNo);
            counts[0] += userRepository.updateInternalTestUsersStatusByIds(userIds, batchNo, UserStatus.shbtg.value);
            counts[1] += ypatInfoRepository.updateInternalTestYpatStatusByUserIds(userIds, batchNo, YpatStatus.shbtg.value, CLEANUP_REASON);
            counts[2] += workRepository.updateInternalTestWorkStatusByUserIds(userIds, batchNo, WorkStatus.xj.value, CLEANUP_REASON);
            counts[3] += internalTestResourceService.releaseResourcesByTargets(batchNo, "user", userIds);
            counts[3] += internalTestResourceService.releaseResourcesByTargets(batchNo, "ypat", ypatIds);
            counts[3] += internalTestResourceService.releaseResourcesByTargets(batchNo, "work", workIds);
        }
        return counts;
    }

    private void addCounts(int[] target, int[] source) {
        target[0] += source[0];
        target[1] += source[1];
        target[2] += source[2];
        target[3] += source[3];
    }

    private List<List<Long>> partition(List<Long> ids) {
        List<List<Long>> chunks = new ArrayList<List<Long>>();
        if (CollectionUtils.isEmpty(ids)) {
            return chunks;
        }
        for (int start = 0; start < ids.size(); start += CHUNK_SIZE) {
            int end = Math.min(start + CHUNK_SIZE, ids.size());
            chunks.add(ids.subList(start, end));
        }
        return chunks;
    }

    private String cleanupCity(InternalTestGenerateQo qo) {
        return qo == null || CommonUtils.isNull(qo.getCity()) ? null : qo.getCity();
    }

    private String cleanupArea(InternalTestGenerateQo qo) {
        return qo == null || CommonUtils.isNull(qo.getArea()) ? null : qo.getArea();
    }

    private String cleanupProfess(InternalTestGenerateQo qo) {
        return qo == null || CommonUtils.isNull(qo.getProfess()) ? null : qo.getProfess();
    }

    private String cleanupGender(InternalTestGenerateQo qo) {
        return qo == null || CommonUtils.isNull(qo.getGender()) ? null : qo.getGender();
    }

    private Long safeLong(Long value) {
        return value == null ? 0L : value;
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
                qo.getUserCount());

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
            markSingleResourceUsed(avatar, batchNo, "user", user.getId());
            users.add(user);
        }
        return new CreateUsersResult(batchNo, users);
    }

    private YpatInfo createYpat(User user, InternalTestResource resource, InternalTestGenerateQo qo, String batchNo) {
        Date now = new Date();
        YpatInfo ypat = new YpatInfo();
        ypat.setDescrib(defaultString(qo.getDescrib(), defaultString(resource.getDescription(), "内测约拍内容")));
        ypat.setTarget(defaultString(qo.getTarget(), resolveTarget(qo.getTemplateType())));
        ypat.setPatdate(resolvePatdate(qo.getPatdate(), now));
        ypat.setPatarea(defaultString(qo.getCity(), defaultString(user.getCity(), "杭州市")));
        ypat.setPatslice(defaultString(qo.getPatslice(), "全天"));
        ypat.setChargeway("0");
        ypat.setChargeamt(BigDecimal.ZERO);
        ypat.setProvince(defaultString(qo.getProvince(), user.getProvince()));
        ypat.setCity(defaultString(qo.getCity(), user.getCity()));
        ypat.setArea(defaultString(qo.getArea(), user.getArea()));
        ypat.setCreditflag(YesNo.yes.value);
        ypat.setRealnameflag(YesNo.yes.value);
        ypat.setPatstyle(resolveYpatStyle(qo));
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
        return ypat;
    }

    private Work createWork(User user, InternalTestResource resource, InternalTestGenerateQo qo, String batchNo) {
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
        return work;
    }

    private Work createWorkFromGroup(User user, List<InternalTestResource> group, InternalTestGenerateQo qo, String batchNo) {
        Date now = new Date();
        InternalTestResource first = group.get(0);
        Work work = new Work();
        work.setUserid(user.getId());
        work.setDescription(defaultString(qo.getDescrib(), defaultString(first.getDescription(), "内测作品内容")));
        work.setDevice("internal-test");
        work.setShootLocation(defaultString(qo.getCity(), user.getCity()));
        work.setReturnPhotoFlag(1);
        work.setMediaType(toWorkMediaType(first.getMediaType()));
        work.setIsNationwide(0);
        work.setStatus(resolveWorkStatus(qo.getPublishStatus()));
        work.setReadCount(0);
        work.setLikeCount(0);
        work.setFavoriteCount(0);
        work.setPublishTime(now);
        work.setCreatedAt(now);
        work.setUpdatedAt(now);
        work.setDeletedFlag(0);
        work.setCity(defaultString(qo.getCity(), user.getCity()));
        work.setArea(defaultString(qo.getArea(), user.getArea()));
        work.setDataFlag(InternalTestDataFlag.internalTest.value);
        work.setInternalBatchNo(batchNo);
        work = workRepository.save(work);
        bindWorkTags(work.getId(), qo.getStyleCodes());

        int sort = 0;
        for (InternalTestResource resource : group) {
            WorkMedia media = new WorkMedia();
            media.setWorkId(work.getId());
            media.setUserId(user.getId());
            media.setType(toWorkMediaType(resource.getMediaType()));
            media.setUrl(resource.getUrl());
            media.setFileSize(0L);
            media.setMime(InternalTestResourceMediaType.video.value.equals(resource.getMediaType()) ? "video/mp4" : "image/jpeg");
            media.setSortNo(sort++);
            media.setUploadStatus("1");
            media.setCreatedAt(now);
            workMediaRepository.save(media);
        }
        return work;
    }

    private void validateWorkGeneration(InternalTestGenerateQo qo) {
        if (qo == null || qo.getUserId() == null || CollectionUtils.isEmpty(qo.getGroupNos())) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        if (CollectionUtils.isEmpty(qo.getStyleCodes())) {
            throw new SysException(ResponseCode.FAIL_PARA, "作品风格不能为空");
        }
        if (qo.getStyleCodes().size() > STYLE_LIMIT) {
            throw new SysException(ResponseCode.FAIL_PARA, "作品风格最多选择5个");
        }
    }

    private void bindWorkTags(Long workId, List<String> styleCodes) {
        Set<String> boundCodes = new HashSet<String>();
        for (String styleCode : styleCodes) {
            if (CommonUtils.isNull(styleCode) || !boundCodes.add(styleCode)) {
                continue;
            }
            WorkTag tag = workTagRepository.findByCode(styleCode);
            if (tag == null || tag.getStatus() == null || tag.getStatus() != 1) {
                throw new SysException(ResponseCode.FAIL_PARA, "作品风格参数错误");
            }
            WorkTagRel rel = new WorkTagRel();
            rel.setWorkId(workId);
            rel.setTagId(tag.getId());
            rel.setCreatedAt(new Date());
            workTagRelRepository.save(rel);
        }
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
                    predicates.add(cb.or(cb.isNull(root.get("usedFlag")), cb.equal(root.get("usedFlag"), 0)));
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

    private void markSingleResourceUsed(InternalTestResource resource, String batchNo, String targetType, Long targetId) {
        List<InternalTestResource> resources = new ArrayList<InternalTestResource>();
        resources.add(resource);
        internalTestResourceService.markResourcesUsed(resources, batchNo, targetType, targetId);
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
            if (resource.getUsedFlag() != null && resource.getUsedFlag() == 1) {
                continue;
            }
            filtered.add(resource);
        }
        return filtered;
    }

    private User loadInternalUser(Long userId) {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        if (!InternalTestDataFlag.internalTest.value.equals(user.getDataFlag())) {
            throw new SysException(ResponseCode.FAIL_PARA, "只能操作内测用户");
        }
        return user;
    }

    private void updateInternalUserContact(User user, InternalTestGenerateQo qo) {
        if (CommonUtils.isNull(qo.getWx()) || CommonUtils.isNull(qo.getMobile())) {
            throw new SysException(ResponseCode.FAIL_PARA, "微信号和联系电话不能为空");
        }
        user.setWx(qo.getWx());
        user.setMobile(qo.getMobile());
        userRepository.save(user);
    }

    private void validateYpatGeneration(InternalTestGenerateQo qo) {
        if (qo == null || qo.getUserId() == null) {
            throw new SysException(ResponseCode.FAIL_PARA, "请选择内测用户");
        }
        if (CommonUtils.isNull(qo.getPatdate()) || CommonUtils.isNull(qo.getPatslice())) {
            throw new SysException(ResponseCode.FAIL_PARA, "约拍日期和时间段不能为空");
        }
        if (CommonUtils.isNull(qo.getProvince()) || CommonUtils.isNull(qo.getCity()) || CommonUtils.isNull(qo.getArea())) {
            throw new SysException(ResponseCode.FAIL_PARA, "约拍地点必须选择到区县");
        }
        if (CommonUtils.isNull(qo.getDescrib())) {
            throw new SysException(ResponseCode.FAIL_PARA, "约拍要求不能为空");
        }
        if (CommonUtils.isNull(qo.getWx()) || qo.getWx().length() > 40) {
            throw new SysException(ResponseCode.FAIL_PARA, "微信号不能为空且不能超过40位");
        }
        if (CommonUtils.isNull(qo.getMobile()) || !qo.getMobile().matches("^1\\d{10}$")) {
            throw new SysException(ResponseCode.FAIL_PARA, "联系电话格式错误");
        }
        if (CollectionUtils.isEmpty(qo.getStyleCodes())) {
            throw new SysException(ResponseCode.FAIL_PARA, "约拍风格不能为空");
        }
        if (qo.getStyleCodes().size() > STYLE_LIMIT) {
            throw new SysException(ResponseCode.FAIL_PARA, "约拍风格最多选择5个");
        }
        for (String styleCode : qo.getStyleCodes()) {
            if (!isValidYpatStyle(styleCode)) {
                throw new SysException(ResponseCode.FAIL_PARA, "约拍风格参数错误");
            }
        }
        if (!YpatTarget.isValid(qo.getTarget())) {
            throw new SysException(ResponseCode.FAIL_PARA, "约拍对象参数错误");
        }
        resolvePatdate(qo.getPatdate(), new Date());
    }

    private boolean isValidYpatStyle(String styleCode) {
        if (CommonUtils.isNull(styleCode)) {
            return false;
        }
        for (YpatPatstyle style : YpatPatstyle.values()) {
            if (style.value.equals(styleCode)) {
                return true;
            }
        }
        return false;
    }

    private List<InternalTestResource> loadAvailableWorkGroup(String groupNo) {
        if (CommonUtils.isNull(groupNo)) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        List<InternalTestResource> resources;
        if (groupNo.startsWith("single-")) {
            Long resourceId = parseSingleResourceId(groupNo);
            InternalTestResource resource = internalTestResourceRepository.findOne(resourceId);
            resources = resource == null
                    ? Collections.<InternalTestResource>emptyList()
                    : Collections.singletonList(resource);
        } else {
            List<String> groupNos = new ArrayList<String>();
            groupNos.add(groupNo);
            resources = internalTestResourceRepository.findByGroupNoIn(groupNos);
        }
        if (CollectionUtils.isEmpty(resources)) {
            throw new SysException(ResponseCode.FAIL_PARA, "作品组资源不足");
        }
        sortResourceGroup(resources);
        String mediaType = resources.get(0).getMediaType();
        for (InternalTestResource resource : resources) {
            if (!InternalTestResourceStatus.enabled.value.equals(resource.getStatus())) {
                throw new SysException(ResponseCode.FAIL_PARA, "作品组资源未启用");
            }
            if (!InternalTestResourceUsageType.work.value.equals(resource.getUsageType())) {
                throw new SysException(ResponseCode.FAIL_PARA, "作品组用途错误");
            }
            if (resource.getUsedFlag() != null && resource.getUsedFlag() == 1) {
                throw new SysException(ResponseCode.FAIL_PARA, "作品组资源已占用");
            }
            if (!mediaType.equals(resource.getMediaType())) {
                throw new SysException(ResponseCode.FAIL_PARA, "同一作品组不能同时包含图片和视频");
            }
        }
        return resources;
    }

    private Long parseSingleResourceId(String groupNo) {
        try {
            return Long.valueOf(groupNo.substring("single-".length()));
        } catch (RuntimeException ex) {
            throw new SysException(ResponseCode.FAIL_PARA, "作品组编号错误");
        }
    }

    private void sortResourceGroup(List<InternalTestResource> resources) {
        Collections.sort(resources, new Comparator<InternalTestResource>() {
            @Override
            public int compare(InternalTestResource left, InternalTestResource right) {
                int leftSort = left.getGroupSortNo() == null ? 0 : left.getGroupSortNo();
                int rightSort = right.getGroupSortNo() == null ? 0 : right.getGroupSortNo();
                if (leftSort != rightSort) {
                    return leftSort - rightSort;
                }
                Long leftId = left.getId() == null ? 0L : left.getId();
                Long rightId = right.getId() == null ? 0L : right.getId();
                return leftId.compareTo(rightId);
            }
        });
    }

    private Specification<User> userSpec(final String batchNo, final String keyword) {
        return new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                predicates.add(cb.equal(root.get("dataFlag"), InternalTestDataFlag.internalTest.value));
                if (CommonUtils.isNotNull(batchNo)) {
                    predicates.add(cb.equal(root.get("internalBatchNo"), batchNo));
                }
                if (CommonUtils.isNotNull(keyword)) {
                    String normalized = keyword.trim();
                    String like = "%" + normalized + "%";
                    List<Predicate> keywordPredicates = new ArrayList<Predicate>();
                    keywordPredicates.add(cb.like(root.get("nickname"), like));
                    keywordPredicates.add(cb.like(root.get("mobile"), like));
                    try {
                        keywordPredicates.add(cb.equal(root.get("id"), Long.valueOf(normalized)));
                    } catch (NumberFormatException ignored) {
                    }
                    predicates.add(cb.or(keywordPredicates.toArray(new Predicate[keywordPredicates.size()])));
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

    private Date resolvePatdate(String patdate, Date defaultValue) {
        if (CommonUtils.isNull(patdate)) {
            return defaultValue;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(patdate);
        } catch (ParseException e) {
            throw new SysException(ResponseCode.FAIL_PARA, "约拍日期格式错误");
        }
    }

    private String resolveYpatStyle(InternalTestGenerateQo qo) {
        if (!CollectionUtils.isEmpty(qo.getStyleCodes())) {
            return String.join(",", qo.getStyleCodes());
        }
        return defaultString(qo.getStyleCode(), "0");
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

    private void mergeBatchAggregates(Map<String, InternalTestBatchQo> batchMap, List<Object[]> rows, String type) {
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        for (Object[] row : rows) {
            if (row == null || row.length < 2 || row[0] == null) {
                continue;
            }
            String batchNo = String.valueOf(row[0]);
            int count = row[1] instanceof Number ? ((Number) row[1]).intValue() : 0;
            Date createdAt = row.length > 2 && row[2] instanceof Date ? (Date) row[2] : null;
            InternalTestBatchQo batch = ensureBatch(batchMap, batchNo, createdAt);
            if ("user".equals(type)) {
                batch.setUserCount(count);
            } else if ("ypat".equals(type)) {
                batch.setYpatCount(count);
            } else if ("work".equals(type)) {
                batch.setWorkCount(count);
            }
            if (createdAt != null && (batch.getCreatedAt() == null || createdAt.before(batch.getCreatedAt()))) {
                batch.setCreatedAt(createdAt);
            }
        }
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
}
